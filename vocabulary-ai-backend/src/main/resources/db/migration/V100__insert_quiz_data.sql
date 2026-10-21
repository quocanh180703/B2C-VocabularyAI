-- ============================================================
-- VocabularyAI - Sample Quiz Data Migration
-- V100__insert_quiz_data.sql
-- Tables: quizzes, questions, quiz_attempts, quiz_answers
-- ============================================================

-- ============================================================
-- 1. INSERT QUIZZES
-- ============================================================
INSERT INTO quizzes (id, category_id, title, description, level, time_limit, status, created_at, updated_at)
VALUES 
(1, 1, 'Business English Essentials', 'Kiểm tra vốn từ vựng tiếng Anh thương mại thường dùng trong môi trường doanh nghiệp: khởi nghiệp, đàm phán, tài chính và quản lý thị phần.', 'B1', 10, 'PUBLISHED', NOW() - INTERVAL 10 DAY, NOW()),
(2, 2, 'Information Technology & Computing', 'Bộ câu hỏi kiểm tra từ vựng chuyên ngành công nghệ thông tin, thuật toán, cơ sở dữ liệu và bảo mật số.', 'B2', 12, 'PUBLISHED', NOW() - INTERVAL 8 DAY, NOW()),
(3, 3, 'Travel & Tourism Vocabulary', 'Luyện tập từ vựng chủ đề du lịch, xuất nhập cảnh, khách sạn, lịch trình và các trải nghiệm khám phá thế giới.', 'A2', 10, 'PUBLISHED', NOW() - INTERVAL 6 DAY, NOW()),
(4, 4, 'Health & Medical Terminology', 'Củng cố từ vựng về sức khỏe, y tế, triệu chứng thường gặp, chẩn đoán và đơn thuốc.', 'B1', 10, 'PUBLISHED', NOW() - INTERVAL 5 DAY, NOW()),
(5, 5, 'Food & Culinary Arts', 'Bộ câu hỏi thú vị về thế giới ẩm thực, phương pháp nấu ăn, hương vị và các nguyên liệu quen thuộc.', 'A2', 10, 'PUBLISHED', NOW() - INTERVAL 4 DAY, NOW()),
(6, NULL, 'Daily Communication & Basic Words', 'Bài kiểm tra các từ vựng tiếng Anh căn bản nhất dành cho giao tiếp hàng ngày và người mới bắt đầu.', 'A1', 8, 'PUBLISHED', NOW() - INTERVAL 2 DAY, NOW())
ON DUPLICATE KEY UPDATE 
    category_id = VALUES(category_id),
    title = VALUES(title),
    description = VALUES(description),
    level = VALUES(level),
    time_limit = VALUES(time_limit),
    status = VALUES(status),
    updated_at = NOW();

-- ============================================================
-- 2. INSERT QUESTIONS (30 questions, 5 questions per quiz)
-- ============================================================

-- Quiz 1: Business English Essentials (id=1, level B1)
INSERT INTO questions (id, quiz_id, content, options, correct_answer, explanation, created_at)
VALUES
(1, 1, 'A person who sets up a business or businesses, taking on financial risks in the hope of profit is called an ______.', 
 JSON_ARRAY('employee', 'entrepreneur', 'accountant', 'intern'), 
 1, 
 '''Entrepreneur'' nghĩa là doanh nhân/người khởi nghiệp, người chấp nhận rủi ro tài chính để thành lập và vận hành doanh nghiệp.', 
 NOW() - INTERVAL 10 DAY),

(2, 1, 'The total amount of money brought in by a company\'s operations, measured over a set amount of time, is known as ______.', 
 JSON_ARRAY('revenue', 'debt', 'expense', 'loss'), 
 0, 
 '''Revenue'' là tổng doanh thu kiếm được từ hoạt động kinh doanh trước khi trừ đi các chi phí hoạt động.', 
 NOW() - INTERVAL 10 DAY),

