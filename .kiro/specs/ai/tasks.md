# Implementation Tasks: AI Integration with Google Gemini (PHASE 7)

## Overview

This document outlines the implementation tasks for PHASE 7: AI Integration with Google Gemini. The tasks are ordered by dependency, with foundational infrastructure first, then services, then controllers, and finally testing. Each task includes acceptance criteria, effort estimates, and detailed implementation guidance.

**Total Estimated Effort**: 80-100 hours (8-10 days for experienced developer)

**Task Dependencies**:
```
Task 1 (Dependency Setup)
    ↓
Task 2 (Entities & Migrations)
    ↓
Task 3 (Repositories & Mappers)
    ↓
Task 4-7 (Core Services)
    ↓
Task 8-11 (Business Logic Services)
    ↓
Task 12-15 (Controllers)
    ↓
Task 16 (Integration Testing)
    ↓
Task 17 (End-to-End Testing & Deployment)
```

---

## Task 1: Update Dependencies and Configuration

**Objective**: Add Google Generative AI SDK to project dependencies and configure Gemini API settings.

**Acceptance Criteria**:
- [ ] Google Generative AI SDK dependency (v0.4.0) added to pom.xml and uncommented
- [ ] Application compiles successfully with `mvn clean compile -q`
- [ ] `application.yml` configured with Gemini API settings (model, temperature, max-tokens, API key placeholder)
- [ ] Redis configuration verified and active
- [ ] New exceptions classes created (AIException, ExternalServiceException, RateLimitExceededException)
- [ ] Custom exception handlers registered in GlobalExceptionHandler

**Implementation Steps**:

1. **Update pom.xml**:
   - Uncomment Google Generative AI SDK dependency (line ~105)
   - Verify Maven downloads the dependency

2. **Create Custom Exception Classes**:
   - `d:\demo-datn\vocabulary-ai-backend\src\main\java\com\vocabularyai\ai\exception\AIException.java`
   - `d:\demo-datn\vocabulary-ai-backend\src\main\java\com\vocabularyai\ai\exception\RateLimitExceededException.java`
   - All extending `RuntimeException` with retry information
   - Include `@Getter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` annotations

3. **Update GlobalExceptionHandler**:
   - Add `@ExceptionHandler` for AIException → 500 Internal Server Error
   - Add `@ExceptionHandler` for RateLimitExceededException → 429 Too Many Requests with Retry-After header
   - Add `@ExceptionHandler` for external service failures → 503 Service Unavailable

4. **Update application.yml**:
   - Add Gemini API configuration section:
     ```yaml
     gemini:
       api-key: ${GEMINI_API_KEY:your-api-key-here}
       model: gemini-1.5-pro-latest
       temperature: 0.7
       top-p: 0.9
       max-tokens: 2048
       request-timeout: 30000
     ai:
       cache-ttl-hours: 24
       rate-limit:
         daily-tokens: 1000000
         daily-requests: 1000
       streaming: true
     ```

**Effort**: 4-6 hours
**Dependencies**: None
**Notes**: 
- Follow existing error handling patterns from PHASES 1-6
- Use @Slf4j for logging in exception handlers
- Test compilation before proceeding

---

## Task 2: Create AI Entities and Database Migration

**Objective**: Create Conversation, Message, and RateLimit entities with proper JPA annotations and database migration.

**Acceptance Criteria**:
- [ ] `Conversation.java` entity created with all fields, relationships, and validations
- [ ] `Message.java` entity created with enum MessageRole
- [ ] `RateLimit.java` entity created with composite index on (user_id, period_start)
- [ ] `ConversationType.java` enum created (GENERAL, VOCABULARY, GRAMMAR, LEARNING)
- [ ] `MessageRole.java` enum created (USER, ASSISTANT)
- [ ] Flyway migration `V7__Create_ai_tables.sql` created
- [ ] `mvn clean compile -q` succeeds
- [ ] `mvn package -q -DskipTests` succeeds
- [ ] Application starts and Flyway migration executes successfully

**Implementation Steps**:

1. **Create Enums**:
   - `d:\demo-datn\vocabulary-ai-backend\src\main\java\com\vocabularyai\ai\entity\ConversationType.java`
   - `d:\demo-datn\vocabulary-ai-backend\src\main\java\com\vocabularyai\ai\entity\MessageRole.java`

