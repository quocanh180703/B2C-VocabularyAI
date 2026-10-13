/**
 * Speaking Module Constants
 * Enums, constants, và display mappings cho Speaking module
 */

// ============================================
// SPEAKING LEVEL ENUM
// ============================================

export const SPEAKING_LEVELS = {
  A1: "A1",
  A2: "A2",
  B1: "B1",
  B2: "B2",
  C1: "C1",
  C2: "C2",
} as const;

export const SPEAKING_LEVEL_DISPLAY: Record<string, string> = {
  A1: "Sơ cấp 1",
  A2: "Sơ cấp 2",
  B1: "Trung cấp 1",
  B2: "Trung cấp 2",
  C1: "Cao cấp 1",
  C2: "Cao cấp 2",
};

export const SPEAKING_LEVEL_DESCRIPTIONS: Record<string, string> = {
  A1: "Beginner Level 1 - Very basic vocabulary and grammar",
  A2: "Beginner Level 2 - Basic vocabulary and simple sentences",
  B1: "Intermediate Level 1 - Conversational ability with some errors",
  B2: "Intermediate Level 2 - Fluent speech with some complex structures",
  C1: "Advanced Level 1 - Fluent and spontaneous speech",
  C2: "Advanced Level 2 - Native-like fluency and precision",
};

// ============================================
// SPEAKING STATUS ENUM
// ============================================

export const SPEAKING_STATUS = {
  IN_PROGRESS: "IN_PROGRESS",
  COMPLETED: "COMPLETED",
  CANCELLED: "CANCELLED",
} as const;

export const SPEAKING_STATUS_DISPLAY: Record<string, string> = {
  IN_PROGRESS: "Đang luyện tập",
  COMPLETED: "Hoàn thành",
  CANCELLED: "Bị hủy",
};

export const SPEAKING_STATUS_COLOR: Record<string, string> = {
  IN_PROGRESS: "bg-blue-100 text-blue-800",
  COMPLETED: "bg-green-100 text-green-800",
  CANCELLED: "bg-red-100 text-red-800",
};

// ============================================
// AUDIO CONSTRAINTS
// ============================================

export const AUDIO_CONSTRAINTS = {
  MAX_SIZE_BYTES: 52428800, // 50MB
  MAX_SIZE_MB: 50,
  ALLOWED_MIME_TYPES: [
    "audio/wav",
    "audio/mpeg",
    "audio/mp4",
    "audio/webm",
    "audio/ogg",
    "audio/flac",
  ],
  ALLOWED_EXTENSIONS: ["wav", "mp3", "m4a", "webm", "ogg", "flac"],
  ALLOWED_EXTENSIONS_DISPLAY: "WAV, MP3, M4A, WebM, OGG, FLAC",
} as const;

// ============================================
// RECORDING CONSTRAINTS
// ============================================

export const RECORDING_CONSTRAINTS = {
  MIN_DURATION_SECONDS: 3,
  MAX_DURATION_SECONDS: 300, // 5 minutes
  SAMPLE_RATE: 16000,
} as const;

// ============================================
// SCORE THRESHOLDS
// ============================================

export const SCORE_THRESHOLDS = {
  EXCELLENT: 85,
  GOOD: 70,
  FAIR: 50,
  POOR: 0,
} as const;

export const SCORE_LEVEL_DISPLAY: Record<string, string> = {
  EXCELLENT: "Xuất sắc",
  GOOD: "Tốt",
  FAIR: "Trung bình",
  POOR: "Yếu",
};

export function getScoreLevel(score: number): string {
  if (score >= SCORE_THRESHOLDS.EXCELLENT) return "EXCELLENT";
  if (score >= SCORE_THRESHOLDS.GOOD) return "GOOD";
  if (score >= SCORE_THRESHOLDS.FAIR) return "FAIR";
  return "POOR";
}

export function getScoreLevelColor(score: number): string {
  const level = getScoreLevel(score);
  const colorMap: Record<string, string> = {
    EXCELLENT: "text-green-600",
    GOOD: "text-blue-600",
    FAIR: "text-yellow-600",
    POOR: "text-red-600",
  };
  return colorMap[level] || "text-gray-600";
}

// ============================================
// SCORE DISPLAY LABELS
// ============================================

