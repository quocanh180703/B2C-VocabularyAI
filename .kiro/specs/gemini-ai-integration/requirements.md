# Requirements Document: AI Integration with Google Gemini

## Introduction

This document specifies the requirements for integrating Google Gemini AI into VocabularyAI Backend. The integration provides four core AI features: conversational chat with context awareness, intelligent vocabulary explanations with examples and etymology, grammar checking with detailed feedback, and personalized learning hints based on user progress. All features are built on a robust service layer with caching, rate limiting, and comprehensive error handling to ensure reliable, secure, and performant AI-powered learning experiences.

---

## Glossary

### Core AI Terms

- **Conversation**: A persistent, organized discussion thread between a user and the AI system. Each conversation maintains historical context and message ordering.
- **ConversationId**: The unique identifier (Long) for a Conversation entity in the database.
- **ConversationType**: An enumeration defining the conversation purpose (GENERAL, VOCABULARY, GRAMMAR, LEARNING).
- **Message**: A single exchange unit within a Conversation, containing content, sender role (USER or ASSISTANT), and metadata.
- **MessageRole**: An enumeration indicating whether a message is from USER or ASSISTANT.
- **Prompt**: The formatted input text sent to the Gemini API, constructed from user input and contextual information.
- **Tokens**: Units of API usage tracked by Gemini (approximately 4 characters per token). Token counting is used for rate limiting and quota management.
- **TokenCount**: The number of tokens consumed by a single API call or within a rate limit period.

### Feature-Specific Terms

- **Explanation**: An AI-generated comprehensive description of a word including definition, usage examples, etymology, related words, and part of speech.
- **GrammarError**: A detected grammatical issue identified during grammar checking, including error type, position, and suggested correction.
- **GrammarSuggestion**: A recommendation to improve grammar, style, or clarity of provided text.
- **Hint**: A contextual learning aid provided to users during vocabulary learning, progressive from minimal to detailed based on attempt count.
- **HintLevel**: A numeric indicator (1=minimal, 2=moderate, 3=detailed) representing the verbosity and detail of a learning hint.

### Rate Limiting & Quota Terms

- **RateLimit**: A daily quota record tracking API request counts and token consumption per user.
- **RequestCount**: The number of API requests made within a rate limit period (24 hours).
- **TokensUsed**: The cumulative number of tokens consumed within a rate limit period.
- **DailyLimit**: The maximum token allocation per user per 24-hour period, tier-dependent.
- **RateLimitExceeded**: A condition where a user has consumed tokens or requests beyond configured limits.
- **PeriodStart/PeriodEnd**: DateTime boundaries (24-hour window) for a rate limit tracking period.

### System Integration Terms

- **CacheService**: A Redis-backed service providing distributed caching for AI responses, conversation context, and rate limit data.
- **GeminiAIClientService**: Core service managing direct communication with Google Gemini API, including prompt engineering and error handling.
- **PromptEngineeringService**: Service responsible for constructing optimized prompts for different AI features.
- **Conversation Isolation**: Security property ensuring a user can only access conversations they created.
- **Freshness**: The property that cached data is not stale (current time < cache_set_time + TTL).

### Data Persistence Terms

- **Cascade Delete**: When a parent entity (Conversation) is deleted, all child entities (Messages) are automatically deleted.
- **Transaction**: An atomic database operation ensuring consistency (e.g., message and rate limit both persist or both fail).

---

## Requirements

### Requirement 1: Create New Conversation

**User Story**: As a learner, I want to start a new conversation with the AI, so that I can receive personalized feedback and guidance on my language learning journey.

#### Acceptance Criteria

1. THE ChatService SHALL create a new Conversation entity with the provided ConversationType and title.
2. WHEN a user requests conversation creation, THE ChatService SHALL associate the conversation with the authenticated user's ID.
3. THE Conversation entity SHALL store a creation timestamp and be initialized with an empty message list.
4. IF the ConversationType is invalid, THEN THE ChatService SHALL reject the request with a 400 Bad Request response.
5. IF the conversation title exceeds 255 characters, THEN THE ChatService SHALL reject the request with a 400 Bad Request response.
6. WHEN a conversation is successfully created, THE ChatService SHALL return a ConversationResponse containing the conversation ID, title, type, and timestamps.

---

### Requirement 2: Send Message and Receive AI Response

**User Story**: As a learner, I want to send messages to the AI in a conversation, so that I can ask questions and receive intelligent responses in real-time.

#### Acceptance Criteria

1. WHEN a user sends a message to an existing conversation, THE ChatService SHALL validate that the conversation belongs to the authenticated user.
2. IF the message content is empty or exceeds 10,000 characters, THEN THE ChatService SHALL reject the request with a 400 Bad Request response.
3. WHEN a valid message is received, THE ChatService SHALL store the user message in the database with the USER role.
4. THE ChatService SHALL retrieve relevant conversation history from the Conversation entity.
5. THE ChatService SHALL invoke the GeminiAIClientService with the conversation history and new message.
6. WHEN the Gemini API returns a response, THE ChatService SHALL store the response in the database with the ASSISTANT role.
7. THE ChatService SHALL record the response time (milliseconds) and token count for the API call.
8. WHEN the operation completes successfully, THE ChatService SHALL return a MessageResponse containing the assistant's message and metadata.
9. WHEN a user lacks permission to access the conversation, THE ChatService SHALL return a 403 Forbidden response.

---

### Requirement 3: Retrieve Conversation with Message History

**User Story**: As a learner, I want to view my conversation history, so that I can review previous interactions and continue learning from our discussions.

#### Acceptance Criteria

1. WHEN a user requests to retrieve a conversation, THE ChatService SHALL verify the conversation belongs to the authenticated user.
2. THE ChatService SHALL return all messages in the conversation ordered by creation timestamp (ascending).
3. WHERE the request includes a limit parameter, THE ChatService SHALL return a maximum of {limit} messages (default: 50).
4. IF a conversation ID is invalid, THEN THE ChatService SHALL return a 404 Not Found response.
5. WHEN retrieving conversation details, THE ChatService SHALL include all MessageResponse objects with roles, content, timestamps, and metadata.

---

### Requirement 4: Retrieve User's Conversations with Pagination

**User Story**: As a learner, I want to see all my conversations organized and paginated, so that I can easily navigate and select which conversation to continue.

#### Acceptance Criteria

1. WHEN a user requests their conversation list, THE ChatService SHALL return all conversations created by that user.
2. THE ChatService SHALL support pagination with page and size parameters.
3. WHEN pagination is applied, THE ChatService SHALL return a Page object containing conversation summaries and total count.
4. WHEN retrieving paginated conversations, THE ChatService SHALL include the total number of messages in each conversation.
5. THE ChatService SHALL order conversations by most recent update timestamp (descending).

---

### Requirement 5: Delete Conversation