(3, 1, 'After two weeks of intense ______, both companies finally reached a mutual agreement on the merger.', 
 JSON_ARRAY('vacation', 'confusion', 'negotiation', 'hesitation'), 
 2, 
 '''Negotiation'' có nghĩa là cuộc đàm phán/thương lượng nhằm đạt được thỏa thuận chung giữa các bên.', 
 NOW() - INTERVAL 10 DAY),

(4, 1, 'Our primary objective this quarter is to expand our market ______ in Southeast Asia.', 
 JSON_ARRAY('share', 'loss', 'problem', 'crisis'), 
 0, 
 '''Market share'' là thị phần, tỷ lệ phần trăm thị trường mà doanh nghiệp chiếm lĩnh.', 
 NOW() - INTERVAL 10 DAY),

(5, 1, 'The board of directors approved the annual ______ for marketing and product development.', 
 JSON_ARRAY('budget', 'receipt', 'penalty', 'debt'), 
 0, 
 '''Budget'' là ngân sách, kế hoạch tài chính phân bổ nguồn vốn cho các hoạt động cụ thể của doanh nghiệp.', 
 NOW() - INTERVAL 10 DAY),

-- Quiz 2: Information Technology & Computing (id=2, level B2)
(6, 2, 'A well-defined step-by-step procedure or set of rules to be followed in calculations or problem-solving is an ______.', 
 JSON_ARRAY('algorithm', 'hardware', 'analog', 'interface'), 
 0, 
 '''Algorithm'' (thuật toán) là tập hợp các chỉ thị rõ ràng từng bước để giải quyết một bài toán hoặc thực hiện tính toán.', 
 NOW() - INTERVAL 8 DAY),

(7, 2, 'An organized collection of data that is electronically stored and accessed from a computer system is called a ______.', 
 JSON_ARRAY('spreadsheet', 'database', 'motherboard', 'firewall'), 
 1, 
 '''Database'' (cơ sở dữ liệu) là hệ thống lưu trữ dữ liệu có cấu trúc cho phép truy xuất, cập nhật và quản lý hiệu quả.', 
 NOW() - INTERVAL 8 DAY),

(8, 2, 'Organizations invest millions into ______ to protect their sensitive data and network infrastructure from malicious attacks.', 
 JSON_ARRAY('social media', 'cybersecurity', 'graphic design', 'entertainment'), 
 1, 
 '''Cybersecurity'' là an ninh mạng/bảo mật thông tin số chống lại các cuộc tấn công mạng nguy hiểm.', 
 NOW() - INTERVAL 8 DAY),

(9, 2, 'The development team is working hard to fix several software ______ before the official product launch.', 
 JSON_ARRAY('features', 'bugs', 'documents', 'servers'), 
 1, 
 '''Bug'' trong ngành lập trình là lỗi phần mềm khiến chương trình chạy sai hoặc bị dừng đột ngột.', 
 NOW() - INTERVAL 8 DAY),

(10, 2, 'The process of converting plain text into unreadable code to prevent unauthorized access is known as ______.', 
 JSON_ARRAY('compression', 'deletion', 'encryption', 'download'), 
 2, 
 '''Encryption'' (mã hóa) là quá trình chuyển đổi thông tin sang dạng mật mã để bảo vệ tính bảo mật của dữ liệu.', 
 NOW() - INTERVAL 8 DAY),

-- Quiz 3: Travel & Tourism Vocabulary (id=3, level A2)
(11, 3, 'Before traveling abroad, make sure your passport and tourist ______ are still valid.', 
 JSON_ARRAY('menu', 'visa', 'receipt', 'ticket'), 
 1, 
 '''Visa'' (thị thực) là giấy phép hợp pháp do chính phủ cấp cho phép người nước ngoài nhập cảnh vào quốc gia.', 
 NOW() - INTERVAL 6 DAY),

(12, 3, 'We received a detailed tour ______ outlining each day\'s planned activities and sightseeing spots.', 
 JSON_ARRAY('itinerary', 'bill', 'luggage', 'brochure'), 
 0, 
 '''Itinerary'' là lịch trình chi tiết của một chuyến đi, liệt kê các điểm đến và thời gian cụ thể.', 
 NOW() - INTERVAL 6 DAY),

