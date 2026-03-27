import React from 'react';

function MetricCard({ title, value, delta, unit = '' }) {
  return (
    <div style={{
      backgroundColor: '#1a1a2e',
      border: '1px solid #2a2a4a',
      borderRadius: '8px',
      padding: '16px 20px',
      flex: 1,
      minWidth: '140px',
    }}>
      <div style={{ fontSize: '12px', color: '#9ca3af', marginBottom: '4px', fontWeight: 500 }}>
        {title}
      </div>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px' }}>
        <span style={{ fontSize: '24px', fontWeight: 700, color: '#e0e0e0' }}>
          {value !== null && value !== undefined ? `${value}${unit}` : 'N/A'}
        </span>
        {delta !== null && delta !== undefined && (
          <span style={{
            fontSize: '12px',
            fontWeight: 600,
            color: delta > 0 ? '#10b981' : delta < 0 ? '#ef4444' : '#6b7280',
          }}>
            {delta > 0 ? '+' : ''}{delta}
          </span>
        )}
      </div>
    </div>
  );
}

export default MetricCard;