**User Story**: As a learner, I want to delete old or unwanted conversations, so that I can keep my learning space organized and clean.

#### Acceptance Criteria

1. WHEN a user requests to delete a conversation, THE ChatService SHALL verify the conversation belongs to the authenticated user.
2. THE ChatService SHALL perform a cascade delete of all messages associated with the conversation.
3. WHEN the conversation is deleted, THE CacheService SHALL invalidate all cached context for that conversation ID.
4. WHEN the delete operation completes successfully, THE ChatService SHALL return a 204 No Content response.
5. IF a user attempts to delete another user's conversation, THEN THE ChatService SHALL return a 403 Forbidden response.

---

### Requirement 6: Clear Conversation Message History

**User Story**: As a learner, I want to clear the message history in a conversation while keeping the conversation itself, so that I can start fresh while maintaining conversation metadata.

#### Acceptance Criteria

1. WHEN a user requests to clear conversation history, THE ChatService SHALL delete all Message entities associated with the conversation.
2. THE ChatService SHALL preserve the Conversation entity with its metadata (creation date, title, type).
3. WHEN history is cleared, THE CacheService SHALL invalidate cached context for the conversation.
4. WHEN the operation completes, THE ChatService SHALL return a 204 No Content response.

---

### Requirement 7: Explain Word with AI-Generated Details

**User Story**: As a learner, I want the AI to explain a word with definition, examples, etymology, and related words, so that I can build deeper vocabulary understanding.

#### Acceptance Criteria

1. WHEN a user requests an explanation for a word ID, THE VocabularyExplanationService SHALL first check the cache for an existing explanation.
2. WHERE an explanation exists in cache and is fresh (TTL not exceeded), THE VocabularyExplanationService SHALL return the cached explanation.
3. WHERE cache misses, THE VocabularyExplanationService SHALL fetch the word details from the database.
4. IF the word ID does not exist in the database, THEN THE VocabularyExplanationService SHALL return a 404 Not Found response.
5. WHEN fetching word details succeeds, THE VocabularyExplanationService SHALL invoke GeminiAIClientService with the PromptEngineeringService-constructed prompt.
6. THE VocabularyExplanationService SHALL structure the response including word, definition, examples, etymology, related words, and part of speech.
7. WHEN the AI response is received, THE VocabularyExplanationService SHALL cache the explanation with a 24-hour TTL.
8. WHEN the operation completes, THE VocabularyExplanationService SHALL return an ExplanationResponse with all fields populated.

---

### Requirement 8: Explain Multiple Words in Batch

**User Story**: As a learner, I want to get explanations for multiple words at once, so that I can efficiently learn groups of related vocabulary.

#### Acceptance Criteria

1. WHEN a user requests explanations for multiple word IDs, THE VocabularyExplanationService SHALL process each word ID independently.
2. THE VocabularyExplanationService SHALL check cache first for each word.
3. WHEN cache exists and is fresh, THE VocabularyExplanationService SHALL use the cached explanation.
4. WHEN cache misses, THE VocabularyExplanationService SHALL fetch and generate explanations as per Requirement 7.
5. WHEN some words are not found, THE VocabularyExplanationService SHALL return explanations for found words with a partial success indicator.
6. THE VocabularyExplanationService SHALL return a List<ExplanationResponse> with all available explanations.

---

### Requirement 9: Explain Word by Text

**User Story**: As a learner, I want to get an explanation for a word by typing its text, so that I can learn about words without needing to know their database ID.

#### Acceptance Criteria

1. WHEN a user provides word text, THE VocabularyExplanationService SHALL search for the word in the Word entity.
2. IF multiple words match exactly or partially, THE VocabularyExplanationService SHALL return the closest match.
3. IF no word matches, THEN THE VocabularyExplanationService SHALL return a 404 Not Found response.
4. WHEN a word is found, THE VocabularyExplanationService SHALL invoke the standard explanation flow (Requirement 7).

---

### Requirement 10: Generate Custom Explanation with User Context

**User Story**: As a learner, I want to get a vocabulary explanation tailored to my current learning context, so that the explanation is more relevant to what I'm studying.

#### Acceptance Criteria

1. WHEN a user provides a word ID and optional context string, THE VocabularyExplanationService SHALL invoke PromptEngineeringService with the context.
2. THE PromptEngineeringService SHALL incorporate the context into the vocabulary prompt for more targeted explanation.
3. WHEN the AI generates a contextualized explanation, THE VocabularyExplanationService SHALL cache it separately (context-aware cache key).
4. THE VocabularyExplanationService SHALL return an ExplanationResponse tailored to the provided context.

---

### Requirement 11: Check Grammar of Provided Text

**User Story**: As a learner, I want the AI to check my written text for grammar errors, so that I can improve my writing accuracy and learn from corrections.

#### Acceptance Criteria

1. WHEN a user submits text for grammar checking, THE GrammarCheckingService SHALL validate the text is non-empty and ≤10,000 characters.
2. IF the text is invalid, THEN THE GrammarCheckingService SHALL return a 400 Bad Request response with specific validation errors.
3. WHEN text is valid, THE GrammarCheckingService SHALL check cache for a matching text hash.
4. WHERE cache hit occurs, THE GrammarCheckingService SHALL return the cached GrammarCheckResponse.
5. WHERE cache misses, THE GrammarCheckingService SHALL invoke GeminiAIClientService with the grammar prompt.
6. THE GrammarCheckingService SHALL structure the response to include original text, corrected text, errors list with positions and suggestions.
7. WHEN the grammar check completes, THE GrammarCheckingService SHALL cache the response with a 1-hour TTL.
8. THE GrammarCheckingService SHALL return a GrammarCheckResponse containing all errors and suggestions.

---

### Requirement 12: Get Grammar Suggestions for Text

**User Story**: As a learner, I want to receive specific grammar improvement suggestions, so that I understand what to correct and why.

#### Acceptance Criteria

1. WHEN a user requests grammar suggestions, THE GrammarCheckingService SHALL invoke the grammar check flow (Requirement 11).
2. THE GrammarCheckingService SHALL extract and return only the GrammarSuggestion list from the full response.
3. WHEN returning suggestions, THE GrammarCheckingService SHALL include error type, suggested correction, and explanation for each error.

---

### Requirement 13: Auto-Correct Text

**User Story**: As a learner, I want the AI to automatically correct my text, so that I can quickly see the corrected version without manually applying individual suggestions.

#### Acceptance Criteria

1. WHEN a user requests automatic text correction, THE GrammarCheckingService SHALL perform grammar checking (Requirement 11).
2. THE GrammarCheckingService SHALL return the corrected text from the GrammarCheckResponse.
3. THE GrammarCheckingService SHALL return a simple response object containing only the corrected text.

---

### Requirement 14: Provide Personalized Learning Hint

