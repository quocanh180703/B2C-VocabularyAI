# 📚 VocabularyAI — Nền Tảng Học Tiếng Anh Thông Minh Tích Hợp AI Đa Phương Thức

> **Hệ thống Học từ vựng, Ngữ pháp, Luyện nói phản xạ và Tự tạo bài tập AI từ tài liệu học tập cá nhân**  
> Ứng dụng mô hình AI tiên tiến: Google Gemini 3.7 Flash, OpenAI Whisper Large-v3 STT, Grok Voice TTS, Apache PDFBox/POI và Cổng thanh toán tự động PayOS VietQR.

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.0-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg?logo=openjdk)](https://www.oracle.com/java/)
[![React](https://img.shields.io/badge/React-19.x-blue.svg?logo=react)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-6.x-blue.svg?logo=typescript)](https://www.typescriptlang.org/)
[![Vite](https://img.shields.io/badge/Vite-8.x-646CFF.svg?logo=vite)](https://vitejs.dev/)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1.svg?logo=mysql)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-6+-DC382D.svg?logo=redis)](https://redis.io/)

---

## 📑 Mục Lục
1. [Giới Thiệu Dự Án](#-giới-thiệu-dự-án)
2. [Kiến Trúc Phân Quyền Người Dùng (RBAC)](#-kiến-trúc-phân-quyền-người-dùng-rbac)
3. [Các Phân Hệ & Tính Năng Cốt Lõi](#-các-phân-hệ--tính-năng-cốt-lõi)
4. [Công Nghệ Sử Dụng (Tech Stack)](#-công-nghệ-sử-dụng-tech-stack)
5. [Cấu Trúc Thư Mục Dự Án](#-cấu-trúc-thư-mục-dự-án)
6. [Yêu Cầu Hệ Thống & Cài Đặt Cơ Sở Dữ Liệu](#-yêu-cầu-hệ-thống--cài-đặt-cơ-sở-dữ-liệu)
7. [Cấu Hình Biến Môi Trường (.env)](#-cấu-hình-biến-môi-trường-env)
8. [Khởi Chạy Ứng Dụng](#-khởi-chạy-ứng-dụng)
9. [Tài Liệu API & Swagger UI](#-tài-liệu-api--swagger-ui)
10. [Bảo Mật & Best Practices](#-bảo-mật--best-practices)

---

## 🌟 Giới Thiệu Dự Án

**VocabularyAI** là nền tảng học tiếng Anh trực tuyến toàn diện, kết hợp phương pháp sư phạm hiện đại với công nghệ Trí Tuệ Nhân Tạo (AI) đa phương thức:
- **Tạo bài tập AI từ tài liệu cá nhân (Document AI Quiz):** Tải lên tài liệu PDF, DOCX, TXT; AI trích xuất nội dung, phân tích từ vựng và tự động tạo đề thi trắc nghiệm theo chủ đề và trình độ CEFR.
- **Hệ thống ôn tập ngắt quãng (Spaced Repetition System - SRS):** Áp dụng chu kỳ lặp lại thông minh (1 – 3 – 7 – 14 – 30 ngày) giúp ghi nhớ dài hạn kho 5.000+ từ vựng Oxford theo chuẩn CEFR A1–C1.
- **Luyện nói phản xạ & Chấm điểm 5 tiêu chí (Voice Chat AI):** Nhận diện giọng nói siêu chính xác qua OpenAI Whisper Large v3, tổng hợp giọng nói bản ngữ qua TTS và AI chấm điểm chi tiết 5 tiêu chuẩn: *Ngữ pháp, Từ vựng, Độ trôi chảy, Nội dung, Phát âm*.
- **Gia sư ảo Vi (AI Tutor & Chat Coach):** Trò chuyện, giải thích từ vựng theo ngữ cảnh thực tế, phân tích lỗi ngữ pháp và đề xuất phương án cải thiện tức thì.
- **Quy trình kiểm duyệt bài tập cộng đồng (Instructor Review):** Học viên chia sẻ bài tập tự tạo lên kho đề thi chung; Giảng viên kiểm duyệt, phê duyệt công khai kèm tên tác giả (`@username`) hoặc từ chối kèm lý do phản hồi.
- **Thanh toán tự động PayOS VietQR:** Nâng cấp gói thành viên PRO tự động qua quét mã ngân hàng VietQR, xử lý webhook bảo mật và quản lý hạn mức AI hàng ngày.

---

## 👥 Kiến Trúc Phân Quyền Người Dùng (RBAC)

Hệ thống phân chia 3 cấp bậc vai trò độc lập, đảm bảo bảo mật và luồng công việc tối ưu:

```
                    ┌───────────────────────────────┐
                    │       VocabularyAI RBAC       │
                    └───────────────┬───────────────┘
            ┌───────────────────────┼───────────────────────┐
            ▼                       ▼                       ▼
   ┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
   │     STUDENT     │     │   INSTRUCTOR    │     │      ADMIN      │
   │    (Học viên)   │     │  (Giảng viên)   │     │ (Quản trị viên) │
   └────────┬────────┘     └────────┬────────┘     └────────┬────────┘
            │                       │                       │
     • Học từ vựng Oxford    • Quản lý học viên      • Toàn quyền quản trị
     • Ôn tập SRS Flashcard  • Quản lý Từ vựng/CEFR  • Quản lý Đơn hàng/Gói
     • Voice Chat AI 5 tiêu  • Quản lý Trắc nghiệm   • Cấp quyền Instructor
     • Tạo bài tập từ Doc    • Duyệt đề thi từ User  • Trợ lý AI giám sát
     • Placement & Luyện đề  • Báo cáo & Thống kê    • Quản trị Logs hệ thống
```

1. **Học viên (`ROLE_USER` / `STUDENT`):**
   - Học từ vựng, tra cứu từ điển Oxford, xem ví dụ và phát âm audio bản ngữ.
   - Luyện nói phản xạ với AI, luyện nghe, luyện ngữ pháp chuyên sâu.
   - Tải lên tài liệu PDF/DOCX/TXT để AI tạo đề trắc nghiệm cá nhân hóa.
   - Gửi yêu cầu duyệt đề thi để công khai cho cộng đồng học viên cùng làm bài.
   - Kiểm tra trình độ đầu vào (Placement Test) và nhận lộ trình thích ứng (Adaptive Roadmap).
2. **Giảng viên (`ROLE_INSTRUCTOR`):**
   - Sử dụng giao diện chuyên biệt **Instructor Portal** (ẩn các chức năng học viên để tập trung chuyên môn).
   - Quản lý danh sách học viên và theo dõi tiến độ học tập.
   - Quản trị kho từ vựng và câu hỏi trắc nghiệm hệ thống.
   - **Duyệt nội dung (`/admin/content-review`):** Xem xét đề thi học viên gửi duyệt, xem từng câu hỏi/đáp án/giải thích; thực hiện **Phê duyệt & Công khai** hoặc **Từ chối** kèm ghi chú phản hồi.
   - Xem báo cáo, đo lường năng lực và thống kê hoạt động học tập.
3. **Quản trị viên (`ROLE_ADMIN`):**
   - Toàn quyền vận hành toàn bộ hệ thống.
   - Quản lý doanh thu, đơn hàng, gói cước PRO và lịch sử giao dịch PayOS.
   - Cấp quyền, kích hoạt và quản lý tài khoản Giảng viên (Instructor).
   - Truy cập **Trợ lý AI & Giám sát hệ thống (`/admin/ai-assistant`)** để kiểm tra log, phân tích cảnh báo và hiệu năng ứng dụng.

---

## 🚀 Các Phân Hệ & Tính Năng Cốt Lõi

| Phân hệ | Mô tả nghiệp vụ chi tiết |
|---|---|
| 📄 **Document AI Exercise Generator** | Upload tài liệu học tập (PDF, DOCX, TXT); Apache PDFBox & Apache POI trích xuất văn bản; AI phân tích chủ đề, độ dài, cấp độ CEFR; AI Chat tinh chỉnh yêu cầu đề thi; tự động sinh trắc nghiệm chống trùng lặp câu hỏi. |
| 🛡️ **Instructor Review Workflow** | Đề thi mới tạo ở trạng thái `PRIVATE`. Học viên bấm *Gửi duyệt* (`PENDING`). Giảng viên kiểm tra và xác nhận qua modal chuyên dụng: *Phê duyệt* (`PUBLIC`) hiển thị nhãn `@tác_giả` trong kho đề thi chung, hoặc *Từ chối* (`REJECTED`) kèm lý do. |
| 📖 **Kho Từ Vựng Oxford & CEFR** | 5.000+ từ vựng chuẩn Oxford cấp độ A1 đến C1, tích hợp phiên âm IPA, âm thanh bản ngữ chuẩn, ví dụ ngữ cảnh, danh mục chủ đề và hỗ trợ tạo từ vựng riêng của người dùng. |
| 🔄 **Spaced Repetition System (SRS)** | Thuật toán lặp lại ngắt quãng tính toán đường cong quên lãng, tự động nhắc nhở và tổ chức phiên ôn tập thẻ ghi nhớ (Flashcards) hàng ngày. |
| 🎙️ **Voice Chat AI & Chấm Điểm 5 Tiêu Chí** | Giao tiếp 2 chiều trực tiếp qua micro; Whisper Large v3 phiên âm thời gian thực; Gemini AI phản hồi ngữ cảnh và chấm điểm 5 khía cạnh (*Grammar, Vocabulary, Fluency, Content, Pronunciation*). Audio lưu trữ trên Cloudinary CDN. |
| 🧩 **Luyện Ngữ Pháp & Luyện Nghe** | Bài tập điền từ, chọn đáp án đúng theo chủ đề ngữ pháp; hệ thống bài luyện nghe audio kèm phụ đề và câu hỏi kiểm tra khả năng bắt từ. |
| 🎯 **Placement Test & AI Roadmap** | Bài thi đánh giá năng lực đầu vào theo chuẩn CEFR; AI chẩn đoán chủ đề còn yếu (Weak Topics) và cá nhân hóa lộ trình học phù hợp. |
| 💳 **Cổng Thanh Toán PayOS VietQR** | Tích hợp mã thanh toán ngân hàng VietQR động, xác thực chữ ký Webhook (HMAC-SHA256), tự động kích hoạt tài khoản PRO và quản trị hạn mức AI hàng ngày. |
| 🔐 **Bảo Mật & Xác Thực Đa Kênh** | Đăng ký & đăng nhập qua Email OTP (SMTP/Mailtrap) hoặc **Google One Tap / Google OAuth2 ID Token**; cơ chế Stateless JWT kép (Access Token 15 phút + Refresh Token 7 ngày); mã hóa mật khẩu BCrypt. |

---

## 🛠️ Công Nghệ Sử Dụng (Tech Stack)

### Backend
- **Nền tảng & Framework:** Java 21 LTS, Spring Boot 3.3.0
- **Bảo mật & Xác thực:** Spring Security 6, JJWT 0.12.5 (Access/Refresh Tokens), Google API Client 2.7.1, BCrypt
- **Cơ sở dữ liệu & ORM:** MySQL 8.3+, Spring Data JPA, Hibernate, Flyway Migration 9.22 (122 scripts)
- **Bộ nhớ đệm & Phân tán:** Redis 6+, Lettuce Driver, Spring Cache, Bucket4j Rate Limiting
- **Xử lý tài liệu:** Apache PDFBox 2.0.30 (PDF), Apache POI OOXML 5.2.5 (DOCX, TXT)
- **Dịch vụ AI & Âm thanh:** Google Gemini API (gemini-3.7-flash-high), OpenAI Whisper Large v3 STT, Grok Voice TTS, Cloudinary SDK 1.38
- **Cổng thanh toán:** PayOS REST API & Webhook (VietQR)
- **Tài liệu API:** SpringDoc OpenAPI 2.4.0 (Swagger UI v3)

### Frontend
- **Thư viện & Công cụ:** React 19 (`19.2.8`), TypeScript 6, Vite 8
- **Điều hướng & Hiệu ứng:** React Router v7 (`7.18.4`), Motion v14 (`14.4.0`), Oxlint
- **Đăng nhập Google:** `@react-oauth/google 0.13.5`
- **Giao diện người dùng:** Soft Modern Design System (Glassmorphism, CSS Custom Properties, Portal Dialogs, Responsive Mobile Tabbar)
- **Icons & Đồ họa:** Custom SVG Icons System, 3D WebP Mascot Assets, Lucide Icons

---

## 📂 Cấu Trúc Thư Mục Dự Án

```
d:/demo-datn/
├── vocabulary-ai-backend/                 # Source code Backend (Spring Boot 3 + Java 21)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/vocabularyai/
│   │   │   │   ├── admin/                # API thống kê, quản trị hệ thống, giám sát AI
│   │   │   │   ├── ai/                   # Tích hợp Gemini Chat, Prompts, Sửa lỗi ngữ pháp
│   │   │   │   ├── auth/                 # Xác thực JWT, OTP Email, Google One Tap, Security
│   │   │   │   ├── common/               # DTOs, Base Entities, Custom Exceptions, Global Handler
│   │   │   │   ├── document/             # Xử lý PDF/DOCX, trích xuất text, sinh đề thi AI
│   │   │   │   ├── learning/             # Lịch ôn từ SRS Spaced Repetition, Flashcards
│   │   │   │   ├── listening/            # Bài tập luyện nghe & xử lý audio
│   │   │   │   ├── notification/         # Quản lý thông báo người dùng trong ứng dụng
│   │   │   │   ├── payment/              # Tích hợp PayOS VietQR, Webhook, Quản lý đơn hàng
│   │   │   │   ├── personalization/      # Lộ trình học AI, Hạn mức sử dụng hàng ngày
│   │   │   │   ├── placement/            # Bài thi đánh giá trình độ đầu vào CEFR
│   │   │   │   ├── quiz/                 # Sinh câu hỏi trắc nghiệm, Nộp bài, Chấm điểm
│   │   │   │   ├── report/               # Báo cáo tiến độ học viên, Thống kê hệ thống
│   │   │   │   ├── speaking/             # Voice Chat 2 chiều, Whisper STT, TTS, Cloudinary
│   │   │   │   ├── user/                 # Quản lý hồ sơ, Cài đặt người dùng, Phân quyền
│   │   │   │   └── vocabulary/           # Từ điển Oxford B1-C1, Phân loại chủ đề, Từ riêng
│   │   │   └── resources/
│   │   │       ├── application.yml       # Cấu hình môi trường chính (sử dụng Environment Variables)
│   │   │       ├── application-test.yml  # Cấu hình kiểm thử tự động
│   │   │       └── db/migration/         # 120+ bản script nâng cấp cơ sở dữ liệu Flyway
│   │   └── test/                         # Unit tests & Integration tests
│   └── pom.xml
│
├── FE-Vocabulary-AI/                     # Source code Frontend (React 19 + TypeScript + Vite)
│   ├── src/
│   │   ├── assets/                       # Mascot 3D, Hình minh họa WebP, Audio icons
│   │   ├── auth/                         # AuthContext, Token Storage, Protected Routes
│   │   ├── components/                   # AdminLayout, Layout, VoiceChatPanel, Guards, Modals
│   │   │   ├── quiz/                     # CreateAiQuizModal, QuizDetailEditModal
│   │   │   └── icons.tsx                 # Hệ thống biểu tượng SVG chuẩn hóa
│   │   ├── pages/                        # Toàn bộ màn hình giao diện ứng dụng
│   │   │   ├── admin/                    # Admin Dashboard, Users, Quizzes, Review, Reports
│   │   │   ├── Dashboard.tsx             # Trang tổng quan học viên & tiến độ
│   │   │   ├── Words.tsx & WordDetail.tsx# Tra cứu từ vựng & Flashcard học tập
│   │   │   ├── Speaking.tsx              # Giao diện luyện nói Voice Chat AI
│   │   │   ├── QuizList.tsx & QuizDo.tsx # Danh sách bài thi công khai & Giao diện làm bài
│   │   │   ├── Chat.tsx                  # Trò chuyện trực tiếp với Gia sư ảo Vi
│   │   │   └── ...
│   │   ├── api/                          # Axios Client, Interceptors, Type Definitions
│   │   ├── motion/                       # Cấu hình Animation với thư viện Motion
│   │   ├── App.tsx                       # Cấu hình Route & Phân quyền bảo vệ
│   │   └── index.css                     # Design System CSS Tokens
│   ├── package.json
│   └── vite.config.ts
│
├── docker-compose.yml                    # Khởi tạo nhanh MySQL 8 và Redis qua Docker
└── README.md                             # Tài liệu kỹ thuật dự án
```

---

## ⚙️ Yêu Cầu Hệ Thống & Cài Đặt Cơ Sở Dữ Liệu

### 1. Yêu cầu môi trường chạy:
- **Java:** OpenJDK 21 LTS
- **Node.js:** v18+ hoặc v20 LTS
- **Build Tools:** Apache Maven 3.8+ và npm 9+
- **Databases:** MySQL 8.0+ và Redis 6.0+

### 2. Khởi tạo Cơ sở dữ liệu:
Sử dụng **Docker Compose** để khởi chạy nhanh MySQL và Redis:
```bash
docker compose up -d
```
Hoặc tạo cơ sở dữ liệu thủ công trong MySQL Client:
```sql
CREATE DATABASE IF NOT EXISTS datn_2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
> Flyway sẽ tự động kích hoạt và chạy toàn bộ các bản script di chuyển cơ sở dữ liệu (`db/migration/V1` đến `V122`) khi Backend khởi động lần đầu tiên.

---

## 🔑 Cấu Hình Biến Môi Trường (.env)

Tạo file `.env` tại thư mục `vocabulary-ai-backend` hoặc cấu hình các biến môi trường trực tiếp trên hệ thống:

```env
# 1. Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=datn_2
DB_USERNAME=root
DB_PASSWORD=your_mysql_password_here

# 2. Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# 3. JWT Security (Khóa bí mật tối thiểu 256 bits cho chuẩn HS256)
JWT_SECRET=your_jwt_access_secret_key_minimum_256_bits_here_1234567890
JWT_REFRESH_SECRET=your_jwt_refresh_secret_key_minimum_256_bits_here_0987654321

# 4. Google OAuth2 Client ID (Dùng cho cả Backend xác thực và Frontend One Tap)
GOOGLE_CLIENT_ID=your_google_client_id.apps.googleusercontent.com

# 5. Dịch vụ AI & Voice Services
AI_CHAT_URL=https://api.xah.io/v1/chat/completions
AI_CHAT_API_KEY=your_gemini_api_key_here
AI_CHAT_MODEL=gemini-3.7-flash-high

WHISPER_URL=https://api.vilao.ai/v1/audio/transcriptions
WHISPER_API_KEY=your_whisper_api_key_here
WHISPER_MODEL=tsa/groq/whisper-large-v3

TTS_URL=https://api.xah.io/v1/audio/speech
TTS_API_KEY=your_tts_api_key_here
TTS_MODEL=namth/grok-voice-latest

# 6. Cloudinary (Lưu trữ file ghi âm luyện nói của học viên)
CLOUDINARY_URL=cloudinary://your_api_key:your_api_secret@your_cloud_name

# 7. Cổng thanh toán tự động PayOS VietQR
PAYOS_CLIENT_ID=your_payos_client_id_here
PAYOS_API_KEY=your_payos_api_key_here
PAYOS_CHECKSUM_KEY=your_payos_checksum_key_here
PAYOS_SANDBOX=true

# 8. Cấu hình Email SMTP (Gửi mã OTP đăng ký / xác thực tài khoản)
MAIL_HOST=sandbox.smtp.mailtrap.io
MAIL_PORT=2525
MAIL_USERNAME=your_mailtrap_username
MAIL_PASSWORD=your_mailtrap_password
```

Cấu hình cho **Frontend** (`FE-Vocabulary-AI/.env`):
```env
VITE_API_URL=http://localhost:8080/api
VITE_GOOGLE_CLIENT_ID=your_google_client_id.apps.googleusercontent.com
```

---

## 🏃 Khởi Chạy Ứng Dụng

### Khởi chạy Backend (Spring Boot):
```bash
cd vocabulary-ai-backend
mvn clean spring-boot:run
```
- Endpoint Backend hoạt động tại: `http://localhost:8080/api`

### Khởi chạy Frontend (React + Vite):
```bash
cd FE-Vocabulary-AI
npm install
npm run dev
```
- Ứng dụng chạy trên trình duyệt tại: `http://localhost:5173`

---

## 📡 Tài Liệu API & Swagger UI

Toàn bộ hệ thống REST API được tài liệu hóa chi tiết qua chuẩn OpenAPI 3.0:
- **Giao diện tương tác trực quan (Swagger UI):** [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html)
- **Tài liệu OpenAPI 3 JSON:** [http://localhost:8080/api/v3/api-docs](http://localhost:8080/api/v3/api-docs)

### Danh mục nhóm API chính:
| Nhóm API | Base Endpoint | Chức năng chính |
|---|---|---|
| **Auth & Security** | `/api/auth/**` | Đăng ký OTP, Đăng nhập thường, Google One Tap Login, Refresh Token, Đổi mật khẩu |
| **Documents & AI Quiz** | `/api/documents/**` | Upload tài liệu (PDF/DOCX/TXT), Phân tích AI, Chat tinh chỉnh, Sinh đề thi trắc nghiệm |
| **Exercise Review** | `/api/admin/exercise-reviews/**` | Danh sách bài tập chờ duyệt, Xem chi tiết, Phê duyệt công khai hoặc Từ chối kèm lý do |
| **Vocabulary & CEFR** | `/api/words/**` | Tra cứu từ điển Oxford A1-C1, Gợi ý tự động (Autocomplete), Quản lý từ vựng cá nhân |
| **Learning & SRS** | `/api/learning/**` | Thuật toán lặp lại ngắt quãng SRS, Đánh dấu độ nhớ từ, Phiên ôn tập Flashcard |
| **Speaking & Voice** | `/api/speaking/**` | Voice Chat 2 chiều, Phiên âm Whisper STT, AI chấm điểm 5 tiêu chí chi tiết |
| **Quiz & Placement** | `/api/quizzes/**` & `/api/placement/**` | Bài kiểm tra tính giờ, Nộp bài chấm điểm tự động, Đánh giá năng lực đầu vào CEFR |
| **Payments & Orders** | `/api/payments/**` | Tạo link thanh toán VietQR PayOS, Webhook xác thực giao dịch, Kích hoạt gói PRO |
| **Personalization** | `/api/personalization/**` | Lộ trình học cá nhân hóa, Kiểm tra hạn mức sử dụng tính năng AI (Daily Quotas) |
| **Admin & Reports** | `/api/admin/**` & `/api/reports/**` | Quản lý người dùng/giảng viên, Báo cáo tiến độ học tập, Trợ lý AI giám sát hệ thống |

---

## 🔒 Bảo Mật & Best Practices

1. **Phân quyền đa tầng (Multi-layer RBAC):** Kiểm soát quyền hạn chặt chẽ ở cả tầng Frontend (`Guards.tsx`, `AdminLayout.tsx`) và tầng Backend (`@PreAuthorize("hasRole('ADMIN')")`, `@PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")`).
2. **Xác thực giao dịch Webhook PayOS an toàn:** Mọi webhook gửi từ PayOS đều được kiểm tra chữ ký HMAC-SHA256 theo chuỗi dữ liệu gốc (`checksumKey`), ngăn chặn hoàn toàn tấn công giả mạo thanh toán.
3. **Cơ chế chống sinh ảo (Anti-hallucination) trong Voice Chat:** Nếu tiến trình chấm điểm giọng nói gặp sự cố hoặc timeout, hệ thống phản hồi mã lỗi dịch vụ rõ ràng, yêu cầu thử lại thay vì tạo bảng điểm giả mạo.
4. **Cơ chế sinh câu hỏi tự thân (Self-contained AI Questions):** Prompt sinh câu hỏi trắc nghiệm từ tài liệu được thiết kế nghiêm ngặt: các câu hỏi kiểm tra kiến thức và từ vựng theo chủ đề mà không đòi hỏi người làm phải có văn bản trước mặt để trả lời.
5. **Bảo vệ Secret & Dữ liệu nhạy cảm:** Toàn bộ API keys, mật khẩu cơ sở dữ liệu, JWT secrets được cấu hình thông qua biến môi trường (`Environment Variables`), không commit lên kho mã nguồn Git.

---

## 👥 Nhóm Tác Giả & Bản Quyền
Dự án được xây dựng và phát triển phục vụ mục đích nghiên cứu, học tập và Đồ án tốt nghiệp:
- **Tác giả:** Đội ngũ phát triển VocabularyAI
- **Bản quyền:** © 2026 VocabularyAI. All rights reserved.
