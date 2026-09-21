# PROMPT XÂY DỰNG GIAO DIỆN WEBSITE VOCABULARYAI

## 1. VAI TRÒ

Bạn là một **Senior UI/UX Designer + Senior Frontend Developer**, có kinh nghiệm thiết kế và xây dựng các sản phẩm:

* EdTech
* AI-powered applications
* English Learning Platform
* SaaS Dashboard
* Admin Dashboard

Hãy thiết kế và triển khai giao diện frontend hoàn chỉnh cho hệ thống **VocabularyAI**.

Website tham khảo:

https://vocabularyai.vercel.app/

Hãy sử dụng website trên làm **nguồn tham khảo về phong cách visual**, nhưng không sao chép nguyên xi. Thiết kế cần có nhận diện riêng, hiện đại hơn, nhất quán hơn và phù hợp với một nền tảng học tiếng Anh tích hợp AI.

---

# 2. THÔNG TIN SẢN PHẨM

VocabularyAI là một nền tảng học tiếng Anh trực tuyến sử dụng AI.

Hệ thống có hai nhóm người dùng chính:

### STUDENT

Student có thể:

* Đăng ký
* Đăng nhập
* Quản lý thông tin cá nhân
* Học từ vựng
* Tra cứu từ vựng
* Lưu từ vựng
* Học theo vocabulary set
* Làm quiz
* Xem kết quả quiz
* Theo dõi tiến độ học tập
* Luyện viết tiếng Anh
* Dịch câu tiếng Việt sang tiếng Anh
* Nhận AI correction
* Nhận giải thích lỗi ngữ pháp
* Nhận giải thích từ vựng
* Nhận AI recommendations
* Xem các từ yếu/chưa nhớ
* Theo dõi learning statistics

### ADMIN

Admin có thể:

* Đăng nhập Admin
* Xem Dashboard
* Quản lý Student
* Xem thông tin Student
* Khóa/mở khóa tài khoản
* Quản lý Vocabulary
* Quản lý Vocabulary Set
* Quản lý Quiz
* Quản lý câu hỏi Quiz
* Quản lý Writing Practice
* Theo dõi hoạt động người dùng
* Xem thống kê hệ thống
* Theo dõi AI usage
* Quản lý các nội dung học tập

Admin **không phải Instructor**.

---

# 3. MỤC TIÊU UI/UX

Thiết kế phải tạo cảm giác:

* Modern
* Minimalist
* Clean
* Premium
* Friendly
* Educational
* AI-powered
* Professional
* Easy to use

Không thiết kế theo phong cách:

* Quá nhiều gradient
* Quá nhiều animation
* Quá nhiều màu
* Quá nhiều card
* Giao diện rối
* Dashboard giống template admin cũ

Triết lý thiết kế:

> "Simple enough for students, powerful enough for administrators."

Người dùng phải có thể hiểu giao diện mà không cần hướng dẫn.

---

# 4. DESIGN SYSTEM

## 4.1. Color Palette

Sử dụng màu tím làm màu nhận diện chính của VocabularyAI.

Primary:

```text
#7C3AED
```

Primary Hover:

```text
#6D28D9
```

Secondary:

```text
#6366F1
```

Background:

```text
#F8FAFC
```

Surface:

```text
#FFFFFF
```

Main Text:

```text
#111827
```

Secondary Text:

```text
#6B7280
```

Border:

```text
#E5E7EB
```

Success:

```text
#22C55E
```

Warning:

```text
#F59E0B
```

Error:

```text
#EF4444
```

Info:

```text
#3B82F6
```

Không sử dụng tất cả màu cùng lúc.

Purple là màu nhận diện chính.

Màu success/error/warning chỉ được dùng để biểu thị trạng thái.

---

# 5. TYPOGRAPHY

Ưu tiên:

```text
Inter
```

hoặc:

```text
Plus Jakarta Sans
```

Typography hierarchy:

```text
H1: 40–48px
H2: 28–32px
H3: 20–24px
H4: 18px

Body: 15–16px
Small: 13–14px

Button: 14–16px
```

