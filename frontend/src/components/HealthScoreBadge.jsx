import React from 'react';
import { getGradeColor } from '../utils/formatters';

function HealthScoreBadge({ score, grade, size = 'medium' }) {
  const sizes = {
    small: { width: '36px', height: '36px', fontSize: '12px', scoreSize: '10px' },
    medium: { width: '56px', height: '56px', fontSize: '20px', scoreSize: '11px' },
    large: { width: '80px', height: '80px', fontSize: '28px', scoreSize: '13px' },
  };

  const s = sizes[size] || sizes.medium;
  const color = getGradeColor(grade);

  return (
    <div style={{
      width: s.width,
      height: s.height,
      borderRadius: '50%',
      border: `3px solid ${color}`,
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      backgroundColor: `${color}15`,
    }}>
      <span style={{ fontSize: s.fontSize, fontWeight: 700, color, lineHeight: 1 }}>
        {grade || 'N/A'}
      </span>
      {score !== undefined && score !== null && (
        <span style={{ fontSize: s.scoreSize, color: '#9ca3af', lineHeight: 1 }}>
          {Math.round(score)}
        </span>
      )}
    </div>
  );
}

export default HealthScoreBadge;
