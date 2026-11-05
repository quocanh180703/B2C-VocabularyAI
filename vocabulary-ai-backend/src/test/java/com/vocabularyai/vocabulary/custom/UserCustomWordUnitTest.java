package com.vocabularyai.vocabulary.custom;

import com.vocabularyai.common.exception.DuplicateResourceException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.user.entity.User.UserRole;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.vocabulary.custom.dto.CreateCustomWordRequest;
import com.vocabularyai.vocabulary.custom.dto.CustomWordResponse;
import com.vocabularyai.vocabulary.custom.dto.UpdateCustomWordRequest;
import com.vocabularyai.vocabulary.custom.entity.UserCustomWord;
import com.vocabularyai.vocabulary.custom.repository.UserCustomWordRepository;
import com.vocabularyai.vocabulary.custom.service.UserCustomWordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserCustomWordUnitTest {

    @Mock
    private UserCustomWordRepository userCustomWordRepository;

    @InjectMocks
    private UserCustomWordServiceImpl userCustomWordService;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        userA = User.builder()
                .id(100L)
                .username("usera")
                .email("usera@example.com")
                .role(UserRole.STUDENT)
                .build();

        userB = User.builder()
                .id(200L)
                .username("userb")
                .email("userb@example.com")
                .role(UserRole.STUDENT)
                .build();
    }

    @Test
    @DisplayName("User creates custom word successfully")
    void testCreateWord_Success() {
        CreateCustomWordRequest request = CreateCustomWordRequest.builder()
                .lemma("serendipity")
                .vietnameseMeaning("Sự tình cờ may mắn")
                .partOfSpeech("noun")
                .example("Finding this book was sheer serendipity.")
                .exampleTranslation("Tìm thấy cuốn sách này quả là sự may mắn tình cờ.")
                .cefrLevel("C1")
                .topic("ARTS")
                .build();

        when(userCustomWordRepository.existsByUserIdAndLemmaIgnoreCase(100L, "serendipity"))
                .thenReturn(false);
        when(userCustomWordRepository.save(any(UserCustomWord.class))).thenAnswer(i -> {
            UserCustomWord w = i.getArgument(0);
            w.setId(1L);
            return w;
        });

        CustomWordResponse response = userCustomWordService.createWord(userA, request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("serendipity", response.getLemma());
        assertEquals("Sự tình cờ may mắn", response.getVietnameseMeaning());
        assertEquals(100L, response.getUserId());
        assertEquals("C1", response.getCefrLevel());
        assertEquals("ARTS", response.getTopic());
    }

    @Test
    @DisplayName("User creates custom word with existing lemma throws DuplicateResourceException (409)")
    void testCreateWord_DuplicateLemma_ThrowsDuplicate() {
        CreateCustomWordRequest request = CreateCustomWordRequest.builder()
                .lemma("apple")
                .vietnameseMeaning("Quả táo")
                .build();

        when(userCustomWordRepository.existsByUserIdAndLemmaIgnoreCase(100L, "apple"))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userCustomWordService.createWord(userA, request));
        verify(userCustomWordRepository, never()).save(any(UserCustomWord.class));
    }

    @Test
    @DisplayName("User updates own custom word successfully")
    void testUpdateWord_Success() {
        UserCustomWord existing = UserCustomWord.builder()
                .id(1L)
                .userId(100L)
                .lemma("hello")
                .vietnameseMeaning("Xin chào")
                .cefrLevel("A1")
                .topic("GENERAL")
                .build();

        when(userCustomWordRepository.findByIdAndUserId(1L, 100L)).thenReturn(Optional.of(existing));
        when(userCustomWordRepository.save(any(UserCustomWord.class))).thenAnswer(i -> i.getArgument(0));

        UpdateCustomWordRequest updateReq = UpdateCustomWordRequest.builder()
                .vietnameseMeaning("Lời chào hỏi thân mật")
                .cefrLevel("A1")
                .topic("FAMILY")
                .build();

        CustomWordResponse response = userCustomWordService.updateWord(userA, 1L, updateReq);

        assertNotNull(response);
        assertEquals("Lời chào hỏi thân mật", response.getVietnameseMeaning());
        assertEquals("FAMILY", response.getTopic());
    }

    @Test
    @DisplayName("User B tries to update User A's word throws ResourceNotFoundException (404)")
    void testUpdateWord_WrongOwner_ThrowsNotFound() {
        when(userCustomWordRepository.findByIdAndUserId(1L, 200L)).thenReturn(Optional.empty());

        UpdateCustomWordRequest updateReq = UpdateCustomWordRequest.builder()
                .vietnameseMeaning("Hack meaning")
                .build();

        assertThrows(ResourceNotFoundException.class, () -> userCustomWordService.updateWord(userB, 1L, updateReq));
        verify(userCustomWordRepository, never()).save(any(UserCustomWord.class));
    }

    @Test
    @DisplayName("User B tries to delete User A's word throws ResourceNotFoundException (404)")
    void testDeleteWord_WrongOwner_ThrowsNotFound() {
        when(userCustomWordRepository.findByIdAndUserId(1L, 200L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userCustomWordService.deleteWord(userB, 1L));
        verify(userCustomWordRepository, never()).delete(any(UserCustomWord.class));
    }

    @Test
    @DisplayName("User A deletes own word successfully")
    void testDeleteWord_Success() {
        UserCustomWord existing = UserCustomWord.builder()
                .id(1L)
                .userId(100L)
                .lemma("delete_me")
                .build();

        when(userCustomWordRepository.findByIdAndUserId(1L, 100L)).thenReturn(Optional.of(existing));

        userCustomWordService.deleteWord(userA, 1L);

        verify(userCustomWordRepository, times(1)).delete(existing);
    }
}