(13, 3, 'The resort offers luxury ______ with ocean views, complimentary breakfast, and free high-speed Wi-Fi.', 
 JSON_ARRAY('transportation', 'flight', 'accommodation', 'souvenir'), 
 2, 
 '''Accommodation'' là chỗ ở, nơi lưu trú cho du khách như khách sạn, nhà nghỉ, resort.', 
 NOW() - INTERVAL 6 DAY),

(14, 3, 'Passengers must pass through airport security and ______ before collecting their checked luggage.', 
 JSON_ARRAY('customs', 'kitchen', 'supermarket', 'parking'), 
 0, 
 '''Customs'' (hải quan) là cơ quan kiểm soát hàng hóa và hành lý khi xuất nhập cảnh tại sân bay hoặc cửa khẩu.', 
 NOW() - INTERVAL 6 DAY),

(15, 3, 'I bought a small hand-crafted keychain as a ______ from my memorable trip to Kyoto.', 
 JSON_ARRAY('penalty', 'passport', 'souvenir', 'luggage'), 
 2, 
 '''Souvenir'' (quà lưu niệm) là vật phẩm mua để ghi nhớ một chuyến đi hay nơi chốn đã ghé thăm.', 
 NOW() - INTERVAL 6 DAY),

-- Quiz 4: Health & Medical Terminology (id=4, level B1)
(16, 4, 'High fever, persistent dry cough, and extreme fatigue are common ______ of viral respiratory infections.', 
 JSON_ARRAY('cures', 'treatments', 'symptoms', 'medicines'), 
 2, 
 '''Symptom'' (triệu chứng) là biểu hiện lâm sàng hoặc cảm giác bất thường báo hiệu tình trạng bệnh tật.', 
 NOW() - INTERVAL 5 DAY),

(17, 4, 'After reviewing the blood test results, the specialist confirmed the patient\'s medical ______.', 
 JSON_ARRAY('diagnosis', 'appointment', 'prescription', 'allergy'), 
 0, 
 '''Diagnosis'' (chẩn đoán) là việc xác định tên và nguyên nhân gây bệnh dựa trên triệu chứng và xét nghiệm lâm sàng.', 
 NOW() - INTERVAL 5 DAY),

(18, 4, 'Annual flu ______ helps protect vulnerable populations, especially seniors and young children.', 
 JSON_ARRAY('operation', 'vaccination', 'injury', 'infection'), 
 1, 
 '''Vaccination'' (tiêm chủng/tiêm phòng vắc-xin) kích thích hệ miễn dịch tạo kháng thể bảo vệ cơ thể.', 
 NOW() - INTERVAL 5 DAY),

(19, 4, 'You need a valid doctor\'s ______ to purchase this specific antibiotic medication at the pharmacy.', 
 JSON_ARRAY('receipt', 'license', 'prescription', 'invoice'), 
 2, 
 '''Prescription'' là đơn thuốc/toa thuốc do bác sĩ kê để mua các loại thuốc điều trị theo chỉ định.', 
 NOW() - INTERVAL 5 DAY),

(20, 4, 'Regular aerobic exercise and a balanced diet significantly lower the risk of cardiovascular ______.', 
 JSON_ARRAY('diseases', 'vitamins', 'injuries', 'surgeries'), 
 0, 
 '''Diseases'' là bệnh tật/bệnh lý. Cardiovascular diseases là bệnh lý tim mạch.', 
 NOW() - INTERVAL 5 DAY),

-- Quiz 5: Food & Culinary Arts (id=5, level A2)
(21, 5, 'Follow the traditional baking ______ carefully to ensure the sponge cake rises properly in the oven.', 
 JSON_ARRAY('recipe', 'menu', 'bill', 'order'), 
 0, 
 '''Recipe'' (công thức nấu ăn) là tập hợp hướng dẫn và danh sách nguyên liệu để chế biến một món ăn hoàn chỉnh.', 
 NOW() - INTERVAL 4 DAY),

(22, 5, 'Fresh basil, garlic, parmesan cheese, and olive oil are the essential ______ for authentic pesto sauce.', 
 JSON_ARRAY('flavors', 'utensils', 'ingredients', 'spices'), 
 2, 
 '''Ingredients'' (nguyên liệu) là các thành phần thực phẩm được kết hợp lại để nấu thành món ăn.', 
 NOW() - INTERVAL 4 DAY),