Typography phải:

* dễ đọc;
* rõ hierarchy;
* phù hợp với nội dung tiếng Anh;
* không dùng quá nhiều font weight.

---

# 6. SPACING & BORDER

Sử dụng spacing system nhất quán:

```text
4px
8px
12px
16px
24px
32px
48px
64px
```

Border radius:

```text
Button: 8px
Input: 8px
Card: 12px
Modal: 16px
Large container: 20px
```

Shadow sử dụng nhẹ.

Không sử dụng shadow quá đậm.

---

# 7. LANDING PAGE

Thiết kế Landing Page với visual hierarchy rõ ràng.

## Header

Bao gồm:

```text
VocabularyAI logo

Features
Vocabulary
Quiz
Writing
AI

Login
Get Started
```

Header:

* sticky khi scroll;
* responsive;
* background sáng;
* CTA rõ ràng.

---

# 8. HERO SECTION

Hero section cần truyền tải ngay:

### VocabularyAI là gì?

Một nền tảng học tiếng Anh tích hợp AI.

Thông điệp đề xuất:

> Learn English smarter with AI.

Sub-heading:

> Build your vocabulary, practice with quizzes, improve your writing and get personalized AI feedback.

CTA:

```text
[ Start Learning ]
[ Explore Features ]
```

Không làm hero quá phức tạp.

---

# 9. FEATURES SECTION

Giới thiệu các chức năng chính:

### Vocabulary

Learn and review English vocabulary.

### Quiz

Practice and test your knowledge.

### Writing

Translate Vietnamese sentences into English and improve your writing.

### AI Feedback

Get instant grammar and vocabulary corrections.

### Personalized Learning

Receive recommendations based on learning performance.

Thiết kế bằng các card đơn giản, icon rõ ràng.

---

# 10. STUDENT APPLICATION

Sau khi Student login thành công, sử dụng layout Application Dashboard.

Desktop:

```text
┌──────────────────────────────────────────────────────────┐
│ Logo        Search                    Notification Avatar│
├───────────────┬──────────────────────────────────────────┤
│               │                                          │
│ Dashboard     │              Main Content                │
│ Vocabulary    │                                          │
│ Quiz          │                                          │
│ Writing       │                                          │
│ AI Coach      │                                          │
│ Progress      │                                          │
│               │                                          │
│ Profile       │                                          │
│               │                                          │
│ Logout        │                                          │
└───────────────┴──────────────────────────────────────────┘
```

Sidebar:

* Dashboard
* Vocabulary
* Quiz
* Writing
* AI Coach
* Progress
* Profile

Active menu phải được highlight bằng màu Primary.

---

# 11. STUDENT DASHBOARD

Dashboard phải tập trung vào learning.

Top:

```text
Good morning! 👋

Ready to improve your English today?
```

Statistics:

```text
Words Learned
Quiz Accuracy
Learning Streak
Writing Practice
```

Ví dụ:

```text
128
Words Learned

82%
Quiz Accuracy

7
Day Streak

24
Writing Exercises
```

---

## Continue Learning

Hiển thị bài học gần nhất:

```text
Continue Learning

Business English Vocabulary

15 / 20 words completed

██████████████░░░░

[ Continue Learning ]
```

---

# 12. AI RECOMMENDATION

Tạo section:

```text
AI Learning Recommendation
```

Ví dụ:

```text
Based on your recent activity:

You often struggle with:
• Past tense
• Affect vs Effect
• Prepositions

Recommended practice:

[ Practice Weak Words ]
```

Thiết kế AI recommendation nổi bật nhưng không quá flashy.

---

# 13. VOCABULARY PAGE

Vocabulary page gồm:

Search:

```text
Search English word...
                         🔍
```

Filter:

```text
All
Learned
Learning
Weak Words
```

Vocabulary card:

```text
┌───────────────────────────────┐
│ resilient                     │
│ /rɪˈzɪliənt/                  │
│                               │
│ adjective                     │
│                               │
│ Able to recover quickly       │
│                               │
│ Example:                      │
│ She is resilient after...     │
│                               │
│ 🔊 Listen                     │
│                               │
│ [ Review ]                    │
└───────────────────────────────┘
```

