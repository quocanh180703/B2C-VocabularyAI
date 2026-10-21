# CHƯƠNG 3: TRIỂN KHAI VÀ ĐÁNH GIÁ (Phần 1)

## 3.1. Môi Trường Triển Khai

### 3.1.1. Môi Trường Phát Triển

**Máy Lập Trình (Development Machine):**
```
OS: Windows 11 / macOS / Ubuntu Linux
RAM: >= 8GB
Storage: >= 50GB SSD
```

**Software Stack:**
```
JDK 21: https://www.oracle.com/java/technologies/downloads/#java21
Maven 3.8+: https://maven.apache.org/
Node.js 18+: https://nodejs.org/
Git 2.40+: https://git-scm.com/
Docker Desktop: https://www.docker.com/products/docker-desktop/
IDE: IntelliJ IDEA / VS Code
```

### 3.1.2. Môi Trường Staging

**Servers:**
```
Frontend Server: Nginx (port 80, 443)
Backend Server: Java/Spring Boot (port 8080, tạm internal)
Database Server: MySQL 8.x (port 3306, nội bộ)
Cache Server: Redis 6+ (port 6379, nội bộ)
```

**Infrastructure:**
- Cloud provider: AWS / Google Cloud / VPS tự host
- Load Balancer: Nginx / AWS ELB
- DNS: Route53 / Cloudflare
- Monitoring: Prometheus + Grafana

### 3.1.3. Môi Trường Production

**Phân Tán:**
- Frontend CDN: Cloudflare / AWS CloudFront
- Backend: 2-3 instances behind load balancer
- Database: Primary + Read Replica
- Cache: Redis Cluster

**Backup & Recovery:**
- Database backup: Hàng ngày, giữ lại 30 ngày
- Infrastructure as Code: Terraform / CloudFormation
- Disaster recovery plan: RTO 4h, RPO 1h

---

## 3.2. Chi Tiết Triển Khai Backend Modules

### 3.2.1. Module Authentication

#### Cấu Trúc Thư Mục

```
src/main/java/com/vocabularyai/auth/
├── config/
│   └── SecurityConfig.java
├── controller/
│   └── AuthController.java
├── service/
│   ├── AuthService.java
│   ├── JwtProvider.java
│   ├── OtpService.java
│   └── EmailService.java
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   └── VerifyOtpRequest.java
│   └── response/
│       ├── AuthResponse.java
│       └── TokenResponse.java
├── entity/
│   ├── User.java
│   └── OtpLog.java
└── repository/
    ├── UserRepository.java
    └── OtpLogRepository.java
```

#### Triển Khai Chi Tiết

**1. User Entity**
```java
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String fullName;
    
    @Enumerated(EnumType.STRING)
    private Role role; // USER, ADMIN
    
    @Column(nullable = false)
    private Boolean emailVerified = false;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

**2. JWT Provider**
```java
@Component
public class JwtProvider {
    private static final long ACCESS_TOKEN_EXPIRY = 24 * 60 * 60 * 1000; // 24h
    private static final long REFRESH_TOKEN_EXPIRY = 7 * 24 * 60 * 60 * 1000; // 7d
    
    @Value("${app.jwt.secret-key}")
    private String secretKey;
    
