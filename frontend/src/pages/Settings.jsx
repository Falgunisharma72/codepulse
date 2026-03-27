import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getThresholds, updateThresholds, resetThresholds } from '../api/codepulseApi';

function Settings() {
  const { id } = useParams();
  const [thresholds, setThresholds] = useState({
    maxCyclomaticComplexity: 10,
    maxMethodLength: 30,
    maxFileLength: 300,
    maxNestingDepth: 4,
    minHealthScore: 70.0,
  });
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState(null);

  useEffect(() => {
    getThresholds(id)
      .then((res) => setThresholds(res.data))
      .catch(console.error);
  }, [id]);

  const handleSave = async () => {
    setSaving(true);
    try {
      await updateThresholds(id, thresholds);
      setMessage('Thresholds saved successfully');
      setTimeout(() => setMessage(null), 3000);
    } catch (err) {
      setMessage('Failed to save: ' + err.message);
    } finally {
      setSaving(false);
    }
  };

  const handleReset = async () => {
    try {
      await resetThresholds(id);
      setThresholds({
        maxCyclomaticComplexity: 10,
        maxMethodLength: 30,
        maxFileLength: 300,
        maxNestingDepth: 4,
        minHealthScore: 70.0,
      });
      setMessage('Reset to defaults');
      setTimeout(() => setMessage(null), 3000);
    } catch (err) {
      setMessage('Failed to reset: ' + err.message);
    }
  };

  const inputStyle = {
    width: '100%',
    padding: '8px 12px',
    backgroundColor: '#0f0f23',
    border: '1px solid #2a2a4a',
    borderRadius: '6px',
    color: '#e0e0e0',
    fontSize: '14px',
  };

  const fields = [
    { key: 'maxCyclomaticComplexity', label: 'Max Cyclomatic Complexity', type: 'number' },
    { key: 'maxMethodLength', label: 'Max Method Length (lines)', type: 'number' },
    { key: 'maxFileLength', label: 'Max File Length (lines)', type: 'number' },
    { key: 'maxNestingDepth', label: 'Max Nesting Depth', type: 'number' },
    { key: 'minHealthScore', label: 'Min Health Score', type: 'number', step: '0.1' },
  ];

  return (
    <div style={{ maxWidth: '500px', margin: '0 auto' }}>
      <Link to={`/repositories/${id}`} style={{ color: '#6b7280', fontSize: '14px' }}>
        &larr; Back
      </Link>
      <h1 style={{ fontSize: '24px', fontWeight: 700, color: '#e0e0e0', margin: '16px 0 24px' }}>
        Quality Thresholds
      </h1>

      <div style={{
        backgroundColor: '#1a1a2e',
        border: '1px solid #2a2a4a',
        borderRadius: '8px',
        padding: '24px',
      }}>
        {fields.map(({ key, label, type, step }) => (
          <div key={key} style={{ marginBottom: '16px' }}>
            <label style={{ display: 'block', fontSize: '13px', color: '#9ca3af', marginBottom: '6px' }}>
              {label}
            </label>
            <input
              type={type}
              step={step}
              style={inputStyle}
              value={thresholds[key] || ''}
              onChange={(e) => setThresholds({
                ...thresholds,
                [key]: type === 'number' ? parseFloat(e.target.value) : e.target.value,
              })}
            />
          </div>
        ))}

        {message && (
          <div style={{
            padding: '10px', marginBottom: '16px', borderRadius: '6px', fontSize: '13px',
            backgroundColor: message.includes('Failed') ? '#2d1215' : '#122d15',
            color: message.includes('Failed') ? '#ef4444' : '#10b981',
          }}>
            {message}
          </div>
        )}

        <div style={{ display: 'flex', gap: '12px' }}>
          <button onClick={handleSave} disabled={saving} style={{
            flex: 1, padding: '10px', backgroundColor: '#6c63ff', color: 'white',
            border: 'none', borderRadius: '6px', fontWeight: 600, cursor: 'pointer',
          }}>
            {saving ? 'Saving...' : 'Save Thresholds'}
          </button>
          <button onClick={handleReset} style={{
            padding: '10px 16px', backgroundColor: 'transparent', color: '#9ca3af',
            border: '1px solid #2a2a4a', borderRadius: '6px', cursor: 'pointer',
          }}>
            Reset
          </button>
        </div>
      </div>
    </div>
  );
}

export default Settings;
