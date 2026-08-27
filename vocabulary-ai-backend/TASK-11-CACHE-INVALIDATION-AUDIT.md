# TASK 11: Cache Invalidation on User Status Changes - AUDIT REPORT
**Date**: 2026-08-27  
**Phase**: 11 - Security Hardening & Authorization  
**Task Status**: IN PROGRESS (Audit Complete)

---

## PART 1: EXISTING REDIS CACHE INVENTORY

### All User-Specific Caches (Must Be Invalidated on Status Change)

| # | Cache Type | Key Pattern | Service | Contains UserId | TTL | Purpose |
|---|---|---|---|---|---|---|
| 1 | Adaptive Difficulty | `ai:adaptive-difficulty:{userId}` | AdaptiveDifficultyServiceImpl | ✅ YES | 30 days | Difficulty recommendations |
| 2 | Recommendations | `ai:recommendations:{userId}` | RecommendationServiceImpl | ✅ YES | 3 hours | Learning recommendations |
| 3 | Weakness/Strength | `ai:weakness-strength:{userId}` | WeaknessStrengthServiceImpl | ✅ YES | 2 hours | Analysis results |
| 4 | Progress Analysis | `ai:progress-analysis:{userId}` | ProgressAnalysisServiceImpl | ✅ YES | 1 day | Progress tracking |
| 5 | Learning Plan | `ai:learning-plan:{userId}` | LearningPlanServiceImpl | ✅ YES | 7 days | 4-week learning plan |
| 6 | Daily Practice | `ai:daily-practice:{userId}:{date}` | DailyPracticeServiceImpl | ✅ YES | 8 hours | Daily activities |
| 7 | Rate Limit Tokens | `ai:ratelimit:tokens:{userId}` | CacheServiceImpl | ✅ YES | 24 hours | Gemini API tokens |
| 8 | Rate Limit Requests | `ai:ratelimit:requests:{userId}` | CacheServiceImpl | ✅ YES | 24 hours | Gemini API requests |

### Non-User-Specific Caches (Content-Based, Do NOT Invalidate on Status Change)

| # | Cache Type | Key Pattern | Service | Contains UserId | TTL | Purpose |
|---|---|---|---|---|---|---|
| 9 | Grammar Check | `ai:grammar:check:{textHash}` | GrammarCheckingServiceImpl | ❌ NO | 1 hour | Grammar analysis (shared) |
| 10 | Vocabulary Explanation | `ai:explanation:{wordId}` | VocabularyExplanationServiceImpl | ❌ NO | 24 hours | Word meanings (shared) |
| 11 | Conversation Context | `ai:conversation:{conversationId}` | CacheServiceImpl | ❌ NO | 30 min | AI conversation history |
| 12 | Learning Hint | `ai:hint:progress:{progressId}:{attempt}` | LearningHintServiceImpl | ❌ NO | 1 hour | Study hints (shared) |

---

## PART 2: USER STATUS CHANGE FLOW

### Current Status in Codebase

**Finding**: No admin API endpoint currently exists to update user status.

**Current Status Validation Locations** (Authentication/Authorization layer):
```
1. JwtAuthenticationFilter.java:72
   - Checks user.getStatus() during authentication
   - Only ACTIVE users can authenticate
   - INACTIVE/BANNED users are rejected (401)

2. AuthService.java:163
   - validateUserStatusForTokenRefresh()
   - Prevents INACTIVE/BANNED from refreshing tokens
   
3. CustomUserDetailsService.java:61
   - validateUserStatus()
   - Throws exception if INACTIVE or BANNED
```

**User Status Enum** (User.java):
```java
public enum UserStatus {
    ACTIVE,      // ✅ Normal user
    INACTIVE,    // ❌ Account suspended
    BANNED       // ❌ Account banned
}
```

### What Needs to Be Done

Since there's no admin API to update user status currently, we need to:
1. **Create methods in UserService** to update user status
2. **Create endpoints in UserController** (or separate AdminController) to call these methods
3. **Add cache invalidation** to UserService status update methods
4. **Ensure security** - only ADMIN users can change status

---

## PART 3: CACHE SERVICES WITH INVALIDATION CAPABILITY