    public String generateAccessToken(String email) {
        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY))
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact();
    }
    
    public String generateRefreshToken(String email) {
        return Jwts.builder()
            .setSubject(email)
            .claim("type", "refresh")
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY))
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact();
    }
    
    public String getEmailFromToken(String token) {
        return Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }
}
```

**3. Auth Service**
```java
@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtProvider jwtProvider;
    
    @Autowired
    private OtpService otpService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public void register(RegisterRequest request) {
        // Validate email không trùng
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }
        
        // Tạo user mới
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(Role.USER);
        user.setEmailVerified(false);
        
        userRepository.save(user);
        
        // Gửi OTP
        otpService.sendOtp(request.getEmail());
    }
    
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid email/password"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email/password");
        }
        
        if (!user.getEmailVerified()) {
            throw new EmailNotVerifiedException("Email not verified");
        }
        
        String accessToken = jwtProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());
        
        return new TokenResponse(accessToken, refreshToken);
    }
}
```

**4. Auth Controller**
```java
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered. OTP sent to email.");
    }
    
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        TokenResponse tokens = authService.login(request);
        return ResponseEntity.ok(tokens);
    }
    
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok("Email verified successfully");
    }
}
```

### 3.2.2. Module Vocabulary

#### Cấu Trúc & Triển Khai

**1. Word Entity**
```java
@Entity
@Table(name = "words")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Word {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String wordText;
    
    @Column(columnDefinition = "TEXT")
    private String definition;
    
    @Enumerated(EnumType.STRING)
    private PartOfSpeech partOfSpeech; // noun, verb, adj, adv...
    
    @Enumerated(EnumType.STRING)
    private Level level; // A1, A2, B1, B2, C1, C2
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    
    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL)
    private List<Collocation> collocations;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

**2. Word Repository**
```java
@Repository
public interface WordRepository extends JpaRepository<Word, Long> {
    
    @Query("SELECT w FROM Word w WHERE " +
           "w.wordText LIKE %:search% OR w.definition LIKE %:search%")
    Page<Word> search(@Param("search") String search, Pageable pageable);
    
    List<Word> findByLevel(Level level);
    
    List<Word> findByCategory(Category category);
    
    @Query("SELECT w FROM Word w ORDER BY RAND() LIMIT :count")
    List<Word> getRandomWords(@Param("count") int count);
}
```

**3. Word Service & Controller**
```java
@Service
public class WordService {
    
    @Autowired
    private WordRepository wordRepository;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public Page<Word> searchWords(String search, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return wordRepository.search(search, pageable);
    }
    
    public Word getWordDetail(Long wordId) {
        // Try cache first
        String cacheKey = "word:" + wordId;
        Word cached = (Word) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) return cached;
        
        Word word = wordRepository.findById(wordId)
            .orElseThrow(() -> new ResourceNotFoundException("Word not found"));
        
        // Cache for 24 hours
        redisTemplate.opsForValue().set(cacheKey, word, Duration.ofHours(24));
        
        return word;
    }
}

@RestController
@RequestMapping("/api/words")
@CrossOrigin("http://localhost:5173")
public class WordController {
    
    @Autowired
    private WordService wordService;
    
    @GetMapping
    public ResponseEntity<Page<Word>> getWords(
        @RequestParam(defaultValue = "") String search,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int pageSize) {
        
        Page<Word> words = wordService.searchWords(search, page, pageSize);
        return ResponseEntity.ok(words);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Word> getWordDetail(@PathVariable Long id) {
        Word word = wordService.getWordDetail(id);
        return ResponseEntity.ok(word);
    }
}
```

### 3.2.3. Module Learning (Spaced Repetition)

#### Triển Khai

**1. Learning Progress Entity**
```java
@Entity
@Table(name = "learning_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "word_id")
    private Word word;
    
    @Column(name = "quality") // 0-5
    private Integer quality;
    
    @Column(name = "next_review")
    private LocalDate nextReviewDate;
    
    @Column(name = "repetition")
    private Integer repetition = 0;
    
    @Column(name = "easiness_factor")
    private Double easinessFactor = 2.5;
    
    @Column(name = "interval")
    private Integer interval = 0; // days
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

**2. Spaced Repetition Service**
```java
@Service
public class SpacedRepetitionService {
    
