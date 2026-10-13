import { useState, useCallback } from "react"

export interface FormErrors {
  [key: string]: string
}

export interface FormTouched {
  [key: string]: boolean
}

export interface UseFormValidationProps<T> {
  initialValues: T
  onSubmit: (values: T) => Promise<void> | void
  validate?: (values: T) => FormErrors
}

export interface UseFormValidationResult<T> {
  values: T
  errors: FormErrors
  touched: FormTouched
  isSubmitting: boolean
  setFieldValue: (field: keyof T, value: any) => void
  setFieldTouched: (field: keyof T, touched: boolean) => void
  handleChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => void
  handleBlur: (e: React.FocusEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => void
  handleSubmit: (e: React.FormEvent) => Promise<void>
  resetForm: () => void
}

/**
 * Custom hook for form state management and validation
 * Similar to Formik but lighter weight for our use case
 */
export function useFormValidation<T extends Record<string, any>>({
  initialValues,
  onSubmit,
  validate,
}: UseFormValidationProps<T>): UseFormValidationResult<T> {
  const [values, setValues] = useState<T>(initialValues)
  const [errors, setErrors] = useState<FormErrors>({})
  const [touched, setTouched] = useState<FormTouched>({})
  const [isSubmitting, setIsSubmitting] = useState(false)

  const validateForm = useCallback(
    (valuesToValidate: T): FormErrors => {
      const newErrors: FormErrors = {}
      if (validate) {
        const validationErrors = validate(valuesToValidate)
        Object.assign(newErrors, validationErrors)
      }
      return newErrors
    },
    [validate]
  )

  const setFieldValue = useCallback(
    (field: keyof T, value: any) => {
      setValues((prev) => {
        const updated = { ...prev, [field]: value }
        // Validate field on change if already touched
        if (touched[field as string]) {
          const newErrors = validateForm(updated)
          setErrors(newErrors)
        }
        return updated
      })
    },
    [touched, validateForm]
  )

  const setFieldTouched = useCallback((field: keyof T, isTouched: boolean) => {
    setTouched((prev) => ({ ...prev, [field]: isTouched }))
  }, [])

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
      const { name, value, type } = e.target
      setFieldValue(name as keyof T, type === "checkbox" ? (e.target as HTMLInputElement).checked : value)
    },
    [setFieldValue]
  )

  const handleBlur = useCallback(
    (e: React.FocusEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
      const { name } = e.target
      setFieldTouched(name as keyof T, true)
    },
    [setFieldTouched]
  )

  const handleSubmit = useCallback(
    async (e: React.FormEvent) => {
      e.preventDefault()

      // Validate all fields
      const newErrors = validateForm(values)
      setErrors(newErrors)

      // Mark all fields as touched
      const newTouched: FormTouched = {}
      Object.keys(values).forEach((key) => {
        newTouched[key] = true
      })
      setTouched(newTouched)

      // If there are errors, don't submit
      if (Object.keys(newErrors).length > 0) {
        return
      }

      try {
        setIsSubmitting(true)
        await onSubmit(values)
      } catch (error) {
        // Error handling is typically done in the onSubmit callback
        console.error("Form submission error:", error)
      } finally {
        setIsSubmitting(false)
      }
    },
    [values, validateForm, onSubmit]
  )

  const resetForm = useCallback(() => {
    setValues(initialValues)
    setErrors({})
    setTouched({})
    setIsSubmitting(false)
  }, [initialValues])

  return {
    values,
    errors,
    touched,
    isSubmitting,
    setFieldValue,
    setFieldTouched,
    handleChange,
    handleBlur,
    handleSubmit,
    resetForm,
  }
}