2. **Create Entities**:
   - `d:\demo-datn\vocabulary-ai-backend\src\main\java\com\vocabularyai\ai\entity\Conversation.java`
     - Fields: id, user (FK), type, title, summary, createdAt, updatedAt, isArchived, messages (OneToMany)
     - Use `@Entity`, `@Table(name="conversations")`, `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
     - Add `@Column(columnDefinition="LONGTEXT")` for summary field
     - Add `@OneToMany(mappedBy="conversation", cascade=CascadeType.ALL, orphanRemoval=true)`
   
   - `d:\demo-datn\vocabulary-ai-backend\src\main\java\com\vocabularyai\ai\entity\Message.java`
     - Fields: id, conversation (FK), role, content, tokensUsed, responseTimeMs, createdAt, metadata
     - Use `@Column(columnDefinition="LONGTEXT")` for content
     - Use `@ManyToOne(fetch=FetchType.LAZY)` for conversation
     - No updatable timestamp
   
   - `d:\demo-datn\vocabulary-ai-backend\src\main\java\com\vocabularyai\ai\entity\RateLimit.java`
     - Fields: id, user (FK), tokensUsed, requestCount, periodStart, periodEnd, createdAt, updatedAt
     - Add `@Index(name="idx_user_period", columnList="user_id, period_start")`
     - Use `@Column(nullable=false)` with default 0 for counters

3. **Create Flyway Migration**:
   - File: `d:\demo-datn\vocabulary-ai-backend\src\main\resources\db\migration\V7__Create_ai_tables.sql`
   - Tables: conversations, messages, rate_limits
   - Indexes: conversations(user_id), messages(conversation_id), rate_limits(user_id, period_start)
   - Foreign key: conversations.user_id → users.id, messages.conversation_id → conversations.id, rate_limits.user_id → users.id
   - Use InnoDB for transactions, UTF-8MB4 collation

**Effort**: 6-8 hours
**Dependencies**: Task 1 (pom.xml)
**Notes**:
- Follow JPA best practices: lazy loading, cascade delete, proper indexing
- Test migration by running application with Flyway validation mode
- Ensure migration is idempotent

---

## Task 3: Create DTOs and Mappers

**Objective**: Create all request/response DTOs and MapStruct mappers for API communication.

**Acceptance Criteria**:
- [ ] All request DTOs created with validation annotations
- [ ] All response DTOs created with `@Data`, `@Builder` annotations
- [ ] MapStruct mappers created for entity ↔ DTO conversions
- [ ] Custom mapping methods for complex conversions (e.g., Conversation → ConversationResponse)
- [ ] `mvn clean compile -q` succeeds with MapStruct processor generating mappers

**DTOs to Create**:

1. **Request DTOs** (in `d:\demo-datn\vocabulary-ai-backend\src\main\java\com\vocabularyai\ai\dto\request\`):
   - `CreateConversationRequest.java` (type, title)
   - `SendMessageRequest.java` (content, context)
   - `GrammarCheckRequest.java` (text, language, includeSuggestions)
   - `ExplainWordRequest.java` (wordId, context optional)
   - `ExplainWordsRequest.java` (wordIds list)
   - `HintRequest.java` (learningProgressId, attemptCount)

2. **Response DTOs** (in `d:\demo-datn\vocabulary-ai-backend\src\main\java\com\vocabularyai\ai\dto\response\`):
   - `ConversationResponse.java` (id, title, type, createdAt, updatedAt, messageCount)
   - `ConversationDetailResponse.java` (extends ConversationResponse, add messages list)
   - `MessageResponse.java` (id, role, content, createdAt, tokensUsed, responseTimeMs)
   - `ExplanationResponse.java` (wordId, word, explanation, examples, etymology, relatedWords, partOfSpeech)
   - `GrammarCheckResponse.java` (originalText, correctedText, errors, suggestions)
   - `GrammarError.java` (startOffset, endOffset, errorType, suggestion, explanation)
   - `GrammarSuggestion.java` (errorType, suggestion, explanation)
   - `HintResponse.java` (learningProgressId, hint, hintLevel, strategies, explanation)

3. **MapStruct Mappers** (in `d:\demo-datn\vocabulary-ai-backend\src\main\java\com\vocabularyai\ai\mapper\`):
   - `ConversationMapper.java` - Conversation ↔ ConversationResponse + ConversationDetailResponse
   - `MessageMapper.java` - Message ↔ MessageResponse
   - `ExplanationMapper.java` - Word + explanation ↔ ExplanationResponse

**Implementation Steps**:

1. Create all DTO classes with:
   - `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder` annotations
   - Validation annotations from `javax.validation`: `@NotNull`, `@NotBlank`, `@Size`, `@Min`, `@Max`
   - Nested DTOs for complex structures (GrammarError, GrammarSuggestion)

2. Create MapStruct mappers with:
   - `@Mapper(componentModel = "spring")` 
   - `@Mapping` annotations for field name mismatches
   - Custom methods for complex conversions (e.g., messageList to MessageResponseList)

**Effort**: 8-10 hours
**Dependencies**: Task 2 (Entities)
**Notes**:
- Follow MapStruct best practices: immutable DTOs, use `@Mapping` for clarity
- Implement validation annotations consistently
- Test mapper generation by compiling

---

## Task 4: Create Repository Interfaces

**Objective**: Create Spring Data JPA repositories for AI entities with custom query methods.

**Acceptance Criteria**:
- [ ] `ConversationRepository.java` created with custom finder methods
- [ ] `MessageRepository.java` created with pagination support
- [ ] `RateLimitRepository.java` created with rate limit queries
- [ ] All repositories extend `JpaRepository<Entity, Long>`
- [ ] Repositories use `@Query` annotations for complex queries
- [ ] All compile without errors

**Repositories to Create** (in `d:\demo-datn\vocabulary-ai-backend\src\main\java\com\vocabularyai\ai\repository\`):

1. **ConversationRepository.java**:
   ```java
   Page<Conversation> findByUserId(Long userId, Pageable pageable);
   Optional<Conversation> findByIdAndUserId(Long id, Long userId);
   List<Conversation> findByUserIdAndType(Long userId, ConversationType type);
   long countByUserId(Long userId);
   ```

2. **MessageRepository.java**:
   ```java
   List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
   Page<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);
   int countByConversationId(Long conversationId);
   void deleteByConversationId(Long conversationId);
   List<Message> findByConversationIdAndRoleOrderByCreatedAtDesc(Long conversationId, MessageRole role, Pageable pageable);
   ```

3. **RateLimitRepository.java**:
   ```java
   Optional<RateLimit> findByUserIdAndPeriodStartBeforeAndPeriodEndAfter(Long userId, LocalDateTime start, LocalDateTime end);
   Optional<RateLimit> findLatestByUserId(Long userId);  // @Query with ORDER BY createdAt DESC LIMIT 1
   long sumTokensUsedByUserInPeriod(Long userId, LocalDateTime start, LocalDateTime end);  // @Query
   void deleteExpiredRecords(LocalDateTime expiredBefore);  // @Query DELETE
   ```

**Implementation Steps**:

1. Create repository interfaces extending `JpaRepository<Entity, Long>`
2. Add custom finder methods
3. Use `@Query` for complex queries with JPQL or native SQL
4. Add `@Transactional` for delete operations where needed

**Effort**: 3-4 hours
**Dependencies**: Task 2 (Entities)
**Notes**:
- Use named query methods where possible (Spring auto-implements)
- Use `@Query` for complex criteria (date ranges, aggregations)
- Verify pagination support with `Pageable` parameter

---

## Task 5: Implement CacheService with Redis Integration

**Objective**: Create Redis-backed caching service for AI responses and rate limit tracking.

**Acceptance Criteria**:
- [ ] `CacheService.java` interface created with all cache operations
- [ ] `CacheServiceImpl.java` implementation created with Redis integration
- [ ] Serialization/deserialization using ObjectMapper (Jackson)
- [ ] TTL support for different data types
- [ ] All cache operations tested and working
- [ ] Atomic operations for rate limit tracking using Redis INCR commands
- [ ] `mvn clean compile -q` succeeds

**Implementation Steps**:

1. **Create CacheService Interface** (in `d:\demo-datn\vocabulary-ai-backend\src\main\java\com\vocabularyai\ai\service\`):
   ```java
   public interface CacheService {
       void set(String key, Object value, long ttlSeconds);
       void set(String key, Object value);
       <T> Optional<T> get(String key, Class<T> type);
       void delete(String key);
       void deletePattern(String pattern);
       
       // Conversation-specific
       void cacheConversationContext(Long conversationId, List<Message> messages, long ttlSeconds);
       Optional<List<Message>> getConversationContext(Long conversationId);
       
       // Explanation-specific
       void cacheExplanation(Long wordId, ExplanationResponse explanation);
       Optional<ExplanationResponse> getExplanation(Long wordId);
       void deleteExplanation(Long wordId);
       
       // Rate limit-specific
       void incrementRateLimit(Long userId, int tokens);
       long getRateLimitTokensUsed(Long userId);
       long getRateLimitRequestCount(Long userId);
   }
   ```

2. **Implement CacheServiceImpl**:
   - Inject `StringRedisTemplate` or `RedisTemplate<String, String>`
   - Use `ObjectMapper` for serialization
   - Implement retry logic with exponential backoff for connection failures
   - Add `@Slf4j` for logging cache hits/misses
   - Use try-catch to gracefully handle Redis unavailability
   - Generate cache keys with consistent prefix: `ai:{type}:{id}`

3. **Configuration**:
   - Add Redis connection pooling settings to `application.yml`
   - Configure default TTLs for each cache type

**Cache Key Naming**:
```
Conversations:     ai:conversation:{conversationId}
Explanations:      ai:explanation:{wordId}
Rate Limits:       ai:ratelimit:{userId}:{period}
Messages:          ai:messages:{conversationId}
```

**Effort**: 8-10 hours
**Dependencies**: Task 1 (Configuration)
**Notes**:
- Use `StringRedisTemplate` for better type safety
- Implement fail-open pattern: if Redis unavailable, continue without cache
- Add metrics/monitoring for cache hit rates
- Test with actual Redis connection

---

## Task 6: Implement PromptEngineeringService

**Objective**: Create service for building optimized prompts for different AI use cases.

**Acceptance Criteria**:
- [ ] `PromptEngineeringService.java` interface created
- [ ] `PromptEngineeringServiceImpl.java` implementation with all prompt builders
- [ ] System prompts configured externally (application.yml or properties file)
- [ ] Chat history formatting with proper structure
- [ ] Context injection for vocabulary, grammar, and learning
- [ ] Prompt sanitization (remove control characters, normalize whitespace)
- [ ] Token estimation accuracy (4 chars ≈ 1 token)
- [ ] `mvn clean compile -q` succeeds

**Prompt Templates to Implement**:

1. **Chat Prompt** - For conversational AI:
   ```
   You are a helpful English learning assistant. The user is learning English.
   [CONTEXT based on ConversationType]
   
   Previous conversation:
   [Message history with roles]
   
   User: [New user message]
   Assistant: [Generate response]
   ```

2. **Vocabulary Explanation Prompt**:
   ```
   Explain the English word "{word}" in detail. Include:
   - Clear definition
   - 2-3 usage examples
   - Etymology (word origin)
   - Related words (synonyms/antonyms)
   - Part of speech
   
   Format as structured text with clear sections.
   ```

3. **Grammar Checking Prompt**:
   ```
   Check the following text for grammar, spelling, and style errors:
   "{text}"
   
   For each error, provide:
   - Error type (grammar/spelling/style/punctuation)
   - Character position (start and end)
   - Suggested correction
   - Brief explanation
   
   Also provide the fully corrected text.
   ```

4. **Learning Hint Prompt** (with progressive difficulty):
   ```
   The user has attempted this word {attemptCount} times:
   - Word: {word}
   - Definition: {definition}
   - Previous attempts: {attemptHistory}
   
   Based on attempt {attemptCount}, provide a hint at difficulty level {hintLevel}:
   - Level 1: Very minimal hint (definition only)
   - Level 2: Moderate hint (definition + 1 example)
   - Level 3: Detailed hint (full explanation + learning strategies)
   ```

**Implementation Steps**:

1. Create `PromptEngineeringService` interface
2. Implement `PromptEngineeringServiceImpl` with @Service annotation
3. Inject dependencies: ConversationRepository, Word repository, LearningProgressRepository
4. Create private methods for:
   - Sanitizing user input
   - Formatting message history
   - Building context-aware prompts
   - Estimating token count
5. Create prompt template loader (properties file or @ConfigurationProperties)
6. Add `@Slf4j` for debugging prompt generation

**Effort**: 6-8 hours
**Dependencies**: Task 3 (DTOs), Task 4 (Repositories)
**Notes**:
- Store system prompts externally for easy tuning
- Use template variables like {word}, {context}, {attemptCount}
- Implement sanitization: trim, remove control chars, normalize whitespace
- Test prompt templates manually with Gemini API first

---

## Task 7: Implement GeminiAIClientService

**Objective**: Create core Gemini API client with streaming, rate limiting, and error handling.

**Acceptance Criteria**:
- [ ] `GeminiAIClientService.java` interface created
- [ ] `GeminiAIClientServiceImpl.java` implementation with Gemini SDK integration
- [ ] Streaming response support with callbacks
- [ ] Rate limit pre-check and enforcement
- [ ] Token counting before API calls
- [ ] Exponential backoff retry logic (1s, 2s, 4s, 8s, 16s max)
- [ ] Circuit breaker pattern for API unavailability
- [ ] All Gemini configuration from application.yml
- [ ] `mvn clean compile -q` succeeds
- [ ] Integration test with Gemini API (with mocking for CI/CD)

**Implementation Steps**:

1. **Create Interface** (in `d:\demo-datn\vocabulary-ai-backend\src\main\java\com\vocabularyai\ai\service\`):
   ```java
   public interface GeminiAIClientService {
       String generateResponse(String prompt, GenerationConfig config);
       void generateResponseStream(String prompt, StreamCallback callback);
       String generateConversationResponse(List<Message> history, String userMessage);
       boolean isRateLimitExceeded(Long userId);
       int countTokens(String text);
   }
   ```

2. **Implement GeminiAIClientServiceImpl**:
   - Initialize Google Generative AI SDK in constructor
   - Use configuration properties from `gemini.model`, `gemini.temperature`, etc.
   - Implement `@Transactional` for atomic rate limit updates
   - Add `@Slf4j` for request/response logging
   - Inject RateLimitRepository, CacheService, PromptEngineeringService

3. **Rate Limiting Logic**:
   ```
   Before API call:
   - Get current RateLimit record for user (24-hour window)
   - Count tokens in prompt
   - If tokens_used + new_tokens > daily_limit: throw RateLimitExceededException
   
   After API call:
   - Update RateLimit: tokensUsed += actual_tokens, requestCount++
   - Use atomic Redis INCR for distributed consistency
   ```

4. **Retry Logic**:
   - Catch `IOException`, `ApiException`, `TimeoutException`
   - Implement exponential backoff: 1s, 2s, 4s, 8s, 16s (max 5 attempts)
   - Log retry attempts
   - Throw exception if all retries fail

5. **Circuit Breaker**:
   - Track consecutive API failures
   - After 5 consecutive failures, fail fast for 30 seconds
   - Reset counter on success

6. **Streaming Support**:
   ```java
   public interface StreamCallback {
       void onContent(String chunk);
       void onComplete();
       void onError(Exception error);
   }
   ```

**Effort**: 10-12 hours
**Dependencies**: Task 5 (CacheService), Task 6 (PromptEngineeringService)
**Notes**:
- Mock Gemini API in tests using `@MockBean`
- Test rate limiting with in-memory database and Redis
- Verify token counting accuracy
- Handle streaming gracefully (connection drops, timeouts)

---

## Task 8: Implement ChatService

**Objective**: Create high-level chat service managing conversations and messages.

**Acceptance Criteria**:
- [ ] `ChatService.java` interface created with all conversation operations
- [ ] `ChatServiceImpl.java` implementation with transaction management
- [ ] All 6 requirements (create, send, retrieve, list, delete, clear) implemented
- [ ] Authorization checks for conversation ownership
- [ ] Cache invalidation on updates/deletes
- [ ] Transaction management with proper rollback
- [ ] `mvn clean compile -q` succeeds
- [ ] All unit tests pass

**Implementation Steps**:

1. **Create Interface**:
   - Methods from design document (6 conversation operations)
   - Inject authentication context (Principal, SecurityContext)
   - Return appropriate DTOs

2. **Implement ChatServiceImpl**:
   - Inject: ConversationRepository, MessageRepository, GeminiAIClientService, CacheService, ConversationMapper, MessageMapper
   - Add `@Transactional` for message sending (atomic insert for user + AI message)
   - Add authorization checks using `getCurrentUserId()` helper
   - Return 403 Forbidden for unauthorized access

3. **Implementation Details**:
   - **createConversation**: Create new Conversation entity, save to DB, cache empty context
   - **sendMessage**: Save user message, invoke Gemini client, save AI response, update cache, measure response time
   - **getConversation**: Verify ownership, retrieve with pagination, load from cache if available
   - **getUserConversations**: Query paginated conversations for current user
   - **deleteConversation**: Verify ownership, cascade delete messages, invalidate cache
   - **clearConversationHistory**: Delete all messages but keep conversation metadata, invalidate cache

4. **Helper Methods**:
   - `getCurrentUserId()` - Extract from SecurityContext
   - `verifyConversationOwnership(conversationId)` - Check ownership or throw ForbiddenException
   - `measureResponseTime(startTime)` - Return milliseconds

**Effort**: 8-10 hours
**Dependencies**: Task 4 (Repositories), Task 5-7 (Services)
**Notes**:
- Use `@Transactional` for message sending to ensure atomicity
- Implement optimistic locking if concurrent updates expected
- Log all operations at info level, errors at error level

---

## Task 9: Implement VocabularyExplanationService

**Objective**: Create vocabulary explanation service with caching and context support.

**Acceptance Criteria**:
- [ ] `VocabularyExplanationService.java` interface created
- [ ] `VocabularyExplanationServiceImpl.java` implementation
- [ ] All 4 requirements (single, batch, by text, with context) implemented
- [ ] Caching with 24-hour TTL
- [ ] Graceful handling of missing words
- [ ] Context-aware explanations
- [ ] `mvn clean compile -q` succeeds

**Implementation Steps**:

1. **Create Interface**:
   - 4 methods: explainWord, explainWords, explainByText, explainWithContext

2. **Implement VocabularyExplanationServiceImpl**:
   - Inject: WordRepository, GeminiAIClientService, CacheService, PromptEngineeringService, ExplanationMapper
   - Check cache first for all operations
   - Generate cache key: `ai:explanation:{wordId}:{context_hash}` for context-aware

3. **Implementation Details**:
   - **explainWord(wordId)**: Check cache → if hit return; else fetch word → generate prompt → call Gemini → cache → return
   - **explainWords(wordIds)**: For each word, check cache, collect misses, batch call Gemini, cache results, combine
   - **explainByText(word)**: Search WordRepository for matching word, invoke explainWord, handle 404 if not found
   - **explainWithContext(wordId, context)**: Use context in prompt, cache with context-aware key

4. **Error Handling**:
   - Word not found → throw ResourceNotFoundException
   - Gemini API error → throw AIException
   - Rate limit → throw RateLimitExceededException

**Effort**: 6-8 hours
**Dependencies**: Task 6-7 (Services), Task 4 (Repositories)
**Notes**:
- Test batch operations with varying cache hit rates
- Verify context incorporation in prompt engineering

---

## Task 10: Implement GrammarCheckingService

**Objective**: Create grammar checking service with detailed error reporting and corrections.

**Acceptance Criteria**:
- [ ] `GrammarCheckingService.java` interface created
- [ ] `GrammarCheckingServiceImpl.java` implementation
- [ ] All 3 requirements (check, get suggestions, auto-correct) implemented
- [ ] Caching with text hash (1-hour TTL)
- [ ] Error detection with positions and explanations
- [ ] Auto-correction support
- [ ] `mvn clean compile -q` succeeds

**Implementation Steps**:

1. **Create Interface**:
   - 3 methods: checkGrammar, getSuggestions, correctText

2. **Implement GrammarCheckingServiceImpl**:
   - Inject: GeminiAIClientService, CacheService, PromptEngineeringService
   - Hash input text using SHA-256 for cache key
   - Parse Gemini response into GrammarError and GrammarSuggestion objects

3. **Implementation Details**:
   - **checkGrammar(request)**: Validate text → hash → check cache → generate prompt → call Gemini → parse response → cache → return
   - **getSuggestions(text)**: Call checkGrammar, extract suggestions list
   - **correctText(text)**: Call checkGrammar, return correctedText field

4. **Response Parsing**:
   - Gemini returns structured text with errors and corrected version
   - Parse with regex or structured prompt requesting JSON output
   - Extract: startOffset, endOffset, errorType, suggestion, explanation

5. **Input Validation**:
   - Text 1-10,000 characters
   - Return 400 Bad Request if invalid

**Effort**: 6-7 hours
**Dependencies**: Task 6-7 (Services)
**Notes**:
- Use SHA-256 for consistent text hashing
- Test with various grammar error types
- Verify error position accuracy

---

## Task 11: Implement LearningHintService

**Objective**: Create personalized learning hint service with progressive difficulty levels.

**Acceptance Criteria**:
- [ ] `LearningHintService.java` interface created
- [ ] `LearningHintServiceImpl.java` implementation
- [ ] All 3 requirements (personalized, contextual with attempt count, batch for review) implemented
- [ ] Progressive hint difficulty (levels 1-3)
- [ ] Caching with 1-hour TTL
- [ ] Integration with LearningProgress and attempt tracking
- [ ] `mvn clean compile -q` succeeds

**Implementation Steps**:

1. **Create Interface**:
   - 3 methods: getHint, getHintWithContext, getHintsForReview

2. **Implement LearningHintServiceImpl**:
   - Inject: LearningProgressRepository, GeminiAIClientService, CacheService, PromptEngineeringService
   - Attempt count determines hint difficulty: 1→Level 1, 2→Level 2, 3+→Level 3

3. **Implementation Details**:
   - **getHint(learningProgressId)**: Check cache → fetch LearningProgress → generate prompt with attempt count → call Gemini → cache → return
   - **getHintWithContext(learningProgressId, attemptCount)**: Same as above with explicit attempt count
   - **getHintsForReview(userId, count)**: Query LearningProgress items needing review → generate hints for up to count items → return list

4. **Hint Difficulty Mapping**:
   ```
   Level 1 (attemptCount ≤ 1): Definition only, very brief
   Level 2 (attemptCount == 2): Definition + 1 example, moderate length
   Level 3 (attemptCount ≥ 3): Full explanation + strategies, detailed
   ```

5. **Cache Key**:
   - `ai:hint:{learningProgressId}:{attemptCount}`
   - TTL: 1 hour

**Effort**: 6-8 hours
**Dependencies**: Task 6-7 (Services), existing LearningProgressRepository
**Notes**:
- Verify attempt count tracking from existing LearningProgress entity
- Test progressive hint generation with different attempt counts
- Ensure learning strategies are included in Level 3 hints

---

## Task 12: Create ChatController

**Objective**: Create REST API endpoints for conversation management.

**Acceptance Criteria**:
- [ ] `ChatController.java` created with all 6 conversation endpoints
- [ ] Proper HTTP methods and status codes (201 Created, 204 No Content, etc.)
- [ ] Request/response validation
- [ ] Swagger/OpenAPI documentation (@Operation, @ApiResponse)
- [ ] JWT authentication enforcement
- [ ] Rate limiting headers in responses
- [ ] `mvn clean compile -q` succeeds

**Endpoints to Implement**:

```
POST   /api/ai/chat/conversations
       Create new conversation
       Request: CreateConversationRequest
       Response: 201 Created + ConversationResponse

