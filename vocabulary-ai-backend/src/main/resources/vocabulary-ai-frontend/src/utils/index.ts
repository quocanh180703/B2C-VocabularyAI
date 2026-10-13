/**
 * Utility functions for VocabularyAI
 */

/**
 * Format date to "Monday, June 24" format
 */
export const formatDateGreeting = (date: Date = new Date()): string => {
  const days = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
  const months = [
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
  ];
  
  const dayName = days[date.getDay()];
  const monthName = months[date.getMonth()];
  const dayNum = date.getDate();
  
  return `${dayName}, ${monthName} ${dayNum}`;
};

/**
 * Capitalize first letter
 */
export const capitalize = (str: string): string => {
  return str.charAt(0).toUpperCase() + str.slice(1);
};

/**
 * Format percentage for display
 */
export const formatPercentage = (value: number): string => {
  return Math.round(value) + "%";
};

/**
 * Truncate text with ellipsis
 */
export const truncate = (text: string, length: number): string => {
  if (text.length <= length) return text;
  return text.slice(0, length) + "...";
};

/**
 * Debounce function
 */
export const debounce = <T extends (...args: any[]) => any>(
  func: T,
  delay: number
): ((...args: Parameters<T>) => void) => {
  let timeoutId: NodeJS.Timeout;
  return (...args: Parameters<T>) => {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => func(...args), delay);
  };
};

/**
 * Generate random item from array
 */
export const randomItem = <T>(arr: T[]): T => {
  return arr[Math.floor(Math.random() * arr.length)];
};

/**
 * Get initials from name (e.g., "John Doe" => "JD")
 */
export const getInitials = (firstName: string, lastName: string): string => {
  return (firstName.charAt(0) + lastName.charAt(0)).toUpperCase();
};

/**
 * Filter array by search query
 */
export const filterByQuery = <T extends Record<string, any>>(
  items: T[],
  query: string,
  searchFields: (keyof T)[]
): T[] => {
  if (!query.trim()) return items;
  
  const lowerQuery = query.toLowerCase();
  return items.filter((item) =>
    searchFields.some((field) =>
      String(item[field]).toLowerCase().includes(lowerQuery)
    )
  );
};

/**
 * Merge class names
 */
export const mergeClasses = (...classes: (string | undefined)[]): string => {
  return classes.filter(Boolean).join(" ");
};

/**
 * Get current hour greeting
 */
export const getTimeGreeting = (): string => {
  const hour = new Date().getHours();
  if (hour < 12) return "Good morning";
  if (hour < 18) return "Good afternoon";
  return "Good evening";
};

/**
 * Mock data for development (no API integration)
 */

export const MOCK_WORDS = [
  {
    word: "abandon",
    phonetic: "/əˈbændən/",
    meaning: "to leave someone or something completely",
    translation: "từ bỏ, bỏ rơi",
    level: "B1",
    type: "Verb",
    category: "Business",
  },
  {
    word: "achieve",
    phonetic: "/əˈtʃiːv/",
    meaning: "to succeed in reaching a goal",
    translation: "đạt được",
    level: "B1",
    type: "Verb",
    category: "Daily life",
  },
  {
    word: "accurate",
    phonetic: "/ˈækjərət/",
    meaning: "correct and without any mistakes",
    translation: "chính xác",
    level: "B2",
    type: "Adjective",
    category: "Technology",
  },
  {
    word: "sustainable",
    phonetic: "/səˈsteɪnəbəl/",
    meaning: "able to continue over a period of time",
    translation: "bền vững",
    level: "B2",
    type: "Adjective",
    category: "Business",
  },
  {
    word: "negotiate",
    phonetic: "/nɪˈɡəʊʃieɪt/",
    meaning: "to discuss in order to reach an agreement",
    translation: "đàm phán",
    level: "B2",
    type: "Verb",
    category: "Business",
  },
  {
    word: "itinerary",
    phonetic: "/aɪˈtɪnərəri/",
    meaning: "a planned route or journey",
    translation: "lịch trình",
    level: "B1",
    type: "Noun",
    category: "Travel",
  },
];