**User Story**: As a learner, I want to receive a personalized hint during vocabulary learning, so that I get just-in-time support at the right difficulty level.

#### Acceptance Criteria

1. WHEN a user requests a hint for a learning progress item, THE LearningHintService SHALL fetch the LearningProgress entity and associated Word.
2. IF the learning progress ID is invalid, THEN THE LearningHintService SHALL return a 404 Not Found response.
3. WHEN learning progress is found, THE LearningHintService SHALL check cache for an existing hint.
4. WHERE cache exists and is fresh, THE LearningHintService SHALL return the cached HintResponse.
5. WHERE cache misses, THE LearningHintService SHALL invoke PromptEngineeringService to build a hint prompt.
6. THE PromptEngineeringService SHALL incorporate user's learning history and attempt count into the prompt.
7. WHEN the AI generates a hint, THE LearningHintService SHALL include the hint text, difficulty level, and learning strategies.
8. THE LearningHintService SHALL cache the hint with a 1-hour TTL.
9. WHEN the operation completes, THE LearningHintService SHALL return a HintResponse with all fields populated.

---

### Requirement 15: Provide Contextual Hints Based on Attempt Count

**User Story**: As a learner, I want hints that progressively reveal more information as I attempt the word more times, so that I learn at a sustainable pace.

#### Acceptance Criteria

1. WHEN a user requests a hint with an attempt count, THE LearningHintService SHALL use the attempt count to determine hint difficulty.
2. WHERE attemptCount ≤ 1, THE LearningHintService SHALL generate a minimal hint (level 1).
3. WHERE attemptCount == 2, THE LearningHintService SHALL generate a moderate hint (level 2).
4. WHERE attemptCount ≥ 3, THE LearningHintService SHALL generate a detailed hint (level 3).
5. THE LearningHintService SHALL include progressively more information (definition, examples, related words) as hint level increases.
6. WHEN the hint is generated, THE LearningHintService SHALL cache it with the attempt count as part of the cache key.

---

### Requirement 16: Get Hints for Words Needing Review

**User Story**: As a learner, I want to get hints for multiple words that need review, so that I can efficiently practice weak areas.

#### Acceptance Criteria

1. WHEN a user requests hints for review, THE LearningHintService SHALL query for LearningProgress items needing review.
2. THE LearningHintService SHALL generate hints for up to {limit} items (default: 10, max: 50).
3. WHEN generating batch hints, THE LearningHintService SHALL use attempt count and recency of last attempt to determine hint difficulty.
4. THE LearningHintService SHALL return a List<HintResponse> for all review items.

---

### Requirement 17: Generate Response with Gemini API

**User Story**: As a system component, I need to communicate with Gemini API to generate AI responses, so that the application can leverage advanced language models.

#### Acceptance Criteria

1. WHEN GeminiAIClientService receives a prompt, THE GeminiAIClientService SHALL initialize the Gemini SDK with the configured API key.
2. THE GeminiAIClientService SHALL construct a GenerateContentRequest with the prompt and configured parameters (temperature, top-p, max-tokens).
3. WHEN sending the request, THE GeminiAIClientService SHALL include the model name from configuration (default: gemini-1.5-pro-latest).
4. WHEN the API returns a response, THE GeminiAIClientService SHALL extract and aggregate response content.
5. IF the API response is successful, THE GeminiAIClientService SHALL return the aggregated text response.
6. WHEN the response includes token count information, THE GeminiAIClientService SHALL capture and return the TokenCount.

---

### Requirement 18: Support Streaming Responses from Gemini

**User Story**: As a user, I want to receive streaming responses from the AI to see responses in real-time, so that I experience responsive, interactive feedback.

#### Acceptance Criteria

1. WHEN a client requests streaming responses, THE GeminiAIClientService SHALL support streaming with callback mechanism.
2. THE GeminiAIClientService SHALL invoke the Gemini API in streaming mode.
3. AS response chunks arrive, THE GeminiAIClientService SHALL invoke the provided StreamCallback with each chunk.
4. WHEN the stream completes, THE GeminiAIClientService SHALL invoke a completion callback.
5. IF an error occurs during streaming, THE GeminiAIClientService SHALL invoke an error callback with the exception.

---

### Requirement 19: Support Multi-Turn Conversations with History

**User Story**: As a system component, I need to maintain and use conversation history, so that the AI can provide contextually aware responses.

#### Acceptance Criteria

1. WHEN generating a response in a conversation, THE GeminiAIClientService SHALL include previous messages in the conversation.
2. THE GeminiAIClientService SHALL format the conversation history into the prompt using PromptEngineeringService.
3. THE GeminiAIClientService SHALL maintain message ordering (chronological).
4. WHEN building the prompt, THE GeminiAIClientService SHALL include both user and assistant messages for full context.

---

### Requirement 20: Enforce User-Level Rate Limiting

**User Story**: As a system administrator, I want rate limiting to prevent API quota abuse, so that the system remains sustainable and fair for all users.

#### Acceptance Criteria

1. BEFORE executing any Gemini API call, THE GeminiAIClientService SHALL invoke rate limit checking.
2. WHEN checking rate limits, THE GeminiAIClientService SHALL retrieve the current RateLimit record for the user and current 24-hour period.
3. IF no RateLimit record exists, THE GeminiAIClientService SHALL create a new record for the user with the current period boundaries.
4. WHEN the RateLimit record is retrieved, THE GeminiAIClientService SHALL compare tokensUsed + estimatedTokens against the user's daily limit.
5. IF tokens_used + estimated_tokens exceeds the daily limit, THEN THE GeminiAIClientService SHALL throw a RateLimitExceededException with the reset time.
6. WHEN the API call completes successfully, THE GeminiAIClientService SHALL atomically increment the RateLimit tokensUsed by the actual token count.
7. WHEN the API call completes, THE GeminiAIClientService SHALL atomically increment requestCount by 1.
8. THE GeminiAIClientService SHALL return HTTP 429 Too Many Requests when rate limits are exceeded, with Retry-After header.

---

### Requirement 21: Count Tokens Before API Call

**User Story**: As a system component, I need to accurately count tokens before calling the API, so that rate limiting decisions are informed and prevent quota overages.

#### Acceptance Criteria

1. WHEN GeminiAIClientService receives a prompt, THE GeminiAIClientService SHALL invoke the Gemini tokenization endpoint to count tokens.
2. WHEN counting tokens, THE GeminiAIClientService SHALL return the token count for the exact prompt text.
3. IF token counting fails, THE GeminiAIClientService SHALL estimate tokens as text_length / 4 (approximate Gemini ratio).
4. THE GeminiAIClientService SHALL use token count for rate limit pre-checks and post-call accounting.

---

### Requirement 22: Build Optimized Chat Prompt with History

**User Story**: As a system component, I need to construct effective prompts for chat conversations, so that the AI provides accurate and contextually relevant responses.

#### Acceptance Criteria