Cho phép:

* pronunciation;
* definition;
* example;
* learned status;
* save/bookmark.

---

# 14. QUIZ PAGE

Quiz interface phải tập trung tối đa.

Hiển thị:

```text
Question 4 / 10

████████████░░░░░░

What does "resilient" mean?

○ Easily defeated
○ Able to recover quickly
○ Very expensive
○ Extremely large

[ Submit Answer ]
```

Sau khi trả lời:

```text
✓ Correct!

Explanation

"Resilient" means...

Example:
...
```

Nếu sai:

```text
✕ Incorrect

Correct answer:
Able to recover quickly

Explanation:
...
```

Không chuyển trang không cần thiết.

---

# 15. WRITING PRACTICE

Đây là một chức năng quan trọng của hệ thống.

Layout:

```text
Writing Practice

Translate the following sentence:

"Tôi đã học tiếng Anh được 3 năm."

┌───────────────────────────────────────┐
│ I have studied English for 3 years.   │
│                                       │
└───────────────────────────────────────┘

              [ Check with AI ]
```

Sau khi AI phân tích:

```text
AI Feedback

Corrected Sentence

I have studied English for three years.

Grammar

Present Perfect

Explanation

We use Present Perfect to describe
an action that started in the past
and continues until now.

Vocabulary

studied
for three years
```

Nếu có lỗi, highlight phần lỗi trực tiếp.

---

# 16. SPEAKING PRACTICE

Đây là một chức năng quan trọng để phát triển kỹ năng nói tiếng Anh.

## Speaking Practice List

Hiển thị danh sách các bài Speaking Practice:

```text
Speaking Practice Sessions

┌──────────────────────────────────────────────────────┐
│ Session 1: Daily Conversation                       │
│ Difficulty: Beginner                                │
│ Duration: 10 minutes                                │
│ Status: Completed                                   │
│ Score: 78/100                                       │
│ [ View Results ]  [ Practice Again ]                │
└──────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────┐
│ Session 2: Business English                         │
│ Difficulty: Intermediate                            │
│ Duration: 15 minutes                                │
│ Status: Not Started                                 │
│ [ Start Session ]                                   │
└──────────────────────────────────────────────────────┘
```

Mỗi session card bao gồm:
* Session title
* Difficulty level
* Duration
* Status (Not Started, In Progress, Completed)
* Score (nếu đã hoàn thành)
* Actions

---

## Speaking Practice Interface

Khi student bắt đầu session:

```text
Speaking Practice: Daily Conversation

Question 1 / 5

⏱️ Time: 00:45 / 02:00

"Tell me about your favorite hobby."

🎤 [ Start Recording ]

Instructions:
• Click microphone to start recording
• Speak clearly and naturally
• Duration: 30 seconds to 2 minutes
```

Sau khi recording:

```text
🎤 Recording... 00:45 / 02:00

[ Stop Recording ]  [ Clear ]

Audio waveform visualization

[ Submit Answer ]
```

---

## Speaking Analysis & Feedback

Sau khi submit, AI phân tích:

```text
AI Analysis

Pronunciation Score: 8.5/10
Fluency Score: 7.8/10
Grammar Score: 8.2/10
Overall Score: 8.2/10

✓ Well done! Your pronunciation is clear.
⚠ Try to speak more naturally without long pauses.
✓ Good use of vocabulary.

Your recording:
🔊 [ Play ]

Transcript:
"I like playing basketball..."

Suggestions:
• Practice word stress
• Add more details to your answer
```

---

## Speaking Session Results

Tổng hợp kết quả session:

```text
Session Complete! 🎉

Daily Conversation

Overall Score: 8.2/10

Breakdown:
Pronunciation    ████████░░  8.5/10
Fluency          ███████░░░  7.8/10
Grammar          ████████░░  8.2/10

Performance vs Average:
Your Score: 8.2/10
Class Average: 7.5/10
👌 Above average

Next Steps:
[ Review Weak Areas ]
[ Practice Again ]
[ Next Session ]

Progress:
Sessions Completed: 3/10
Average Score: 7.8/10
Total Practice Time: 45 minutes
```