GET    /api/ai/chat/conversations
       Get user's conversations (paginated)
       Query: page=0, size=20, sort=updatedAt,desc
       Response: 200 OK + Page<ConversationResponse>

GET    /api/ai/chat/conversations/{conversationId}
       Get conversation with message history
       Query: limit=50
       Response: 200 OK + ConversationDetailResponse

POST   /api/ai/chat/conversations/{conversationId}/messages
       Send message and get AI response
       Request: SendMessageRequest
       Response: 200 OK + MessageResponse

DELETE /api/ai/chat/conversations/{conversationId}
       Delete conversation
       Response: 204 No Content

DELETE /api/ai/chat/conversations/{conversationId}/messages
       Clear conversation message history
       Response: 204 No Content
```

**Implementation Steps**:

1. **Create Controller Class**:
   ```java
   @RestController
   @RequestMapping("/api/ai/chat")
   @RequiredArgsConstructor
   @Slf4j
   @Tag(name = "Chat", description = "Conversational AI chat operations")
   public class ChatController { ... }
   ```

2. **Implement Endpoints**:
   - Add `@PostMapping`, `@GetMapping`, `@DeleteMapping` annotations
   - Add `@Operation` with descriptions
   - Add `@ApiResponse` for different status codes
   - Add `@PreAuthorize("hasRole('USER')")` for authentication
   - Add `@Valid` for request validation
   - Add proper response entities with HttpStatus

3. **Error Handling**:
   - Catch ChatServiceException and related exceptions
   - Return appropriate error responses
   - Log exceptions at error level

4. **Pagination**:
   - Accept `@PageableDefault(size=20, sort="updatedAt", direction=DESC)`
   - Return `Page<ConversationResponse>`

**Effort**: 4-5 hours
**Dependencies**: Task 8 (ChatService), Task 3 (DTOs)
**Notes**:
- Follow existing controller patterns from PHASES 1-6
- Add comprehensive Swagger documentation
- Test with Swagger UI

---

## Task 13: Create VocabularyExplanationController

**Objective**: Create REST API endpoints for vocabulary explanations.

**Acceptance Criteria**:
- [ ] `VocabularyExplanationController.java` created with 4 endpoints
- [ ] Request/response validation
- [ ] Swagger documentation
- [ ] JWT authentication
- [ ] `mvn clean compile -q` succeeds

**Endpoints to Implement**:

```
GET    /api/ai/vocabulary/{wordId}/explain
       Get AI explanation for word
       Response: 200 OK + ExplanationResponse

