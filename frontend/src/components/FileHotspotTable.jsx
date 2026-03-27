import React from 'react';

const styles = {
  container: {
    backgroundColor: '#1a1a2e',
    border: '1px solid #2a2a4a',
    borderRadius: '8px',
    padding: '20px',
    overflowX: 'auto',
  },
  title: { fontSize: '14px', fontWeight: 600, color: '#e0e0e0', marginBottom: '16px' },
  table: { width: '100%', borderCollapse: 'collapse', fontSize: '13px' },
  th: {
    textAlign: 'left', padding: '8px 12px', borderBottom: '1px solid #2a2a4a',
    color: '#9ca3af', fontWeight: 500, fontSize: '11px', textTransform: 'uppercase',
  },
  td: { padding: '8px 12px', borderBottom: '1px solid #1e1e3a', color: '#e0e0e0' },
  filePath: { fontFamily: 'monospace', fontSize: '12px', color: '#6c63ff' },
};

function FileHotspotTable({ files = [] }) {
  if (!files.length) {
    return (
      <div style={styles.container}>
        <h3 style={styles.title}>File Hotspots</h3>
        <p style={{ color: '#6b7280', fontSize: '13px' }}>No data available</p>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <h3 style={styles.title}>File Hotspots (Top Complex Files)</h3>
      <table style={styles.table}>
        <thead>
          <tr>
            <th style={styles.th}>File</th>
            <th style={styles.th}>Complexity</th>
            <th style={styles.th}>Methods</th>
            <th style={styles.th}>Nesting</th>
            <th style={styles.th}>LOC</th>
          </tr>
        </thead>
        <tbody>
          {files.map((file, i) => (
            <tr key={i}>
              <td style={{ ...styles.td, ...styles.filePath }}>{file.filePath}</td>
              <td style={{
                ...styles.td,
                color: file.cyclomaticComplexity > 10 ? '#ef4444' : '#e0e0e0',
                fontWeight: file.cyclomaticComplexity > 10 ? 600 : 400,
              }}>
                {file.cyclomaticComplexity}
              </td>
              <td style={styles.td}>{file.methodCount}</td>
              <td style={{
                ...styles.td,
                color: file.nestingDepth > 4 ? '#f59e0b' : '#e0e0e0',
              }}>
                {file.nestingDepth}
              </td>
              <td style={styles.td}>{file.linesOfCode}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default FileHotspotTable;
