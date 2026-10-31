package com.vocabularyai.ai.service.chat;

import com.vocabularyai.ai.dto.UserSnapshotDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * Formats UserSnapshotDto into a clean, markdown-friendly text block
 * for direct injection into Gemini system context.
 */
@Component
public class UserSnapshotFormatter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public String format(UserSnapshotDto snapshot) {
        if (snapshot == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        String generatedAt = LocalDateTime.now(VN_ZONE).format(DATE_TIME_FMT);

        sb.append("=== USER DATA — VERIFIED ===\n");
        sb.append("Computed by server on ").append(generatedAt).append(" (Asia/Ho_Chi_Minh) for user: ")
          .append(snapshot.getDisplayName() != null ? snapshot.getDisplayName() : "User")
          .append(" (ID: ").append(snapshot.getUserId()).append(").\n");
        sb.append("These are the ONLY true, verified and authoritative numbers about this user. DO NOT alter or guess.\n\n");

        // [1. Tài khoản & Trình độ]
        sb.append("[TÀI KHOẢN & TRÌNH ĐỘ]\n");
        sb.append("- Tên người dùng: ").append(snapshot.getDisplayName() != null ? snapshot.getDisplayName() : "Chưa đặt tên").append("\n");
        if (snapshot.getAccountCreatedAt() != null) {
            sb.append("- Ngày tạo tài khoản: ").append(snapshot.getAccountCreatedAt().format(DATE_FMT))
              .append(" (đã tham gia ").append(snapshot.getDaysSinceJoined() != null ? snapshot.getDaysSinceJoined() : 0).append(" ngày)\n");
        }
        sb.append("- Trình độ hiệu dụng: ").append(snapshot.getEffectiveLevel() != null ? snapshot.getEffectiveLevel() : "A1")
          .append(" (Ước lượng qua quá trình học: ").append(snapshot.getEstimatedLevel() != null ? snapshot.getEstimatedLevel() : "A1")
          .append("; Kiểm tra đầu vào (Placement): ");
        if (snapshot.getPlacementLevel() != null) {
            sb.append(snapshot.getPlacementLevel());
            if (snapshot.getPlacementCompletedAt() != null) {
                sb.append(" làm ngày ").append(snapshot.getPlacementCompletedAt().format(DATE_FMT));
            }
        } else {
            sb.append("Chưa làm bài kiểm tra đầu vào");
        }
        sb.append(")\n\n");

        // [2. Hoạt động & Chuỗi học (Streak)]
        sb.append("[HOẠT ĐỘNG & STREAK]\n");
        sb.append("- Chuỗi học liên tục hiện tại: ").append(snapshot.getCurrentStreak() != null ? snapshot.getCurrentStreak() : 0).append(" ngày liên tiếp\n");
        sb.append("- Chuỗi kỷ lục dài nhất: ").append(snapshot.getLongestStreak() != null ? snapshot.getLongestStreak() : 0).append(" ngày\n");
        sb.append("- Tổng số ngày thực sự có học: ").append(snapshot.getActiveLearningDays() != null ? snapshot.getActiveLearningDays() : 0).append(" ngày\n");
        sb.append("- Tổng số hoạt động từ trước đến nay: ").append(snapshot.getTotalActivities() != null ? snapshot.getTotalActivities() : 0).append(" lượt\n");
        sb.append("- Hoạt động hôm nay: ").append(snapshot.getTodayTotalActivities() != null ? snapshot.getTodayTotalActivities() : 0).append(" lượt");
        if (snapshot.getTodayActivitiesByType() != null && !snapshot.getTodayActivitiesByType().isEmpty()) {
            String breakdown = snapshot.getTodayActivitiesByType().entrySet().stream()
                    .map(e -> e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining(", "));
            sb.append(" (Chi tiết: ").append(breakdown).append(")");
        }
        sb.append("\n\n");

        // [3. Tiến độ từ vựng (SRS)]
        sb.append("[TIẾN ĐỘ TỪ VỰNG (SRS)]\n");
        sb.append("- Tổng từ vựng đã học: ").append(snapshot.getWordsLearned() != null ? snapshot.getWordsLearned() : 0)
          .append(" từ (Đang học: ").append(snapshot.getWordsLearning() != null ? snapshot.getWordsLearning() : 0)
          .append(", Đã thuộc: ").append(snapshot.getWordsMastered() != null ? snapshot.getWordsMastered() : 0).append(")\n");
        sb.append("- Từ đến hạn ôn hôm nay: ").append(snapshot.getReviewsDueToday() != null ? snapshot.getReviewsDueToday() : 0).append(" từ");
        if (snapshot.getReviewsOverdue() != null && snapshot.getReviewsOverdue() > 0) {
            sb.append(" (Trong đó có ").append(snapshot.getReviewsOverdue()).append(" từ quá hạn)");
        }
        sb.append("\n");
        sb.append("- Số từ yêu thích đã lưu: ").append(snapshot.getFavoriteWordsCount() != null ? snapshot.getFavoriteWordsCount() : 0).append(" từ\n");
        sb.append("- Top từ hay quên nhất: ");
        if (snapshot.getTopForgottenWords() != null && !snapshot.getTopForgottenWords().isEmpty()) {
            String forgotten = snapshot.getTopForgottenWords().stream()
                    .map(f -> f.getWord() + " (" + f.getWrongCount() + " lần sai)")
                    .collect(Collectors.joining(", "));
            sb.append(forgotten);
        } else {
            sb.append("Chưa có từ nào bị ghi nhận sai nhiều lần");
        }
        sb.append("\n\n");

        // [4. Xu hướng 7 ngày gần đây]
        sb.append("[XU HƯỚNG 7 NGÀY GẦN ĐÂY]\n");
        sb.append("- Số hoạt động 7 ngày qua: ").append(snapshot.getActivitiesLast7Days() != null ? snapshot.getActivitiesLast7Days() : 0)
          .append(" lượt (So với 7 ngày trước đó: ").append(snapshot.getActivitiesPrev7Days() != null ? snapshot.getActivitiesPrev7Days() : 0).append(" lượt)\n");
        if (snapshot.getAccuracyRateLast7Days() != null) {
            sb.append("- Độ chính xác 7 ngày qua: ").append(String.format("%.1f%%", snapshot.getAccuracyRateLast7Days())).append("\n");
        } else {
            sb.append("- Độ chính xác 7 ngày qua: Chưa có dữ liệu bài làm\n");
        }
        sb.append("\n");

        // [5. Luyện nói & Trắc nghiệm (Quiz)]
        sb.append("[LUYỆN NÓI & TRẮC NGHIỆM]\n");
        sb.append("- Luyện nói: ").append(snapshot.getSpeakingSessionsCount() != null ? snapshot.getSpeakingSessionsCount() : 0).append(" buổi hoàn thành. ");
        if (snapshot.getLatestSpeakingScore() != null) {
            sb.append("Điểm buổi gần nhất: ").append(String.format("%.1f/100", snapshot.getLatestSpeakingScore()));
            if (snapshot.getLatestSpeakingAt() != null) {
                sb.append(" (ngày ").append(snapshot.getLatestSpeakingAt().format(DATE_FMT)).append(")");
            }
            sb.append(". ");
        }
        if (snapshot.getMaxSpeakingScore() != null) {
            sb.append("Điểm nói cao nhất: ").append(String.format("%.1f/100", snapshot.getMaxSpeakingScore())).append(".");
        }
        sb.append("\n");

        sb.append("- Trắc nghiệm (Quiz): ").append(snapshot.getQuizzesCount() != null ? snapshot.getQuizzesCount() : 0).append(" bài đã làm. ");
        if (snapshot.getQuizAverageScore() != null) {
            sb.append("Điểm trung bình: ").append(String.format("%.1f%%", snapshot.getQuizAverageScore())).append(". ");
        }
        if (snapshot.getLatestQuizScore() != null) {
            sb.append("Điểm bài gần nhất: ").append(String.format("%.1f%%", snapshot.getLatestQuizScore()));
            if (snapshot.getLatestQuizAt() != null) {
                sb.append(" (ngày ").append(snapshot.getLatestQuizAt().format(DATE_FMT)).append(")");
            }
            sb.append(".");
        }
        sb.append("\n\n");

        // [6. Gói cước & Lượt dùng hôm nay]
        sb.append("[GÓI CƯỚC & LƯỢT DÙNG AI HÔM NAY]\n");
        sb.append("- Gói tài khoản: ").append(snapshot.getSubscriptionPlan() != null ? snapshot.getSubscriptionPlan() : "FREE");
        if (Boolean.TRUE.equals(snapshot.getIsVip())) {
            sb.append(" (Gói Premium VIP");
            if (snapshot.getSubscriptionExpiresAt() != null) {
                sb.append(", có hạn đến ").append(snapshot.getSubscriptionExpiresAt().format(DATE_FMT));
            }
            sb.append(")\n");
            sb.append("- Lượt dùng AI hôm nay: Không giới hạn tất cả các tính năng AI\n");
        } else {
            sb.append(" (Gói Miễn phí)\n");
            if (snapshot.getRemainingQuotas() != null && !snapshot.getRemainingQuotas().isEmpty()) {
                String quotaStr = snapshot.getRemainingQuotas().entrySet().stream()
                        .map(e -> e.getKey() + ": còn " + e.getValue() + " lượt")
                        .collect(Collectors.joining(", "));
                sb.append("- Lượt dùng AI còn lại hôm nay: ").append(quotaStr).append("\n");
            }
        }
        sb.append("=== END USER DATA ===");

        return sb.toString();
    }
}
