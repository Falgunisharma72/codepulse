import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createRepository } from '../api/codepulseApi';

function AddRepository() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    githubRepoUrl: '',
    repoName: '',
    defaultBranch: 'main',
  });
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await createRepository(formData);
      navigate('/');
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const inputStyle = {
    width: '100%',
    padding: '10px 14px',
    backgroundColor: '#0f0f23',
    border: '1px solid #2a2a4a',
    borderRadius: '6px',
    color: '#e0e0e0',
    fontSize: '14px',
    outline: 'none',
  };

  return (
    <div style={{ maxWidth: '500px', margin: '0 auto' }}>
      <h1 style={{ fontSize: '24px', fontWeight: 700, color: '#e0e0e0', marginBottom: '24px' }}>
        Add Repository
      </h1>

      <form onSubmit={handleSubmit} style={{
        backgroundColor: '#1a1a2e',
        border: '1px solid #2a2a4a',
        borderRadius: '8px',
        padding: '24px',
      }}>
        <div style={{ marginBottom: '16px' }}>
          <label style={{ display: 'block', fontSize: '13px', color: '#9ca3af', marginBottom: '6px' }}>
            GitHub Repository URL
          </label>
          <input
            style={inputStyle}
            placeholder="https://github.com/username/repo"
            value={formData.githubRepoUrl}
            onChange={(e) => setFormData({ ...formData, githubRepoUrl: e.target.value })}
            required
          />
        </div>

        <div style={{ marginBottom: '16px' }}>
          <label style={{ display: 'block', fontSize: '13px', color: '#9ca3af', marginBottom: '6px' }}>
            Repository Name
          </label>
          <input
            style={inputStyle}
            placeholder="my-awesome-project"
            value={formData.repoName}
            onChange={(e) => setFormData({ ...formData, repoName: e.target.value })}
            required
          />
        </div>

        <div style={{ marginBottom: '24px' }}>
          <label style={{ display: 'block', fontSize: '13px', color: '#9ca3af', marginBottom: '6px' }}>
            Default Branch
          </label>
          <input
            style={inputStyle}
            placeholder="main"
            value={formData.defaultBranch}
            onChange={(e) => setFormData({ ...formData, defaultBranch: e.target.value })}
          />
        </div>

        {error && (
          <div style={{
            padding: '10px', marginBottom: '16px', backgroundColor: '#2d1215',
            border: '1px solid #ef4444', borderRadius: '6px', color: '#ef4444', fontSize: '13px',
          }}>
            {error}
          </div>
        )}

        <button type="submit" disabled={submitting} style={{
          width: '100%',
          padding: '10px',
          backgroundColor: submitting ? '#4a4580' : '#6c63ff',
          color: 'white',
          border: 'none',
          borderRadius: '6px',
          fontSize: '14px',
          fontWeight: 600,
          cursor: submitting ? 'not-allowed' : 'pointer',
        }}>
          {submitting ? 'Registering...' : 'Register Repository'}
        </button>
      </form>
    </div>
  );
}

export default AddRepository;
