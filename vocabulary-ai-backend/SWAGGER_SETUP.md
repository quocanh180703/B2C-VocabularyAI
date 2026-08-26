# Swagger / OpenAPI Documentation

## Overview
VocabularyAI Backend API documentation is automatically generated and available through Swagger UI using SpringDoc OpenAPI.

## Access Swagger UI
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/api/v3/api-docs.yaml

## Configuration Files

### 1. pom.xml
```xml
<!-- OpenAPI / Swagger -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.4.0</version>
</dependency>

<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-api</artifactId>
    <version>2.4.0</version>
</dependency>
```

### 2. application.yml
```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: method
    tags-sorter: alpha
  show-actuator: false
  use-management-port: false
```

### 3. OpenApiConfig.java
Configuration class that customizes OpenAPI documentation:
- Location: `src/main/java/com/vocabularyai/common/config/OpenApiConfig.java`
- Defines API info, contact, license
- Configures JWT Bearer authentication scheme
- Sets up security requirements

## API Documentation by Feature

### 1. Authentication (/auth)
- `POST /auth/register` - Register new user
- `POST /auth/login` - User login
- `POST /auth/refresh` - Refresh access token
- `POST /auth/logout` - User logout

### 2. Users (/users)
- `GET /users/me` - Get current user profile
- `PUT /users/me` - Update user profile
- `PUT /users/me/password` - Change password

### 3. Categories (/categories)
- `GET /categories` - Get all categories
- `GET /categories/{id}` - Get category by ID
- `POST /categories` - Create category (admin)
- `PUT /categories/{id}` - Update category (admin)
- `DELETE /categories/{id}` - Delete category (admin)

### 4. Words (/words)
- `GET /words` - Get all words
- `GET /words/{id}` - Get word by ID
- `POST /words` - Create word (admin)
- `PUT /words/{id}` - Update word (admin)
- `DELETE /words/{id}` - Delete word (admin)
- `GET /words/category/{categoryId}` - Get words by category
- `GET /words/search` - Search words
- `POST /words/{wordId}/favorite` - Add to favorites
- `DELETE /words/{wordId}/favorite` - Remove from favorites
- `GET /words/favorites` - Get user's favorite words

### 5. Learning (/learning)
- `POST /learning/words/{wordId}/start` - Start learning a word
- `POST /learning/words/{wordId}/review` - Review word
- `GET /learning/progress` - Get learning progress
- `GET /learning/review-queue` - Get words needing review
- `GET /learning/history` - Get learning history
- `GET /learning/statistics` - Get progress statistics

### 6. Quizzes (/quizzes)
- `GET /quizzes` - Get published quizzes
- `GET /quizzes/{id}` - Get quiz with questions
- `POST /quizzes/{quizId}/start` - Start quiz attempt
- `POST /quizzes/{attemptId}/submit` - Submit quiz answers
- `GET /quizzes/attempts/{attemptId}` - Get attempt details
- `GET /quizzes/history` - Get user's quiz history

### 7. Health Check (/health)
- `GET /health` - Simple health check
- `GET /health/detailed` - Detailed health check
- `GET /health/ready` - Ready probe (Kubernetes)
- `GET /health/live` - Live probe (Kubernetes)

## Authentication in Swagger UI

### How to Test Protected Endpoints

1. **Register & Login**
   - First, call `POST /auth/register` or `POST /auth/login` to get tokens
   - Save the `accessToken` from response

2. **Authenticate in Swagger**
   - Click the "Authorize" button (lock icon) at the top right
   - Paste the token in the "Bearer" field without "Bearer" prefix
   - Click "Authorize"
   - Now all protected endpoints will include the token automatically

3. **Alternative: Direct Header**
   - For each request, add header: `Authorization: Bearer <your_token>`

## Features

### Automatic Documentation Generation
- All endpoints automatically documented from code annotations
- Request/response models with field descriptions
- Request examples generated from DTOs
- Response examples from actual entity structures

### Annotations Used

- `@Tag` - Group endpoints by resource
- `@Operation` - Describe operation
- `@SecurityRequirement` - Indicate authentication required
- `@ApiResponse` - Document response codes
- `@RequestParam` / `@PathVariable` / `@RequestBody` - Parameter documentation

### Security Scheme

- JWT Bearer authentication
- Scheme: HTTP Bearer with JWT format
- Configure via "Authorize" button in Swagger UI

## Testing Workflow

### 1. Register New User
```bash
POST /api/auth/register
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "Password123!"
}
```

### 2. Login
```bash
POST /api/auth/login
{
  "username": "testuser",
  "password": "Password123!"
}
```
Response includes `accessToken` and `refreshToken`

### 3. Use Token
Click "Authorize" in Swagger UI and paste the `accessToken`

### 4. Test Protected Endpoints
Now you can test all `/users`, `/learning`, `/words/favorites`, and `/quizzes/history` endpoints

## Troubleshooting

### Swagger UI Not Loading
- Check URL: `http://localhost:8080/api/swagger-ui.html`
- Verify server is running: `mvn spring-boot:run`
- Check logs for errors

### Authentication Not Working
- Ensure token is copied correctly (without "Bearer" prefix in Authorize dialog)
- Check token expiration (15 minutes for access token)
- Use refresh token to get new access token if expired

### API Specs Not Updating
- Clear browser cache
- Restart the application
- Check that endpoint has proper `@Operation` annotation

## Production Deployment

For production, consider:

1. **Disable Swagger in Production**
   ```yaml
   springdoc:
     swagger-ui:
       enabled: false
   ```

2. **Use Environment-Specific Profiles**
   - Create `application-prod.yml`
   - Override Swagger settings for production

3. **Document API Externally**
   - Export OpenAPI JSON/YAML from `/v3/api-docs`
   - Use external tools like Postman or ReDoc
   - Host documentation separately

## References

- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI 3.0 Specification](https://spec.openapis.org/oas/v3.0.3)
- [Swagger UI Documentation](https://swagger.io/tools/swagger-ui/)