POST   /api/ai/vocabulary/explain/batch
       Get explanations for multiple words
       Request: { wordIds: [1, 2, 3] }
       Response: 200 OK + List<ExplanationResponse>

GET    /api/ai/vocabulary/explain
       Search and explain by word text
       Query: word=example
       Response: 200 OK + ExplanationResponse
```

**Effort**: 3-4 hours
**Dependencies**: Task 9 (VocabularyExplanationService)
**Notes**:
- Reuse error handling from ChatController pattern
- Add pagination for batch endpoints if needed

---

## Task 14: Create GrammarCheckController

**Objective**: Create REST API endpoints for grammar checking.

**Acceptance Criteria**:
- [ ] `GrammarCheckController.java` created with 3 endpoints
- [ ] Request/response validation
- [ ] Swagger documentation
- [ ] JWT authentication
- [ ] `mvn clean compile -q` succeeds

**Endpoints to Implement**:

```
POST   /api/ai/grammar/check
       Check grammar in provided text
       Request: GrammarCheckRequest
       Response: 200 OK + GrammarCheckResponse

POST   /api/ai/grammar/correct
       Get corrected version of text
       Request: { text: "..." }
       Response: 200 OK + { correctedText: "..." }

POST   /api/ai/grammar/suggestions
       Get grammar suggestions for text
       Request: { text: "..." }
       Response: 200 OK + List<GrammarSuggestion>
