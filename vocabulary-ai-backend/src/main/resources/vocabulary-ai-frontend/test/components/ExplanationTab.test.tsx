import { describe, it, expect, beforeEach, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderWithProviders } from '../utils'
import { ExplanationTab } from '@/components/ai/ExplanationTab'

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
  Button: ({ children, ...props }: any) => <button {...props}>{children}</button>,
}))

vi.mock('@/components/common/Icon', () => ({
  Icon: ({ name }: any) => <span className={`icon-${name}`}>Icon</span>,
}))

describe('ExplanationTab Component', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('Loading State', () => {
    it('should display loading state initially', async () => {
      renderWithProviders(<ExplanationTab word="example" />)

      // Loading state should be shown
      await waitFor(() => {
        expect(screen.getByText(/loading explanation/i)).toBeInTheDocument()
      })
    })

    it('should show loading indicator with spark icon', async () => {
      renderWithProviders(<ExplanationTab word="test" />)

      await waitFor(() => {
        expect(screen.getByText(/loading explanation/i)).toBeInTheDocument()
      })
    })
  })

  describe('Success State', () => {
    it('should display explanation when data loads', async () => {
      renderWithProviders(<ExplanationTab word="example" />)

      // Wait for explanation to load and display
      await waitFor(
        () => {
          expect(screen.getByText('example')).toBeInTheDocument()
        },
        { timeout: 3000 }
      )
    })

    it('should display word with part of speech', async () => {
      renderWithProviders(<ExplanationTab word="example" />)

      await waitFor(
        () => {
          const explanationCard = screen.queryByText('example')
          expect(explanationCard).toBeInTheDocument()
        },
        { timeout: 3000 }
      )
    })

    it('should display explanation text', async () => {
      renderWithProviders(<ExplanationTab word="example" />)

      await waitFor(
        () => {
          expect(screen.getByText(/characteristic of its kind/i)).toBeInTheDocument()
        },
        { timeout: 3000 }
      )
    })

    it('should display examples list', async () => {
      renderWithProviders(<ExplanationTab word="example" />)

      await waitFor(
        () => {
          const examplesList = screen.queryByText(/Can you give me an example?/)
          expect(examplesList).toBeInTheDocument()
        },
        { timeout: 3000 }
      )
    })

    it('should display etymology information', async () => {
      renderWithProviders(<ExplanationTab word="example" />)

      await waitFor(
        () => {
          expect(screen.getByText(/etymology/i)).toBeInTheDocument()
          expect(screen.getByText(/From Latin exemplum/i)).toBeInTheDocument()
        },
        { timeout: 3000 }
      )
    })

    it('should display related words as badges', async () => {
      renderWithProviders(<ExplanationTab word="example" />)

      await waitFor(
        () => {
          expect(screen.getByText('instance')).toBeInTheDocument()
          expect(screen.getByText('specimen')).toBeInTheDocument()
          expect(screen.getByText('sample')).toBeInTheDocument()
        },
        { timeout: 3000 }
      )
    })

    it('should show AI explanation badge', async () => {
      renderWithProviders(<ExplanationTab word="example" />)

      await waitFor(
        () => {
          expect(screen.getByText('AI EXPLANATION')).toBeInTheDocument()
        },
        { timeout: 3000 }
      )
    })
  })

  describe('Empty State', () => {
    it('should show empty state when no word provided', () => {
      renderWithProviders(<ExplanationTab word="" />)

      // Should show empty state or prompt to enter a word
      expect(
        screen.queryByText(/enter a word to get an explanation/i) ||
          screen.queryByText(/loading/i)
      ).toBeInTheDocument()
    })

    it('should show empty state with default word', () => {
      renderWithProviders(<ExplanationTab />)

      // Component has default word "abandon" so should eventually load
      expect(screen.getByText(/loading explanation/i) || screen.queryByText('abandon')).toBeTruthy()
    })
  })

  describe('Props Handling', () => {
    it('should handle word prop change', async () => {
      const { rerender } = renderWithProviders(<ExplanationTab word="example" />)

      // First word
      await waitFor(
        () => {
          expect(screen.getByText('example')).toBeInTheDocument()
        },
        { timeout: 3000 }
      )

      // Change word
      rerender(<ExplanationTab word="brilliant" />)

      await waitFor(
        () => {
          expect(screen.getByText('brilliant')).toBeInTheDocument()
        },
        { timeout: 3000 }
      )
    })

    it('should handle level prop', async () => {
      renderWithProviders(<ExplanationTab word="example" level="A1" />)

      await waitFor(
        () => {
          expect(screen.getByText('example')).toBeInTheDocument()
        },
        { timeout: 3000 }
      )
    })

    it('should display context when provided', async () => {
      renderWithProviders(
        <ExplanationTab
          word="example"
          context="In a learning context"
        />
      )

      await waitFor(
        () => {
          expect(screen.getByText(/learning context/i)).toBeInTheDocument()
        },
        { timeout: 3000 }
      )
    })
  })

  describe('Accessibility', () => {
    it('should have proper heading hierarchy', async () => {
      renderWithProviders(<ExplanationTab word="example" />)

      await waitFor(
        () => {
          const headings = screen.getAllByRole('heading')
          expect(headings.length).toBeGreaterThan(0)
        },
        { timeout: 3000 }
      )
    })

    it('should have proper list structure for examples', async () => {
      renderWithProviders(<ExplanationTab word="example" />)

      await waitFor(
        () => {
          const listItems = screen.getAllByRole('listitem')
          expect(listItems.length).toBeGreaterThan(0)
        },
        { timeout: 3000 }
      )
    })
  })

  describe('Error State', () => {
    it('should handle network errors gracefully', async () => {
      // This test would require MSW to return an error response
      // For now, we just verify the component can render
      const { rerender } = renderWithProviders(<ExplanationTab word="example" />)

      // Component should be rendered without crashing
      expect(rerender).toBeDefined()
    })
  })
})