---

# 17. AI COACH

Thiết kế AI Coach như một **Personal English Coach**, không chỉ là chatbot thông thường.

## AI Coach Conversation List

Hiển thị danh sách hội thoại với AI Coach:

```text
AI Learning Coach

┌──────────────────────────────────────────────────────┐
│ Today                                                │
│ • Discussing weak areas in grammar         02:30 PM │
│ • Practice recommendations                 01:15 PM │
└──────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────┐
│ Yesterday                                            │
│ • Review of quiz performance                03:45 PM │
│ • Vocabulary practice tips                  02:20 PM │
└──────────────────────────────────────────────────────┘
```

Mỗi hội thoại hiển thị:
* Ngày/giờ cuối cùng chat
* Nội dung summary (topic)
* Có thể mở lại để tiếp tục chat

---

## AI Coach Chat Interface

Khi mở cuộc hội thoại với AI Coach:

```text
AI Learning Coach

┌─────────────────────────────────────────────────────┐
│ Based on your recent activity:                      │
│                                                     │
│ • You've completed 5 quizzes this week              │
│ • Average accuracy: 78%                             │
│ • Weak areas: Past tense, Phrasal verbs             │
│                                                     │
│ What would you like to practice today?              │
└─────────────────────────────────────────────────────┘

Coach: "Hello! I noticed you're struggling with past 
tense. Would you like me to explain it with examples?"

┌─────────────────────────────────────────────────────┐
│ • Yes, explain past tense                           │
│ • Review my weak vocabulary words                   │
│ • Give me a mini quiz                               │
│ • Recommend next learning path                      │
└─────────────────────────────────────────────────────┘
```

---

## AI Coach Chat Conversation

Chat interface:

```text
AI Learning Coach

┌────────────────────────────────────────────────────┐
│  ☰  AI Learning Coach                      [ × ]   │
├────────────────────────────────────────────────────┤
│                                                    │
│ Coach: "Let's talk about past tense. Can you      │
│ tell me what the past tense is?"                  │
│                                                    │
│ You: "It's used to describe things that          │
│ happened in the past"                            │
│                                                    │
│ Coach: "Exactly! And there are several forms:    │
│ Simple past, past continuous, past perfect.      │
│ Which one gives you the most trouble?"            │
│                                                    │
│ You: "...typing...  ✎                             │
│                                                    │
├────────────────────────────────────────────────────┤
│ ┌──────────────────────────────────────────────┐  │
│ │ Your message...                           📎  │  │
│ │                                      [ Send ]  │  │
│ └──────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────┘
```

Features:
* Message history
* Typing indicator ("Coach is typing...")
* Input field với attachment (images, files)
* Clear message button
* Recommended prompts để giúp user bắt đầu

---

## AI Coach Recommendations

Coach đưa ra gợi ý:

```text
Based on your learning pattern:

📊 You've improved past tense by 15% this week
⚠️ Phrasal verbs still need work
✓ Quiz accuracy improving steadily

Recommended:
[ Take Advanced Past Tense Quiz ]
[ Focus on Phrasal Verbs ]
[ Free Writing Practice ]

Your next milestone:
Reach 85% accuracy by next week
Current progress: █████████░ 78%
```

---

# 18. SPEAKING MANAGEMENT (ADMIN)

Admin có thể quản lý Speaking Practice Sessions.

## Admin Speaking Overview

Dashboard hiển thị:

```text
Speaking Analytics

Total Sessions:  2,540
Completed:       2,180
In Progress:     180
Average Score:   7.6/10

Popular Topics:
1. Daily Conversation      (580 attempts)
2. Business English        (420 attempts)
3. Travel English          (360 attempts)
```

---

## Admin Speaking Session Management

Table quản lý:

