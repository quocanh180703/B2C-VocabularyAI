import { useState, useEffect } from "react"
import { Button, Modal } from "../common/ui"
import { useFormValidation } from "../../hooks/useFormValidation"
import { packageValidationSchema, validateForm } from "../../utils/ValidationSchema"
import { useAppStore } from "../../store/useAppStore"

export interface PackageFormData {
  code: string
  name: string
  price: number
  durationDays: number
  status: string
  description?: string
}

interface PackageFormModalProps {
  open: boolean
  onClose: () => void
  onSubmit: (data: PackageFormData) => Promise<void>
  initialData?: Partial<PackageFormData>
  mode: "create" | "edit"
}

const statusOptions = ["ACTIVE", "INACTIVE", "ARCHIVED"]

export default function PackageFormModal({
  open,
  onClose,
  onSubmit,
  initialData,
  mode,
}: PackageFormModalProps) {
  const { notify } = useAppStore()
  const [isLoading, setIsLoading] = useState(false)

  const { values, errors, touched, handleChange, handleBlur, handleSubmit, resetForm } = useFormValidation<PackageFormData>({
    initialValues: initialData || {
      code: "",
      name: "",
      price: 0,
      durationDays: 30,
      status: "ACTIVE",
      description: "",
    },
    validate: (vals) => validateForm(vals, packageValidationSchema),
    onSubmit: async (data) => {
      try {
        setIsLoading(true)
        await onSubmit(data)
        notify(`Package ${mode === "create" ? "created" : "updated"} successfully`, "success")
        resetForm()
        onClose()
      } catch (error) {
        notify(`Failed to ${mode} package: ${error}`, "error")
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
      title={mode === "create" ? "Create Package" : "Edit Package"}
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
        {mode === "create" && (
          <div>
            <label className="block text-sm font-medium mb-1">
              Code <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              name="code"
              value={values.code}
              onChange={handleChange}
              onBlur={handleBlur}
              placeholder="e.g., PREMIUM_MONTHLY"
              className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                touched.code && errors.code ? "border-red-500" : "border-gray-300"
              }`}
              disabled={isLoading || mode === "edit"}
            />
            {touched.code && errors.code && <p className="text-red-500 text-xs mt-1">{errors.code}</p>}
          </div>
        )}

        <div>
          <label className="block text-sm font-medium mb-1">
            Name <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            name="name"
            value={values.name}
            onChange={handleChange}
            onBlur={handleBlur}
            placeholder="e.g., Premium Monthly"
            className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
              touched.name && errors.name ? "border-red-500" : "border-gray-300"
            }`}
            disabled={isLoading}
          />
          {touched.name && errors.name && <p className="text-red-500 text-xs mt-1">{errors.name}</p>}
        </div>

        <div className="grid grid-cols-2 gap-4">
          {/* Price */}
          <div>
            <label className="block text-sm font-medium mb-1">
              Price (VND) <span className="text-red-500">*</span>
            </label>
            <input
              type="number"
              name="price"
              value={values.price}
              onChange={handleChange}
              onBlur={handleBlur}
              min="0"
              step="1000"
              className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                touched.price && errors.price ? "border-red-500" : "border-gray-300"
              }`}
              disabled={isLoading}
            />
            {touched.price && errors.price && <p className="text-red-500 text-xs mt-1">{errors.price}</p>}
          </div>

          {/* Duration Days */}
          <div>
            <label className="block text-sm font-medium mb-1">
              Duration (days) <span className="text-red-500">*</span>
            </label>
            <input
              type="number"
              name="durationDays"
              value={values.durationDays}
              onChange={handleChange}
              onBlur={handleBlur}
              min="1"
              className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                touched.durationDays && errors.durationDays ? "border-red-500" : "border-gray-300"
              }`}
              disabled={isLoading}
            />
            {touched.durationDays && errors.durationDays && (
              <p className="text-red-500 text-xs mt-1">{errors.durationDays}</p>
            )}
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Description</label>
          <textarea
            name="description"
            value={values.description}
            onChange={handleChange}
            onBlur={handleBlur}
            placeholder="Package description..."
            rows={3}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            disabled={isLoading}
          />
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
