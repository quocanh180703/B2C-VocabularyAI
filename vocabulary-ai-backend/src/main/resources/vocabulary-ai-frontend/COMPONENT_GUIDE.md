# VocabularyAI Component Guide

Comprehensive guide for using components in the VocabularyAI application.

## Table of Contents

1. [AI Components](#ai-components)
   - [PersonalizationTab](#personalizationtab)
   - [SpeakingTab](#speakingtab)
2. [Common Components](#common-components)
3. [Props Reference](#props-reference)
4. [Usage Examples](#usage-examples)
5. [Styling & Customization](#styling--customization)
6. [Accessibility](#accessibility)

---

## AI Components

### PersonalizationTab

Display personalization features and adaptive learning insights.

**Location:** `src/components/ai/PersonalizationTab.tsx`

**Features:**
- User profile overview
- Learning plan visualization
- Progress analysis
- Personalized recommendations
- Weakness/strength analysis
- Adaptive difficulty settings
- Daily practice tracking

**Props:**

```typescript
interface PersonalizationTabProps {
  activeTab?: "overview" | "plan" | "progress" | "recommendations" | "analysis";
  onTabChange?: (tab: string) => void;
}
```

**Basic Usage:**

```tsx
import PersonalizationTab from '@/components/ai/PersonalizationTab'

export default function MyPage() {
  const [activeTab, setActiveTab] = useState('overview')

  return (
    <PersonalizationTab
      activeTab={activeTab}
      onTabChange={setActiveTab}
    />
  )
}
```

**Tabs:**

1. **Overview**
   - Shows current level, streak, total learning time
   - Displays learning style
   - Daily goal progress

2. **Plan**
   - Visual representation of learning plan
   - Focus areas
   - Overall progress percentage

3. **Progress**
   - Weekly progress metrics
   - Learning analytics
   - Performance trends

4. **Recommendations**
   - AI-generated learning recommendations
   - Priority indicators
   - Estimated time for activities

5. **Analysis**
   - Strength areas (what you're good at)
   - Weakness areas (where to improve)
   - Adaptive difficulty level
   - Progress to next level

**Data Dependencies:**

```typescript
// These hooks are used internally
useProfile()
useLearningPlan()
useProgressAnalysis()
useRecommendations()
useWeaknessStrength()
useAdaptiveDifficulty()
useDailyPractice()
```

**Loading States:**

- Shows skeleton loaders while fetching data
- Displays error boundary on failures
- Retry button on errors

**Accessibility:**

- Semantic HTML with proper heading hierarchy
- ARIA labels on interactive elements
- Keyboard navigation support
- Color contrast ≥ 4.5:1

---

### SpeakingTab

Audio recording and speaking practice interface.

**Location:** `src/components/ai/SpeakingTab.tsx`

**Features:**
- Create speaking sessions
- Audio recording with Web Audio API
- Real-time recording feedback
- Session management
- Speaking feedback display
- Session history

**Props:**

```typescript
interface SpeakingTabProps {
  activeSession?: string | null;
}
```

**Basic Usage:**

```tsx
import SpeakingTab from '@/components/ai/SpeakingTab'

export default function SpeakingPage() {
  const [activeSession, setActiveSession] = useState<string | null>(null)

  return (
    <SpeakingTab activeSession={activeSession} />
  )
}
```

**Features:**

1. **Create Session Form**
   - Topic input
   - Difficulty level selector (A1-C2)
   - Optional title and description

2. **Recording Controls**
   - Start/Stop recording buttons
   - Pause/Resume functionality
   - Duration display
   - Error handling for unsupported browsers

3. **Session Management**
   - Display current question progress
   - Show session status
   - Display difficulty level

4. **Feedback Display**
   - Overall score (0-100)
   - Accuracy percentage
   - Fluency score
   - Detailed feedback explanation
   - Corrections with explanations
   - Strengths list
   - Areas to improve

5. **Sessions History**
   - List of all speaking sessions
   - Session scores
   - Session status indicators

**Audio Recording:**

Uses the `AudioRecorder` utility for recording:

```typescript
// Internally uses:
const audioRecorder = new AudioRecorder()
await audioRecorder.start()
audioRecorder.pause()
audioRecorder.resume()
const blob = audioRecorder.stop()
```

**Browser Compatibility:**

- Requires modern browser with Web Audio API support
- Falls back gracefully on unsupported browsers
- Shows error message if audio recording not available

**Data Dependencies:**

```typescript
useCreateSpeakingSession()
useSpeakingSessions()
useSpeakingSession()
useSubmitSpeakingAnswer()
useSpeakingFeedback()
useCompleteSpeakingSession()
```

**Error Handling:**

- Audio recording permission denied
- Unsupported browser/device
- Network errors
- API failures

---

## Common Components

### SkeletonLoader

Loading placeholder component.

```tsx
<SkeletonLoader count={3} />
```

### ErrorBoundary

Error fallback component.

```tsx
<ErrorBoundary
  message="Failed to load data"
  onRetry={() => refetch()}
/>
```

---

## Props Reference

### PersonalizationTab Props

```typescript
{
  // Active tab name
  activeTab?: "overview" | "plan" | "progress" | "recommendations" | "analysis"

  // Callback when tab changes
  onTabChange?: (tab: string) => void
}
```

### SpeakingTab Props

```typescript
{
  // Currently active speaking session ID
  activeSession?: string | null
}
```

---

## Usage Examples

### Example 1: Personalization Page

```tsx
import { useState } from 'react'
import PersonalizationTab from '@/components/ai/PersonalizationTab'

export default function PersonalizationPage() {
  const [activeTab, setActiveTab] = useState<'overview' | 'plan' | 'progress' | 'recommendations' | 'analysis'>('overview')

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950">
      <div className="mx-auto max-w-7xl px-4 py-8">
        <h1 className="text-3xl font-bold text-slate-900 dark:text-white">
          Personalized Learning
        </h1>

        <PersonalizationTab
          activeTab={activeTab}
          onTabChange={(tab) => setActiveTab(tab as any)}
        />
      </div>
    </div>
  )
}
```

### Example 2: Speaking Practice

```tsx
import { useState } from 'react'
import SpeakingTab from '@/components/ai/SpeakingTab'

export default function SpeakingPage() {
  const [activeSession, setActiveSession] = useState<string | null>(null)

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950">
      <div className="mx-auto max-w-7xl px-4 py-8">
        <h1 className="text-3xl font-bold">Speaking Practice</h1>

        <SpeakingTab activeSession={activeSession} />
      </div>
    </div>
  )
}
```

### Example 3: Using Hooks Directly

```tsx
import { useProfile, useLearningPlan } from '@/hooks/usePersonalization'

export default function DashboardCard() {
  const { data: profile, isLoading } = useProfile()
  const { data: plan } = useLearningPlan({ weeks: 4 })

  if (isLoading) return <div>Loading...</div>

  return (
    <div className="p-4">
      <h2>{profile?.username}</h2>
      <p>Level: {profile?.proficiencyLevel}</p>
      <p>Current Streak: {profile?.currentStreak} days</p>
    </div>
  )
}
```

### Example 4: Speaking with Session Management

```tsx
import { useState } from 'react'
import SpeakingTab from '@/components/ai/SpeakingTab'
import { useSpeakingSessions } from '@/hooks/useSpeaking'

export default function SpeakingDashboard() {
  const [activeSession, setActiveSession] = useState<string | null>(null)
  const { data: sessions } = useSpeakingSessions()

  return (
    <div>
      <div className="flex gap-2 mb-4">
        {sessions?.sessions.map(session => (
          <button
            key={session.id}
            onClick={() => setActiveSession(session.id.toString())}
            className={activeSession === session.id.toString()
              ? 'bg-indigo-600 text-white'
              : 'bg-gray-200'}
          >
            {session.title}
          </button>
        ))}
      </div>

      <SpeakingTab activeSession={activeSession} />
    </div>
  )
}
```

---

## Styling & Customization

### CSS Framework

All components use **Tailwind CSS v4** for styling.

### Dark Mode

Components support dark mode automatically:

```tsx
// Light mode classes
<div className="bg-white text-slate-900">

// Dark mode classes
<div className="dark:bg-slate-900 dark:text-white">
```

### Customization

To customize component styles, modify Tailwind classes:

```tsx
// In PersonalizationTab.tsx or SpeakingTab.tsx
<div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm
              dark:border-slate-700 dark:bg-slate-900">
```

### Theme Colors

Default theme colors used:

- **Primary:** Indigo (600/400 for dark)
- **Success:** Green (600/400 for dark)
- **Warning:** Amber (600/400 for dark)
- **Error:** Red (600/400 for dark)
- **Neutral:** Slate (200/700 for dark)

---

## Accessibility

### WCAG 2.1 AA Compliance

Components follow accessibility best practices:

**Keyboard Navigation:**
- Tab through interactive elements
- Enter/Space to activate buttons
- Arrow keys in tab components

**Screen Reader Support:**
- Semantic HTML elements
- ARIA labels on buttons
- ARIA live regions for status updates
- Proper heading hierarchy

**Color Contrast:**
- Text: 4.5:1 contrast ratio
- Buttons: 3:1 minimum for graphics
- Focus indicators: clearly visible

**Motion:**
- Reduced motion respected via `prefers-reduced-motion`
- Smooth transitions for sighted users

**Example Accessible Component:**

```tsx
<button
  onClick={handleClick}
  aria-label="Create new speaking session"
  className="rounded-lg bg-indigo-600 px-4 py-2 text-white
             hover:bg-indigo-700
             focus:outline-none focus:ring-2 focus:ring-indigo-500
             focus:ring-offset-2 dark:ring-offset-slate-900"
>
  New Session
</button>
```

### Testing Accessibility

```bash
# Run accessibility tests
npm run test:a11y

# Check component with axe DevTools
# Use browser extension: https://www.deque.com/axe/devtools/
```

---

## Performance Optimization

### React Query Caching

Components use optimized React Query caching:

```typescript
// 10-minute stale time for personalization data
staleTime: 10 * 60 * 1000

// 15-minute garbage collection time
gcTime: 15 * 60 * 1000
```

### Lazy Loading

Pages are lazy-loaded:

```tsx
const PersonalizationPage = lazy(() => import('@/pages/personalization/PersonalizationPage'))
const SpeakingPage = lazy(() => import('@/pages/speaking/SpeakingPage'))
```

### Image Optimization

All images are optimized with proper sizing.

### Bundle Size

Components are tree-shakeable and modular.

---

## Troubleshooting

### Component Not Displaying

1. Check if required hooks are working
2. Verify data is being fetched correctly
3. Check console for errors
4. Ensure component is properly imported

### Audio Recording Issues

1. Check browser compatibility
2. Verify microphone permissions are granted
3. Check for audio recording errors in console
4. Ensure HTTPS is used (required for audio API)

### Performance Issues

1. Check React Query cache settings
2. Reduce component complexity
3. Implement code splitting
4. Profile with React DevTools

---

## Best Practices

1. **Always use hooks** for data fetching, not direct API calls
2. **Provide loading states** while data is being fetched
3. **Handle errors gracefully** with error boundaries
4. **Use TypeScript** for type safety
5. **Test accessibility** with screen readers and keyboard
6. **Optimize performance** with React Query caching
7. **Follow naming conventions** for consistency
8. **Document complex logic** with comments
9. **Keep components focused** on single responsibility
10. **Use Tailwind classes** consistently