1. WHEN PromptEngineeringService receives conversation history and new message, THE PromptEngineeringService SHALL format the history into a structured prompt.
2. THE PromptEngineeringService SHALL include conversation metadata (type: GENERAL, VOCABULARY, GRAMMAR, LEARNING) in the prompt.
3. WHEN the conversation type is VOCABULARY, THE PromptEngineeringService SHALL include vocabulary learning context.
4. WHEN the conversation type is GRAMMAR, THE PromptEngineeringService SHALL include grammar learning context.
5. WHEN the conversation type is LEARNING, THE PromptEngineeringService SHALL include user's learning progress if available.
6. THE PromptEngineeringService SHALL sanitize user messages (trim whitespace, remove control characters).
7. THE PromptEngineeringService SHALL order messages chronologically in the prompt.

---

### Requirement 23: Build Optimized Vocabulary Explanation Prompt

**User Story**: As a system component, I need to construct effective vocabulary prompts, so that explanations are comprehensive and learning-focused.

#### Acceptance Criteria

1. WHEN PromptEngineeringService receives a Word entity, THE PromptEngineeringService SHALL construct a prompt requesting definition, examples, etymology, and related words.
2. THE PromptEngineeringService SHALL include the word text, current part of speech if available, and any user context.
3. WHEN user context is provided, THE PromptEngineeringService SHALL emphasize how the word relates to the context.
4. THE PromptEngineeringService SHALL request structured output with clear sections for each explanation component.

---

### Requirement 24: Build Optimized Grammar Checking Prompt

**User Story**: As a system component, I need to construct effective grammar prompts, so that error detection is accurate and suggestions are actionable.

#### Acceptance Criteria

1. WHEN PromptEngineeringService receives text for grammar checking, THE PromptEngineeringService SHALL construct a prompt requesting error detection and correction.
2. THE PromptEngineeringService SHALL request error classification (grammar, spelling, style, punctuation).
3. THE PromptEngineeringService SHALL request explanation for each error and suggested correction.
4. THE PromptEngineeringService SHALL include character position information to enable error highlighting.

---

### Requirement 25: Build Optimized Learning Hint Prompt

**User Story**: As a system component, I need to construct effective hint prompts, so that hints are personalized, progressive, and learning-effective.

#### Acceptance Criteria

1. WHEN PromptEngineeringService receives LearningProgress and attempt count, THE PromptEngineeringService SHALL construct a progressive hint prompt.
2. THE PromptEngineeringService SHALL include the attempt count to indicate hint difficulty level.
3. WHEN attempt count indicates first attempt, THE PromptEngineeringService SHALL request minimal hint (definition only).
4. WHEN attempt count indicates second attempt, THE PromptEngineeringService SHALL request moderate hint (definition + example).
5. WHEN attempt count indicates third+ attempt, THE PromptEngineeringService SHALL request detailed hint (full explanation with strategies).
6. THE PromptEngineeringService SHALL include user's previous mistakes if available for targeted guidance.

---

### Requirement 26: Cache AI Responses with Expiration

**User Story**: As a system component, I need to cache AI responses, so that repeated requests return instantly and reduce API calls.

#### Acceptance Criteria

1. WHEN CacheService receives a cache set request, THE CacheService SHALL serialize the object to JSON.
2. THE CacheService SHALL store the JSON in Redis with the provided key.
3. WHEN a TTL is provided, THE CacheService SHALL set the Redis expiration time to TTL seconds.
4. WHERE no TTL is provided, THE CacheService SHALL use a default TTL from configuration.
5. WHEN the cache operation completes, THE CacheService SHALL confirm successful storage.

---

### Requirement 27: Retrieve Cached Values with Type Safety

**User Story**: As a system component, I need to safely retrieve and deserialize cached objects, so that type errors are prevented and data integrity is maintained.

#### Acceptance Criteria

1. WHEN CacheService retrieves a cached value, THE CacheService SHALL fetch from Redis using the provided key.
2. WHERE the key exists, THE CacheService SHALL deserialize the JSON to the requested class type.
3. WHERE deserialization succeeds, THE CacheService SHALL return an Optional<T> containing the deserialized object.
4. WHERE the key doesn't exist or is expired, THE CacheService SHALL return an empty Optional.
5. IF deserialization fails, THE CacheService SHALL log the error and return an empty Optional (fail-safe).

---

### Requirement 28: Manage Conversation Context Cache

**User Story**: As a system component, I need to cache conversation context to enable quick retrieval, so that conversation loading is fast.

#### Acceptance Criteria

1. WHEN ChatService saves conversation messages, THE CacheService SHALL cache the conversation context (message list).
2. THE CacheService SHALL use the conversationId as the cache key.
3. THE CacheService SHALL set the TTL to 30 minutes by default.
4. WHEN the conversation is accessed, THE CacheService SHALL refresh the TTL (extend on access).
5. WHEN the conversation is updated or deleted, THE CacheService SHALL invalidate the cache entry.

---

### Requirement 29: Manage Vocabulary Explanation Cache

**User Story**: As a system component, I need to cache vocabulary explanations, so that repeated explanations load instantly.

#### Acceptance Criteria

1. WHEN VocabularyExplanationService generates an explanation, THE CacheService SHALL cache it with the wordId as the key.
2. THE CacheService SHALL set the TTL to 24 hours.
3. WHEN a word is updated, THE CacheService SHALL invalidate the related explanation cache.
4. WHEN retrieving explanations, THE CacheService SHALL check cache before calling the API.

---

### Requirement 30: Track API Rate Limit Usage Atomically

**User Story**: As a system component, I need to track API usage accurately, so that rate limits are enforced fairly and quota is managed correctly.

#### Acceptance Criteria

1. WHEN an API call completes, THE CacheService SHALL atomically increment the rate limit counter.
2. WHEN incrementing rate limit, THE CacheService SHALL ensure the operation is atomic (no race conditions).
3. THE CacheService SHALL track both token count and request count.
4. WHEN retrieving rate limit data, THE CacheService SHALL return the current usage without modifying state.

---

### Requirement 31: Initialize Gemini SDK with API Key

**User Story**: As a system component, I need to securely initialize the Gemini SDK, so that the application can communicate with the AI service.

#### Acceptance Criteria

1. WHEN GeminiAIClientService starts, THE GeminiAIClientService SHALL read the GEMINI_API_KEY from environment or properties.
2. IF the API key is missing, THEN THE application startup SHALL fail with a descriptive error.
3. WHEN the API key is present, THE GeminiAIClientService SHALL initialize the Gemini SDK with the key.
4. THE GeminiAIClientService SHALL not log or expose the API key in any output.
5. THE GeminiAIClientService SHALL cache the initialized SDK instance for reuse.

---

### Requirement 32: Implement Exponential Backoff Retry Logic

