export type CefrLevel = "A1" | "A2" | "B1" | "B2" | "C1"
export type Status = "Active" | "Inactive" | "Draft" | "Banned"

export interface Vocabulary {
  id: number
  word: string
  meaning: string
  vietnameseMeaning: string
  pronunciation: string
  partOfSpeech: string
  cefr: CefrLevel
  example: string
  exampleTranslation: string
  synonyms: string
  antonyms: string
  hasAiExplanation: boolean
  status: Status
  createdAt: string
}

export interface Student {
  id: number
  name: string
  username: string
  email: string
  cefr: CefrLevel
  wordsLearned: number
  subscription: string
  status: Status
  createdAt: string
  avatar: string
}

export interface AdminUser {
  id: number
  name: string
  email: string
  role: "ADMIN" | "STUDENT"
  avatar: string
}

export interface TableRecord {
  id: string | number
  [key: string]: string | number | boolean
}
