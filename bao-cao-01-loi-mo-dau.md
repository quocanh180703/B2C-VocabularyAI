# LỜI MỞ ĐẦU

## 1. Tính chất thực tiễn của đề tài

VocabularyAI là một nền tảng học tiếng Anh trực tuyến được xây dựng với mục đích cải thiện kỹ năng tiếng Anh thông qua các công nghệ AI hiện đại. Đề tài này phản ánh xu hướng toàn cầu về:

- **Chuyển đổi số trong giáo dục**: Từ phương pháp học truyền thống sang các giải pháp e-learning tích hợp AI
- **Nhu cầu cá nhân hóa học tập**: Phần lớn người dùng hiện nay mong muốn có các khóa học được tối ưu hóa theo khả năng riêng của họ
- **Sự phát triển của các mô hình ngôn ngữ lớn (LLM)**: Như Google Gemini, OpenAI GPT, giúp cải thiện chất lượng giải thích và hỗ trợ học tập

Hệ thống VocabularyAI được phát triển để giải quyết các vấn đề thực tiễn:
- Giáo viên không đủ nguồn lực để hỗ trợ học sinh cá nhân
- Học viên không có thời gian để học theo lịch trình cố định
- Thiếu phương pháp lặp lại hiệu quả (spaced repetition) để ghi nhớ lâu dài

## 2. Tính cấp thiết của đề tài

Theo thống kê từ các nền tảng học tiếng Anh:
- **Duolingo** có hơn 500 triệu người dùng toàn cầu
- **Babbel** phục vụ hơn 10 triệu người học mỗi tháng
- **Quizlet** cung cấp công cụ học tập cho 60 triệu người dùng

Tuy nhiên, các nền tảng này chưa tối ưu hóa hoàn toàn:
- Lặp lại không linh hoạt theo từng người học
- Phản hồi từ AI chưa được cá nhân hóa sâu
- Thiếu tích hợp giữa học từ vựng, ngữ pháp và kỹ năng nói

VocabularyAI được xây dựng để khắc phục các hạn chế này bằng cách:
- Sử dụng thuật toán Spaced Repetition cải tiến
- Tích hợp Google Gemini API để cung cấp giải thích AI
- Tích hợp Google Whisper API để nhận diện và đánh giá kỹ năng nói tiếng Anh
- Cá nhân hóa hoàn toàn lộ trình học tập dựa trên tiến độ của từng người học

## 3. Mục tiêu của đề tài

Đề tài này hướng tới 7 mục tiêu chính:

**Mục tiêu 1: Phát triển hệ thống quản lý từ vựng thông minh**
- Tạo cơ sở dữ liệu từ vựng phong phú (từ TOEIC, IELTS, A1-C2)
- Xây dựng công cụ tìm kiếm và lọc từ vựng nâng cao

**Mục tiêu 2: Xây dựng thuật toán học tập cá nhân hóa**
- Triển khai thuật toán Spaced Repetition với trọng số cá nhân hóa
- Tạo bộ nhớ người học thích ứng dựa trên tần suất lỗi

**Mục tiêu 3: Tích hợp AI để hỗ trợ học tập**
- Sử dụng Google Gemini API để giải thích từ vựng, ví dụ và ngữ pháp
- Tạo các bài kiểm tra AI-generated với mức độ khó tùy chỉnh

**Mục tiêu 4: Phát triển kỹ năng nói tiếng Anh**
- Tích hợp Google Whisper API để ghi âm và nhận diện giọng nói
- Xây dựng tính năng đánh giá độ chính xác phát âm

**Mục tiêu 5: Cung cấp hệ thống quiz tương tác**
- Xây dựng các bộ quiz cho từng chủ đề
- Tạo hệ thống đánh giá và trả lời chi tiết

**Mục tiêu 6: Phát triển tính năng admin và phân tích**
- Xây dựng bảng điều khiển quản trị hệ thống
- Tạo các báo cáo thống kê chi tiết về tiến độ học tập của người dùng

**Mục tiêu 7: Đảm bảo bảo mật và hiệu suất hệ thống**
- Xây dựng hệ thống xác thực JWT an toàn
- Tối ưu hóa hiệu suất với Redis caching

## 4. Phương pháp thực hiện

Để hoàn thành đề tài, tôi sử dụng các phương pháp sau:

### 4.1. Phương pháp nghiên cứu tài liệu
- Tìm hiểu các nền tảng học tiếng Anh hiện có (Duolingo, Quizlet, Babbel)
- Nghiên cứu các công nghệ AI trong giáo dục (LLM, nhận diện giọng nói)
- Tìm hiểu về thuật toán Spaced Repetition và các biến thể

### 4.2. Phương pháp phát triển phần mềm
- Sử dụng mô hình **Agile/Scrum** để phát triển từng tính năng
- Thực hiện unit testing, integration testing cho từng module
- Áp dụng các quy tắc clean code và SOLID principles

### 4.3. Phương pháp đánh giá
- Kiểm thử tính năng (functional testing) với các test cases cụ thể
- Kiểm thử hiệu suất (performance testing) với nhiều người dùng giả lập
- Kiểm thử bảo mật (security testing) cho các API nhạy cảm

## 5. Nội dung báo cáo

Báo cáo này gồm các phần chính:

**Chương 1: Cơ sở lý thuyết**
- Phát biểu bài toán: Các thách thức hiện tại trong học tiếng Anh
- So sánh các giải pháp hiện có (Duolingo, Quizlet, Babbel)
- Cơ sở lý thuyết: Spaced Repetition, AI trong giáo dục, Personalization
- Các công nghệ liên quan