    public LearningProgress calculateNextReview(
        LearningProgress progress, int quality) {
        
        // Calculate new EF
        double newEF = progress.getEasinessFactorValue() + 
                      (0.1 - (5 - quality) * 0.08);
        
        if (newEF < 1.3) newEF = 1.3;
        
        // Calculate new interval
        int newInterval;
        if (quality < 3) {
            newInterval = 1; // Lặp lại nếu sai
        } else if (progress.getInterval() == 0) {
            newInterval = 1;
        } else if (progress.getInterval() == 1) {
            newInterval = 3;
        } else {
            newInterval = Math.round(progress.getInterval() * (float) newEF);
        }
        
        progress.setEasinessFactorValue(newEF);
        progress.setInterval(newInterval);
        progress.setNextReviewDate(LocalDate.now().plusDays(newInterval));
        progress.setRepetition(progress.getRepetition() + 1);
        progress.setQuality(quality);
        
        return progress;
    }
}
```

**3. Learning Controller**
```java
@RestController
@RequestMapping("/api/learning")
@CrossOrigin("http://localhost:5173")
public class LearningController {
    
    @Autowired
    private LearningService learningService;
    
    @GetMapping("/review-queue")
    public ResponseEntity<List<Word>> getReviewQueue() {
        User user = getCurrentUser(); // from SecurityContext
        List<Word> queue = learningService.getReviewQueueForToday(user);
        return ResponseEntity.ok(queue);
    }
    
    @PostMapping("/progress/{wordId}")
    public ResponseEntity<?> submitProgress(
        @PathVariable Long wordId,
        @RequestBody ProgressRequest request) {
        
        User user = getCurrentUser();
        learningService.recordProgress(user, wordId, request.getQuality());
        return ResponseEntity.ok("Progress recorded");
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<LearningStatsResponse> getStatistics() {
        User user = getCurrentUser();
        LearningStatsResponse stats = learningService.getStats(user);
        return ResponseEntity.ok(stats);
    }
}
```

### 3.2.4. Module Quiz

#### Triển Khai

**1. Quiz Entities**
```java
@Entity
@Table(name = "quizzes")
@Data
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    private List<Question> questions;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}

@Entity
@Table(name = "questions")
@Data
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;
    
    @Column(columnDefinition = "TEXT")
    private String questionText;
    
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<Answer> answers;
    
    @Column(name = "correct_answer_id")
    private Long correctAnswerId;
}

@Entity
@Table(name = "quiz_submissions")
@Data
public class QuizSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;
    
    @Column(name = "score") // 0-100
    private Double score;
    
    @CreationTimestamp
    private LocalDateTime submittedAt;
}
```

**2. Quiz Service**
```java
@Service
public class QuizService {
    
    @Autowired
    private QuizRepository quizRepository;
    
    @Autowired
    private QuizSubmissionRepository submissionRepository;
    
    public QuizResponseDto getQuizQuestions(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
        
        List<QuestionResponseDto> questionDtos = quiz.getQuestions()
            .stream()
            .map(q -> new QuestionResponseDto(
                q.getId(),
                q.getQuestionText(),
                q.getAnswers().stream()
                    .map(a -> new AnswerDto(a.getId(), a.getAnswerText()))
                    .collect(Collectors.toList())
            ))
            .collect(Collectors.toList());
        
        return new QuizResponseDto(quiz.getId(), quiz.getTitle(), questionDtos);
    }
    
    public QuizResultDto submitQuiz(User user, Long quizId, 
                                    Map<Long, Long> answers) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow();
        
        int correctCount = 0;
        for (Question question : quiz.getQuestions()) {
            Long userAnswer = answers.get(question.getId());
            if (userAnswer != null && userAnswer.equals(question.getCorrectAnswerId())) {
                correctCount++;
            }
        }
        
        double score = (double) correctCount / quiz.getQuestions().size() * 100;
        
        QuizSubmission submission = new QuizSubmission();
        submission.setUser(user);
        submission.setQuiz(quiz);
        submission.setScore(score);
        
        submissionRepository.save(submission);
        
