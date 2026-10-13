import React, { ReactNode, ErrorInfo } from 'react';
import { Button } from './Button';
import { Icon } from './Icon';

interface ErrorBoundaryProps {
  children: ReactNode;
}

interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
  errorInfo: ErrorInfo | null;
  errorCount: number;
}

/**
 * ErrorBoundary Component
 * Bắt lỗi React component và hiển thị fallback UI
 * 
 * Sử dụng:
 * <ErrorBoundary>
 *   <SomeComponent />
 * </ErrorBoundary>
 */
export class ErrorBoundary extends React.Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
      errorCount: 0,
    };
  }

  static getDerivedStateFromError(error: Error): Partial<ErrorBoundaryState> {
    return { hasError: true };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('ErrorBoundary caught an error:', error, errorInfo);
    
    this.setState((prevState) => ({
      error,
      errorInfo,
      errorCount: prevState.errorCount + 1,
    }));

    // Log to external error tracking service (e.g., Sentry)
    // if (window.Sentry) {
    //   window.Sentry.captureException(error, { contexts: { react: errorInfo } });
    // }
  }

  handleReset = () => {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null,
    });
  };

  render() {
    if (this.state.hasError) {
      const isDevelopment = process.env.NODE_ENV === 'development';
      const { error, errorInfo, errorCount } = this.state;

      return (
        <div style={styles.container}>
          <div style={styles.content}>
            <div style={styles.iconContainer}>
              <Icon name="alert" />
            </div>

            <h2 style={styles.title}>Oops! Something went wrong</h2>
            
            <p style={styles.subtitle}>
              {errorCount === 1
                ? 'We encountered an unexpected error. Please try refreshing the page.'
                : `Multiple errors detected (${errorCount}). We recommend refreshing the page.`}
            </p>

            {isDevelopment && error && (
              <details style={styles.details}>
                <summary style={styles.summary}>Error Details (Development Only)</summary>
                <pre style={styles.errorCode}>
                  <strong>Error:</strong> {error.toString()}
                  {errorInfo && (
                    <>
                      {'\n\n'}
                      <strong>Component Stack:</strong>
                      {errorInfo.componentStack}
                    </>
                  )}
                </pre>
              </details>
            )}

            <div style={styles.actions}>
              <Button onClick={this.handleReset} style={styles.resetButton}>
                <Icon name="refresh" /> Try Again
              </Button>
              <Button
                variant="secondary"
                onClick={() => window.location.href = '/'}
                style={styles.homeButton}
              >
                Go Home
              </Button>
            </div>

            {errorCount > 3 && (
              <p style={styles.warning}>
                ⚠️ Multiple errors detected. Please contact support if the problem persists.
              </p>
            )}
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

// Styles
const styles: Record<string, React.CSSProperties> = {
  container: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    minHeight: '100vh',
    backgroundColor: '#f5f5f5',
    padding: '20px',
  },
  content: {
    backgroundColor: 'white',
    borderRadius: '12px',
    padding: '40px',
    maxWidth: '500px',
    textAlign: 'center',
    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
  },
  iconContainer: {
    fontSize: '48px',
    color: '#d32f2f',
    marginBottom: '20px',
  },
  title: {
    fontSize: '24px',
    fontWeight: '600',
    color: '#333',
    marginBottom: '10px',
  },
  subtitle: {
    fontSize: '14px',
    color: '#666',
    marginBottom: '20px',
    lineHeight: '1.5',
  },
  details: {
    marginBottom: '20px',
    textAlign: 'left',
    backgroundColor: '#f9f9f9',
    padding: '10px',
    borderRadius: '8px',
    border: '1px solid #eee',
  },
  summary: {
    cursor: 'pointer',
    fontWeight: '500',
    color: '#0066cc',
    padding: '10px',
    marginBottom: '10px',
  },
  errorCode: {
    backgroundColor: '#1e1e1e',
    color: '#d4d4d4',
    padding: '12px',
    borderRadius: '6px',
    fontSize: '12px',
    overflow: 'auto',
    maxHeight: '200px',
    fontFamily: 'Courier New, monospace',
    whiteSpace: 'pre-wrap',
    wordBreak: 'break-word',
  },
  actions: {
    display: 'flex',
    gap: '10px',
    justifyContent: 'center',
    marginBottom: '20px',
  },
  resetButton: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
  },
  homeButton: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
  },
  warning: {
    fontSize: '12px',
    color: '#f57c00',
    marginTop: '10px',
    padding: '10px',
    backgroundColor: '#fff3e0',
    borderRadius: '6px',
  },
};
