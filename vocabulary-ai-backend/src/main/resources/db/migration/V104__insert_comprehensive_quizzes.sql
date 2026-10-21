-- ============================================================
-- VocabularyAI - Comprehensive Quiz Data Migration (A1 - C1)
-- V104__insert_comprehensive_quizzes.sql
-- Tables: categories, quizzes, questions
-- ============================================================

USE datn_2;
SET NAMES utf8mb4;

-- ============================================================
-- 1. INSERT CATEGORIES
-- ============================================================
INSERT INTO categories (id, name, description, created_at, updated_at)
VALUES 
(1, 'Business', 'Vocabulary related to business, commerce, finance, and marketing', NOW(), NOW()),
(2, 'Technology', 'Technology, computing, data, cybersecurity, and artificial intelligence', NOW(), NOW()),
(3, 'Travel', 'Travel, transportation, international tourism, and accommodations', NOW(), NOW()),
(4, 'Health', 'Medical terminology, public health, wellness, and biology', NOW(), NOW()),
(5, 'Food', 'Culinary arts, ingredients, cooking techniques, and flavors', NOW(), NOW()),
(6, 'Education', 'Academic study, scientific research, learning skills, and university life', NOW(), NOW()),
(7, 'Environment', 'Ecology, biodiversity, climate change, and sustainability', NOW(), NOW()),
(8, 'Culture & Society', 'Arts, entertainment, sociology, psychology, and human behavior', NOW(), NOW()),
(9, 'Everyday Life', 'Daily communication, family, home, shopping, and routines', NOW(), NOW()),
(10, 'Law & Philosophy', 'Jurisprudence, governance, ethics, logic, and critical thought', NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    description = VALUES(description),
    updated_at = NOW();

-- ============================================================
-- 2. INSERT QUIZZES (IDs 7 to 24)
-- ============================================================
INSERT INTO quizzes (id, category_id, title, description, level, time_limit, status, created_at, updated_at)
VALUES 
-- Level A1
(7, 9, 'Family, Home & Daily Living', 'Kiểm tra các từ vựng căn bản về các thành viên gia đình, đồ đạc trong nhà và sinh hoạt mỗi ngày.', 'A1', 8, 'PUBLISHED', NOW() - INTERVAL 12 DAY, NOW()),
(8, 9, 'Numbers, Calendar, Days & Weather', 'Củng cố cách gọi thứ trong tuần, tháng, các mùa và hiện tượng thời tiết quen thuộc.', 'A1', 8, 'PUBLISHED', NOW() - INTERVAL 11 DAY, NOW()),
(9, 9, 'Colors, Clothes & Everyday Objects', 'Bộ từ vựng trực quan về trang phục, màu sắc và các đồ vật cá nhân thông dụng.', 'A1', 8, 'PUBLISHED', NOW() - INTERVAL 10 DAY, NOW()),
(10, 6, 'School Supplies & Basic Classroom', 'Từ vựng gần gũi trong lớp học: dụng cụ học tập, bàn ghế, sách vở và giáo viên.', 'A1', 8, 'PUBLISHED', NOW() - INTERVAL 9 DAY, NOW()),

-- Level A2
(11, 1, 'Shopping, Prices & Customer Service', 'Giao tiếp khi mua sắm: hỏi giá, giảm giá, phương thức thanh toán và hóa đơn bán hàng.', 'A2', 10, 'PUBLISHED', NOW() - INTERVAL 8 DAY, NOW()),
(12, 8, 'Hobbies, Sports & Active Lifestyle', 'Từ vựng phong phú về thể thao, sở thích cá nhân, năng lượng và rèn luyện thể chất.', 'A2', 10, 'PUBLISHED', NOW() - INTERVAL 7 DAY, NOW()),
(13, 1, 'Jobs, Professions & Workplace Essentials', 'Tìm hiểu về các ngành nghề, nơi làm việc, sơ yếu lý lịch và chế độ lương thưởng.', 'A2', 10, 'PUBLISHED', NOW() - INTERVAL 6 DAY, NOW()),

-- Level B1
(14, 6, 'Education, Academic Life & Studying Skills', 'Từ vựng học đường, phương pháp học tập hiệu quả, học bổng và môi trường đại học.', 'B1', 10, 'PUBLISHED', NOW() - INTERVAL 5 DAY, NOW()),
(15, 8, 'Entertainment, Media & Digital Social Life', 'Chủ đề phim ảnh, mạng xã hội, phương tiện truyền thông và hoạt động văn hóa giải trí.', 'B1', 10, 'PUBLISHED', NOW() - INTERVAL 4 DAY, NOW()),
(16, 7, 'Environment, Climate & Natural Phenomena', 'Từ vựng về hiện tượng thời tiết cực đoan, tái chế rác thải và bảo vệ môi trường sống.', 'B1', 10, 'PUBLISHED', NOW() - INTERVAL 3 DAY, NOW()),

-- Level B2
(17, 1, 'Corporate Strategy, Marketing & Brand Value', 'Từ vựng phân tích kinh doanh, vốn đầu tư mạo hiểm, chiến dịch quảng cáo và lòng trung thành thương hiệu.', 'B2', 12, 'PUBLISHED', NOW() - INTERVAL 2 DAY, NOW()),
(18, 7, 'Sustainability, Biodiversity & Conservation', 'Chủ đề bảo tồn đa dạng sinh học, suy giảm tài nguyên, dấu chân carbon và năng lượng tái tạo.', 'B2', 12, 'PUBLISHED', NOW() - INTERVAL 36 HOUR, NOW()),
(19, 8, 'Psychology, Cognition & Human Behavior', 'Từ vựng chuyên sâu về trí tuệ cảm xúc, động lực nội tại, hội chứng kiệt sức và tâm lý học nhận thức.', 'B2', 12, 'PUBLISHED', NOW() - INTERVAL 28 HOUR, NOW()),
(20, 2, 'Science, Innovation & Space Exploration', 'Khám phá thuật ngữ khoa học vũ trụ, chỉnh sửa gen, đột phá công nghệ và nghiên cứu thực nghiệm.', 'B2', 12, 'PUBLISHED', NOW() - INTERVAL 20 HOUR, NOW()),

-- Level C1
(21, 6, 'Academic Research & Critical Epistemology', 'Từ vựng học thuật đỉnh cao: tư duy phản biện, phương pháp luận nghiên cứu và tính khách quan.', 'C1', 15, 'PUBLISHED', NOW() - INTERVAL 16 HOUR, NOW()),
(22, 1, 'Global Macroeconomics & Monetary Policy', 'Chính sách tiền tệ, lạm phát, mất giá tiền tệ, tái cơ cấu nợ công và chuỗi cung ứng toàn cầu.', 'C1', 15, 'PUBLISHED', NOW() - INTERVAL 12 HOUR, NOW()),
(23, 10, 'Jurisprudence, Governance & International Law', 'Thuật ngữ luật pháp quốc tế, hiến pháp, tiền lệ tư pháp, quyền miễn trừ ngoại giao và phê chuẩn hiệp ước.', 'C1', 15, 'PUBLISHED', NOW() - INTERVAL 8 HOUR, NOW()),
(24, 10, 'Philosophical Ethics & Rhetorical Discourse', 'Học thuyết đạo đức triết học, tư tưởng hiện sinh, biện pháp tu từ và các mâu thuẫn xã hội.', 'C1', 15, 'PUBLISHED', NOW() - INTERVAL 4 HOUR, NOW())
ON DUPLICATE KEY UPDATE 
    category_id = VALUES(category_id),
    title = VALUES(title),
    description = VALUES(description),
    level = VALUES(level),
    time_limit = VALUES(time_limit),
    status = VALUES(status),
    updated_at = NOW();

-- ============================================================
-- 3. INSERT QUESTIONS (IDs 31 to 120) - 90 Questions
-- ============================================================

-- Quiz 7: Family, Home & Daily Living (id=7, level A1)
INSERT INTO questions (id, quiz_id, content, options, correct_answer, explanation, created_at) VALUES
(31, 7, 'My mother\'s mother is my ______.', JSON_ARRAY('grandmother', 'sister', 'daughter', 'niece'), 0, '''Grandmother'' nghĩa là bà (mẹ của bố hoặc mẹ).', NOW() - INTERVAL 12 DAY),
(32, 7, 'We prepare and cook our family meals in the ______.', JSON_ARRAY('bedroom', 'kitchen', 'garage', 'bathroom'), 1, '''Kitchen'' là nhà bếp, nơi chuẩn bị và nấu các món ăn.', NOW() - INTERVAL 12 DAY),
(33, 7, 'I usually brush my teeth and ______ up at 6:30 AM every morning.', JSON_ARRAY('sleep', 'wake', 'stay', 'lie'), 1, '''Wake up'' là thức dậy vào buổi sáng.', NOW() - INTERVAL 12 DAY),
(34, 7, 'There are four wooden ______ around the dining table.', JSON_ARRAY('chairs', 'doors', 'roofs', 'windows'), 0, '''Chairs'' là những chiếc ghế ngồi.', NOW() - INTERVAL 12 DAY),
(35, 7, 'He lives in a cozy, peaceful house with his ______.', JSON_ARRAY('furniture', 'family', 'homework', 'garden'), 1, '''Family'' có nghĩa là gia đình.', NOW() - INTERVAL 12 DAY),

-- Quiz 8: Numbers, Calendar, Days & Weather (id=8, level A1)
(36, 8, 'The month that comes right after January is ______.', JSON_ARRAY('March', 'February', 'April', 'December'), 1, '''February'' là tháng Hai (ngay sau tháng Một - January).', NOW() - INTERVAL 11 DAY),
(37, 8, 'It is very cold and often snowy during the ______ season.', JSON_ARRAY('summer', 'winter', 'spring', 'autumn'), 1, '''Winter'' là mùa đông, thời điểm lạnh và có tuyết.', NOW() - INTERVAL 11 DAY),
(38, 8, 'Saturday and Sunday are commonly called the ______.', JSON_ARRAY('weekday', 'weekend', 'midnight', 'morning'), 1, '''Weekend'' là ngày cuối tuần (thứ Bảy và Chủ Nhật).', NOW() - INTERVAL 11 DAY),
(39, 8, 'There are twenty-four ______ in a single day.', JSON_ARRAY('minutes', 'seconds', 'hours', 'weeks'), 2, '''Hours'' là giờ (24 giờ trong 1 ngày).', NOW() - INTERVAL 11 DAY),
(40, 8, 'The sun is shining brightly today. It is wonderfully ______.', JSON_ARRAY('rainy', 'cloudy', 'sunny', 'foggy'), 2, '''Sunny'' nghĩa là trời nắng rực rỡ, nhiều ánh nắng.', NOW() - INTERVAL 11 DAY),

-- Quiz 9: Colors, Clothes & Everyday Objects (id=9, level A1)
(41, 9, 'When it is raining heavily outside, I always wear a waterproof ______.', JSON_ARRAY('jacket', 'tie', 'sock', 'sandal'), 0, '''Jacket'' là áo khoác (waterproof jacket là áo khoác chống nước/áo mưa).', NOW() - INTERVAL 10 DAY),
(42, 9, 'Fresh grass and tree leaves in the garden are bright ______.', JSON_ARRAY('blue', 'green', 'red', 'black'), 1, '''Green'' là màu xanh lá cây.', NOW() - INTERVAL 10 DAY),
(43, 9, 'He wears comfortable sports ______ when jogging around the park.', JSON_ARRAY('shoes', 'hats', 'gloves', 'rings'), 0, '''Shoes'' là đôi giày thể thao.', NOW() - INTERVAL 10 DAY),
(44, 9, 'I glance at my wrist ______ to check the exact current time.', JSON_ARRAY('clock', 'watch', 'mirror', 'lamp'), 1, '''Watch'' là đồng hồ đeo tay (wrist watch).', NOW() - INTERVAL 10 DAY),
(45, 9, 'My brother bought a plain ______ cotton shirt for school.', JSON_ARRAY('white', 'darkness', 'noisy', 'tired'), 0, '''White'' là màu trắng, màu áo sơ mi học sinh phổ biến.', NOW() - INTERVAL 10 DAY),

-- Quiz 10: School Supplies & Basic Classroom (id=10, level A1)
(46, 10, 'Students write down important lecture notes in their ______.', JSON_ARRAY('notebook', 'pillow', 'fridge', 'curtain'), 0, '''Notebook'' là cuốn vở/sổ ghi chú học tập.', NOW() - INTERVAL 9 DAY),
(47, 10, 'Our English ______ is very knowledgeable, patient, and kind.', JSON_ARRAY('farmer', 'pilot', 'teacher', 'mechanic'), 2, '''Teacher'' là giáo viên/thầy cô giáo.', NOW() - INTERVAL 9 DAY),
(48, 10, 'You can use an eraser to rub out mistakes made with a ______.', JSON_ARRAY('pen', 'pencil', 'scissors', 'ruler'), 1, '''Pencil'' là bút chì (tẩy/gôm xóa nét bút chì).', NOW() - INTERVAL 9 DAY),
(49, 10, 'Please open your English ______ to page forty-two.', JSON_ARRAY('textbook', 'wardrobe', 'wallet', 'blanket'), 0, '''Textbook'' là cuốn sách giáo khoa.', NOW() - INTERVAL 9 DAY),
(50, 10, 'The pupils sit attentively at their wooden ______ during the lesson.', JSON_ARRAY('desks', 'beds', 'ceilings', 'stairs'), 0, '''Desks'' là bàn học của học sinh trong lớp.', NOW() - INTERVAL 9 DAY),

-- Quiz 11: Shopping, Prices & Customer Service (id=11, level A2)
(51, 11, 'Excuse me, how much does this leather handbag ______?', JSON_ARRAY('pay', 'cost', 'spend', 'buy'), 1, '''How much does it cost?'' là câu hỏi quen thuộc để hỏi giá tiền của một món đồ.', NOW() - INTERVAL 8 DAY),
(52, 11, 'Please retain your store ______ if you need to exchange the item.', JSON_ARRAY('recipe', 'ticket', 'receipt', 'menu'), 2, '''Receipt'' là biên lai/hóa đơn thanh toán tiền khi mua sắm.', NOW() - INTERVAL 8 DAY),
(53, 11, 'All winter clothing is currently on ______ with up to 50% discount.', JSON_ARRAY('sale', 'order', 'rent', 'demand'), 0, '''On sale'' nghĩa là đang trong đợt giảm giá/khuyến mãi.', NOW() - INTERVAL 8 DAY),
(54, 11, 'The friendly shop ______ helped me choose the perfect shoe size.', JSON_ARRAY('director', 'assistant', 'scientist', 'driver'), 1, '''Shop assistant'' là nhân viên bán hàng/tư vấn tại cửa hàng.', NOW() - INTERVAL 8 DAY),
(55, 11, 'Do you accept international credit cards, or do you only take ______?', JSON_ARRAY('cash', 'stamps', 'coupons', 'bills'), 0, '''Cash'' nghĩa là tiền mặt.', NOW() - INTERVAL 8 DAY),

-- Quiz 12: Hobbies, Sports & Active Lifestyle (id=12, level A2)
(56, 12, 'Playing badminton twice a week helps me keep ______ and healthy.', JSON_ARRAY('fit', 'lazy', 'heavy', 'dull'), 0, '''Keep fit'' nghĩa là giữ dáng khỏe mạnh, duy trì thể lực tốt.', NOW() - INTERVAL 7 DAY),
(57, 12, 'Our local football team won an exciting championship ______ yesterday.', JSON_ARRAY('race', 'match', 'test', 'lesson'), 1, '''Match'' là trận thi đấu thể thao (football match).', NOW() - INTERVAL 7 DAY),
(58, 12, 'She spends her Sunday afternoon ______ watercolor landscapes.', JSON_ARRAY('singing', 'painting', 'dancing', 'cooking'), 1, '''Painting'' là vẽ tranh (tranh màu nước - watercolor).', NOW() - INTERVAL 7 DAY),
(59, 12, 'Morning cardio exercises increase stamina and elevate your daily ______ levels.', JSON_ARRAY('fever', 'stress', 'energy', 'sleep'), 2, '''Energy levels'' là mức năng lượng hoạt động của cơ thể.', NOW() - INTERVAL 7 DAY),
(60, 12, 'He joined an amateur photography ______ to exchange shooting tips.', JSON_ARRAY('office', 'club', 'clinic', 'factory'), 1, '''Club'' là câu lạc bộ sở thích (photography club).', NOW() - INTERVAL 7 DAY),

-- Quiz 13: Jobs, Professions & Workplace Essentials (id=13, level A2)
(61, 13, 'A professional who designs houses and modern buildings is an ______.', JSON_ARRAY('author', 'architect', 'accountant', 'actor'), 1, '''Architect'' là kiến trúc sư thiết kế công trình kiến trúc.', NOW() - INTERVAL 6 DAY),
(62, 13, 'The tech firm posted an urgent job ______ for frontend developers.', JSON_ARRAY('vacancy', 'holiday', 'penalty', 'meeting'), 0, '''Job vacancy'' là vị trí tuyển dụng công việc còn trống.', NOW() - INTERVAL 6 DAY),
(63, 13, 'Applicants should submit a comprehensive ______ detailing work experience.', JSON_ARRAY('recipe', 'resume', 'novel', 'passport'), 1, '''Resume'' (CV) là bản lý lịch tóm tắt kinh nghiệm làm việc và học vấn.', NOW() - INTERVAL 6 DAY),
(64, 13, 'Full-time employees receive their fixed monthly ______ at month-end.', JSON_ARRAY('debt', 'salary', 'fine', 'fare'), 1, '''Salary'' là tiền lương hàng tháng nhận từ người sử dụng lao động.', NOW() - INTERVAL 6 DAY),
(65, 13, 'She takes compassionate care of hospitalized patients as a certified ______.', JSON_ARRAY('nurse', 'lawyer', 'pilot', 'mechanic'), 0, '''Nurse'' là y tá/điều dưỡng viên chăm sóc bệnh nhân.', NOW() - INTERVAL 6 DAY),

-- Quiz 14: Education, Academic Life & Studying Skills (id=14, level B1)
(66, 14, 'Due to her outstanding grades, she was awarded a merit ______ to university.', JSON_ARRAY('penalty', 'loan', 'scholarship', 'invoice'), 2, '''Scholarship'' là học bổng khen thưởng sinh viên có thành tích học tập xuất sắc.', NOW() - INTERVAL 5 DAY),
(67, 14, 'Students are required to hand in their chemistry research ______ by 5 PM.', JSON_ARRAY('assignment', 'invitation', 'complaint', 'advertisement'), 0, '''Assignment'' là bài tập/nhiệm vụ học tập được giao nghiên cứu.', NOW() - INTERVAL 5 DAY),
(68, 14, 'The modern science ______ contains specialized microscopes and safety goggles.', JSON_ARRAY('dormitory', 'laboratory', 'cafeteria', 'gymnasium'), 1, '''Laboratory'' (lab) là phòng thí nghiệm khoa học thực hành.', NOW() - INTERVAL 5 DAY),
(69, 14, 'Spaced repetition flashcards are an exceptionally effective memorization ______.', JSON_ARRAY('obstacle', 'technique', 'mistake', 'accident'), 1, '''Technique'' là phương pháp/kỹ thuật chuyên môn.', NOW() - INTERVAL 5 DAY),
(70, 14, 'After finishing all thesis requirements, she will ______ with high distinction.', JSON_ARRAY('graduate', 'enroll', 'hesitate', 'collapse'), 0, '''Graduate'' nghĩa là tốt nghiệp ra trường sau khi hoàn thành khóa học.', NOW() - INTERVAL 5 DAY),

-- Quiz 15: Entertainment, Media & Digital Social Life (id=15, level B1)
(71, 15, 'The animated masterpiece received glowing ______ from international film critics.', JSON_ARRAY('complaints', 'reviews', 'warnings', 'debts'), 1, '''Reviews'' là các bài đánh giá, phê bình tác phẩm nghệ thuật.', NOW() - INTERVAL 4 DAY),
(72, 15, 'Social networking applications have hundreds of millions of daily active ______.', JSON_ARRAY('users', 'enemies', 'patients', 'strangers'), 0, '''Users'' là người dùng/người tương tác trên ứng dụng.', NOW() - INTERVAL 4 DAY),
(73, 15, 'Psychologists study the pervasive ______ of screen time on child development.', JSON_ARRAY('budget', 'impact', 'salary', 'border'), 1, '''Impact'' là tác động, ảnh hưởng sâu sắc đến sự việc.', NOW() - INTERVAL 4 DAY),
(74, 15, 'The rock band gave an unforgettable live ______ in front of thousands of fans.', JSON_ARRAY('interview', 'performance', 'quarrel', 'treatment'), 1, '''Performance'' là buổi biểu diễn trực tiếp trên sân khấu.', NOW() - INTERVAL 4 DAY),
(75, 15, 'Audio podcasts serve as a convenient educational ______ for busy professionals.', JSON_ARRAY('medium', 'weapon', 'danger', 'obstacle'), 0, '''Medium'' (số nhiều: media) là phương tiện truyền thông/công cụ truyền tải.', NOW() - INTERVAL 4 DAY),

-- Quiz 16: Environment, Climate & Natural Phenomena (id=16, level B1)
(76, 16, 'Excessive carbon dioxide emissions accelerate irreversible global ______.', JSON_ARRAY('cooling', 'warming', 'darkness', 'silence'), 1, '''Global warming'' là hiện tượng nóng lên toàn cầu do hiệu ứng nhà kính.', NOW() - INTERVAL 3 DAY),
(77, 16, 'Citizens are strongly encouraged to sort and ______ aluminum cans and paper.', JSON_ARRAY('pollute', 'recycle', 'discard', 'ignite'), 1, '''Recycle'' là tái chế phế liệu thành nguyên liệu có ích.', NOW() - INTERVAL 3 DAY),
(78, 16, 'Wind, solar, and hydroelectric facilities generate clean, ______ power.', JSON_ARRAY('renewable', 'exhaustible', 'hazardous', 'artificial'), 0, '''Renewable'' là năng lượng tái tạo (tự nhiên không cạn kiệt).', NOW() - INTERVAL 3 DAY),
(79, 16, 'Illegal timber logging fragments the natural ______ of wild Asian elephants.', JSON_ARRAY('habitat', 'laboratory', 'pavement', 'factory'), 0, '''Habitat'' là môi trường sống tự nhiên của động thực vật hoang dã.', NOW() - INTERVAL 3 DAY),
(80, 16, 'Torrential monsoon downpours caused catastrophic urban ______ across the delta.', JSON_ARRAY('drought', 'flooding', 'famine', 'blizzard'), 1, '''Flooding'' là tình trạng lũ lụt, ngập úng nghiêm trọng.', NOW() - INTERVAL 3 DAY),

-- Quiz 17: Corporate Strategy, Marketing & Brand Value (id=17, level B2)
(81, 17, 'Fostering long-term customer ______ requires consistent product quality and service excellence.', JSON_ARRAY('dispute', 'loyalty', 'penalty', 'apathy'), 1, '''Brand loyalty'' là lòng trung thành của khách hàng với thương hiệu.', NOW() - INTERVAL 2 DAY),
(82, 17, 'The biotech startup closed a $25 million Series B venture ______ round.', JSON_ARRAY('capital', 'ransom', 'fine', 'loss'), 0, '''Venture capital'' là nguồn vốn đầu tư mạo hiểm cho các công ty khởi nghiệp.', NOW() - INTERVAL 2 DAY),
(83, 17, 'Proprietary patented technology gives the enterprise a distinctive competitive ______.', JSON_ARRAY('barrier', 'disadvantage', 'advantage', 'liability'), 2, '''Competitive advantage'' là lợi thế cạnh tranh vượt trội so với đối thủ.', NOW() - INTERVAL 2 DAY),
(84, 17, 'The agency devised an omnichannel advertising ______ spanning social, search, and TV.', JSON_ARRAY('campaign', 'conflict', 'lawsuit', 'accident'), 0, '''Advertising campaign'' là chiến dịch quảng cáo toàn diện.', NOW() - INTERVAL 2 DAY),
(85, 17, 'Conducting empirical market ______ minimizes commercial risk prior to product rollout.', JSON_ARRAY('rumor', 'research', 'gossip', 'friction'), 1, '''Market research'' là nghiên cứu và khảo sát thị trường chuyên sâu.', NOW() - INTERVAL 2 DAY),

-- Quiz 18: Sustainability, Biodiversity & Conservation (id=18, level B2)
(86, 18, 'Unchecked industrial consumption risks the irreversible ______ of groundwater aquifers.', JSON_ARRAY('depletion', 'enrichment', 'purification', 'expansion'), 0, '''Depletion'' là sự cạn kiệt nguồn tài nguyên thiên nhiên hữu hạn.', NOW() - INTERVAL 36 HOUR),
(87, 18, 'Coral reefs nurture exceptional marine ______, supporting a quarter of ocean species.', JSON_ARRAY('pollution', 'biodiversity', 'monotony', 'congestion'), 1, '''Biodiversity'' là sự đa dạng sinh học trong một hệ sinh thái.', NOW() - INTERVAL 36 HOUR),
(88, 18, 'Governmental environmental standards mandate stringent caps on factory greenhouse ______.', JSON_ARRAY('emissions', 'donations', 'admissions', 'absorptions'), 0, '''Emissions'' là khí thải/chất thải công nghiệp phát tán vào bầu khí quyển.', NOW() - INTERVAL 36 HOUR),
(89, 18, 'Organic crop rotation bolsters soil fertility and supports natural moisture ______.', JSON_ARRAY('conservation', 'contamination', 'destruction', 'evaporation'), 0, '''Conservation'' là sự bảo tồn và giữ gìn tài nguyên thiên nhiên.', NOW() - INTERVAL 36 HOUR),
(90, 18, 'Energy-efficient architecture significantly diminishes a household\'s overall carbon ______.', JSON_ARRAY('fingerprint', 'footprint', 'handshake', 'shadow'), 1, '''Carbon footprint'' là dấu chân carbon, lượng khí thải carbon phát sinh.', NOW() - INTERVAL 36 HOUR),

-- Quiz 19: Psychology, Cognition & Human Behavior (id=19, level B2)
(91, 19, 'Learners guided by ______ motivation pursue knowledge for sheer personal fascination.', JSON_ARRAY('extrinsic', 'intrinsic', 'accidental', 'reluctant'), 1, '''Intrinsic motivation'' là động lực nội tại xuất phát từ niềm say mê bên trong.', NOW() - INTERVAL 28 HOUR),
(92, 19, 'High emotional ______ enables executives to perceive interpersonal undercurrents with empathy.', JSON_ARRAY('intelligence', 'stubbornness', 'arrogance', 'indifference'), 0, '''Emotional intelligence'' (EQ) là trí tuệ cảm xúc và thấu cảm xã hội.', NOW() - INTERVAL 28 HOUR),
(93, 19, 'Prolonged unmanageable workplace stressors can culminate in chronic physical and mental ______.', JSON_ARRAY('vitality', 'burnout', 'euphoria', 'agility'), 1, '''Burnout'' là hội chứng kiệt sức vì căng thẳng và áp lực kéo dài.', NOW() - INTERVAL 28 HOUR),
(94, 19, 'Cognitive ______ describes psychological distress arising when actions conflict with core values.', JSON_ARRAY('harmony', 'dissonance', 'sympathy', 'euphoria'), 1, '''Cognitive dissonance'' là sự bất hòa nhận thức giữa niềm tin và hành vi.', NOW() - INTERVAL 28 HOUR),
(95, 19, 'Cognitive scientists demonstrate that human memory is a dynamic, ______ reconstruction.', JSON_ARRAY('constructive', 'photographic', 'infallible', 'passive'), 0, '''Constructive'' thể hiện tính tái tạo/kiến tạo linh hoạt của ký ức.', NOW() - INTERVAL 28 HOUR),

-- Quiz 20: Science, Innovation & Space Exploration (id=20, level B2)
(96, 20, 'Deep space observatories capture photons emanating from primordial ______ nebulae.', JSON_ARRAY('celestial', 'terrestrial', 'domestic', 'mundane'), 0, '''Celestial'' nghĩa là thuộc về bầu trời, các thiên thể và vũ trụ bao la.', NOW() - INTERVAL 20 HOUR),
(97, 20, 'The discovery of room-temperature superconductivity would constitute a titanic technological ______.', JSON_ARRAY('setback', 'breakthrough', 'stagnation', 'dilemma'), 1, '''Breakthrough'' là bước đột phá khoa học công nghệ mang tính cách mạng.', NOW() - INTERVAL 20 HOUR),
(98, 20, 'Astrophysicists formulate a mathematical ______ before conducting verifying observational experiments.', JSON_ARRAY('hypothesis', 'superstition', 'rumor', 'prejudice'), 0, '''Hypothesis'' là giả thuyết khoa học cần được kiểm chứng qua thực nghiệm.', NOW() - INTERVAL 20 HOUR),
(99, 20, 'Exploratory rovers are engineered to withstand the exceedingly ______ Martian atmosphere.', JSON_ARRAY('hospitable', 'hostile', 'tropical', 'comforting'), 1, '''Hostile'' là môi trường khắc nghiệt, thù địch và nguy hiểm cho sinh vật.', NOW() - INTERVAL 20 HOUR),
(100, 20, 'Solid-state battery cells require non-toxic chemical ______ with superior thermal stability.', JSON_ARRAY('elements', 'hallucinations', 'superstitions', 'illusions'), 0, '''Elements'' là các nguyên tố/thành phần hóa học cấu tạo vật chất.', NOW() - INTERVAL 20 HOUR),

-- Quiz 21: Academic Research & Critical Epistemology (id=21, level C1)
(101, 21, 'The professor delivered an ______ deconstruction of neo-classical economic deterministic models.', JSON_ARRAY('incisive', 'indolent', 'inept', 'insipid'), 0, '''Incisive'' nghĩa là sắc bén, sâu sắc và có tính phân tích phê phán cao.', NOW() - INTERVAL 16 HOUR),
(102, 21, 'Every credible scholarly paper must candidly delineate its empirical and methodological ______.', JSON_ARRAY('glories', 'limitations', 'triumphs', 'infallibilities'), 1, '''Methodological limitations'' là những hạn chế mang tính phương pháp luận.', NOW() - INTERVAL 16 HOUR),
(103, 21, 'Rigorous academic discourse repudiates dogmatic dogma in favor of ______ substantiated inquiry.', JSON_ARRAY('empirically', 'randomly', 'superficially', 'emotionally'), 0, '''Empirically'' nghĩa là bằng phương pháp thực nghiệm dựa trên bằng chứng xác thực.', NOW() - INTERVAL 16 HOUR),
(104, 21, 'Her interdisciplinary dissertation brilliantly coalesced two previously ______ theoretical paradigms.', JSON_ARRAY('incompatible', 'identical', 'synonymous', 'redundant'), 0, '''Incompatible'' là xung đột, không tương thích hoặc dường như loại trừ nhau.', NOW() - INTERVAL 16 HOUR),
(105, 21, 'Scholarly peer review exists to safeguard epistemological ______ and ethical compliance.', JSON_ARRAY('integrity', 'deception', 'falsification', 'plagiarism'), 0, '''Integrity'' (học thuật) là tính liêm chính, trung thực và chuẩn mực khoa học.', NOW() - INTERVAL 16 HOUR),

-- Quiz 22: Global Macroeconomics & Monetary Policy (id=22, level C1)
(106, 22, 'Central banks coordinate quantitative tightening regimes to curb galloping inflationary ______.', JSON_ARRAY('pressures', 'pleasures', 'hesitations', 'remedies'), 0, '''Inflationary pressures'' là áp lực lạm phát đẩy giá cả tăng cao.', NOW() - INTERVAL 12 HOUR),
(107, 22, 'Sharp currency ______ inflates imported commodity costs while potentially boosting exports.', JSON_ARRAY('appreciation', 'depreciation', 'stabilization', 'pegging'), 1, '''Depreciation'' là sự mất giá/giảm giá trị của đồng nội tệ so với ngoại tệ.', NOW() - INTERVAL 12 HOUR),
(108, 22, 'Multilateral debt restructuring necessitates consensus among sovereign lenders and institutional ______.', JSON_ARRAY('stakeholders', 'bystanders', 'trespassers', 'spectators'), 0, '''Stakeholders'' là các bên liên quan trực tiếp có quyền lợi và nghĩa vụ.', NOW() - INTERVAL 12 HOUR),
(109, 22, 'Geopolitical friction and export embargoes exacerbated critical raw material ______ across industries.', JSON_ARRAY('surplus', 'abundance', 'scarcity', 'glut'), 2, '''Scarcity'' là tình trạng khan hiếm, thiếu hụt hàng hóa nghiêm trọng.', NOW() - INTERVAL 12 HOUR),
(110, 22, 'Counter-cyclical fiscal intervention was enacted to stave off debilitating macroeconomic ______.', JSON_ARRAY('stagnation', 'prosperity', 'boom', 'vitality'), 0, '''Economic stagnation'' là sự đình đốn kinh tế kéo dài, tăng trưởng trì trệ.', NOW() - INTERVAL 12 HOUR),

-- Quiz 23: Jurisprudence, Governance & International Law (id=23, level C1)
(111, 23, 'The constitutional tribunal ruled that the summary deportation order breached ______ due process.', JSON_ARRAY('procedural', 'accidental', 'negligible', 'casual'), 0, '''Procedural due process'' là quy trình tố tụng công bằng theo luật định.', NOW() - INTERVAL 8 HOUR),
(112, 23, 'Diplomatic privileges under the Geneva Convention cannot confer absolute ______ for war atrocities.', JSON_ARRAY('impunity', 'guilt', 'culpability', 'sanction'), 0, '''Impunity'' là sự miễn trừ trừng phạt/không bị trừng trị trước pháp luật.', NOW() - INTERVAL 8 HOUR),
(113, 23, 'An autonomous judiciary serves as a vital safeguard preventing executive and legislative ______.', JSON_ARRAY('overreach', 'cooperation', 'moderation', 'humility'), 0, '''Overreach'' (executive overreach) là sự lạm quyền/vượt quá quyền hạn hiến định.', NOW() - INTERVAL 8 HOUR),
(114, 23, 'Landmark appellate verdicts establish authoritative legal ______ that lower jurisdictions must respect.', JSON_ARRAY('precedents', 'coincidences', 'anecdotes', 'gossips'), 0, '''Legal precedents'' là án lệ tư pháp có tính chất định hướng và ràng buộc.', NOW() - INTERVAL 8 HOUR),
(115, 23, 'Before taking binding domestic effect, the bilateral extradition accord requires legislative ______.', JSON_ARRAY('ratification', 'nullification', 'repeal', 'repudiation'), 0, '''Ratification'' là sự phê chuẩn/chuẩn y chính thức của cơ quan lập pháp đối với hiệp ước.', NOW() - INTERVAL 8 HOUR),

-- Quiz 24: Philosophical Ethics & Rhetorical Discourse (id=24, level C1)
(116, 24, 'Classic utilitarian philosophers posit that normative righteousness hinges upon net teleological ______.', JSON_ARRAY('outcomes', 'motives', 'intentions', 'dogmas'), 0, '''Outcomes'' (kết quả/hệ quả) là trọng tâm của thuyết vị lợi (utilitarianism).', NOW() - INTERVAL 4 HOUR),
(117, 24, 'The debater employed subtle ______ wit to unmask the demagogue\'s fallacious populist rhetoric.', JSON_ARRAY('satirical', 'clumsy', 'naive', 'monotonous'), 0, '''Satirical'' mang tính châm biếm, mỉa mai sắc sảo nhằm vạch trần ngụy biện.', NOW() - INTERVAL 4 HOUR),
(118, 24, 'Existentialist literature depicts humanity\'s profound struggle to forge subjective ______ amid absurdity.', JSON_ARRAY('meaning', 'chaos', 'futility', 'despair'), 0, '''Meaning'' là ý nghĩa cuộc sống/lý do tồn tại trong triết học hiện sinh.', NOW() - INTERVAL 4 HOUR),
(119, 24, 'Radical epistemological skepticism questions whether immutable, infallible objective truth is ______.', JSON_ARRAY('attainable', 'redundant', 'worthless', 'unwanted'), 0, '''Attainable'' là có thể đạt được, có thể thấu triệt và nắm bắt được.', NOW() - INTERVAL 4 HOUR),
(120, 24, 'The classic treatise elucidates the perennial ideological ______ between individual liberty and social order.', JSON_ARRAY('dichotomy', 'harmony', 'symbiosis', 'concord'), 0, '''Dichotomy'' là sự phân đôi, tính lưỡng phân giữa hai mặt đối lập.', NOW() - INTERVAL 4 HOUR)
ON DUPLICATE KEY UPDATE 
    quiz_id = VALUES(quiz_id),
    content = VALUES(content),
    options = VALUES(options),
    correct_answer = VALUES(correct_answer),
    explanation = VALUES(explanation);