**User Story**: As a system component, I need to implement retry logic with exponential backoff, so that transient API failures are handled gracefully.

#### Acceptance Criteria

1. WHEN a Gemini API call fails with a retryable error, THE GeminiAIClientService SHALL retry the request.
2. THE GeminiAIClientService SHALL implement exponential backoff with configured multiplier (default: 2.0).
3. WHEN retrying, THE GeminiAIClientService SHALL wait 1s, then 2s, then 4s, then 8s, then 16s before each retry.
4. THE GeminiAIClientService SHALL stop retrying after a configured maximum (default: 3 retries).
5. IF all retries are exhausted, THEN THE GeminiAIClientService SHALL throw an ExternalServiceException.

---

### Requirement 33: Handle Gemini API Errors with Appropriate HTTP Status

**User Story**: As an API consumer, I need appropriate HTTP status codes and error messages, so that I can handle errors correctly.

#### Acceptance Criteria

1. WHEN Gemini API returns a 400 error, THE GeminiAIClientService SHALL map it to HTTP 400 Bad Request.
2. WHEN Gemini API returns a 429 error, THE GeminiAIClientService SHALL map it to HTTP 429 Too Many Requests with Retry-After header.
3. WHEN Gemini API returns a 500 error, THE GeminiAIClientService SHALL map it to HTTP 503 Service Unavailable.
4. WHEN Gemini API is unreachable, THE GeminiAIClientService SHALL map it to HTTP 503 Service Unavailable.
5. WHEN an error occurs, THE error response SHALL include a user-friendly message and error code.

---

### Requirement 34: Validate User Authorization for Conversation Access

**User Story**: As a system administrator, I need to ensure users can only access their own conversations, so that user data is protected.

#### Acceptance Criteria

1. BEFORE returning conversation data, THE ChatService SHALL verify the requesting user's ID matches the conversation's userId.
2. IF the IDs don't match, THEN THE ChatService SHALL return a 403 Forbidden response.
3. IF the user is an ADMIN, THEN THE ChatService SHALL allow access to any conversation.
4. WHEN authorizing deletion, THE ChatService SHALL apply the same authorization rules.

---

### Requirement 35: Validate Input Text Length and Content

**User Story**: As a system administrator, I need to validate all user inputs, so that malicious or oversized inputs are rejected.

#### Acceptance Criteria

1. WHEN a user sends a message, THE ChatService SHALL validate the content is not empty (after trimming).
2. THE ChatService SHALL validate the content does not exceed 10,000 characters.
3. WHEN creating a conversation, THE ChatService SHALL validate the title is not empty and does not exceed 255 characters.
4. WHEN checking grammar, THE GrammarCheckingService SHALL validate the text is not empty and does not exceed 10,000 characters.
5. IF any validation fails, THE service SHALL return a 400 Bad Request response with specific field-level errors.

---

### Requirement 36: Sanitize User Input to Prevent Injection

**User Story**: As a system administrator, I need to sanitize inputs, so that injection attacks are prevented.

#### Acceptance Criteria

1. WHEN processing user input, THE InputSanitizer SHALL trim leading and trailing whitespace.
2. THE InputSanitizer SHALL replace multiple consecutive spaces with a single space.
3. THE InputSanitizer SHALL remove control characters (ASCII 0x00-0x08, 0x0B-0x0C, 0x0E-0x1F).
4. THE InputSanitizer SHALL NOT modify or filter user input beyond these standard sanitizations (preserve user intent).

---

### Requirement 37: Create Database Schema for Conversation Storage

**User Story**: As a system administrator, I need database tables for conversations, so that conversation data persists reliably.

#### Acceptance Criteria

1. THE database SHALL include a conversations table with columns: id, user_id, type, title, summary, created_at, updated_at, is_archived.
2. THE conversations table SHALL have a primary key on id (auto-increment Long).
3. THE conversations table SHALL have a foreign key on user_id referencing the users table.
4. THE conversations table SHALL have an index on user_id for efficient queries by user.
5. THE conversations table SHALL enforce NOT NULL on user_id and type columns.

---

### Requirement 38: Create Database Schema for Message Storage

**User Story**: As a system administrator, I need database tables for messages, so that message history persists reliably.

#### Acceptance Criteria

1. THE database SHALL include a messages table with columns: id, conversation_id, role, content, tokens_used, response_time_ms, created_at, metadata.
2. THE messages table SHALL have a primary key on id (auto-increment Long).
3. THE messages table SHALL have a foreign key on conversation_id referencing the conversations table with cascade delete.
4. THE messages table SHALL have an index on conversation_id for efficient retrieval of conversation history.
5. THE messages table SHALL enforce NOT NULL on conversation_id, role, and content columns.
6. THE content column SHALL support up to 10,000 characters (LONGTEXT).

---

### Requirement 39: Create Database Schema for Rate Limit Tracking

**User Story**: As a system administrator, I need database tables for rate limiting, so that API quota is tracked accurately.

#### Acceptance Criteria

1. THE database SHALL include a rate_limits table with columns: id, user_id, tokens_used, request_count, period_start, period_end, created_at, updated_at.
2. THE rate_limits table SHALL have a primary key on id (auto-increment Long).
3. THE rate_limits table SHALL have a foreign key on user_id referencing the users table.
4. THE rate_limits table SHALL have a composite index on (user_id, period_start) for efficient period lookups.
5. THE rate_limits table SHALL enforce NOT NULL on user_id, period_start, and period_end columns.

---

### Requirement 40: Configure Redis Cache Connection

**User Story**: As a system administrator, I need Redis configured for caching, so that cached data is available to all application instances.

#### Acceptance Criteria

1. THE application SHALL connect to a Redis server via the configured SPRING_REDIS_URL.
2. WHEN Redis is unavailable, THE application SHALL continue functioning with cache bypassed (fail-safe).
3. THE Redis connection SHALL use connection pooling for efficiency.
4. THE application SHALL handle Redis connection timeouts gracefully without affecting core functionality.

---

### Requirement 41: Configure Gemini API Parameters

**User Story**: As a system administrator, I need Gemini API parameters configured, so that AI responses are consistent and controlled.

#### Acceptance Criteria

1. THE application configuration SHALL include gemini.model (default: gemini-1.5-pro-latest).
2. THE application configuration SHALL include gemini.temperature (default: 0.7, range: 0.0-2.0).
3. THE application configuration SHALL include gemini.top-p (default: 0.95, range: 0.0-1.0).
4. THE application configuration SHALL include gemini.top-k (default: 40, min: 1).
5. THE application configuration SHALL include gemini.max-output-tokens (default: 2048).
6. WHEN these parameters are updated, THE application SHALL apply them to subsequent API calls without requiring restart.

---

### Requirement 42: Configure Rate Limit Thresholds

**User Story**: As a system administrator, I need rate limit thresholds configurable, so that quota management can be adjusted per deployment.