```

**Effort**: 3-4 hours
**Dependencies**: Task 10 (GrammarCheckingService)
**Notes**:
- Simple request/response structures
- Add input validation for text length

---

## Task 15: Create LearningHintController

**Objective**: Create REST API endpoints for learning hints.

**Acceptance Criteria**:
- [ ] `LearningHintController.java` created with 3 endpoints
- [ ] Request/response validation
- [ ] Swagger documentation
- [ ] JWT authentication
- [ ] `mvn clean compile -q` succeeds

**Endpoints to Implement**:

```
POST   /api/ai/learning/{learningProgressId}/hint
       Get personalized learning hint
       Query: attemptCount (optional)
       Response: 200 OK + HintResponse

GET    /api/ai/learning/hints/review
       Get hints for words needing review
       Query: limit=10
       Response: 200 OK + List<HintResponse>
```

**Effort**: 3-4 hours
**Dependencies**: Task 11 (LearningHintService)
**Notes**:
- Integrate with existing LearningProgress entities
- Verify user ownership if needed

---

## Task 16: Integration Testing (Unit + Integration Tests)

**Objective**: Create comprehensive tests for all AI services and controllers.

**Acceptance Criteria**:
- [ ] Unit tests for all services created in `src/test/java`
- [ ] Integration tests with real database (H2 or TestContainers)
- [ ] Mocked Gemini API for predictable testing
- [ ] Controller tests with MockMvc
- [ ] All tests pass with `mvn test`
- [ ] Coverage report generated
- [ ] Edge cases tested (empty input, rate limiting, caching, errors)

**Test Classes to Create** (in `src/test/java/com/vocabularyai/ai/`):

1. **Service Tests**:
   - `ChatServiceTest` - test all 6 conversation operations
   - `VocabularyExplanationServiceTest` - test caching and batch operations
   - `GrammarCheckingServiceTest` - test error detection and parsing
   - `LearningHintServiceTest` - test progressive difficulty levels
   - `GeminiAIClientServiceTest` - test rate limiting, retry logic, token counting (with mocked SDK)
   - `PromptEngineeringServiceTest` - test prompt generation for different use cases
   - `CacheServiceTest` - test Redis operations, TTL, atomicity

2. **Controller Tests**:
   - `ChatControllerTest` - test all endpoints with MockMvc
   - `VocabularyExplanationControllerTest`
   - `GrammarCheckControllerTest`
   - `LearningHintControllerTest`

3. **Integration Tests**:
   - `ChatIntegrationTest` - end-to-end conversation flow
   - `AIIntegrationTest` - all features together

**Test Framework**:
- JUnit 5 (already in pom.xml via spring-boot-starter-test)
- Mockito for mocking services
- @SpringBootTest for integration tests
- @DataJpaTest for repository tests
- TestContainers for MySQL/Redis (optional but recommended)

**Effort**: 12-15 hours
**Dependencies**: All tasks 1-15
**Notes**:
- Mock Gemini API using Mockito to avoid API calls in tests
- Test error scenarios (rate limit exceeded, 404 errors, etc.)
- Test caching behavior (hits, misses, expiration)
- Use property-based testing with QuickTheories or similar for edge cases

---

## Task 17: End-to-End Testing and Deployment

**Objective**: Verify entire PHASE 7 system, compile, and deploy to production environment.

**Acceptance Criteria**:
- [ ] `mvn clean compile -q` succeeds with zero errors
- [ ] `mvn package -q -DskipTests` succeeds
- [ ] Application starts on port 8080 with no errors
- [ ] Swagger UI accessible at `http://localhost:8080/api/swagger-ui.html`
- [ ] All PHASE 7 endpoints functional via Swagger or Postman
- [ ] JWT authentication working
- [ ] Rate limiting enforced correctly
- [ ] Caching working (verify with Redis monitoring)
- [ ] Database migrations applied successfully
- [ ] Existing PHASES 1-6 functionality still working
- [ ] No compilation warnings or Lombok issues
- [ ] All tests pass: `mvn test`
- [ ] Load testing: simulate 100+ concurrent users with acceptable response times