export const SCORE_LABELS = {
  OVERALL: "Điểm tổng thể",
  GRAMMAR: "Ngữ pháp",
  VOCABULARY: "Từ vựng",
  FLUENCY: "Trôi chảy",
  CONTENT: "Nội dung",
  PRONUNCIATION: "Phát âm",
} as const;

// ============================================
// ERROR MESSAGES
// ============================================

export const SPEAKING_ERROR_MESSAGES = {
  SESSION_NOT_FOUND: "Phiên luyện tập không tìm thấy",
  SESSION_NOT_OWNED: "Bạn không có quyền truy cập phiên này",
  AUDIO_REQUIRED: "Vui lòng ghi âm trước khi gửi",
  AUDIO_TOO_LARGE: `File audio không được vượt quá ${AUDIO_CONSTRAINTS.MAX_SIZE_MB}MB`,
  AUDIO_INVALID_TYPE: `Định dạng audio không được hỗ trợ. Vui lòng sử dụng: ${AUDIO_CONSTRAINTS.ALLOWED_EXTENSIONS_DISPLAY}`,
  RECORDING_TOO_SHORT: `Bài ghi âm phải dài ít nhất ${RECORDING_CONSTRAINTS.MIN_DURATION_SECONDS} giây`,
  RECORDING_TOO_LONG: `Bài ghi âm không được vượt quá ${RECORDING_CONSTRAINTS.MAX_DURATION_SECONDS} giây`,
  MICROPHONE_NOT_AVAILABLE: "Thiết bị ghi âm không khả dụng",
  MICROPHONE_PERMISSION_DENIED: "Bạn đã từ chối quyền truy cập microphone",
  SUBMISSION_FAILED: "Lỗi khi gửi bài. Vui lòng thử lại",
  NETWORK_ERROR: "Lỗi kết nối mạng",
  SERVER_ERROR: "Lỗi máy chủ. Vui lòng thử lại sau",
} as const;

// ============================================
// SUCCESS MESSAGES
// ============================================

export const SPEAKING_SUCCESS_MESSAGES = {
  SESSION_CREATED: "Phiên luyện tập đã được tạo",
  SESSION_COMPLETED: "Phiên luyện tập đã hoàn thành",
  SESSION_CANCELLED: "Phiên luyện tập đã bị hủy",
  ATTEMPT_SUBMITTED: "Bài ghi âm đã được gửi thành công",
  RECORDING_SAVED: "Bài ghi âm đã được lưu",
} as const;

// ============================================
// SORTING OPTIONS
// ============================================

export const FEEDBACK_SORT_OPTIONS = {
  BY_SCORE: "sorted-by-score",
  BY_GRAMMAR: "sorted-by-grammar",
  BY_VOCABULARY: "sorted-by-vocabulary",
} as const;

export const FEEDBACK_SORT_LABELS: Record<string, string> = {
  "sorted-by-score": "Điểm tổng thể",
  "sorted-by-grammar": "Ngữ pháp",
  "sorted-by-vocabulary": "Từ vựng",
};

// ============================================
// TREND VALUES
// ============================================

export const SCORE_TREND = {
  IMPROVING: "Improving",
  STABLE: "Stable",
  DECLINING: "Declining",
} as const;

export const SCORE_TREND_DISPLAY: Record<string, string> = {
  Improving: "Đang cải thiện ↗",
  Stable: "Ổn định →",
  Declining: "Đang giảm ↘",
};

export const SCORE_TREND_COLOR: Record<string, string> = {
  Improving: "text-green-600",
  Stable: "text-gray-600",
  Declining: "text-red-600",
};

// ============================================
// PAGINATION DEFAULTS
// ============================================

export const PAGINATION_DEFAULTS = {
  DEFAULT_PAGE: 0,
  DEFAULT_SIZE: 20,
  MAX_SIZE: 100,
} as const;

// ============================================
// ROUTES
// ============================================

export const SPEAKING_ROUTES = {
  SESSIONS: "/speaking/sessions",
  SESSION_DETAIL: (id: number) => `/speaking/sessions/${id}`,
  SESSION_CREATE: "/speaking/sessions/create",
  SESSION_PRACTICE: (id: number) => `/speaking/sessions/${id}/practice`,
  PROGRESS: "/speaking/progress",
  FEEDBACK: (attemptId: number) => `/speaking/feedback/${attemptId}`,
} as const;