#### Acceptance Criteria

1. THE application configuration SHALL include daily-tokens limit (default: 1,000,000 per user per 24h).
2. THE application configuration SHALL include daily-requests limit (default: 1,000 per user per 24h).
3. THE application configuration SHALL include hourly-burst limit (default: 100 per user per hour for spike prevention).
4. WHEN a user exceeds limits, THE application SHALL enforce blocking until period resets.

---

### Requirement 43: Configure Cache TTL Values

**User Story**: As a system administrator, I need cache TTLs configurable, so that cache retention can be tuned per environment.

#### Acceptance Criteria

1. THE application configuration SHALL include default-ttl (default: 3600 seconds).
2. THE application configuration SHALL include conversation-ttl (default: 1800 seconds).
3. THE application configuration SHALL include explanation-ttl (default: 86400 seconds / 24 hours).
4. THE application configuration SHALL include hint-ttl (default: 3600 seconds / 1 hour).
5. WHEN these values are updated in configuration, THE application SHALL apply them to new cache entries.

---

### Requirement 44: Configure Gemini API Timeouts and Retries

**User Story**: As a system administrator, I need timeout and retry configuration, so that API calls are resilient to transient failures.

#### Acceptance Criteria

1. THE application configuration SHALL include request-timeout (default: 30 seconds).
2. THE application configuration SHALL include connect-timeout (default: 10 seconds).
3. THE application configuration SHALL include max-retries (default: 3).
4. THE application configuration SHALL include retry-backoff-multiplier (default: 2.0).
5. WHEN these values are updated, THE application SHALL apply them to subsequent API calls.

---

### Requirement 45: Enable/Disable AI Features via Configuration

**User Story**: As a system administrator, I need to enable/disable features, so that problematic features can be quickly disabled.

#### Acceptance Criteria

1. THE application configuration SHALL include chat-enabled flag (default: true).
2. THE application configuration SHALL include grammar-check-enabled flag (default: true).
3. THE application configuration SHALL include vocabulary-explain-enabled flag (default: true).
4. THE application configuration SHALL include learning-hints-enabled flag (default: true).
5. WHEN a feature is disabled, THE corresponding endpoint SHALL return a 503 Service Unavailable response.

---

### Requirement 46: Support Async Message Processing

**User Story**: As a system component, I need async support for long-running operations, so that API responses are not blocked.

#### Acceptance Criteria

1. THE application SHALL support @Async annotation on long-running methods.
2. WHEN an async operation is invoked, THE application SHALL execute it on a thread pool.
3. THE thread pool SHALL have core pool size of 10 and max pool size of 20.
4. THE application SHALL return CompletableFuture immediately without blocking the API response thread.

---

### Requirement 47: Implement Graceful Shutdown for Async Operations

**User Story**: As a system administrator, I need graceful shutdown, so that in-flight operations complete before shutdown.

#### Acceptance Criteria

1. WHEN the application is shutting down, THE thread pool executor SHALL wait for in-flight tasks to complete.
2. THE application SHALL allow a maximum of 60 seconds for tasks to complete before forcing shutdown.
3. IF tasks don't complete within the timeout, THE application SHALL log which tasks were interrupted.

---

### Requirement 48: Log API Calls to Gemini

**User Story**: As a system administrator, I need to log API calls, so that debugging and monitoring are possible.

#### Acceptance Criteria

1. WHEN an API call is made to Gemini, THE application SHALL log at INFO level the feature, request size, and timestamp.
2. WHEN an API response is received, THE application SHALL log at INFO level the response size, token count, and response time.
3. WHEN an API error occurs, THE application SHALL log at ERROR level the error code, message, and full stack trace.
4. THE logs SHALL NOT include the actual API key or sensitive user data.

---

### Requirement 49: Track Performance Metrics

**User Story**: As a system administrator, I need to track performance metrics, so that system health can be monitored.

#### Acceptance Criteria

1. THE application SHALL track counter metrics for gemini.api.calls (tagged by feature).
2. THE application SHALL track timer metrics for gemini.response.time (percentiles: p50, p95, p99).
3. THE application SHALL track gauge metrics for cache.hit.ratio and cache.miss.ratio.
4. THE application SHALL track counter metrics for rate.limit.exceeded events.
5. WHEN metrics are collected, THE application SHALL export them to Actuator endpoint for monitoring.

---

### Requirement 50: Alert on High Error Rates

**User Story**: As a system administrator, I need alerting for high error rates, so that issues are detected quickly.

#### Acceptance Criteria

1. WHEN the AI API error rate exceeds 5% in a 5-minute window, THE application SHALL log an WARN level alert.
2. WHEN the response time exceeds 10 seconds average, THE application SHALL log an WARN level alert.
3. WHEN cache hit rate falls below 50%, THE application SHALL log an WARN level alert.
4. WHEN rate limit exceeded count exceeds 100 in an hour, THE application SHALL log an WARN level alert.

---

### Requirement 51: Provide API Documentation

**User Story**: As an API consumer, I need comprehensive API documentation, so that I can understand how to use the endpoints.

#### Acceptance Criteria

1. THE application SHALL generate OpenAPI 3.0 specification via Springdoc OpenAPI.
2. THE specification SHALL include all AI endpoints (chat, vocabulary, grammar, hints).
3. EACH endpoint SHALL include description, request/response models, and example values.
4. EACH endpoint SHALL document possible HTTP status codes and error responses.
5. THE specification SHALL be accessible at /v3/api-docs and /swagger-ui.html.

---

### Requirement 52: Implement Circuit Breaker for Gemini API

**User Story**: As a system component, I need a circuit breaker, so that the system gracefully handles persistent API failures.

#### Acceptance Criteria

1. WHEN Gemini API fails 5 consecutive times, THE circuit breaker SHALL trip to OPEN state.
2. WHEN the circuit is OPEN, THE GeminiAIClientService SHALL immediately return cached responses if available.
3. WHEN the circuit is OPEN, THE GeminiAIClientService SHALL return a 503 Service Unavailable if no cache exists.
4. AFTER 30 seconds in OPEN state, THE circuit breaker SHALL transition to HALF_OPEN for recovery testing.
5. IF the next request in HALF_OPEN succeeds, THE circuit breaker SHALL close and resume normal operation.
6. IF the next request in HALF_OPEN fails, THE circuit breaker SHALL reopen and continue failing fast.

---

### Requirement 53: Implement Distributed Locking for Concurrent Updates

**User Story**: As a system component, I need distributed locking, so that concurrent requests don't cause race conditions.

#### Acceptance Criteria

1. WHEN multiple requests update the same rate limit record simultaneously, THE rate limit update SHALL use Redis distributed locking.
2. THE lock SHALL be acquired before reading and updating the rate limit.
3. WHEN the lock is acquired, THE rate limit update SHALL be atomic (read-modify-write is single operation).
4. THE lock SHALL expire after 5 seconds to prevent deadlocks.
5. IF the lock cannot be acquired within 2 seconds, THE operation SHALL fail with a 503 response.