**Deployment Checklist**:

1. **Pre-Deployment**:
   - [ ] Code review completed
   - [ ] All tests passing (unit + integration)
   - [ ] Build successful with no warnings
   - [ ] Configuration prepared for production (API keys, endpoints)
   - [ ] Database backups taken
   - [ ] Monitoring/alerting configured

2. **Deployment**:
   - [ ] Stop existing application
   - [ ] Deploy new WAR/JAR
   - [ ] Start application with PHASE 7 configuration
   - [ ] Run smoke tests
   - [ ] Verify all endpoints
   - [ ] Monitor application logs for errors

3. **Post-Deployment**:
   - [ ] Monitor error rates and latency
   - [ ] Verify cache hit rates
   - [ ] Check Gemini API quota usage
   - [ ] Monitor rate limit enforcement
   - [ ] Collect user feedback

4. **Rollback Plan**:
   - [ ] Document current version (git tag: `v7.0.0`)
   - [ ] Keep previous version available
   - [ ] Plan rollback procedure (revert database migrations if needed)

**End-to-End Testing Steps**:

1. **Manual API Testing** (via Swagger UI):
   - Create conversation
   - Send chat message with AI response
   - Retrieve conversation history
   - Request vocabulary explanation
   - Submit grammar check request
   - Request learning hint