**Chương 2: Phương pháp thiết kế**
- Yêu cầu chức năng (Functional Requirements)
- Yêu cầu phi chức năng (Non-Functional Requirements)
- Kiến trúc hệ thống (Architecture)
- Thiết kế cơ sở dữ liệu (Database Design)
- Lựa chọn công nghệ (Technology Selection)
- Mô tả các thuật toán

**Chương 3: Triển khai và đánh giá**
- Môi trường triển khai
- Chi tiết triển khai các tính năng chính
- Tích hợp API AI (Gemini, Whisper)
- Kết quả kiểm thử
- Đánh giá đạt được mục tiêu

**Kết luận**
- Tổng kết mục tiêu hoàn thành
- Kiến thức lý thuyết đạt được
- Kỹ năng thực tiễn phát triển
- Các vấn đề chưa giải quyết
- Hướng phát triển trong tương lai

## 6. Phạm vi của đề tài

### 6.1. Phạm vi về chức năng
Báo cáo tập trung vào các tính năng chính của hệ thống:
- Quản lý từ vựng (create, read, update, delete)
- Lộ trình học tập cá nhân hóa
- Kiểm tra qua quiz
- Kỹ năng nói (speaking practice)
- Hệ thống xác thực người dùng
- Bảng điều khiển quản trị

### 6.2. Phạm vi về công nghệ
- Backend: Spring Boot 3.3.0 với Java 21
- Frontend: React 19.2.8 với TypeScript
- Cơ sở dữ liệu: MySQL 8.x
- Cache: Redis 6+
- APIs ngoài: Google Gemini, Google Whisper, Cloudinary

### 6.3. Phạm vi về số lượng người dùng
- Hỗ trợ từ 100 đến 10,000 người dùng đồng thời
- Xử lý được hàng triệu bản ghi từ vựng, quiz, learning history

### 6.4. Những tính năng không được đề cập
- Mobile app (chỉ web)
- Offline learning
- Social networking features
- Advanced analytics (real-time dashboards)
- Internationalization (i18n) cho các ngôn ngữ khác

## 7. Các đơn vị chuyên ngành liên quan

Báo cáo này liên quan đến các lĩnh vực sau:

1. **Công nghệ thông tin (IT/Software Engineering)**
   - Phát triển web application
   - Thiết kế cơ sở dữ liệu
   - API development
   - Bảo mật hệ thống

2. **Trí tuệ nhân tạo (AI/Machine Learning)**
   - Tích hợp LLM (Google Gemini)
   - Nhận diện giọng nói (Speech Recognition)
   - Thuật toán personalization

3. **Giáo dục (Education Technology)**
   - Phương pháp dạy học hiệu quả
   - Spaced Repetition
   - Learning path design
   - Adaptive learning

4. **Người dùng cuối (User Experience)**
   - Thiết kế giao diện (UI/UX)
   - User interface responsiveness
   - Accessibility

## 8. Cấu trúc của báo cáo

Báo cáo gồm các phần sau:

```
Lời Mở Đầu
├── Tính chất thực tiễn
├── Tính cấp thiết
├── Mục tiêu (7 mục tiêu chính)
├── Phương pháp thực hiện
├── Nội dung báo cáo
├── Phạm vi của đề tài
├── Các đơn vị chuyên ngành liên quan
└── Cấu trúc của báo cáo

Chương 1: Cơ Sở Lý Thuyết
├── 1.1 Phát biểu bài toán
├── 1.2 So sánh các giải pháp hiện có
├── 1.3 Cơ sở lý thuyết - Spaced Repetition
├── 1.4 Cơ sở lý thuyết - AI trong giáo dục
├── 1.5 Cơ sở lý thuyết - Personalization
├── 1.6 Các công nghệ liên quan
└── 1.7 Tóm tắt chương

Chương 2: Phương Pháp Thiết Kế
├── 2.1 Phương pháp thiết kế hệ thống
├── 2.2 Yêu cầu chức năng
├── 2.3 Yêu cầu phi chức năng
├── 2.4 Kiến trúc hệ thống
├── 2.5 Thiết kế cơ sở dữ liệu
├── 2.6 Lựa chọn công nghệ
├── 2.7 Mô tả các thuật toán
└── 2.8 Tóm tắt chương

Chương 3: Triển Khai và Đánh Giá
├── 3.1 Môi trường triển khai
├── 3.2 Chi tiết triển khai backend modules
├── 3.3 Chi tiết triển khai frontend pages
├── 3.4 Tích hợp APIs AI
├── 3.5 Giải quyết các thách thức
├── 3.6 Kết quả kiểm thử
├── 3.7 Đánh giá đạt được mục tiêu
├── 3.8 Các vấn đề chưa giải quyết
└── 3.9 Hướng phát triển trong tương lai

Kết Luận
├── Tổng kết mục tiêu hoàn thành
├── Kiến thức lý thuyết đạt được
├── Kỹ năng thực tiễn phát triển
├── Những điểm chưa hoàn thành
├── Bài học rút ra từ dự án
├── Hướng phát triển trong tương lai
└── Lời kết
```

---

**Cuối Lời Mở Đầu**

Báo cáo này trình bày quá trình phát triển VocabularyAI - một nền tảng học tiếng Anh thông minh sử dụng AI. Thông qua 7 mục tiêu chính, hệ thống được thiết kế để cung cấp trải nghiệm học tập được cá nhân hóa, hiệu quả và hấp dẫn cho người học tiếng Anh ở tất cả các cấp độ. Các chương tiếp theo sẽ trình bày chi tiết cơ sở lý thuyết, phương pháp thiết kế, quá trình triển khai và kết quả đánh giá của hệ thống.