### Services with invalidateCache() Methods (Already Exist)

```java
// 1. AdaptiveDifficultyServiceImpl
public void invalidateCache(Long userId) {
    cacheService.delete(CACHE_KEY_PREFIX + userId);
}

// 2. RecommendationServiceImpl
public void invalidateCache(Long userId) {
    cacheService.delete(CACHE_KEY_PREFIX + userId);
}

// 3. WeaknessStrengthServiceImpl
public void invalidateCache(Long userId) {
    cacheService.delete(CACHE_KEY_PREFIX + userId);
}

// 4. ProgressAnalysisServiceImpl
public void invalidateCache(Long userId) {
    cacheService.delete(CACHE_KEY_PREFIX + userId);
}

// 5. LearningPlanServiceImpl
public void invalidateCache(Long userId) {
    cacheService.delete(CACHE_KEY_PREFIX + userId);
}

// 6. DailyPracticeServiceImpl
// ⚠️ NO invalidateCache() method - must ADD
// Needs to delete pattern: "ai:daily-practice:{userId}:*"
```

### Services WITHOUT Invalidation Methods (Need to Add)

```java
// Need to add invalidate methods for:
- GrammarCheckingServiceImpl (but this is TEXT-based, not user-specific)
- VocabularyExplanationServiceImpl (but this is WORD-based, not user-specific)
- LearningHintServiceImpl (but this is PROGRESS-based, not user-specific)

// Only need USER-SPECIFIC invalidation:
- DailyPracticeServiceImpl ✅ NEED TO ADD
```

---

## PART 4: PROPOSED CACHE INVALIDATION STRATEGY

### Centralized Approach (Recommended)

Create a new `CacheInvalidationService` that handles all user cache invalidation:

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheInvalidationService {
    
    private final AdaptiveDifficultyServiceImpl adaptiveDifficultyService;
    private final RecommendationServiceImpl recommendationService;
    private final WeaknessStrengthServiceImpl weaknessStrengthService;
    private final ProgressAnalysisServiceImpl progressAnalysisService;
    private final LearningPlanServiceImpl learningPlanService;
    private final DailyPracticeServiceImpl dailyPracticeService;
    private final CacheService cacheService;
    
    /**
     * Invalidate all user-specific caches when user status changes
     * Called when: ACTIVE → INACTIVE or ACTIVE → BANNED
     */
    public void invalidateAllUserCaches(Long userId) {
        log.info("[TASK 11] Invalidating all user caches for userId: {}", userId);
        
        try {
            // Invalidate personalization caches (8 cache types)
            adaptiveDifficultyService.invalidateCache(userId);
            recommendationService.invalidateCache(userId);
            weaknessStrengthService.invalidateCache(userId);
            progressAnalysisService.invalidateCache(userId);
            learningPlanService.invalidateCache(userId);
            
            // Invalidate daily practice (delete pattern)
            cacheService.deletePattern("ai:daily-practice:" + userId + ":*");
            
            // Invalidate rate limit counters
            cacheService.delete("ai:ratelimit:tokens:" + userId);
            cacheService.delete("ai:ratelimit:requests:" + userId);
            
            log.info("[TASK 11] All user caches invalidated successfully for userId: {}", userId);
        } catch (Exception e) {
            log.error("[TASK 11] Error invalidating user caches for userId: {}: {}", 
                      userId, e.getMessage(), e);
            // Don't throw - cache is not security-critical, continue with operation
        }
    }
}
```

### Integration Points

```
UserService.updateUserStatus()
    ↓ (If status changes to INACTIVE or BANNED)
CacheInvalidationService.invalidateAllUserCaches(userId)
    ↓
[Delete all 8 user-specific cache types]
    ↓
User attempts new request
    ↓
JwtAuthenticationFilter validates user status
    ↓
If INACTIVE/BANNED → 401 Unauthorized
If ACTIVE → Proceed to endpoint
```

---

## PART 5: SECURITY FLOW

### Before Implementation

```
ACTIVE User → Request Resource
    ↓
Authentication OK ✅
    ↓
Authorization OK ✅
    ↓
Service Layer: verifyUserOwnership() ✅
    ↓
Cache exists and valid
    ↓
Return cached data ✅

