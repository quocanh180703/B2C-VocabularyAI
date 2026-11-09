package com.vocabularyai.learning.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vocabularyai.vocabulary.entity.Category;
import com.vocabularyai.vocabulary.repository.CategoryRepository;
import com.vocabularyai.learning.dto.LearningProgressResponse;
import com.vocabularyai.learning.entity.LearningProgress;
import com.vocabularyai.learning.repository.LearningProgressRepository;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import com.vocabularyai.vocabulary.entity.Word;
import com.vocabularyai.vocabulary.repository.WordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("TC-SRS-02: Spaced Repetition System Tests")
public class LearningServiceTest {

    @Autowired
    private LearningService learningService;

    @Autowired
    private LearningProgressRepository progressRepository;

    @Autowired
    private WordRepository wordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @Transactional
    @DisplayName("TC-SRS-02: Trả lời sai không reset số lần ôn (reviewCount không bị reset về 0)")
    public void testSrs02_AnsweringWrongDoesNotResetReviewCount() throws Exception {
        // 1. Arrange: Tạo User và Word thực tế
        User user = userRepository.save(User.builder()
                .username("srs_tester_" + System.currentTimeMillis())
                .email("srs_tester_" + System.currentTimeMillis() + "@test.com")
                .password("$2a$10$encryptedPasswordPlaceholder")
                .fullName("SRS Tester")
                .role(User.UserRole.STUDENT)
                .status(User.UserStatus.ACTIVE)
                .build());

        Category category = categoryRepository.findAll().stream().findFirst().orElseGet(() ->
                categoryRepository.save(Category.builder().name("General").description("General category").build())
        );

        Word word = wordRepository.save(Word.builder()
                .word("abandon")
                .meaning("từ bỏ, bỏ rơi")
                .level("B2")
                .partOfSpeech("verb")
                .category(category)
                .build());

        // Tiến độ ban đầu: Đã ôn 2 lần (reviewCount = 2, correctCount = 2, wrongCount = 0)
        // và đã đến hạn ôn (nextReviewAt là 1 giờ trước)
        LearningProgress progress = progressRepository.save(LearningProgress.builder()
                .user(user)
                .word(word)
                .reviewCount(2)
                .correctCount(2)
                .wrongCount(0)
                .status(LearningProgress.LearningStatus.LEARNING)
                .lastReviewedAt(LocalDateTime.now().minusDays(1))
                .nextReviewAt(LocalDateTime.now().minusHours(1))
                .build());

        System.out.println("\n==================================================================");
        System.out.println(">>> [BƯỚC 1] DỮ LIỆU BAN ĐẦU CỦA TỪ:");
        System.out.println("- Từ vựng kiểm thử : " + word.getWord());
        System.out.println("- Số lần ôn ban đầu : reviewCount = " + progress.getReviewCount());
        System.out.println("- Số lần đúng       : correctCount = " + progress.getCorrectCount());
        System.out.println("- Số lần sai        : wrongCount = " + progress.getWrongCount());
        System.out.println("==================================================================");

        // 2. Act: Thực hiện ôn tập và trả lời SAI (isCorrect = false)
        System.out.println("\n>>> [BƯỚC 2] THỰC HIỆN TEST CASE TC-SRS-02: BẤM TRẢ LỜI SAI ('Chưa nhớ')...");
        LearningProgressResponse reviewResult = learningService.reviewWord(user, word.getId(), false);

        // 3. Assert: Kiểm tra số lần ôn sau khi trả lời sai
        assertNotNull(reviewResult);
        assertEquals("abandon", reviewResult.getWord().getWord());
        
        // TIÊU CHÍ TC-SRS-02: reviewCount KHÔNG được reset về 0! Nó phải tăng lên 3 (2 + 1)!
        assertEquals(3, reviewResult.getReviewCount(), "TC-SRS-02 THẤT BẠI: reviewCount bị reset về 0!");
        assertEquals(1, reviewResult.getWrongCount(), "wrongCount phải tăng lên 1!");
        assertEquals(2, reviewResult.getCorrectCount(), "correctCount phải giữ nguyên là 2!");

        // 4. Kiểm tra response GET /learning/review-queue
        Page<LearningProgressResponse> queuePage = learningService.getWordsNeedingReview(user.getId(), PageRequest.of(0, 10));
        assertFalse(queuePage.isEmpty(), "Từ vừa trả lời sai phải tiếp tục nằm trong review-queue hôm nay để ôn lại!");
        
        LearningProgressResponse queueItem = queuePage.getContent().stream()
                .filter(item -> item.getWord().getId().equals(word.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(queueItem, "Từ 'abandon' phải xuất hiện trong response GET /learning/review-queue!");
        assertEquals(3, queueItem.getReviewCount(), "reviewCount trong response GET /learning/review-queue phải bằng 3 (KHÔNG BỊ RESET VỀ 0)!");

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        System.out.println("\n==================================================================");
        System.out.println(">>> [BƯỚC 3] KẾT QUẢ THỰC TẾ TEST CASE TC-SRS-02:");
        System.out.println("• Tên từ               : " + queueItem.getWord().getWord());
        System.out.println("• Số lần ôn trước đó   : 2");
        System.out.println("• Kết quả sau khi sai  : reviewCount = " + queueItem.getReviewCount() + " (KHÔNG BỊ RESET VỀ 0, TĂNG LÊN 3)");
        System.out.println("• Số lần sai           : wrongCount = " + queueItem.getWrongCount());
        System.out.println("• Trạng thái test case : ĐẠT (PASS)");
        System.out.println("------------------------------------------------------------------");
        System.out.println(">>> [BƯỚC 4] RESPONSE GET /learning/review-queue:");
        System.out.println(mapper.writeValueAsString(queueItem));
        System.out.println("==================================================================\n");
    }
}