---

### Requirement 54: Support Conversation Archiving

**User Story**: As a learner, I want to archive old conversations, so that I can keep my conversation list clean without permanently deleting history.

#### Acceptance Criteria

1. WHEN a user requests to archive a conversation, THE ChatService SHALL set the is_archived flag to true.
2. WHEN retrieving conversations, THE ChatService SHALL exclude archived conversations by default.
3. WHERE a user requests archived conversations, THE ChatService SHALL return only archived conversations.
4. WHEN a conversation is archived, THE ChatService SHALL preserve all message history.

---

### Requirement 55: Export Conversation as Text

**User Story**: As a learner, I want to export a conversation, so that I can save my learning interactions for offline review.

#### Acceptance Criteria

1. WHEN a user requests to export a conversation, THE ChatService SHALL format all messages chronologically.
2. THE export format SHALL include timestamp, role (user/assistant), and message content for each message.
3. WHEN the export completes, THE ChatService SHALL return the formatted text or trigger a download.

---

### Requirement 56: Support Conversation Search

**User Story**: As a learner, I want to search my conversations, so that I can find previous interactions about specific topics.

#### Acceptance Criteria

1. WHEN a user searches for text, THE ChatService SHALL search across all conversation titles and message content.
2. THE search SHALL be case-insensitive and support partial matching.
3. WHEN search completes, THE ChatService SHALL return matching conversations with matched message snippets.
4. THE search SHALL support pagination of results.

---

### Requirement 57: Validate Conversation Type is Valid Enum

**User Story**: As a system component, I need to validate conversation types, so that invalid types are rejected.

#### Acceptance Criteria

1. WHEN a user creates a conversation with a ConversationType, THE ChatService SHALL validate the type is one of: GENERAL, VOCABULARY, GRAMMAR, LEARNING.
2. IF the type is invalid, THEN THE ChatService SHALL return a 400 Bad Request response.
3. THE validation SHALL occur before database insertion.

---

### Requirement 58: Validate Message Role is Valid Enum

**User Story**: As a system component, I need to validate message roles, so that invalid roles are rejected.

#### Acceptance Criteria

1. WHEN a message is created, THE ChatService SHALL validate the role is either USER or ASSISTANT.
2. IF the role is invalid, THEN THE ChatService SHALL return a 400 Bad Request response.
3. THE role SHALL be set automatically based on the message source (user input = USER, API response = ASSISTANT).

---

### Requirement 59: Prevent Message Content Overflow

**User Story**: As a system component, I need to prevent oversized messages, so that database performance is not degraded.

#### Acceptance Criteria

1. WHEN a user sends a message, THE ChatService SHALL enforce maximum 10,000 characters for user messages.
2. WHEN storing AI response, THE ChatService SHALL enforce maximum 5,000 characters for assistant messages (truncate if needed).
3. IF a message exceeds the limit, THE truncated version SHALL be stored with a truncation indicator.

---

### Requirement 60: Handle Empty Conversation Response

**User Story**: As a system component, I need to handle empty responses, so that errors don't cascade.

#### Acceptance Criteria

1. WHEN Gemini API returns an empty response body, THE GeminiAIClientService SHALL return a user-friendly error message.
2. THE error message SHALL suggest retrying or checking input.
3. THE application SHALL not crash or throw unexpected exceptions.

---

## Non-Functional Requirements

### Requirement NFR-1: Response Time for Chat Messages

**Requirement**: WHEN a user sends a message, THE system SHALL return an AI response within 5 seconds for 95% of requests.

**Rationale**: Users expect near-real-time feedback to maintain engagement and learning effectiveness.

**Measurement**: Average response time ≤ 3 seconds, P95 ≤ 5 seconds, P99 ≤ 10 seconds (including Gemini API latency).

---

### Requirement NFR-2: Response Time for Cached Responses

**Requirement**: WHEN a cached response is requested, THE system SHALL return the response within 100 milliseconds for 99% of requests.

**Rationale**: Cache hit performance should be near-instant to provide optimal user experience.

**Measurement**: P99 ≤ 100ms for cache hits.

---

### Requirement NFR-3: Cache Hit Rate

**Requirement**: THE system SHALL achieve a cache hit rate of at least 70% for vocabulary explanations and grammar checks.

**Rationale**: High cache hit rates reduce API costs and improve response times.

**Measurement**: Monitor cache hit rate via metrics; alert if <50%.

---

### Requirement NFR-4: API Availability

**Requirement**: THE system SHALL maintain 99.5% availability for AI endpoints during normal operating hours.

**Rationale**: Users depend on consistent access to learning features.

**Measurement**: Monitor uptime; alert on outages >5 minutes.

---

### Requirement NFR-5: Concurrent Request Handling

**Requirement**: THE system SHALL handle at least 100 concurrent requests from different users without degradation.

**Rationale**: Multiple users should be able to use the system simultaneously without interference.

**Measurement**: Load test with 100 concurrent users; verify response times remain <5 seconds.

---

### Requirement NFR-6: Database Query Performance

**Requirement**: WHEN retrieving conversation history, THE system SHALL complete within 50 milliseconds for conversations with ≤1,000 messages.

**Rationale**: Quick retrieval is necessary for good UX.

**Measurement**: Query execution time with pagination limits.

---

### Requirement NFR-7: Gemini API Rate Limit Compliance

**Requirement**: THE system SHALL track and enforce per-user rate limits to comply with configured tier limits (100-1000 requests/day, 50K-500K tokens/day).

**Rationale**: Prevents quota overages and ensures fair resource sharing.

**Measurement**: Verify rate limit tracking in database; test enforcement.

---

### Requirement NFR-8: Cache Consistency

**Requirement**: WHEN cached data is invalidated, THE system SHALL clear all related cache entries within 100 milliseconds.

**Rationale**: Prevents stale data from being served.

**Measurement**: Verify invalidation timestamps; test with manual cache operations.

---

### Requirement NFR-9: Security - JWT Token Validation

**Requirement**: ALL API requests SHALL be authenticated via valid JWT tokens before processing.

**Rationale**: Prevents unauthorized access to user data and features.

**Measurement**: Test unauthenticated requests; verify 401 Unauthorized response.

---

### Requirement NFR-10: Security - Data Encryption

**Requirement**: THE system SHALL encrypt sensitive data (API keys, tokens) at rest and in transit.

**Rationale**: Protects against data breaches and eavesdropping.

**Measurement**: Verify HTTPS for all communication; verify Redis encryption if enabled.

---

### Requirement NFR-11: Security - Input Validation

**Requirement**: ALL user inputs SHALL be validated and sanitized before processing or storage.

**Rationale**: Prevents injection attacks and malformed data.

