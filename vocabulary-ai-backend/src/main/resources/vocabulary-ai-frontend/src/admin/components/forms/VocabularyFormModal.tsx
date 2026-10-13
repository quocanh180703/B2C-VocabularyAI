import { useState, useEffect } from "react"
import { X } from "lucide-react"
import { Button, Modal } from "../common/ui"
import { useFormValidation } from "../../hooks/useFormValidation"
import { vocabularyValidationSchema, validateForm } from "../../utils/ValidationSchema"
import { useAppStore } from "../../store/useAppStore"

export interface VocabularyFormData {
  word: string
  meaning: string
  partOfSpeech: string
  level: string
  pronunciation?: string
  definition?: string
  example?: string
  audioUrl?: string
  imageUrl?: string
}

interface VocabularyFormModalProps {
  open: boolean
  onClose: () => void
  onSubmit: (data: VocabularyFormData) => Promise<void>
  initialData?: VocabularyFormData
  mode: "create" | "edit"
}

const partOfSpeechOptions = ["NOUN", "VERB", "ADJECTIVE", "ADVERB", "PREPOSITION", "PRONOUN", "ARTICLE", "CONJUNCTION", "INTERJECTION"]
const cefrLevelOptions = ["A1", "A2", "B1", "B2", "C1", "C2"]

export default function VocabularyFormModal({
  open,
  onClose,
  onSubmit,
  initialData,
  mode,
}: VocabularyFormModalProps) {
  const { notify } = useAppStore()
  const [isLoading, setIsLoading] = useState(false)

  const { values, errors, touched, handleChange, handleBlur, handleSubmit, resetForm } = useFormValidation<VocabularyFormData>({
    initialValues: initialData || {
      word: "",
      meaning: "",
      partOfSpeech: "",
      level: "",
      pronunciation: "",
      definition: "",
      example: "",
      audioUrl: "",
      imageUrl: "",
    },
    validate: (vals) => validateForm(vals, vocabularyValidationSchema),
    onSubmit: async (data) => {
      try {
        setIsLoading(true)
        await onSubmit(data)
        notify(`Vocabulary ${mode === "create" ? "created" : "updated"} successfully`, "success")
        resetForm()
        onClose()
      } catch (error) {
        notify(`Failed to ${mode} vocabulary: ${error}`, "error")
      } finally {
        setIsLoading(false)
      }
    },
  })

  // Reset form when modal opens/closes
  useEffect(() => {
    if (!open) {
      resetForm()
    }
  }, [open, resetForm])

  return (
    <Modal
      open={open}
      onClose={onClose}
      title={mode === "create" ? "Create Vocabulary" : "Edit Vocabulary"}
      wide
      footer={
        <div className="flex gap-2 justify-end">
          <Button variant="secondary" onClick={onClose} disabled={isLoading}>
            Cancel
          </Button>
          <Button onClick={handleSubmit} disabled={isLoading || Object.keys(errors).length > 0}>
            {isLoading ? "Saving..." : "Save"}
          </Button>
        </div>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Word */}
        <div>
          <label className="block text-sm font-medium mb-1">
            Word <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            name="word"
            value={values.word}
            onChange={handleChange}
            onBlur={handleBlur}
            placeholder="e.g., beautiful"
            className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
              touched.word && errors.word ? "border-red-500" : "border-gray-300"
            }`}
            disabled={isLoading}
          />
          {touched.word && errors.word && <p className="text-red-500 text-xs mt-1">{errors.word}</p>}
        </div>

        {/* Meaning */}
        <div>
          <label className="block text-sm font-medium mb-1">
            Meaning <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            name="meaning"
            value={values.meaning}
            onChange={handleChange}
            onBlur={handleBlur}
            placeholder="e.g., đẹp"
            className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
              touched.meaning && errors.meaning ? "border-red-500" : "border-gray-300"
            }`}
            disabled={isLoading}
          />
          {touched.meaning && errors.meaning && <p className="text-red-500 text-xs mt-1">{errors.meaning}</p>}
        </div>

        <div className="grid grid-cols-2 gap-4">
          {/* Part of Speech */}
          <div>
            <label className="block text-sm font-medium mb-1">
              Part of Speech <span className="text-red-500">*</span>
            </label>
            <select
              name="partOfSpeech"
              value={values.partOfSpeech}
              onChange={handleChange}
              onBlur={handleBlur}
              className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                touched.partOfSpeech && errors.partOfSpeech ? "border-red-500" : "border-gray-300"
              }`}
              disabled={isLoading}
            >
              <option value="">Select...</option>
              {partOfSpeechOptions.map((pos) => (
                <option key={pos} value={pos}>
                  {pos}
                </option>
              ))}
            </select>
            {touched.partOfSpeech && errors.partOfSpeech && (
              <p className="text-red-500 text-xs mt-1">{errors.partOfSpeech}</p>
            )}
          </div>

          {/* CEFR Level */}
          <div>
            <label className="block text-sm font-medium mb-1">
              CEFR Level <span className="text-red-500">*</span>
            </label>
            <select
              name="level"
              value={values.level}
              onChange={handleChange}
              onBlur={handleBlur}
              className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                touched.level && errors.level ? "border-red-500" : "border-gray-300"
              }`}
              disabled={isLoading}
            >
              <option value="">Select...</option>
              {cefrLevelOptions.map((level) => (
                <option key={level} value={level}>
                  {level}
                </option>
              ))}
            </select>
            {touched.level && errors.level && <p className="text-red-500 text-xs mt-1">{errors.level}</p>}
          </div>
        </div>

        {/* Pronunciation */}
        <div>
          <label className="block text-sm font-medium mb-1">Pronunciation</label>
          <input
            type="text"
            name="pronunciation"
            value={values.pronunciation || ""}
            onChange={handleChange}
            onBlur={handleBlur}
            placeholder="e.g., /ˈbjuːtɪfəl/"
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            disabled={isLoading}
          />
        </div>

        {/* Definition */}
        <div>
          <label className="block text-sm font-medium mb-1">Definition</label>
          <textarea
            name="definition"
            value={values.definition || ""}
            onChange={handleChange}
            onBlur={handleBlur}
            placeholder="English definition..."
            rows={3}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            disabled={isLoading}
          />
        </div>

        {/* Example */}
        <div>
          <label className="block text-sm font-medium mb-1">Example</label>
          <textarea
            name="example"
            value={values.example || ""}
            onChange={handleChange}
            onBlur={handleBlur}
            placeholder="Usage example..."
            rows={2}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            disabled={isLoading}
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          {/* Audio URL */}
          <div>
            <label className="block text-sm font-medium mb-1">Audio URL</label>
            <input
              type="url"
              name="audioUrl"
              value={values.audioUrl || ""}
              onChange={handleChange}
              onBlur={handleBlur}
              placeholder="https://..."
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              disabled={isLoading}
            />
          </div>

          {/* Image URL */}
          <div>
            <label className="block text-sm font-medium mb-1">Image URL</label>
            <input
              type="url"
              name="imageUrl"
              value={values.imageUrl || ""}
              onChange={handleChange}
              onBlur={handleBlur}
              placeholder="https://..."
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              disabled={isLoading}
            />
          </div>
        </div>
      </form>
    </Modal>
  )
}
