package com.vocabularyai.admin;

import com.vocabularyai.admin.controller.*;
import com.vocabularyai.admin.service.AdminStudentService;
import com.vocabularyai.quiz.controller.QuizController;
import com.vocabularyai.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.vocabularyai.common.exception.ForbiddenException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class AdminInstructorPhase2UnitTest {

    @Test
    @DisplayName("Vocabulary, Questions, Student list, Reports (non-revenue), and Quiz attempts (read) should allow INSTRUCTOR")
    void testInstructorAllowedControllers() {
        Class<?>[] instructorAllowedClasses = new Class<?>[] {
            AdminVocabularyController.class,
            AdminQuestionController.class,
            AdminStudentController.class,
            AdminLearningProgressController.class,
            AdminQuizAttemptController.class,
            AdminReportsController.class
        };

        for (Class<?> clazz : instructorAllowedClasses) {
            PreAuthorize annotation = clazz.getAnnotation(PreAuthorize.class);
            assertNotNull(annotation, clazz.getSimpleName() + " must have @PreAuthorize");
            assertTrue(annotation.value().contains("INSTRUCTOR"),
                    clazz.getSimpleName() + " must allow INSTRUCTOR in class-level @PreAuthorize: " + annotation.value());
        }
    }

    @Test
    @DisplayName("Admin-only controllers must NOT allow INSTRUCTOR")
    void testAdminOnlyControllers() {
        Class<?>[] adminOnlyClasses = new Class<?>[] {
            AdminDashboardController.class,
            AdminPackagesController.class,
            AdminSystemController.class,
            AdminAiController.class,
            AdminAiAnalyticsController.class,
            AdminCopilotController.class,
            AdminAiVocabularyController.class
        };

        for (Class<?> clazz : adminOnlyClasses) {
            PreAuthorize annotation = clazz.getAnnotation(PreAuthorize.class);
            assertNotNull(annotation, clazz.getSimpleName() + " must have @PreAuthorize");
            assertFalse(annotation.value().contains("INSTRUCTOR"),
                    clazz.getSimpleName() + " must NOT allow INSTRUCTOR");
            assertTrue(annotation.value().contains("hasRole('ADMIN')"),
                    clazz.getSimpleName() + " must enforce ADMIN only");
        }
    }

    @Test
    @DisplayName("AdminReportsController revenue endpoint must be ADMIN only")
    void testRevenueEndpointAdminOnly() throws NoSuchMethodException {
        Method revenueMethod = AdminReportsController.class.getMethod("getRevenueReport");
        PreAuthorize annotation = revenueMethod.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "getRevenueReport must have @PreAuthorize");
        assertEquals("hasRole('ADMIN')", annotation.value(), "Revenue must be restricted to ADMIN only");
    }

    @Test
    @DisplayName("AdminStudentController createInstructor must be ADMIN only")
    void testCreateInstructorAdminOnly() throws NoSuchMethodException {
        Method createMethod = AdminStudentController.class.getMethod("createInstructor", com.vocabularyai.admin.dto.request.CreateInstructorRequest.class);
        PreAuthorize annotation = createMethod.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "createInstructor must have @PreAuthorize");
        assertEquals("hasRole('ADMIN')", annotation.value(), "createInstructor must be restricted to ADMIN only");
    }

    @Test
    @DisplayName("Quiz read allows INSTRUCTOR but mutations are ADMIN only in QuizController")
    void testQuizControllerEndpoints() throws NoSuchMethodException {
        Method getAllQuizzes = QuizController.class.getMethod("getAllQuizzes", User.class, Integer.class, Integer.class);
        PreAuthorize ann1 = getAllQuizzes.getAnnotation(PreAuthorize.class);
        assertNotNull(ann1);
        assertTrue(ann1.value().contains("INSTRUCTOR"));

        Method createQuiz = QuizController.class.getMethod("createQuiz", com.vocabularyai.quiz.dto.CreateQuizRequest.class);
        PreAuthorize ann2 = createQuiz.getAnnotation(PreAuthorize.class);
        assertNotNull(ann2);
        assertEquals("hasRole('ADMIN')", ann2.value());

        Method updateQuiz = QuizController.class.getMethod("updateQuiz", Long.class, com.vocabularyai.quiz.dto.CreateQuizRequest.class, String.class);
        PreAuthorize ann3 = updateQuiz.getAnnotation(PreAuthorize.class);
        assertNotNull(ann3);
        assertEquals("hasRole('ADMIN')", ann3.value());

        Method deleteQuiz = QuizController.class.getMethod("deleteQuiz", Long.class);
        PreAuthorize ann4 = deleteQuiz.getAnnotation(PreAuthorize.class);
        assertNotNull(ann4);
        assertEquals("hasRole('ADMIN')", ann4.value());
    }

    @Test
    @DisplayName("AdminQuizAttemptController mutation methods must be ADMIN only")
    void testQuizAttemptMutationsAdminOnly() throws NoSuchMethodException {
        Method createAttempt = AdminQuizAttemptController.class.getMethod("createAttempt", com.vocabularyai.admin.dto.request.CreateQuizAttemptRequest.class);
        PreAuthorize ann = createAttempt.getAnnotation(PreAuthorize.class);
        assertNotNull(ann);
        assertEquals("hasRole('ADMIN')", ann.value());

        Method updateAttempt = AdminQuizAttemptController.class.getMethod("updateAttempt", Long.class, com.vocabularyai.admin.dto.request.UpdateQuizAttemptRequest.class);
        PreAuthorize ann2 = updateAttempt.getAnnotation(PreAuthorize.class);
        assertNotNull(ann2);
        assertEquals("hasRole('ADMIN')", ann2.value());
    }
    @Test
    @DisplayName("Instructor querying role=ADMIN in AdminStudentController throws ForbiddenException")
    void testInstructorQueryingAdminRoleThrowsForbidden() {
        AdminStudentService mockService = mock(AdminStudentService.class);
        AdminStudentController controller = new AdminStudentController(mockService);

        User instructor = User.builder().id(99L).role(User.UserRole.INSTRUCTOR).build();

        assertThrows(ForbiddenException.class, () -> {
            controller.getStudents(instructor, 0, 20, "createdAt,desc", null, null, "ADMIN");
        });
    }

}
