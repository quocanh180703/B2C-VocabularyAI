/**
 * API Services Export
 * Tập hợp tất cả API services
 */

export { apiClient } from "./client";
export * from "./types";
export * from "./endpoints";
export * from "./payment.types";
export { AuthService } from "./auth.service";
export { VocabularyService } from "./vocabulary.service";
export { AIService } from "./ai.service";
export { PaymentService } from "./payment.service";

// Service instances
import { AuthService } from "./auth.service";
import { VocabularyService } from "./vocabulary.service";
import { AIService } from "./ai.service";
import { PaymentService } from "./payment.service";

export const authService = AuthService;
export const vocabularyService = VocabularyService;
export const aiService = AIService;
export const paymentService = PaymentService;