        return new QuizResultDto(score, correctCount, quiz.getQuestions().size());
    }
}
```

---

## 3.3. Chi Tiết Triển Khai Frontend Pages

### 3.3.1. Page Structure

```
src/
├── pages/
│   ├── Login.tsx           // Auth pages
│   ├── Register.tsx
│   ├── Otp.tsx
│   ├── Dashboard.tsx       // Main pages
│   ├── Words.tsx
│   ├── WordDetail.tsx
│   ├── Chat.tsx            // Learning pages
│   ├── QuizList.tsx
│   ├── QuizDo.tsx
│   ├── QuizResult.tsx
│   ├── Speaking.tsx        // Speaking pages
│   ├── SpeakingResult.tsx
│   ├── Favorites.tsx       // Utility pages
│   ├── Profile.tsx
│   ├── Review.tsx
│   └── Roadmap.tsx
├── components/
│   ├── Layout.tsx          // Layout
│   ├── Guards.tsx          // Route protection
│   ├── AiExplanationBox.tsx
│   └── ...other components
├── api/
│   ├── client.ts           // Axios instance
│   └── index.ts            // API calls
└── auth/
    └── AuthContext.tsx      // Auth state management
```

### 3.3.2. Authentication Pages

**Login.tsx**
```typescript
import React, { useState } from 'react';
import axios from 'axios';

interface LoginFormData {
  email: string;
  password: string;
}