2. **Test Scenarios**:
   - Single user conversation
   - Multiple concurrent users
   - Rate limit enforcement (exceed quota)
   - Cache hits (repeated requests)
   - Error scenarios (invalid input, 404 errors, API unavailability)
   - Long conversation history (pagination)

3. **Performance Verification**:
   - Chat response time < 5 seconds (SLA)
   - Cached responses < 100ms (SLA)
   - 100+ concurrent users without errors
   - Memory usage stable
   - Database query performance acceptable

4. **Security Verification**:
   - JWT authentication required
   - User can only access own conversations
   - Rate limits enforced
   - No SQL injection vulnerabilities
   - Sensitive data (API keys) not logged

**Effort**: 8-10 hours
**Dependencies**: All tasks 1-16
**Notes**:
- Document any issues found and resolutions
- Collect metrics for future optimization
- Plan for monitoring and alerting setup
- Document PHASE 7 architecture and operations in README

---

## Task Ordering and Dependency Graph

```
┌─────────────────────────────┐
│ Task 1: Dependencies & Config│
└──────────────┬──────────────┘
               │
┌──────────────▼──────────────┐
│ Task 2: Entities & Migration│
└──────────────┬──────────────┘
               │
      ┌────────┴────────┐
      │                 │
┌─────▼────┐      ┌─────▼─────┐
│ Task 3   │      │ Task 4    │
│ DTOs &   │      │ Repos     │
│ Mappers  │      │           │
└─────┬────┘      └─────┬─────┘
      │                 │
      └────────┬────────┘
               │
      ┌────────┴────────────┬──────────┐
      │                     │          │
 ┌────▼────┐       ┌───────▼───┐  ┌──▼──────┐
 │ Task 5  │       │ Task 6    │  │ Task 7  │
 │ Cache   │       │ Prompt    │  │ Gemini  │
 │ Service │       │ Engineering   │ Client  │
 └────┬────┘       └───────┬───┘  └──┬──────┘
      │                     │         │
      └─────────────────┬───┴─────────┘
                        │
         ┌──────┬───────┼────────┬─────────┐
         │      │       │        │         │
    ┌────▼───┐ ┌▼────┐ ┌▼──────┐ ┌──────▼──┐
    │Task 8  │ │Task 9│ │Task 10│ │ Task 11 │
    │ Chat   │ │Vocab │ │Grammar│ │ Hint    │
    │Service │ │Expl. │ │Check  │ │ Service │
    └────┬───┘ └─┬────┘ └─┬─────┘ └────┬────┘
         │       │        │            │
         └───┬───┴────┬───┴────────┬───┘
             │        │            │
        ┌────▼─┐  ┌───▼───┐  ┌────▼────┐
        │Task12│  │Task13 │  │ Task14  │
        │Chat  │  │Vocab  │  │ Grammar │
        │Ctrl  │  │Ctrl   │  │ Ctrl    │
        └────┬─┘  └───┬───┘  └────┬────┘
             │        │            │
             └────┬───┴────────┬───┘
                  │            │
              ┌───▼────────────▼───┐
              │    Task 15         │
              │ Learning Hint Ctrl │
              └───────┬────────────┘
                      │
              ┌───────▼─────────┐
              │  Task 16        │
              │  Integration    │
              │  Tests          │
              └───────┬─────────┘
                      │
              ┌───────▼──────────────┐
              │  Task 17             │
              │ E2E Testing &        │
              │ Deployment           │
              └──────────────────────┘
```

