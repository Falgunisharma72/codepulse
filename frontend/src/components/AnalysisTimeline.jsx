import React from 'react';
import { Link } from 'react-router-dom';
import { timeAgo, getStatusColor } from '../utils/formatters';

function AnalysisTimeline({ analyses = [], repoId }) {
  if (!analyses.length) {
    return (
      <div style={{ color: '#6b7280', fontSize: '13px', padding: '20px' }}>
        No analysis runs yet
      </div>
    );
  }

  return (
    <div style={{
      backgroundColor: '#1a1a2e',
      border: '1px solid #2a2a4a',
      borderRadius: '8px',
      padding: '20px',
    }}>
      <h3 style={{ fontSize: '14px', fontWeight: 600, color: '#e0e0e0', marginBottom: '16px' }}>
        Analysis History
      </h3>
      {analyses.map((run, i) => (
        <div key={i} style={{
          display: 'flex',
          alignItems: 'center',
          gap: '12px',
          padding: '10px 0',
          borderBottom: i < analyses.length - 1 ? '1px solid #1e1e3a' : 'none',
        }}>
          <span style={{
            width: '10px',
            height: '10px',
            borderRadius: '50%',
            backgroundColor: getStatusColor(run.status),
            flexShrink: 0,
          }} />
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ fontSize: '13px', color: '#e0e0e0' }}>
              <span style={{ fontFamily: 'monospace', color: '#6c63ff' }}>
                {run.commitSha?.substring(0, 7)}
              </span>
              {' '}
              <span style={{ color: '#9ca3af' }}>
                {run.commitMessage?.substring(0, 50)}
              </span>
            </div>
          </div>
          <span style={{ color: '#6b7280', fontSize: '11px', whiteSpace: 'nowrap' }}>
            {timeAgo(run.createdAt)}
          </span>
          <span style={{
            fontSize: '11px',
            fontWeight: 500,
            color: getStatusColor(run.status),
            textTransform: 'uppercase',
          }}>
            {run.status}
          </span>
        </div>
      ))}
    </div>
  );
}

export default AnalysisTimeline;