export const Login: React.FC = () => {
  const [formData, setFormData] = useState<LoginFormData>({
    email: '',
    password: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      const response = await axios.post('http://localhost:8080/api/auth/login', formData);
      
      // Save tokens
      localStorage.setItem('accessToken', response.data.accessToken);
      localStorage.setItem('refreshToken', response.data.refreshToken);
      
      // Redirect to dashboard
      window.location.href = '/dashboard';
    } catch (err) {
      setError('Invalid email or password');
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div className="login-container">
      <form onSubmit={handleSubmit}>
        <input
          type="email"
          placeholder="Email"
          value={formData.email}
          onChange={(e) => setFormData({...formData, email: e.target.value})}
        />
        <input
          type="password"
          placeholder="Password"
          value={formData.password}
          onChange={(e) => setFormData({...formData, password: e.target.value})}
        />
        <button type="submit" disabled={loading}>
          {loading ? 'Logging in...' : 'Login'}
        </button>
        {error && <p className="error">{error}</p>}
      </form>
    </div>
  );
};
```

### 3.3.3. Learning Pages

**Words.tsx** (Danh sách từ vựng)
```typescript
export const Words: React.FC = () => {
  const [words, setWords] = useState<Word[]>([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  
  const handleSearch = async (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearch(e.target.value);
    setLoading(true);
    
    try {
      const response = await axios.get('/api/words', {
        params: { search: e.target.value, page: 0, pageSize: 20 }
      });
      setWords(response.data.content);
      setPage(0);
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div>
      <h1>Vocabulary</h1>
      <input
        type="text"
        placeholder="Search words..."
        value={search}
        onChange={handleSearch}
      />
      {loading && <p>Loading...</p>}
      <ul>
        {words.map(word => (
          <li key={word.id}>
            <a href={`/word/${word.id}`}>{word.wordText}</a>
            <span>{word.partOfSpeech}</span>
          </li>
        ))}
      </ul>
    </div>
  );
};
```

**Review.tsx** (Ôn tập theo Spaced Repetition)
```typescript
export const Review: React.FC = () => {
  const [reviewQueue, setReviewQueue] = useState<Word[]>([]);
  const [currentWordIndex, setCurrentWordIndex] = useState(0);
  const [showAnswer, setShowAnswer] = useState(false);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    const fetchReviewQueue = async () => {
      try {
        const response = await axios.get('/api/learning/review-queue');
        setReviewQueue(response.data);
      } finally {
        setLoading(false);
      }
    };
    
    fetchReviewQueue();
  }, []);
  
  const handleQuality = async (quality: 0 | 1 | 2 | 3 | 4 | 5) => {
    const currentWord = reviewQueue[currentWordIndex];
    
    try {
      await axios.post(`/api/learning/progress/${currentWord.id}`, {
        quality
      });
      
      // Move to next word
      if (currentWordIndex + 1 < reviewQueue.length) {
        setCurrentWordIndex(currentWordIndex + 1);
        setShowAnswer(false);
      } else {
        alert('Review completed!');
        window.location.href = '/dashboard';
      }
    } catch (err) {
      alert('Error recording progress');
    }
  };
  
  if (loading) return <p>Loading review queue...</p>;
  if (reviewQueue.length === 0) return <p>No words to review today!</p>;
  
  const currentWord = reviewQueue[currentWordIndex];
  
  return (
    <div className="review-container">
      <h1>{currentWordIndex + 1} / {reviewQueue.length}</h1>
      <div className="card">
        <h2>{currentWord.wordText}</h2>
        <button onClick={() => setShowAnswer(!showAnswer)}>
          {showAnswer ? 'Hide' : 'Show'} Definition
        </button>
        {showAnswer && (
          <div>
            <p>{currentWord.definition}</p>
            <p className="part-of-speech">{currentWord.partOfSpeech}</p>
          </div>
        )}
      </div>
      
      <div className="quality-buttons">
        <button onClick={() => handleQuality(0)}>Again (0)</button>
        <button onClick={() => handleQuality(1)}>Hard (1)</button>
        <button onClick={() => handleQuality(2)}>Good (2)</button>
        <button onClick={() => handleQuality(3)}>Easy (3)</button>
        <button onClick={() => handleQuality(4)}>Very Easy (4)</button>
        <button onClick={() => handleQuality(5)}>Perfect (5)</button>
      </div>
    </div>
  );
};
```

---

## 3.4. Tích Hợp APIs AI

### 3.4.1. Google Gemini API Integration

**GeminiService.java**
```java
@Service
public class GeminiService {
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    @Value("${gemini.api.url}")
    private String baseUrl;
    
    @Autowired
    private RestTemplate restTemplate;
    
    public String generateExplanation(String word) {
        String prompt = String.format(
            "Explain the English word '%s' in simple terms. " +
            "Include: definition, part of speech, 2-3 example sentences, " +
            "and 2-3 common collocations. Format as plain text.",
            word
        );
        
        GeminiRequest request = new GeminiRequest();
        request.setContents(List.of(
            new Content(prompt)
        ));
        
        try {
            GeminiResponse response = restTemplate.postForObject(
                baseUrl + "?key=" + apiKey,
                request,
                GeminiResponse.class
            );
            
            return response.getCandidates()
                .get(0)
                .getContent()
                .getParts()
                .get(0)
                .getText();
        } catch (Exception e) {
            return "Could not generate explanation at this moment";
        }
    }
    
    public String generateQuizQuestion(String topic, String difficulty) {
        String prompt = String.format(
            "Generate a multiple choice English question about '%s' at %s level. " +
            "Format: Question: [question]\nA) [answer1]\nB) [answer2]\nC) [answer3]\nD) [answer4]\nCorrect: [letter]",
            topic, difficulty
        );
        
        // Similar to above...
        return generateFromPrompt(prompt);
    }
}
```

**AiController.java**
```java
@RestController
@RequestMapping("/api/ai")
public class AiController {
    
    @Autowired
    private GeminiService geminiService;
    
    @GetMapping("/explain/{wordId}")
    public ResponseEntity<Map<String, String>> explainWord(@PathVariable Long wordId) {
        Word word = wordService.getWord(wordId);
        String explanation = geminiService.generateExplanation(word.getWordText());
        
        Map<String, String> result = new HashMap<>();
        result.put("word", word.getWordText());
        result.put("explanation", explanation);
        
        return ResponseEntity.ok(result);
    }
}
```

### 3.4.2. Google Whisper API Integration

**WhisperService.java**
```java
@Service
public class WhisperService {
    
    @Value("${whisper.api.key}")
    private String apiKey;
    
    public WhisperTranscriptionDto transcribeAudio(MultipartFile audioFile) throws IOException {
        // Upload audio to Whisper API
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(audioFile.getOriginalFilename()));
        body.add("model", "whisper-1");
        body.add("language", "en");
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = 
            new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<WhisperResponse> response = restTemplate.exchange(
                "https://api.openai.com/v1/audio/transcriptions",
                HttpMethod.POST,
                requestEntity,
                WhisperResponse.class
            );
            
            WhisperResponse whisperResponse = response.getBody();
            
            return new WhisperTranscriptionDto(
                whisperResponse.getText(),
                calculatePronunciationScore(whisperResponse.getText())
            );
        } catch (Exception e) {
            throw new RuntimeException("Whisper API error: " + e.getMessage());
        }
    }
    
    private double calculatePronunciationScore(String transcribedText) {
        // Simple scoring: if transcription matches expected word
        // In production, use more sophisticated metrics
        return 0.85; // Placeholder
    }
}
```

**SpeakingController.java**
```java
@RestController
@RequestMapping("/api/speaking")
public class SpeakingController {
    