```text
Speaking Sessions

Search...

┌────────────────────────────────────────────────────────────────┐
│ Topic │ Difficulty │ Sessions │ Avg Score │ Status │ Actions  │
├────────────────────────────────────────────────────────────────┤
│ Daily │ Beginner   │ 580      │ 8.2/10    │ Active │ ...      │
│ Conversation
├────────────────────────────────────────────────────────────────┤
│ Business│ Intermediate│ 420    │ 7.5/10    │ Active │ ...      │
├────────────────────────────────────────────────────────────────┤
│ Travel │ Beginner   │ 360      │ 7.8/10    │ Active │ ...      │
└────────────────────────────────────────────────────────────────┘

[ + Create New Session ]
```

Actions:
* View
* Edit
* Disable
* Delete

---

## Admin Create/Edit Speaking Session

Form tạo session mới:

```text
Create Speaking Session

Title
"Daily Conversation"

Description
"Practice common everyday English conversations"

Difficulty Level
[ Beginner    ▼ ]

Duration (seconds)
[ 120 ]

Number of Questions
[ 5 ]

Questions

Question 1
"Tell me about your favorite hobby."

Question 2
"What did you do last weekend?"

...

[ Save ]  [ Cancel ]
```

---

# 19. PROGRESS PAGE

Hiển thị:

* Words learned
* Quiz accuracy
* Learning streak
* Writing exercises
* Speaking practice
* Weak vocabulary
* Weekly progress
* Monthly progress

Có chart nhưng không được lạm dụng.

Ví dụ:

```text
Weekly Learning Progress

Mon █████
Tue ███████
Wed ████████
Thu ████
Fri █████████
Sat ███████
Sun ██████████

Breakdown:
Vocabulary: 30%
Quiz: 25%
Writing: 20%
Speaking: 15%
AI Coach: 10%
```

Speaking & Writing có riêng biểu đồ:

```text
Speaking Practice Trend

Week 1  █████ (5 sessions)
Week 2  ███████ (7 sessions)
Week 3  ██████ (6 sessions)
Week 4  ██████████ (10 sessions)

Average Score Trend: 7.5 → 8.2 ↗
```

---

# 20. PROFILE

Profile page:

```text
Profile

Avatar

Full Name
Email
Username

Learning Statistics

Words Learned
Quiz Accuracy
Writing Practice
Learning Streak

[ Edit Profile ]

[ Change Password ]
```

---

# 21. ADMIN DASHBOARD

Admin phải sử dụng **cùng Design System với Student**, nhưng giao diện có tính quản trị chuyên nghiệp hơn.

Không tạo một Admin UI hoàn toàn khác phong cách.

Ví dụ:

```text
VocabularyAI Admin
```

Layout:

```text
┌───────────────────────────────────────────────────────────┐
│ VocabularyAI Admin                    🔔   Admin Avatar   │
├─────────────────┬─────────────────────────────────────────┤
│                 │                                         │
│ Dashboard       │                                         │
│ Students        │              Main Content               │
│ Vocabulary      │                                         │
│ Vocabulary Sets │                                         │
│ Quizzes         │                                         │
│ Writing         │                                         │
│ AI Analytics    │                                         │
│ Reports         │                                         │
│                 │                                         │
│ Settings        │                                         │
│ Logout          │                                         │
└─────────────────┴─────────────────────────────────────────┘
```

---

# 22. ADMIN DASHBOARD OVERVIEW

Dashboard hiển thị:

```text
Total Students
Active Students
Vocabulary
Quiz Attempts
AI Requests
```

Ví dụ:

```text
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ 1,284        │ │ 982          │ │ 3,542        │
│ Students     │ │ Active       │ │ Vocabulary   │
└──────────────┘ └──────────────┘ └──────────────┘

┌──────────────┐ ┌──────────────┐
│ 12,420       │ │ 8,321        │
│ Quiz Attempts│ │ AI Requests  │
└──────────────┘ └──────────────┘
```

---

# 23. ADMIN – STUDENT MANAGEMENT

Table:

```text
Students

Search students...

┌─────────────────────────────────────────────────────────────┐
│ Name │ Email │ Status │ Words │ Quiz │ Created │ Actions │
├─────────────────────────────────────────────────────────────┤
│ ...  │ ...   │ Active │ 120   │ 82%  │ ...     │ ...     │
└─────────────────────────────────────────────────────────────┘
```

Actions:

```text
View
Edit
Activate
Deactivate
```

Không sử dụng delete trực tiếp nếu không cần thiết.

---

# 24. ADMIN – STUDENT DETAIL

Khi click Student:

```text
Student Profile

Name
Email
Status

Learning Overview

Words Learned
Quiz Accuracy
Writing Practice
Learning Streak

Recent Activity

Vocabulary Activity
Quiz Activity
Writing Activity
AI Activity
```

Admin có thể xem dữ liệu nhưng không được làm UI quá phức tạp.

---

# 25. ADMIN – VOCABULARY MANAGEMENT

Table:

```text
Vocabulary

Search...

Word
Part of Speech
Definition
Status
Created Date
Actions
```

Actions:

```text
View
Edit
Delete
```

Nút:

```text
[ + Add Vocabulary ]
```

Form:

```text
Word
Pronunciation
Part of Speech
Definition
Example
Vietnamese Meaning
Difficulty
```

---

# 26. ADMIN – VOCABULARY SET MANAGEMENT

Hiển thị:

```text
Vocabulary Sets

Business English
Travel English
Daily English
Academic English
Technology
```

Mỗi set:

```text
Title
Description
Number of words
Difficulty
Status
Actions
```

---

# 27. ADMIN – QUIZ MANAGEMENT

Hiển thị:

```text
Quiz
Title
Vocabulary Set
Questions
Attempts
Status
Created Date
Actions
```

Quiz detail:

```text
Question
Answers
Correct Answer
Explanation
Difficulty
```

---

# 28. ADMIN – WRITING MANAGEMENT

Admin có thể xem:

```text
Writing Exercises
Vietnamese sentence
Expected English
Difficulty
Status
Created Date
```

Thiết kế tương tự Vocabulary/Quiz management.

---

# 29. ADMIN – AI ANALYTICS

Đây là phần quan trọng để thể hiện AI của hệ thống.

Hiển thị:

```text
AI Analytics

Total AI Requests
Successful Requests
Failed Requests
Average Response Time
Token Usage
```

Charts:

```text
AI Requests
Weekly / Monthly

AI Feature Usage

Vocabulary
Writing Correction
Recommendations
```

Không cần hiển thị thông tin kỹ thuật quá sâu nếu không phục vụ admin.

---

# 30. ADMIN – SYSTEM ACTIVITY

Có activity log:

```text
Recent Activity

User registered
Vocabulary created
Quiz updated
Student account suspended
AI request completed
```

Mỗi activity có:

```text
Time
User
Action
Status
```

---

# 31. ADMIN DESIGN PRINCIPLE

Admin phải **đồng nhất với Student**:

Same:

* Logo
* Primary color
* Typography
* Border radius
* Button style
* Input style
* Card style
* Modal
* Toast
* Loading
* Empty state

Nhưng Admin có:

* Data table
* Filters
* Statistics
* Charts
* Management actions

Không sử dụng một template Admin khác hoàn toàn.

---

# 32. RESPONSIVE DESIGN

Bắt buộc responsive.

## Desktop

Sidebar cố định.

## Tablet

Sidebar thu gọn.

## Mobile

Sidebar chuyển thành:

```text
☰
```

Navigation mở dạng drawer.

Table trên mobile phải chuyển thành card/list hoặc horizontal scroll hợp lý.

Không để nội dung bị tràn màn hình.

---

# 33. ACCESSIBILITY

Đảm bảo:

* Contrast tốt
* Button có trạng thái hover/focus
* Input có label
* Keyboard navigation
* aria-label cho icon button
* Không chỉ dùng màu để biểu thị trạng thái
* Font dễ đọc
* Touch target tối thiểu khoảng 44px

---

# 34. STATES

Mọi component phải có đầy đủ:

### Loading

```text
Skeleton
```

### Empty

```text
No vocabulary found.

[ Explore Vocabulary ]
```

