# 📚 VocabularyAI — Nền Tảng Học Tiếng Anh Thông Minh Tích Hợp AI

> **Dự án:** Hệ thống học từ vựng, ngữ pháp và luyện nói tiếng Anh phản xạ ứng dụng Trí Tuệ Nhân Tạo (Gemini AI, Whisper STT, TTS, PayOS).

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.0-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg?logo=openjdk)](https://www.oracle.com/java/)
[![React](https://img.shields.io/badge/React-18.x-blue.svg?logo=react)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-blue.svg?logo=typescript)](https://www.typescriptlang.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-blue.svg?logo=mysql)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-6+-red.svg?logo=redis)](https://redis.io/)

---

## 📑 Mục Lục
1. [Giới Thiệu Dự Án](#-giới-thiệu-dự-án)
2. [Tính Năng Nổi Bật](#-tính-năng-nổi-bật)
3. [Công Nghệ Sử Dụng (Tech Stack)](#-công-nghệ-sử-dụng-tech-stack)
4. [Cấu Trúc Thư Mục](#-cấu-trúc-thư-mục)
5. [Yêu Cầu Hệ Thống & Hướng Dẫn Cài Đặt](#-yêu-cầu-hệ-thống--hướng-dẫn-cài-đặt)
6. [Cấu Hình Biến Môi Trường (.env)](#-cấu-hình-biến-môi-trường-env)
7. [Chạy Ứng Dụng](#-chạy-ứng-dụng)
8. [Tài Liệu API & Swagger](#-tài-liệu-api--swagger)
9. [Bảo Mật & Best Practices](#-bảo-mật--best-practices)

---

## 🌟 Giới Thiệu Dự Án
**VocabularyAI** là nền tảng học tiếng Anh trực tuyến toàn diện, kết hợp các phương pháp sư phạm hiện đại cùng công nghệ AI tiên tiến:
- **Spaced Repetition System (SRS):** Lịch ôn từ vựng ngắt quãng 1 – 3 – 7 – 14 – 30 ngày giúp ghi nhớ dài hạn từ vựng Oxford B1–C1.
- **Trợ lý học tập ảo Vi (AI Tutor):** Tích hợp Google Gemini để giải thích từ vựng theo ngữ cảnh thực tế, phân tích lỗi ngữ pháp và đàm thoại tự nhiên.
- **Luyện nói & Chấm điểm phản xạ (Voice Chat AI):** Nhận diện giọng nói chuẩn xác bằng OpenAI Whisper, tổng hợp giọng nói qua TTS và chấm điểm 5 tiêu chí (Ngữ pháp, Từ vựng, Trôi chảy, Nội dung, Phát âm).
- **Thanh toán tự động PayOS:** Nâng cấp gói thành viên VIP qua mã QR ngân hàng VietQR an toàn, tức thì với cơ chế bảo mật xác thực chặt chẽ.

---

## 🚀 Tính Năng Nổi Bật

| Phân hệ | Mô tả chi tiết |
|---|---|
| 📖 **Học Từ Vựng (Vocabulary)** | Kho 5.000+ từ vựng Oxford B1–C1, phân loại theo chủ đề, phát âm audio bản ngữ, ví dụ thực tế và flashcard. |
| 🔄 **Ôn Tập Thông Minh (SRS Review)** | Thuật toán tính toán chu kỳ quên và nhắc nhở ôn từ đến hạn mỗi ngày, theo dõi chỉ số nhớ từ. |
| 💬 **Gia Sư AI (AI Chat & Coach)** | Trò chuyện theo ngữ cảnh, hỏi đáp nghĩa từ, tự động sửa lỗi diễn đạt và ngữ pháp câu tiếng Anh. |
| 🎙️ **Luyện Nói & Chấm Điểm (Speaking & Voice Chat)** | Đàm thoại 2 chiều qua micro, phiên âm Whisper STT thời gian thực, AI chấm điểm và sửa lỗi phát âm/ngữ pháp chi tiết. |
| 📝 **Kiểm Tra & Đánh Giá (Quiz & Placement)** | Bài kiểm tra trình độ đầu vào (Placement Test), bài kiểm tra theo cấp độ, thống kê điểm số và biểu đồ tiến bộ. |
| 💳 **Gói Hội Viên & Thanh Toán (PayOS)** | Tích hợp cổng thanh toán VietQR, webhook xử lý giao dịch tự động, quản lý hạn mức sử dụng tính năng AI. |
| 🔐 **Bảo Mật & Phân Quyền (Security)** | Đăng ký & xác thực OTP qua Email, stateless JWT (Access/Refresh Token), phân quyền Role ADMIN / USER. |

---

## 🛠️ Công Nghệ Sử Dụng (Tech Stack)

### Backend
- **Core:** Java 21, Spring Boot 3.3.0
- **Security:** Spring Security, JWT (jjwt 0.12.5), BCrypt
- **Database:** MySQL 8.x, Hibernate / Spring Data JPA, Flyway Migration
- **Caching & Rate Limit:** Redis 6+, Spring Cache, Bucket4j
- **AI & Audio Integrations:** Google Gemini API (qua REST/HttpClient), OpenAI Whisper API, Edge/Grok TTS, Cloudinary CDN (Lưu trữ audio)
- **Payment Gateway:** PayOS SDK / REST API (VietQR)
- **Documentation:** SpringDoc OpenAPI 3 / Swagger UI

### Frontend
- **Framework:** React 18, Vite, TypeScript
- **Routing:** React Router v6
- **Styling:** Vanilla CSS Variables Design System (Đồng bộ Dark/Light tone, Glassmorphism, Micro-animations)
- **Icons & Assets:** Lucide React, Custom SVG Icons, 3D WebP Assets

---

## 📂 Cấu Trúc Thư Mục

```
B2C-VocabularyAI/
├── vocabulary-ai-backend/             # Mã nguồn Backend (Spring Boot 3)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/vocabularyai/
│   │   │   │   ├── ai/               # Gemini AI Chat, Prompts, Explain, Grammar
│   │   │   │   ├── auth/             # JWT Authentication, OTP, Security Config
│   │   │   │   ├── common/           # DTOs, Exceptions, Global Exception Handler
│   │   │   │   ├── learning/         # SRS Spaced Repetition, Progress Tracking
│   │   │   │   ├── payment/          # PayOS Payment Gateway, Orders, Webhook
│   │   │   │   ├── personalization/  # Roadmap, Diagnostic, Quota Management
│   │   │   │   ├── quiz/             # Quiz generation, Submission, Grading
│   │   │   │   ├── speaking/         # Voice Chat, Whisper STT, TTS, Audio Storage
│   │   │   │   ├── user/             # User Profile, Preferences, Admin Management
│   │   │   │   └── vocabulary/       # Word, Oxford B1-C1 Dictionary, Categories
│   │   │   └── resources/
│   │   │       ├── application.yml   # Cấu hình môi trường (Không lưu secret key)
│   │   │       └── db/migration/     # Flyway SQL migrations
│   │   └── test/                     # Unit tests & Integration tests
│   └── pom.xml
│
├── FE-Vocabulary-AI/                 # Mã nguồn Frontend (React 18 + Vite)
│   ├── src/
│   │   ├── assets/                   # Hình ảnh 3D, Mascot Vi, Audio icons
│   │   ├── auth/                     # AuthContext, ProtectedRoute, Token Storage
│   │   ├── components/               # Layout, Navbar, VoiceChatPanel, Charts, Modals
│   │   ├── pages/                    # Dashboard, Chat, Speaking, Quiz, WordDetail, Profile, LandingPage
│   │   ├── services/                 # Axios API Client & Service Modules
│   │   ├── App.tsx                   # Route Configuration
│   │   └── index.css                 # Global Design System CSS Tokens
│   ├── package.json
│   └── vite.config.ts
│
├── docker-compose.yml                # Docker Compose cho MySQL, Redis
└── README.md                         # Tài liệu dự án
```

---

## ⚙️ Yêu Cầu Hệ Thống & Hướng Dẫn Cài Đặt

### 1. Yêu cầu môi trường
- **Java:** OpenJDK 21 LTS
- **Node.js:** v18+ hoặc v20 LTS
- **Package Manager:** Maven 3.8+ và npm 9+
- **Database:** MySQL 8.0+ và Redis 6.0+

### 2. Cài đặt cơ sở dữ liệu
Khởi động nhanh MySQL và Redis bằng Docker Compose:
```bash
docker compose up -d
```
Hoặc cài đặt trực tiếp trên máy và tạo cơ sở dữ liệu:
```sql
CREATE DATABASE IF NOT EXISTS datn_2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## 🔑 Cấu Hình Biến Môi Trường (.env)

Tạo file `.env` tại thư mục gốc hoặc cấu hình biến môi trường trước khi chạy.
> ⚠️ **Lưu ý bảo mật:** Tuyệt đối không commit giá trị key thực tế vào Git. Sử dụng biến môi trường:

```env
# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=datn_2
DB_USERNAME=root
DB_PASSWORD=your_mysql_password_here

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT Secrets (Tối thiểu 256 bits cho thuật toán HS256)
JWT_SECRET=your_jwt_access_secret_key_minimum_256_bits_here
JWT_REFRESH_SECRET=your_jwt_refresh_secret_key_minimum_256_bits_here

# AI & Speech Services
AI_CHAT_URL=https://api.xah.io/v1/chat/completions
AI_CHAT_API_KEY=your_gemini_or_openai_api_key_here
AI_CHAT_MODEL=gemini-3.7-flash-high

WHISPER_URL=https://api.vilao.ai/v1/audio/transcriptions
WHISPER_API_KEY=your_whisper_api_key_here
WHISPER_MODEL=tsa/groq/whisper-large-v3

TTS_URL=https://api.xah.io/v1/audio/speech
TTS_API_KEY=your_tts_api_key_here
TTS_MODEL=namth/grok-voice-latest

# Cloudinary (Lưu trữ bản ghi âm luyện nói)
CLOUDINARY_URL=cloudinary://your_api_key:your_api_secret@your_cloud_name

# PayOS Payment Gateway
PAYOS_CLIENT_ID=your_payos_client_id_here
PAYOS_API_KEY=your_payos_api_key_here
PAYOS_CHECKSUM_KEY=your_payos_checksum_key_here
PAYOS_SANDBOX=true

# Mailtrap / SMTP (Gửi mã OTP xác thực)
MAIL_HOST=sandbox.smtp.mailtrap.io
MAIL_PORT=2525
MAIL_USERNAME=your_mail_username
MAIL_PASSWORD=your_mail_password
```

---

## 🏃 Chạy Ứng Dụng

### Khởi chạy Backend:
```bash
cd vocabulary-ai-backend
mvn clean spring-boot:run
```
Backend sẽ khởi động tại cổng mặc định: `http://localhost:8080/api`

### Khởi chạy Frontend:
```bash
cd FE-Vocabulary-AI
npm install
npm run dev
```
Giao diện người dùng sẽ chạy tại: `http://localhost:5173`

---

## 📡 Tài Liệu API & Swagger

Khi Backend đang chạy, bạn có thể kiểm tra và thử nghiệm các API endpoint trực tiếp:
- **Swagger UI Interactive:** [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html)
- **OpenAPI 3 JSON Specification:** [http://localhost:8080/api/v3/api-docs](http://localhost:8080/api/v3/api-docs)

### Danh mục Endpoint chính:
| Nhóm API | Base Path | Chức năng chính |
|---|---|---|
| **Auth** | `/api/auth/**` | Đăng ký OTP, Đăng nhập, Refresh Token, Đổi mật khẩu |
| **Vocabulary** | `/api/words/**` | Tra cứu từ vựng Oxford B1-C1, lọc danh mục, tìm kiếm autocomplete |
| **Learning** | `/api/learning/**` | SRS Spaced Repetition, đánh dấu ghi nhớ, lịch ôn từ hàng ngày |
| **Quiz** | `/api/quizzes/**` | Sinh đề quiz theo trình độ, nộp bài, chấm điểm tự động |
| **AI Services** | `/api/ai/**` | Giải thích từ ngữ cảnh, sửa lỗi ngữ pháp, gợi ý câu trả lời |
| **Speaking** | `/api/speaking/**` | Voice Chat 2 chiều thời gian thực, Whisper STT, AI chấm 5 tiêu chí |
| **Payments** | `/api/payments/**` | Tạo link thanh toán PayOS VietQR, xác thực đơn hàng, PayOS Webhook |
| **Personalization** | `/api/personalization/**` | Kiểm tra hạn mức sử dụng AI, lộ trình học cá nhân hóa |

---

## 🔒 Bảo Mật & Best Practices

1. **Khóa bảo vệ Endpoint thanh toán:** Endpoint xác thực thanh toán yêu cầu JWT authentication hợp lệ và kiểm tra quyền sở hữu đơn hàng (chỉ người tạo đơn hoặc ADMIN mới được phép xác thực kích hoạt gói VIP).
2. **Loại bỏ điểm số giả định (Anti-hallucination):** Trường hợp AI chấm điểm Voice Chat bị gián đoạn hoặc timeout, hệ thống phản hồi lỗi dịch vụ rõ ràng để người dùng ghi âm lại, không bịa bảng điểm mặc định.
3. **Cơ chế Secret Safety:** Tất cả API key, mật khẩu cơ sở dữ liệu và token bí mật được cấu hình hoàn toàn qua biến môi trường (Environment Variables) và file cấu hình mẫu không lưu secret thực tế.

---

## 👥 Tác Giả & Bản Quyền
Dự án được xây dựng và phát triển phục vụ mục đích nghiên cứu và học tập.
- **Tác giả:** Đội ngũ phát triển VocabularyAI
- **Bản quyền:** © 2026 VocabularyAI. All rights reserved.
