import { useState, useEffect } from "react"
import { Button, Modal } from "../common/ui"
import { useFormValidation } from "../../hooks/useFormValidation"
import { quizValidationSchema, validateForm } from "../../utils/ValidationSchema"
import { useAppStore } from "../../store/useAppStore"

export interface QuizFormData {
  title: string
  description: string
  category: string
  level: string
  timeLimit: number
  status: string
}

interface QuizFormModalProps {
  open: boolean
  onClose: () => void
  onSubmit: (data: QuizFormData) => Promise<void>
  initialData?: Partial<QuizFormData>
  mode: "create" | "edit"
}

const cefrLevelOptions = ["A1", "A2", "B1", "B2", "C1", "C2"]
const statusOptions = ["DRAFT", "PUBLISHED", "ARCHIVED"]
const categoryOptions = ["Business", "General", "Academic", "Travel", "Entertainment", "Technology", "Health", "Sports"]

export default function QuizFormModal({
  open,
  onClose,
  onSubmit,
  initialData,
  mode,
}: QuizFormModalProps) {
  const { notify } = useAppStore()
  const [isLoading, setIsLoading] = useState(false)

  const { values, errors, touched, handleChange, handleBlur, handleSubmit, resetForm } = useFormValidation<QuizFormData>({
    initialValues: initialData || {
      title: "",
      description: "",
      category: "",
      level: "",
      timeLimit: 30,
      status: "DRAFT",
    },
    validate: (vals) => validateForm(vals, quizValidationSchema),
    onSubmit: async (data) => {
      try {
        setIsLoading(true)
        await onSubmit(data)
        notify(`Quiz ${mode === "create" ? "created" : "updated"} successfully`, "success")
        resetForm()
        onClose()
      } catch (error) {
        notify(`Failed to ${mode} quiz: ${error}`, "error")
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
      title={mode === "create" ? "Create Quiz" : "Edit Quiz"}
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
        {/* Title */}
        <div>
          <label className="block text-sm font-medium mb-1">
            Title <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            name="title"
            value={values.title}
            onChange={handleChange}
            onBlur={handleBlur}
            placeholder="e.g., English Vocabulary Quiz"
            className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
              touched.title && errors.title ? "border-red-500" : "border-gray-300"
            }`}
            disabled={isLoading}
          />
          {touched.title && errors.title && <p className="text-red-500 text-xs mt-1">{errors.title}</p>}
        </div>

        {/* Description */}
        <div>
          <label className="block text-sm font-medium mb-1">Description</label>
          <textarea
            name="description"
            value={values.description}
            onChange={handleChange}
            onBlur={handleBlur}
            placeholder="Quiz description..."
            rows={3}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            disabled={isLoading}
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          {/* Category */}
          <div>
            <label className="block text-sm font-medium mb-1">
              Category <span className="text-red-500">*</span>
            </label>
            <select
              name="category"
              value={values.category}
              onChange={handleChange}
              onBlur={handleBlur}
              className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                touched.category && errors.category ? "border-red-500" : "border-gray-300"
              }`}
              disabled={isLoading}
            >
              <option value="">Select...</option>
              {categoryOptions.map((cat) => (
                <option key={cat} value={cat}>
                  {cat}
                </option>
              ))}
            </select>
            {touched.category && errors.category && (
              <p className="text-red-500 text-xs mt-1">{errors.category}</p>
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

        <div>
          {/* Time Limit */}
          <label className="block text-sm font-medium mb-1">
            Time Limit (minutes) <span className="text-red-500">*</span>
          </label>
          <input
            type="number"
            name="timeLimit"
            value={values.timeLimit}
            onChange={handleChange}
            onBlur={handleBlur}
            min="1"
            className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
              touched.timeLimit && errors.timeLimit ? "border-red-500" : "border-gray-300"
            }`}
            disabled={isLoading}
          />
          {touched.timeLimit && errors.timeLimit && (
            <p className="text-red-500 text-xs mt-1">{errors.timeLimit}</p>
          )}
        </div>

        {/* Status */}
        <div>
          <label className="block text-sm font-medium mb-1">Status</label>
          <select
            name="status"
            value={values.status}
            onChange={handleChange}
            onBlur={handleBlur}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            disabled={isLoading}
          >
            {statusOptions.map((status) => (
              <option key={status} value={status}>
                {status}
              </option>
            ))}
          </select>
        </div>
      </form>
    </Modal>
  )
}
