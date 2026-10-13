/**
 * Environment Configuration
 * Tập hợp tất cả environment variables từ .env files
 */

interface EnvConfig {
  // API
  apiBaseUrl: string;
  apiTimeout: number;

  // App
  appName: string;
  appVersion: string;
  appEnv: "development" | "staging" | "production";

  // Features
  enableAnalytics: boolean;
  enableErrorTracking: boolean;
  debugMode: boolean;

  // Auth
  tokenKey: string;
  refreshTokenKey: string;

  // Pagination
  itemsPerPage: number;
  maxFileSize: number;

  // URLs
  supportEmail: string;
  privacyPolicyUrl: string;
  termsOfServiceUrl: string;

  // Derived
  isProduction: boolean;
  isDevelopment: boolean;
  isStaging: boolean;
}

const getEnvVariable = (key: string, defaultValue?: string): string => {
  const value = import.meta.env[`VITE_${key}`];
  if (!value && !defaultValue) {
    console.warn(`Environment variable VITE_${key} is not defined`);
    return defaultValue || "";
  }
  return value || defaultValue || "";
};

const getEnvNumber = (key: string, defaultValue?: number): number => {
  const value = import.meta.env[`VITE_${key}`];
  if (!value && defaultValue === undefined) {
    console.warn(`Environment variable VITE_${key} is not defined`);
    return 0;
  }
  return value ? parseInt(value, 10) : (defaultValue || 0);
};

const getEnvBoolean = (key: string, defaultValue?: boolean): boolean => {
  const value = import.meta.env[`VITE_${key}`];
  if (!value && defaultValue === undefined) {
    return false;
  }
  if (value === undefined) return defaultValue || false;
  return value === "true" || value === "1" || value === true;
};

const createEnvConfig = (): EnvConfig => {
  const appEnv = getEnvVariable("APP_ENV", "development") as "development" | "staging" | "production";
  const isDevelopment = appEnv === "development";
  const isStaging = appEnv === "staging";
  const isProduction = appEnv === "production";

  return {
    // API
    apiBaseUrl: getEnvVariable("API_BASE_URL", "http://localhost:8080/api"),
    apiTimeout: getEnvNumber("API_TIMEOUT", 30000),

    // App
    appName: getEnvVariable("APP_NAME", "VocabularyAI"),
    appVersion: getEnvVariable("APP_VERSION", "1.0.0"),
    appEnv,

    // Features
    enableAnalytics: getEnvBoolean("ENABLE_ANALYTICS", !isDevelopment),
    enableErrorTracking: getEnvBoolean("ENABLE_ERROR_TRACKING", !isDevelopment),
    debugMode: getEnvBoolean("DEBUG_MODE", isDevelopment),

    // Auth
    tokenKey: getEnvVariable("TOKEN_KEY", "vocabularyai_token"),
    refreshTokenKey: getEnvVariable("REFRESH_TOKEN_KEY", "vocabularyai_refresh_token"),

    // Pagination
    itemsPerPage: getEnvNumber("ITEMS_PER_PAGE", 20),
    maxFileSize: getEnvNumber("MAX_FILE_SIZE", 10485760),

    // URLs
    supportEmail: getEnvVariable("SUPPORT_EMAIL", "support@vocabularyai.com"),
    privacyPolicyUrl: getEnvVariable("PRIVACY_POLICY_URL", "https://vocabularyai.com/privacy"),
    termsOfServiceUrl: getEnvVariable("TERMS_OF_SERVICE_URL", "https://vocabularyai.com/terms"),

    // Derived
    isDevelopment,
    isStaging,
    isProduction,
  };
};

export const env: EnvConfig = createEnvConfig();

// Log environment info (chỉ trong development mode)
if (env.debugMode) {
  console.log("🔧 Environment Configuration:", {
    appName: env.appName,
    appVersion: env.appVersion,
    appEnv: env.appEnv,
    apiBaseUrl: env.apiBaseUrl,
    debugMode: env.debugMode,
  });
}