    @Autowired
    private WhisperService whisperService;
    
    @PostMapping("/submit")
    public ResponseEntity<?> submitSpeakingPractice(
        @RequestParam Long wordId,
        @RequestParam("audio") MultipartFile audioFile) throws IOException {
        
        Word word = wordService.getWord(wordId);
        
        WhisperTranscriptionDto transcription = 
            whisperService.transcribeAudio(audioFile);
        
        // Save result
        SpeakingRecord record = new SpeakingRecord();
        record.setWord(word);
        record.setUser(getCurrentUser());
        record.setTranscribedText(transcription.getText());
        record.setPronunciationScore(transcription.getScore());
        
        speakingRepository.save(record);
        
        return ResponseEntity.ok(new SpeakingResultDto(
            word.getWordText(),
            transcription.getText(),
            transcription.getScore(),
            "Good job!"
        ));
    }
}
```

---

## 3.5. Giải Quyết Các Thách Thức Trong Triển Khai

### 3.5.1. Thách Thức 1: CORS (Cross-Origin Resource Sharing)

**Vấn đề:**
- Frontend chạy trên localhost:5173 (Vite dev server)
- Backend chạy trên localhost:8080
- Browser chặn request từ origin khác

**Giải Pháp:**
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173", 
                           "http://localhost:3000",
                           "https://vocabularyai.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

### 3.5.2. Thách Thức 2: JWT Token Refresh

**Vấn đề:**
- Access token hết hạn sau 24h
- Người dùng không muốn phải login lại
- Refresh token hết hạn sau 7 ngày

**Giải Pháp:**
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
        throws ServletException, IOException {
        
        try {
            String jwt = getJwtFromRequest(request);
            
            if (jwt != null && jwtProvider.validateToken(jwt)) {
                String email = jwtProvider.getEmailFromToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException ex) {
            // Token expired, return 401 to frontend
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, 
                             "Token expired. Please refresh.");
        }
        
        filterChain.doFilter(request, response);
    }
}

// Frontend interceptor
axios.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config;
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      const refreshToken = localStorage.getItem('refreshToken');
      const response = await axios.post('/api/auth/refresh', { refreshToken });
      
      localStorage.setItem('accessToken', response.data.accessToken);
      originalRequest.headers['Authorization'] = 
        `Bearer ${response.data.accessToken}`;
      
      return axios(originalRequest);
    }
    
    return Promise.reject(error);
  }
);
```

### 3.5.3. Thách Thức 3: Gemini API Latency

**Vấn đề:**
- Gemini API có latency 1-3 giây
- Người dùng không muốn đợi lâu

**Giải Pháp:**
```java
@Service
public class CachedGeminiService {
    
    @Autowired
    private GeminiService geminiService;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    public String generateExplanation(String word) {
        String cacheKey = "ai_explanation:" + word.toLowerCase();
        
        // Check cache
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // Generate and cache
        String explanation = geminiService.generateExplanation(word);
        redisTemplate.opsForValue()
            .set(cacheKey, explanation, Duration.ofDays(30));
        
        return explanation;
    }
}
```