**Measurement**: Test with malicious inputs; verify rejection/sanitization.

---

### Requirement NFR-12: Error Handling Completeness

**Requirement**: ALL error scenarios SHALL return appropriate HTTP status codes and descriptive error messages.

**Rationale**: Enables proper error handling and debugging.

**Measurement**: Verify error responses for all failure scenarios.

---

### Requirement NFR-13: Logging Coverage

**Requirement**: ALL API calls, errors, and significant business logic SHALL be logged with appropriate levels (INFO, WARN, ERROR).

**Rationale**: Enables debugging and monitoring.

**Measurement**: Verify logs contain relevant information; check no sensitive data is logged.

---

### Requirement NFR-14: Monitoring and Metrics

**Requirement**: THE system SHALL expose metrics for API calls, response times, error rates, cache performance, and rate limit usage.

**Rationale**: Enables proactive monitoring and alerting.

**Measurement**: Verify metrics available via Actuator endpoint.

---

### Requirement NFR-15: Graceful Degradation

**Requirement**: WHEN the Gemini API is unavailable, THE system SHALL gracefully degrade functionality (serve cached responses when available) rather than failing completely.

**Rationale**: Maintains partial functionality during outages.

**Measurement**: Test with Gemini API mock failures; verify cached response behavior.

---

### Requirement NFR-16: Code Quality

**Requirement**: THE codebase SHALL maintain ≥80% unit test coverage for service layer, follow existing code style, and pass static analysis checks.

**Rationale**: Ensures maintainability and reliability.

**Measurement**: SonarQube analysis; code review verification.

---

### Requirement NFR-17: Documentation

**Requirement**: ALL public APIs, services, and configuration options SHALL be documented with purpose, parameters, return values, and examples.

**Rationale**: Enables maintenance and future development.

**Measurement**: JavaDoc coverage; README updates; API documentation completeness.

---

### Requirement NFR-18: Scalability - Horizontal

**Requirement**: THE system SHALL support horizontal scaling by running multiple application instances behind a load balancer without data inconsistency.

**Rationale**: Enables growth as user base increases.

**Measurement**: Deploy multiple instances; verify consistent behavior.

---

### Requirement NFR-19: Scalability - Data Volume

**Requirement**: THE system SHALL efficiently handle up to 1 million conversations and 100 million messages.

**Rationale**: Supports long-term growth.

**Measurement**: Load test with large data volumes; verify query performance.

---

### Requirement NFR-20: Dependency Management

**Requirement**: ALL external dependencies SHALL be explicitly pinned to specific versions in pom.xml to ensure reproducible builds.

**Rationale**: Prevents unexpected breaking changes.

**Measurement**: Verify all dependencies have explicit version tags; no version ranges.

---

## Test Requirements

### Requirement TEST-1: Property-Based Testing for Message Validation

**Description**: WHEN message content is generated with varying lengths and characters, THE system SHALL always accept valid content and reject invalid content consistently.

**Test Framework**: fast-check (Java)

**Generators**:
- Valid content: string(1, 10000)
- Invalid content: empty string, strings >10000 chars

**Properties to Verify**:
1. For any string of length 1-10000, it SHALL be accepted as valid message content
2. For any string of length 0 or >10000, it SHALL be rejected
3. Message content after sanitization SHALL never contain control characters

---

### Requirement TEST-2: Property-Based Testing for Rate Limit Accuracy

**Description**: WHEN API calls are made with varying token consumption, THE system SHALL enforce rate limits accurately.

**Generators**:
- Token counts: integer(1, 100000)
- User requests: 1-1000 per period

**Properties to Verify**:
1. For any token count below the daily limit, the request SHALL succeed
2. For any token count that would exceed the limit, the request SHALL be rejected with 429
3. Token counters after failed requests SHALL not increment

---

### Requirement TEST-3: Property-Based Testing for Cache Consistency

**Description**: WHEN cache operations are performed with varying keys and TTLs, THE system SHALL maintain consistency.

**Generators**:
- Keys: string(1, 100)
- Values: JSON objects
- TTLs: integer(1, 86400)

**Properties to Verify**:
1. For any cached value before TTL expiration, get SHALL return the value
2. For any cached value after TTL expiration, get SHALL return empty
3. For any deleted key, get SHALL return empty

---

### Requirement TEST-4: Integration Test - End-to-End Chat Flow

**Description**: WHEN a user sends a message in a conversation, THE system SHALL store both user and AI messages and return the AI response.

**Test Steps**:
1. Create a conversation
2. Send a message (with mock Gemini API)
3. Verify user message is stored
4. Verify AI response is stored
5. Retrieve conversation and verify message history
6. Delete conversation and verify cleanup

---

### Requirement TEST-5: Integration Test - Vocabulary Explanation with Cache

**Description**: WHEN requesting vocabulary explanations, THE system SHALL use cache on repeated requests and handle cache misses.

**Test Steps**:
1. Request explanation for word (cache miss)
2. Verify API call to Gemini
3. Request same explanation (cache hit)
4. Verify no new API call
5. Update word and verify cache invalidation
6. Request again and verify new API call

---

### Requirement TEST-6: Integration Test - Rate Limiting Enforcement

**Description**: WHEN a user exceeds rate limits, THE system SHALL prevent further requests.

**Test Steps**:
1. Set low rate limit for test user
2. Make requests up to limit
3. Verify next request returns 429 Too Many Requests
4. Verify Retry-After header includes reset time
5. Wait for reset time and verify requests resume

---

### Requirement TEST-7: Integration Test - Authorization Enforcement

**Description**: WHEN a user attempts to access another user's conversation, THE system SHALL deny access.

**Test Steps**:
1. User A creates a conversation
2. User B attempts to retrieve User A's conversation
3. Verify 403 Forbidden response
4. Verify User A can still access their conversation

---

### Requirement TEST-8: Error Handling - Gemini API Unavailable

**Description**: WHEN Gemini API is unavailable, THE system SHALL gracefully handle the failure.

**Test Steps**:
1. Mock Gemini API to return 503
2. Send a message request
3. Verify circuit breaker trips after threshold
4. Verify cached response is returned if available
5. Verify descriptive error message if no cache

---

### Requirement TEST-9: Performance Test - Response Time under Load

**Description**: WHEN the system receives concurrent requests, THE system SHALL maintain response time SLA.

**Test Steps**:
1. Simulate 100 concurrent users
2. Each user sends 10 messages
3. Measure response times
4. Verify P95 < 5 seconds, P99 < 10 seconds

---

### Requirement TEST-10: Performance Test - Cache Hit Rate

**Description**: WHEN repeated requests are made for vocabulary explanations, THE system SHALL achieve >70% cache hit rate.

**Test Steps**:
1. Request same word 100 times
2. Measure cache hits and misses
3. Verify hit rate > 70%

---