(23, 5, 'Vietnamese ______ is celebrated worldwide for its fresh herbs, fragrant broths, and balanced flavors.', 
 JSON_ARRAY('appetite', 'cuisine', 'utensil', 'nutrition'), 
 1, 
 '''Cuisine'' (nền ẩm thực/phong cách ẩm thực) đặc trưng của một quốc gia, dân tộc hoặc vùng miền.', 
 NOW() - INTERVAL 4 DAY),

(24, 5, 'Would you like to see the dessert ______ after you finish your main course?', 
 JSON_ARRAY('ingredient', 'menu', 'recipe', 'kitchen'), 
 1, 
 '''Menu'' (thực đơn) là danh sách các món ăn và thức uống có sẵn tại nhà hàng kèm giá cả.', 
 NOW() - INTERVAL 4 DAY),

(25, 5, 'This traditional curry has a wonderfully rich and ______ flavor due to the blend of roasted spices and chili peppers.', 
 JSON_ARRAY('bland', 'spicy', 'raw', 'frozen'), 
 1, 
 '''Spicy'' (cay nồng/đậm vị gia vị) mô tả món ăn thơm mùi gia vị hoặc có vị cay hấp dẫn.', 
 NOW() - INTERVAL 4 DAY),

-- Quiz 6: Daily Communication & Basic Words (id=6, level A1)
(26, 6, 'When you meet someone for the first time in the morning, the polite greeting is ''Good ______!''', 
 JSON_ARRAY('night', 'morning', 'bye', 'luck'), 
 1, 
 '''Good morning'' là lời chào lịch sự phổ biến nhất vào buổi sáng khi gặp gỡ ai đó.', 
 NOW() - INTERVAL 2 DAY),

(27, 6, 'My mother\'s brother is my ______.', 
 JSON_ARRAY('aunt', 'cousin', 'uncle', 'nephew'), 
 2, 
 '''Uncle'' nghĩa là chú, bác hoặc cậu (anh/em trai của bố hoặc mẹ).', 
 NOW() - INTERVAL 2 DAY),

(28, 6, 'I usually have a cup of warm coffee and toast for ______ at 7:00 AM.', 
 JSON_ARRAY('dinner', 'lunch', 'breakfast', 'supper'), 
 2, 
 '''Breakfast'' là bữa ăn sáng, bữa ăn đầu tiên trong ngày sau khi thức dậy.', 
 NOW() - INTERVAL 2 DAY),

(29, 6, 'Can you please speak more slowly? I am still ______ English.', 
 JSON_ARRAY('teaching', 'learning', 'selling', 'forgetting'), 
 1, 
 '''Learning'' là đang học (thì hiện tại tiếp diễn của động từ ''learn'').', 
 NOW() - INTERVAL 2 DAY),

(30, 6, 'Today is Monday, so tomorrow will be ______.', 
 JSON_ARRAY('Tuesday', 'Wednesday', 'Friday', 'Sunday'), 
 0, 
 '''Thứ hai là Monday, ngày tiếp theo là thứ ba (Tuesday).''', 
 NOW() - INTERVAL 2 DAY)
ON DUPLICATE KEY UPDATE 
    quiz_id = VALUES(quiz_id),
    content = VALUES(content),
    options = VALUES(options),
    correct_answer = VALUES(correct_answer),
    explanation = VALUES(explanation);