---

## Success Criteria Summary

### Build & Compilation
- ✅ `mvn clean compile -q` succeeds
- ✅ `mvn package -q -DskipTests` succeeds
- ✅ No warnings or errors

### Functionality
- ✅ All 4 AI features functional (Chat, Vocabulary, Grammar, Hints)
- ✅ All 6 conversation operations working
- ✅ JWT authentication enforced
- ✅ Rate limiting operational
- ✅ Caching working with correct TTLs

### Testing
- ✅ All unit tests pass (`mvn test`)
- ✅ All integration tests pass
- ✅ Edge cases tested (errors, rate limits, cache)
- ✅ Load testing passed (100+ concurrent users)

### Deployment
- ✅ Application running on port 8080
- ✅ Swagger UI functional at `/api/swagger-ui.html`
- ✅ Database migrations applied
- ✅ Existing PHASES 1-6 functionality preserved
- ✅ Monitoring and alerting configured

### Documentation
- ✅ API endpoints documented in Swagger
- ✅ README updated with PHASE 7 overview
- ✅ Configuration documented
- ✅ Error handling procedures documented

---

## Resource Allocation

**Total Effort**: 80-100 hours
**Suggested Timeline**: 2-3 weeks (full-time development)
**Team Composition**: 1-2 experienced Java/Spring developers

**Hourly Breakdown by Phase**:
- Weeks 1: Tasks 1-4 (Infrastructure) - 20-24 hours
- Week 2: Tasks 5-11 (Core Services) - 45-50 hours
- Week 2-3: Tasks 12-17 (Controllers, Testing, Deployment) - 15-26 hours

---

## Critical Success Factors

1. **API Key Management**: Ensure Gemini API key is securely stored in environment variables, not hardcoded
2. **Rate Limiting**: Thoroughly test rate limiting logic with boundary conditions
3. **Caching Strategy**: Validate cache hit rates and TTL effectiveness
4. **Error Recovery**: Test all error scenarios and verify graceful degradation
5. **Monitoring**: Set up comprehensive logging for debugging AI integration issues
6. **Performance**: Verify response times meet SLAs under load
7. **Security**: Ensure conversation isolation and JWT enforcement
8. **Database**: Validate Flyway migrations and data integrity
