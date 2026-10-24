package com.vocabularyai.notification.repository;

import com.vocabularyai.notification.entity.EmailReminderLog;
import com.vocabularyai.notification.entity.ReminderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface EmailReminderLogRepository extends JpaRepository<EmailReminderLog, Long> {

    boolean existsByUserIdAndTypeAndSentDateAndStatus(
            Long userId,
            ReminderType type,
            LocalDate sentDate,
            String status
    );

    boolean existsBySubscriptionIdAndTypeAndStatus(
            Long subscriptionId,
            ReminderType type,
            String status
    );

    @Query("SELECT COUNT(l) FROM EmailReminderLog l " +
           "WHERE l.userId = :userId AND l.type = :type " +
           "AND l.sentDate BETWEEN :startDate AND :endDate AND l.status = 'SENT'")
    long countSentReminders(
            @Param("userId") Long userId,
            @Param("type") ReminderType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT MAX(l.sentDate) FROM EmailReminderLog l " +
           "WHERE l.userId = :userId AND l.type = :type AND l.status = 'SENT'")
    Optional<LocalDate> findLatestSentDate(
            @Param("userId") Long userId,
            @Param("type") ReminderType type
    );
}