-- ============================================================
-- 3. INSERT QUIZ ATTEMPTS
-- ============================================================
INSERT INTO quiz_attempts (id, user_id, quiz_id, score, total_questions, correct_answers, started_at, submitted_at, created_at)
VALUES 
(1, 2, 1, 80.00, 5, 4, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY + INTERVAL 420 SECOND, NOW() - INTERVAL 3 DAY),
(2, 2, 3, 100.00, 5, 5, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY + INTERVAL 320 SECOND, NOW() - INTERVAL 2 DAY),
(3, 2, 2, 60.00, 5, 3, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY + INTERVAL 555 SECOND, NOW() - INTERVAL 1 DAY),
(4, 1, 1, 100.00, 5, 5, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY + INTERVAL 250 SECOND, NOW() - INTERVAL 4 DAY),
(5, 1, 4, 80.00, 5, 4, NOW() - INTERVAL 36 HOUR, NOW() - INTERVAL 36 HOUR + INTERVAL 360 SECOND, NOW() - INTERVAL 36 HOUR),
(6, 2, 5, 0.00, 5, 0, NOW() - INTERVAL 5 MINUTE, NULL, NOW() - INTERVAL 5 MINUTE)
ON DUPLICATE KEY UPDATE 
    user_id = VALUES(user_id),
    quiz_id = VALUES(quiz_id),
    score = VALUES(score),
    total_questions = VALUES(total_questions),
    correct_answers = VALUES(correct_answers),
    started_at = VALUES(started_at),
    submitted_at = VALUES(submitted_at);

-- ============================================================
-- 4. INSERT QUIZ ANSWERS
-- ============================================================
INSERT INTO quiz_answers (id, attempt_id, question_id, answer, correct, created_at)
VALUES 
(1, 1, 1, 1, 1, NOW() - INTERVAL 3 DAY + INTERVAL 60 SECOND),
(2, 1, 2, 0, 1, NOW() - INTERVAL 3 DAY + INTERVAL 120 SECOND),
(3, 1, 3, 2, 1, NOW() - INTERVAL 3 DAY + INTERVAL 210 SECOND),
(4, 1, 4, 1, 0, NOW() - INTERVAL 3 DAY + INTERVAL 300 SECOND),
(5, 1, 5, 0, 1, NOW() - INTERVAL 3 DAY + INTERVAL 410 SECOND),

(6, 2, 11, 1, 1, NOW() - INTERVAL 2 DAY + INTERVAL 60 SECOND),
(7, 2, 12, 0, 1, NOW() - INTERVAL 2 DAY + INTERVAL 120 SECOND),
(8, 2, 13, 2, 1, NOW() - INTERVAL 2 DAY + INTERVAL 180 SECOND),
(9, 2, 14, 0, 1, NOW() - INTERVAL 2 DAY + INTERVAL 250 SECOND),
(10, 2, 15, 2, 1, NOW() - INTERVAL 2 DAY + INTERVAL 310 SECOND),

(11, 3, 6, 0, 1, NOW() - INTERVAL 1 DAY + INTERVAL 90 SECOND),
(12, 3, 7, 1, 1, NOW() - INTERVAL 1 DAY + INTERVAL 180 SECOND),
(13, 3, 8, 1, 1, NOW() - INTERVAL 1 DAY + INTERVAL 300 SECOND),
(14, 3, 9, 0, 0, NOW() - INTERVAL 1 DAY + INTERVAL 420 SECOND),
(15, 3, 10, 0, 0, NOW() - INTERVAL 1 DAY + INTERVAL 540 SECOND),

(16, 4, 1, 1, 1, NOW() - INTERVAL 4 DAY + INTERVAL 45 SECOND),
(17, 4, 2, 0, 1, NOW() - INTERVAL 4 DAY + INTERVAL 90 SECOND),
(18, 4, 3, 2, 1, NOW() - INTERVAL 4 DAY + INTERVAL 140 SECOND),
(19, 4, 4, 0, 1, NOW() - INTERVAL 4 DAY + INTERVAL 195 SECOND),
(20, 4, 5, 0, 1, NOW() - INTERVAL 4 DAY + INTERVAL 245 SECOND),

(21, 5, 16, 2, 1, NOW() - INTERVAL 36 HOUR + INTERVAL 60 SECOND),
(22, 5, 17, 0, 1, NOW() - INTERVAL 36 HOUR + INTERVAL 135 SECOND),
(23, 5, 18, 1, 1, NOW() - INTERVAL 36 HOUR + INTERVAL 220 SECOND),
(24, 5, 19, 0, 0, NOW() - INTERVAL 36 HOUR + INTERVAL 300 SECOND),
(25, 5, 20, 0, 1, NOW() - INTERVAL 36 HOUR + INTERVAL 350 SECOND)
ON DUPLICATE KEY UPDATE 
    answer = VALUES(answer),
    correct = VALUES(correct);
