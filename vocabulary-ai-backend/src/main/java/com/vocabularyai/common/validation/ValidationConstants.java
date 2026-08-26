package com.vocabularyai.common.validation;

/**
 * Constants for validation messages and constraints
 */
public class ValidationConstants {
    
    private ValidationConstants() {}
    
    // Username
    public static final String USERNAME_NOT_BLANK = "Username is required";
    public static final String USERNAME_SIZE = "Username must be between 3 and 100 characters";
    public static final String USERNAME_PATTERN = "Username can only contain letters, numbers, and underscores";
    public static final String USERNAME_REGEX = "^[a-zA-Z0-9_]{3,100}$";
    
    // Email
    public static final String EMAIL_NOT_BLANK = "Email is required";
    public static final String EMAIL_INVALID = "Email must be a valid email address";
    
    // Password
    public static final String PASSWORD_NOT_BLANK = "Password is required";
    public static final String PASSWORD_SIZE = "Password must be at least 8 characters";
    public static final String PASSWORD_PATTERN = "Password must contain at least one uppercase letter, one lowercase letter, and one number";
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$";
    
    // Full Name
    public static final String FULL_NAME_NOT_BLANK = "Full name is required";
    public static final String FULL_NAME_SIZE = "Full name must be between 2 and 150 characters";
    
    // Category
    public static final String CATEGORY_NAME_NOT_BLANK = "Category name is required";
    public static final String CATEGORY_NAME_SIZE = "Category name must be between 2 and 100 characters";
    
    // Word
    public static final String WORD_NOT_BLANK = "Word is required";
    public static final String WORD_SIZE = "Word must be between 1 and 255 characters";
    public static final String WORD_MEANING_NOT_BLANK = "Word meaning is required";
    public static final String WORD_LEVEL_NOT_NULL = "Word level is required";
    
    // Quiz
    public static final String QUIZ_TITLE_NOT_BLANK = "Quiz title is required";
    public static final String QUIZ_TITLE_SIZE = "Quiz title must be between 2 and 255 characters";
    public static final String QUIZ_LEVEL_NOT_NULL = "Quiz level is required";
    
    // Question
    public static final String QUESTION_CONTENT_NOT_BLANK = "Question content is required";
    public static final String QUESTION_OPTIONS_NOT_EMPTY = "Question must have at least 2 options";
    public static final String QUESTION_CORRECT_ANSWER_NOT_NULL = "Correct answer index is required";
    
    // AI Message
    public static final String AI_MESSAGE_CONTENT_NOT_BLANK = "Message content is required";
    public static final String AI_MESSAGE_CONTENT_SIZE = "Message content must be between 1 and 5000 characters";
    
    // Speaking
    public static final String SPEAKING_TOPIC_NOT_BLANK = "Speaking topic is required";
    public static final String SPEAKING_LEVEL_NOT_NULL = "Speaking level is required";
}