### 3.5.4. Thách Thức 4: Spaced Repetition Complexity

**Vấn đề:**
- SM-2 algorithm khó để hiểu & maintain
- Khó để debug khi có vấn đề

**Giải Pháp:**
```java
@Service
@Slf4j
public class SpacedRepetitionService {
    
    public LearningProgress calculateNextReview(
        LearningProgress progress, int quality) {
        
        double oldEF = progress.getEasinessFactorValue();
        int oldInterval = progress.getInterval();
        
        // Logging for debugging
        log.info("SR Calculation - Word: {}, Quality: {}, OldEF: {}, OldInterval: {}",
                 progress.getWord().getId(), quality, oldEF, oldInterval);
        
        double newEF = calculateNewEF(oldEF, quality);
        int newInterval = calculateNewInterval(oldInterval, newEF, quality);
        
        log.info("SR Result - NewEF: {}, NewInterval: {}, NextReview: {}",
                 newEF, newInterval, LocalDate.now().plusDays(newInterval));
        
        progress.setEasinessFactorValue(newEF);
        progress.setInterval(newInterval);
        progress.setNextReviewDate(LocalDate.now().plusDays(newInterval));
        progress.setRepetition(progress.getRepetition() + 1);
        progress.setQuality(quality);
        
        return progress;
    }
    
    private double calculateNewEF(double currentEF, int quality) {
        double newEF = currentEF + (0.1 - (5 - quality) * 0.08);
        return Math.max(1.3, newEF); // Min 1.3
    }
    
    private int calculateNewInterval(int currentInterval, double newEF, int quality) {
        if (quality < 3) return 1;
        if (currentInterval == 0) return 1;
        if (currentInterval == 1) return 3;
        return Math.round(currentInterval * (float) newEF);
    }
}
```

### 3.5.5. Thách Thức 5: Audio File Upload & Processing

**Vấn đề:**
- Audio files rất lớn (5-30MB)
- Upload trực tiếp to server gây tải
- Whisper API có timeout

**Giải Pháp:**
```java
@Service
public class AudioUploadService {
    
    @Autowired
    private CloudinaryService cloudinaryService;
    
    @Autowired
    private WhisperService whisperService;
    
    public void processAudioFile(MultipartFile audioFile, Long wordId, 
                                 Long userId) throws IOException {
        // 1. Upload to Cloudinary (external storage)
        String cloudinaryUrl = cloudinaryService.upload(audioFile);
        
        // 2. Save to database for reference
        AudioRecord record = new AudioRecord();
        record.setUrl(cloudinaryUrl);
        record.setWordId(wordId);
        record.setUserId(userId);
        audioRepository.save(record);
        
        // 3. Process with Whisper (async)
        processAudioAsync(cloudinaryUrl, wordId, userId);
    }
    
    @Async
    private void processAudioAsync(String audioUrl, Long wordId, Long userId) {
        try {
            // Download from Cloudinary
            byte[] audioBytes = downloadFromUrl(audioUrl);
            
            // Send to Whisper with timeout
            WhisperTranscriptionDto result = 
                whisperServiceWithTimeout.transcribe(audioBytes, 30); // 30s timeout
            
            // Save result
            updateSpeakingResult(wordId, userId, result);
        } catch (TimeoutException e) {
            log.error("Whisper API timeout for word: {}", wordId);
        } catch (Exception e) {
            log.error("Audio processing error: {}", e.getMessage());
        }
    }
}
```

---

**Hết Chương 3 Part 1**

Tiếp theo, Chương 3 Part 2 sẽ trình bày: Kết quả kiểm thử, đánh giá đạt được mục tiêu, các vấn đề chưa giải quyết, và hướng phát triển trong tương lai.
