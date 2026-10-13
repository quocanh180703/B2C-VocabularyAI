/**
 * Skeleton Component
 * Hiển thị placeholder khi đang load dữ liệu
 * 
 * Sử dụng:
 * <Skeleton width="100%" height="20px" />
 * <Skeleton circle width="40px" height="40px" />
 */

interface SkeletonProps {
  width?: string | number;
  height?: string | number;
  circle?: boolean;
  count?: number;
  style?: React.CSSProperties;
}

export function Skeleton({ 
  width = '100%', 
  height = '20px',
  circle = false,
  count = 1,
  style = {}
}: SkeletonProps) {
  const skeletons = Array.from({ length: count }, (_, i) => i);

  const skeletonStyle: React.CSSProperties = {
    display: 'inline-block',
    width: typeof width === 'number' ? `${width}px` : width,
    height: typeof height === 'number' ? `${height}px` : height,
    backgroundColor: '#e0e0e0',
    borderRadius: circle ? '50%' : '4px',
    marginBottom: count > 1 ? '8px' : '0',
    animation: 'skeleton-loading 1s linear infinite alternate',
    ...style,
  };

  // Add keyframes if not already in document
  if (typeof document !== 'undefined' && !document.querySelector('#skeleton-styles')) {
    const styleElement = document.createElement('style');
    styleElement.id = 'skeleton-styles';
    styleElement.textContent = `
      @keyframes skeleton-loading {
        0% { background-color: #e0e0e0; }
        100% { background-color: #f0f0f0; }
      }
    `;
    document.head.appendChild(styleElement);
  }

  return (
    <>
      {skeletons.map((i) => (
        <div key={i} style={skeletonStyle} />
      ))}
    </>
  );
}

/**
 * Skeleton Page Component
 * Placeholder cho toàn bộ page
 */
export function SkeletonPage() {
  return (
    <div style={{ padding: '20px' }}>
      {/* Header */}
      <div style={{ marginBottom: '40px' }}>
        <Skeleton width="60%" height="32px" style={{ marginBottom: '10px' }} />
        <Skeleton width="40%" height="16px" />
      </div>

      {/* Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px' }}>
        {[1, 2, 3, 4].map((i) => (
          <div key={i} style={{ padding: '20px', border: '1px solid #eee', borderRadius: '8px' }}>
            <Skeleton width="100%" height="200px" style={{ marginBottom: '15px' }} />
            <Skeleton width="80%" height="20px" style={{ marginBottom: '10px' }} />
            <Skeleton width="60%" height="16px" />
          </div>
        ))}
      </div>
    </div>
  );
}

/**
 * Skeleton List Component
 * Placeholder cho danh sách
 */
export function SkeletonList({ count = 5 }: { count?: number }) {
  return (
    <div>
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} style={{ display: 'flex', gap: '15px', marginBottom: '15px', alignItems: 'center' }}>
          <Skeleton circle width="40px" height="40px" />
          <div style={{ flex: 1 }}>
            <Skeleton width="80%" height="16px" style={{ marginBottom: '8px' }} />
            <Skeleton width="60%" height="12px" />
          </div>
        </div>
      ))}
    </div>
  );
}

/**
 * Skeleton Card Component
 * Placeholder cho card
 */
export function SkeletonCard() {
  return (
    <div style={{ padding: '15px', border: '1px solid #eee', borderRadius: '8px' }}>
      <Skeleton width="100%" height="150px" style={{ marginBottom: '15px' }} />
      <Skeleton width="90%" height="18px" style={{ marginBottom: '10px' }} />
      <Skeleton width="70%" height="14px" />
    </div>
  );
}
