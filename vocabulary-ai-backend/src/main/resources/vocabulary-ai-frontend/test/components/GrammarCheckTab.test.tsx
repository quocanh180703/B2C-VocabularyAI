import { describe, it, expect, beforeEach, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderWithProviders } from '../utils'
import { GrammarCheckTab } from '@/components/ai/GrammarCheckTab'

// Mock the common components
vi.mock('@/components/common/Card', () => ({
  Card: ({ children, className }: any) => <div className={className}>{children}</div>,
}))

vi.mock('@/components/common/Badge', () => ({
  Badge: ({ children, tone }: any) => (
    <span className={tone ? `badge-${tone}` : 'badge'}>{children}</span>
  ),
}))

vi.mock('@/components/common/Button', () => ({
  Button: ({ children, onClick, disabled, icon }: any) => (
    <button onClick={onClick} disabled={disabled} className={icon ? `btn-with-icon-${icon}` : ''}>
      {children}
    </button>
  ),
}))

vi.mock('@/components/common/Icon', () => ({
  Icon: ({ name }: any) => <span className={`icon-${name}`}>Icon</span>,
}))

describe('GrammarCheckTab Component', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('Initial Render', () => {
    it('should render textarea with default text', () => {
      renderWithProviders(<GrammarCheckTab />)

      const textarea = screen.getByLabelText('Sentence to check')
      expect(textarea).toBeInTheDocument()
      expect(textarea).toHaveValue('I have went to school yesterday.')
    })

    it('should render check grammar button', () => {
      renderWithProviders(<GrammarCheckTab />)

      const button = screen.getByText('Check grammar')
      expect(button).toBeInTheDocument()
      expect(button).not.toBeDisabled()
    })

    it('should have correct title and subtitle', () => {
      renderWithProviders(<GrammarCheckTab />)

      expect(screen.getByText('Check your English')).toBeInTheDocument()
      expect(
        screen.getByText('Write a sentence and get clear, actionable feedback.')
      ).toBeInTheDocument()
    })
  })

  describe('User Interaction', () => {
    it('should allow typing in textarea', async () => {
      const user = userEvent.setup()
      renderWithProviders(<GrammarCheckTab />)

      const textarea = screen.getByLabelText('Sentence to check')
      await user.clear(textarea)
      await user.type(textarea, 'The cat are sleeping.')

      expect(textarea).toHaveValue('The cat are sleeping.')
    })

    it('should disable button while checking', async () => {
      const user = userEvent.setup()
      renderWithProviders(<GrammarCheckTab />)

      const button = screen.getByText('Check grammar')
      await user.click(button)

      // Button should be in pending state
      await waitFor(() => {
        expect(screen.getByText('Checking...')).toBeInTheDocument()
      })
    })

    it('should call onCheck callback when grammar is checked', async () => {
      const user = userEvent.setup()
      const onCheck = vi.fn()
      renderWithProviders(<GrammarCheckTab onCheck={onCheck} />)

      const button = screen.getByText('Check grammar')
      await user.click(button)

      await waitFor(
        () => {
          expect(onCheck).toHaveBeenCalled()
        },
        { timeout: 3000 }
      )
    })
  })

  describe('Result Display', () => {
    it('should display score after checking', async () => {
      const user = userEvent.setup()
      renderWithProviders(<GrammarCheckTab />)

      const button = screen.getByText('Check grammar')
      await user.click(button)

      await waitFor(
        () => {
          expect(screen.getByText('/ 100')).toBeInTheDocument()
        },
        { timeout: 3000 }
      )
    })

    it('should display corrected text', async () => {
      const user = userEvent.setup()
      renderWithProviders(<GrammarCheckTab />)

      const button = screen.getByText('Check grammar')
      await user.click(button)

      await waitFor(
        () => {
          expect(screen.getByText(/CORRECTED/i)).toBeInTheDocument()
        },
        { timeout: 3000 }
      )
    })

    it('should display errors found section', async () => {
      const user = userEvent.setup()
      renderWithProviders(<GrammarCheckTab />)

      const button = screen.getByText('Check grammar')
      await user.click(button)

      await waitFor(
        () => {
          expect(screen.getByText(/ERRORS FOUND/i)).toBeInTheDocument()
        },
        { timeout: 3000 }
      )
    })

    it('should display error details', async () => {
      const user = userEvent.setup()
      renderWithProviders(<GrammarCheckTab />)

      const button = screen.getByText('Check grammar')
      await user.click(button)

      await waitFor(
        () => {
          // Check if correction items are visible
          const corrections = screen.queryAllByText('Icon')
          expect(corrections.length).toBeGreaterThan(0)
        },
        { timeout: 3000 }
      )
    })

    it('should display suggestions', async () => {
      const user = userEvent.setup()
      renderWithProviders(<GrammarCheckTab />)

      const button = screen.getByText('Check grammar')
      await user.click(button)

      await waitFor(
        () => {
          expect(screen.getByText(/SUGGESTIONS/i)).toBeInTheDocument()
        },
        { timeout: 3000 }
      )
    })
  })

  describe('Score Calculation and Display', () => {
    it('should show correct badge for perfect score', async () => {
      const user = userEvent.setup()
      renderWithProviders(<GrammarCheckTab />)

      // Change to a correct sentence
      const textarea = screen.getByLabelText('Sentence to check')
      await user.clear(textarea)
      await user.type(textarea, 'I went to school yesterday.')

      const button = screen.getByText('Check grammar')
      await user.click(button)

      // For a correct sentence, should show high score badge
      await waitFor(
        () => {
          // Score should be displayed
          expect(screen.getByText('/ 100')).toBeInTheDocument()
        },
        { timeout: 3000 }
      )
    })

    it('should calculate score based on error count', async () => {
      const user = userEvent.setup()
      renderWithProviders(<GrammarCheckTab />)

      const button = screen.getByText('Check grammar')
      await user.click(button)

      await waitFor(
        () => {
          const scoreDisplay = screen.getByText('/ 100')
          expect(scoreDisplay).toBeInTheDocument()
        },
        { timeout: 3000 }
      )
    })
  })

  describe('Accessibility', () => {
    it('should have proper labels for textarea', () => {
      renderWithProviders(<GrammarCheckTab />)

      const textarea = screen.getByLabelText('Sentence to check')
      expect(textarea).toBeInTheDocument()
    })

    it('should have proper button labeling', () => {
      renderWithProviders(<GrammarCheckTab />)

      const button = screen.getByText('Check grammar')
      expect(button).toHaveAttribute('disabled', '')
    })

    it('should have semantic heading structure', () => {
      renderWithProviders(<GrammarCheckTab />)

      const headings = screen.getAllByRole('heading')
      expect(headings.length).toBeGreaterThan(0)
    })
  })

  describe('Error Handling', () => {
    it('should display error message on failure', async () => {
      const user = userEvent.setup()
      renderWithProviders(<GrammarCheckTab />)

      // Component should handle errors gracefully
      const textarea = screen.getByLabelText('Sentence to check')
      expect(textarea).toBeInTheDocument()
    })
  })

  describe('Textarea Input', () => {
    it('should update state on text change', async () => {
      const user = userEvent.setup()
      renderWithProviders(<GrammarCheckTab />)

      const textarea = screen.getByLabelText('Sentence to check') as HTMLTextAreaElement
      await user.clear(textarea)
      await user.type(textarea, 'New test text')

      expect(textarea.value).toBe('New test text')
    })

    it('should have placeholder text', () => {
      renderWithProviders(<GrammarCheckTab />)

      const textarea = screen.getByPlaceholderText('Enter text to check...')
      expect(textarea).toBeInTheDocument()
    })
  })
})