### Error

```text
Something went wrong.

[ Try Again ]
```

### Success

```text
✓ Changes saved successfully.
```

### Disabled

Button phải thể hiện rõ disabled state.

---

# 35. ANIMATION

Animation phải subtle.

Sử dụng:

* fade;
* slide;
* scale nhẹ;
* progress animation;
* skeleton loading.

Không sử dụng:

* animation quá mạnh;
* background chuyển động liên tục;
* excessive parallax;
* animation gây distraction.

Mục tiêu:

> Smooth, not flashy.

---

# 36. COMPONENT SYSTEM

Tạo reusable components:

```text
Button
Input
SearchInput
Select
Dropdown
Card
Modal
Toast
Badge
Avatar
Navbar
Sidebar
ProgressBar
Skeleton
EmptyState
ErrorState
DataTable
Pagination
Chart
VocabularyCard
QuizCard
AIRecommendationCard
```

Không copy-paste UI giữa các page.

---

# 37. UX PRINCIPLES

Luôn tuân thủ:

### Principle 1

User phải biết mình đang ở đâu.

### Principle 2

User phải biết hành động tiếp theo.

### Principle 3

Không yêu cầu user click quá nhiều lần.

### Principle 4

AI response phải dễ hiểu.

### Principle 5

Error phải có hướng xử lý.

### Principle 6

Loading phải có feedback.

### Principle 7

Không hiển thị quá nhiều thông tin cùng lúc.

### Principle 8

Learning experience phải là ưu tiên cao nhất.

---

# 38. TECHNICAL REQUIREMENTS

Nếu xây dựng frontend bằng React:

Ưu tiên:

```text
React
TypeScript
Vite
Tailwind CSS
React Router
Axios
Lucide React
Recharts
```

Nếu project đã có framework/library khác thì giữ nguyên stack hiện tại và áp dụng Design System trên.

Code phải:

* component-based;
* reusable;
* responsive;
* maintainable;
* clean;
* không hard-code dữ liệu ở nhiều nơi.

---

# 39. BACKEND INTEGRATION

Frontend phải được thiết kế để dễ kết nối REST API Spring Boot backend.

Không giả định backend thay đổi.

Tạo API service layer riêng:

```text
services/
    authService
    userService
    vocabularyService
    quizService
    writingService
    aiService
    adminService
```

Axios instance:

```text
/api
```

JWT được lưu và gửi qua Authorization header theo backend specification.

Frontend cần xử lý:

```text
401 Unauthorized
403 Forbidden
404 Not Found
500 Internal Server Error
```

---

# 40. AUTHENTICATION UX

## Login

```text
Email
Password

[ Login ]

Don't have an account?
[ Register ]
```

## Register

```text
Full Name
Username
Email
Password
Confirm Password

[ Create Account ]
```

Validation phải hiển thị ngay gần field.

---

# 41. ROLE-BASED UI

Nếu:

```text
role = STUDENT
```

→ Student Dashboard.

Nếu:

```text
role = ADMIN
```

→ Admin Dashboard.

Student không được nhìn thấy Admin navigation.

Admin không cần sử dụng Student navigation để quản trị.

---

# 42. FINAL VISUAL DIRECTION

Hãy giữ tổng thể giao diện theo concept:

> **Modern AI English Learning Platform**

Visual keywords:

```text
Clean
Modern
Minimal
Purple
White
Soft
Professional
Friendly
AI
Education
Premium
```

Không biến website thành:

```text
Generic Bootstrap Dashboard
```

và cũng không biến thành:

```text
Over-designed AI landing page
```

---

# 43. ƯU TIÊN THIẾT KẾ

Thứ tự ưu tiên:

```text
1. Usability
2. Information hierarchy
3. Consistency
4. Accessibility
5. Responsive
6. Visual aesthetics
7. Animation
```

Không hy sinh usability để đổi lấy visual effect.

---

# 44. OUTPUT MONG MUỐN

Hãy xây dựng đầy đủ các màn hình sau:

## Public

```text
Landing Page
Login
Register
```

