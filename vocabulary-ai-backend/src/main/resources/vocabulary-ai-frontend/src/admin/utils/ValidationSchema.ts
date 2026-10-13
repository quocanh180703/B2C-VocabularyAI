/**
 * Validation schemas for various forms
 */

export interface ValidationRule {
  required?: string
  minLength?: { value: number; message: string }
  maxLength?: { value: number; message: string }
  pattern?: { value: RegExp; message: string }
  custom?: (value: any) => string | undefined
}

export interface ValidationSchema {
  [field: string]: ValidationRule
}

/**
 * Vocabulary creation/update validation schema
 */
export const vocabularyValidationSchema: ValidationSchema = {
  word: {
    required: "Word is required",
    minLength: { value: 1, message: "Word must be at least 1 character" },
    maxLength: { value: 255, message: "Word must be less than 255 characters" },
  },
  meaning: {
    required: "Meaning is required",
    minLength: { value: 1, message: "Meaning must be at least 1 character" },
  },
  partOfSpeech: {
    required: "Part of speech is required",
  },
  level: {
    required: "CEFR level is required",
    pattern: {
      value: /^(A1|A2|B1|B2|C1|C2)$/,
      message: "CEFR level must be A1, A2, B1, B2, C1, or C2",
    },
  },
  pronunciation: {
    maxLength: { value: 200, message: "Pronunciation must be less than 200 characters" },
  },
}

/**
 * Quiz creation/update validation schema
 */
export const quizValidationSchema: ValidationSchema = {
  title: {
    required: "Quiz title is required",
    minLength: { value: 3, message: "Title must be at least 3 characters" },
    maxLength: { value: 255, message: "Title must be less than 255 characters" },
  },
  description: {
    maxLength: { value: 1000, message: "Description must be less than 1000 characters" },
  },
  level: {
    required: "CEFR level is required",
    pattern: {
      value: /^(A1|A2|B1|B2|C1|C2)$/,
      message: "CEFR level must be A1, A2, B1, B2, C1, or C2",
    },
  },
  category: {
    required: "Category is required",
  },
  timeLimit: {
    required: "Time limit is required",
    custom: (value) => {
      if (!Number.isInteger(Number(value)) || Number(value) <= 0) {
        return "Time limit must be a positive number"
      }
      return undefined
    },
  },
}

/**
 * Package creation/update validation schema
 */
export const packageValidationSchema: ValidationSchema = {
  name: {
    required: "Package name is required",
    minLength: { value: 3, message: "Name must be at least 3 characters" },
    maxLength: { value: 255, message: "Name must be less than 255 characters" },
  },
  code: {
    required: "Package code is required",
    pattern: {
      value: /^[A-Z_0-9]+$/,
      message: "Code must contain only uppercase letters, numbers, and underscores",
    },
  },
  price: {
    required: "Price is required",
    custom: (value) => {
      const numValue = parseFloat(value)
      if (isNaN(numValue) || numValue < 0) {
        return "Price must be a positive number"
      }
      return undefined
    },
  },
  durationDays: {
    required: "Duration is required",
    custom: (value) => {
      if (!Number.isInteger(Number(value)) || Number(value) < 1) {
        return "Duration must be at least 1 day"
      }
      return undefined
    },
  },
}

/**
 * Generic field validator
 */
export function validateField(value: any, rule: ValidationRule): string | undefined {
  // Check required
  if (rule.required && (!value || (typeof value === "string" && value.trim() === ""))) {
    return rule.required
  }

  // Check min length
  if (rule.minLength && value && value.length < rule.minLength.value) {
    return rule.minLength.message
  }

  // Check max length
  if (rule.maxLength && value && value.length > rule.maxLength.value) {
    return rule.maxLength.message
  }

  // Check pattern
  if (rule.pattern && value && !rule.pattern.value.test(value)) {
    return rule.pattern.message
  }

  // Check custom validator
  if (rule.custom) {
    return rule.custom(value)
  }

  return undefined
}

/**
 * Validate all form fields against schema
 */
export function validateForm(values: Record<string, any>, schema: ValidationSchema): Record<string, string> {
  const errors: Record<string, string> = {}

  Object.keys(schema).forEach((field) => {
    const error = validateField(values[field], schema[field])
    if (error) {
      errors[field] = error
    }
  })

  return errors
}


/**
 * Package creation/update validation schema
 */
export const packageValidationSchema: ValidationSchema = {
  code: {
    required: "Package code is required",
    pattern: {
      value: /^[A-Z_0-9]+$/,
      message: "Code must contain only uppercase letters, numbers, and underscores",
    },
  },
  name: {
    required: "Package name is required",
    minLength: { value: 3, message: "Name must be at least 3 characters" },
    maxLength: { value: 100, message: "Name must be less than 100 characters" },
  },
  price: {
    required: "Price is required",
    custom: (value) => {
      if (!Number.isFinite(Number(value)) || Number(value) <= 0) {
        return "Price must be a positive number"
      }
      return undefined
    },
  },
  durationDays: {
    required: "Duration is required",
    custom: (value) => {
      if (!Number.isInteger(Number(value)) || Number(value) < 1) {
        return "Duration must be at least 1 day"
      }
      return undefined
    },
  },
}


/**
 * Speaking Session validation schema
 */
export const speakingSessionValidationSchema: ValidationSchema = {
  topic: {
    required: "Topic is required",
    minLength: { value: 3, message: "Topic must be at least 3 characters" },
    maxLength: { value: 255, message: "Topic must be less than 255 characters" },
  },
  level: {
    required: "Level is required",
    pattern: {
      value: /^(A1|A2|B1|B2|C1|C2)$/,
      message: "Level must be A1-C2",
    },
  },
  status: {
    pattern: {
      value: /^(IN_PROGRESS|COMPLETED|CANCELLED)$/,
      message: "Status must be IN_PROGRESS, COMPLETED, or CANCELLED",
    },
  },
}