Then Admin bans user:

BANNED User (with cached data)
    ↓
JwtAuthenticationFilter: status check → ❌ BLOCKED
    ↓
BUT if someone clears their local token cache...
Or if authorization somehow bypassed...
    ↓
Could still access old Redis cache entry ⚠️
```

### After Implementation

```
Admin bans User:
    ↓
UserService.updateUserStatus(userId, BANNED)
    ↓
Database updated
    ↓
CacheInvalidationService.invalidateAllUserCaches(userId)
    ↓
ALL 8 user-specific cache entries DELETED ✅
    ↓
User attempts new request:
    ↓
JwtAuthenticationFilter: status check → ❌ BLOCKED
    ↓
Even if someone bypassed JWT filter:
    ↓
Authorization checks in service layer
    ↓
verifyUserOwnership() verification
    ↓
Service tries to get cache
    ↓
Cache miss (already deleted) → Recompute from database ✅
    ↓
All checks still apply (status, ownership, etc.)
    ↓
Result: SECURE ✅
```

---

## PART 6: TEST SCENARIOS

### Scenario 1: User Banned
```
1. User A (userId=100) is ACTIVE
2. Request: GET /api/personalization/recommendations
3. Response: 200 OK (with cached data)
4. Admin bans User A
5. UserService.updateUserStatus(100, BANNED)
6. CacheInvalidationService.invalidateAllUserCaches(100)
7. ALL 8 user-specific caches DELETED
8. User A tries to make request again
9. Expected: 401 Unauthorized (status validation)
10. Even if JWT bypassed: verifyUserOwnership() + cache miss
```

### Scenario 2: User Deactivated
```
1. User B (userId=200) is ACTIVE
2. Request: GET /api/personalization/progress-analysis
3. Response: 200 OK (with cached data from redis)
4. Admin deactivates User B
5. UserService.updateUserStatus(200, INACTIVE)
6. CacheInvalidationService.invalidateAllUserCaches(200)
7. Cache for userId=200 DELETED
8. User B attempts to refresh token
9. Expected: 401 Unauthorized (status validation in token refresh)
```

### Scenario 3: User Reactivated
```
1. User C (userId=300) was BANNED
2. Now unbanned (status → ACTIVE)
3. No need to invalidate (fresh start)
4. User C logs in
5. Makes first request
6. Cache miss (previous was deleted)
7. New data fetched from AI/Database
8. New cache created for userId=300 ✅
```

### Scenario 4: User A Cannot Access User B Cache
```
1. User A (userId=100) requests recommendations
2. Cache created: "ai:recommendations:100"
3. User B (userId=200) requests recommendations
4. Cache created: "ai:recommendations:200"
5. User A attempts to hack and access userId=200 cache
6. Expected: verifyUserOwnership(200) from SecurityContext
7. currentUser.getId()=100 ≠ requestedUserId=200
8. Result: 403 Forbidden ✅
```

---

## PART 7: FILES TO CREATE/MODIFY

### NEW FILES
1. **CacheInvalidationService.java** - Centralized cache invalidation
2. **TASK-11-IMPLEMENTATION-PLAN.md** - Detailed implementation steps

### MODIFY EXISTING FILES
1. **UserService.java** - Add updateUserStatus() method
2. **UserController.java** - Add status update endpoint (ADMIN only)
3. **DailyPracticeServiceImpl.java** - Add invalidateCache() method

---

## PART 8: DEPENDENCIES & REQUIREMENTS

### Required
- All 6 personalization service classes (already updated with invalidateCache)
- CacheService (already exists with deletePattern)
- UserService (exists, needs status update method)
- UserRepository (exists)

### Optional
- Separate AdminController (if not using UserController)
- Role-based access control (already exists via @PreAuthorize)

---

## CONCLUSION

✅ **Audit Complete**

- **8 user-specific caches identified** (must be invalidated)
- **4 non-user caches identified** (do NOT invalidate)
- **6 services with invalidateCache() found** (1 missing in DailyPractice)
- **No admin status update endpoint found** (must create)
- **Security flow identified** (cache invalidation must happen BEFORE user tries to access resources)

**Next Steps**: Implement cache invalidation in UserService