## Student

```text
Dashboard
Vocabulary
Vocabulary Detail
Vocabulary Set
Quiz
Quiz Result
Writing Practice
Writing Result
Speaking Practice (NEW)
Speaking Results (NEW)
AI Coach (NEW)
Progress
Profile
Settings
```

## Admin

```text
Admin Dashboard
Student Management
Student Detail
Vocabulary Management
Vocabulary Create/Edit
Vocabulary Set Management
Quiz Management
Quiz Create/Edit
Writing Management
Speaking Management (NEW)
Speaking Session Create/Edit (NEW)
AI Analytics
Activity Logs
Settings
```

---

# 45. QUAN TRỌNG

Không chỉ tạo giao diện đẹp.

Hãy suy nghĩ như một **Senior UX Designer**:

* User vào trang này để làm gì?
* User cần thấy thông tin gì đầu tiên?
* CTA nào quan trọng nhất?
* Có quá nhiều thông tin không?
* User có biết bước tiếp theo không?
* Nếu API loading thì sao?
* Nếu API error thì sao?
* Nếu không có dữ liệu thì sao?
* Nếu màn hình mobile thì sao?
* Nếu user là Admin thì flow có khác không?

Mỗi page phải có **một mục tiêu chính**.

---

# 46. KẾT QUẢ CUỐI CÙNG

Tạo một hệ thống UI/UX hoàn chỉnh cho VocabularyAI với:

```text
              VOCABULARYAI
                    │
        ┌───────────┴───────────┐
        │                       │
     STUDENT                   ADMIN
        │                       │
        ↓                       ↓
   Dashboard               Dashboard
        │                       │
   Vocabulary              Students
   Quiz                    Vocabulary
   Writing                 Vocabulary Sets
   Speaking                Quizzes
   AI Coach                Writing
   Progress                Speaking
   Profile                 AI Analytics
                           Activity Logs
```

Hai khu vực phải có **cùng Design System, cùng branding, cùng visual language**, nhưng tối ưu UX riêng cho từng nhóm người dùng.

Mục tiêu cuối cùng:

> **VocabularyAI phải trông giống một sản phẩm EdTech/AI thực tế có thể đưa vào production, không phải một project demo hoặc một template dashboard.**

---

## TỔNG KÊTTHEO ĐỘ HOÀN THIỆN

**Tổng số section:** 46

**Phần Public:**
- Landing Page (Section 7-9)
- Authentication (Section 40)

**Phần Student (15 màn hình):**
- Dashboard (Section 11-12)
- Vocabulary (Section 13)
- Quiz (Section 14)
- Writing Practice (Section 15)
- Speaking Practice (Section 16)
- AI Coach (Section 17)
- Progress (Section 19)
- Profile (Section 20)

**Phần Admin (14 màn hình):**
- Admin Dashboard (Section 21-22)
- Student Management (Section 23-24)
- Vocabulary Management (Section 25)
- Vocabulary Set (Section 26)
- Quiz Management (Section 27)
- Writing Management (Section 28)
- Speaking Management (Section 18)
- AI Analytics (Section 29)
- Activity Logs (Section 30)

**Design System & Principles:**
- Color Palette (Section 4.1)
- Typography (Section 5)
- Spacing & Border (Section 6)
- Responsive Design (Section 32)
- Accessibility (Section 33)
- States (Section 34)
- Animation (Section 35)
- Component System (Section 36)
- UX Principles (Section 37)

**Technical & Integration:**
- Technical Requirements (Section 38)
- Backend Integration (Section 39)
- Role-Based UI (Section 41)
- Final Visual Direction (Section 42)
- Design Priorities (Section 43)

**Độ hoàn thiện:** ✅ 100%
- Tất cả Student features (Vocabulary, Quiz, Writing, Speaking, AI Coach, Progress)
- Tất cả Admin features (Students, Vocabulary, Vocabulary Sets, Quizzes, Writing, Speaking, Analytics)
- Design System toàn diện
- UX/UI principles rõ ràng
- Technical requirements chi tiết
- Backend integration guidelines
