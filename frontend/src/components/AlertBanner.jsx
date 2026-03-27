import React from 'react';
import { timeAgo } from '../utils/formatters';

const severityStyles = {
  CRITICAL: { bg: '#2d1215', border: '#ef4444', icon: '\u25cf', color: '#ef4444' },
  WARNING: { bg: '#2d2215', border: '#f59e0b', icon: '\u25b2', color: '#f59e0b' },
};

function AlertBanner({ alerts = [] }) {
  if (!alerts.length) return null;

  return (
    <div style={{
      backgroundColor: '#1a1a2e',
      border: '1px solid #2a2a4a',
      borderRadius: '8px',
      padding: '16px 20px',
    }}>
      <h3 style={{ fontSize: '14px', fontWeight: 600, color: '#e0e0e0', marginBottom: '12px' }}>
        Active Alerts
      </h3>
      {alerts.slice(0, 5).map((alert, i) => {
        const s = severityStyles[alert.severity] || severityStyles.WARNING;
        return (
          <div key={i} style={{
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            padding: '8px 12px',
            marginBottom: '6px',
            backgroundColor: s.bg,
            borderLeft: `3px solid ${s.border}`,
            borderRadius: '4px',
            fontSize: '13px',
          }}>
            <span style={{ color: s.color }}>{s.icon}</span>
            <span style={{ flex: 1, color: '#e0e0e0' }}>
              {alert.repoName && <strong>{alert.repoName}: </strong>}
              {alert.message}
            </span>
            <span style={{ color: '#6b7280', fontSize: '11px', whiteSpace: 'nowrap' }}>
              {timeAgo(alert.createdAt)}
            </span>
          </div>
        );
      })}
    </div>
  );
}

export default AlertBanner;
