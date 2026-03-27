import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getDashboardSummary, getRepositories, getAlertSummary } from '../api/codepulseApi';
import MetricCard from '../components/MetricCard';
import HealthScoreBadge from '../components/HealthScoreBadge';
import AlertBanner from '../components/AlertBanner';
import { timeAgo, getGradeColor } from '../utils/formatters';

function Dashboard() {
  const [summary, setSummary] = useState(null);
  const [repos, setRepos] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 30000); // Auto-refresh every 30s
    return () => clearInterval(interval);
  }, []);

  const loadData = async () => {
    try {
      const [summaryRes, reposRes, alertsRes] = await Promise.all([
        getDashboardSummary().catch(() => ({ data: null })),
        getRepositories().catch(() => ({ data: [] })),
        getAlertSummary().catch(() => ({ data: { alerts: [] } })),
      ]);
      setSummary(summaryRes.data);
      setRepos(reposRes.data || []);
      setAlerts(alertsRes.data?.alerts || []);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div style={{ color: '#6b7280', textAlign: 'center', padding: '60px' }}>Loading...</div>;
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <h1 style={{ fontSize: '24px', fontWeight: 700, color: '#e0e0e0' }}>Dashboard</h1>
      </div>

      {/* Summary Cards */}
      <div style={{ display: 'flex', gap: '16px', marginBottom: '24px', flexWrap: 'wrap' }}>
        <MetricCard title="Total Repos" value={summary?.totalRepos || 0} />
        <MetricCard title="Avg Health" value={summary?.avgHealthScore?.toFixed(0) || 'N/A'} />
        <MetricCard title="Active Alerts" value={summary?.activeAlerts || 0} />
        <MetricCard title="Total Analyses" value={summary?.totalAnalyses || 0} />
      </div>

      {/* Repository List */}
      <div style={{
        backgroundColor: '#1a1a2e',
        border: '1px solid #2a2a4a',
        borderRadius: '8px',
        marginBottom: '24px',
        overflow: 'hidden',
      }}>
        <div style={{ padding: '16px 20px', borderBottom: '1px solid #2a2a4a' }}>
          <h2 style={{ fontSize: '16px', fontWeight: 600, color: '#e0e0e0' }}>Repositories</h2>
        </div>
        {repos.length === 0 ? (
          <div style={{ padding: '40px', textAlign: 'center', color: '#6b7280' }}>
            No repositories tracked yet.{' '}
            <Link to="/add-repository" style={{ color: '#6c63ff' }}>Add one</Link>
          </div>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr>
                {['Repo Name', 'Health', 'Last Run', 'Alerts'].map((h) => (
                  <th key={h} style={{
                    textAlign: 'left', padding: '10px 20px', fontSize: '11px',
                    fontWeight: 500, color: '#9ca3af', textTransform: 'uppercase',
                    borderBottom: '1px solid #2a2a4a',
                  }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {repos.map((repo) => (
                <tr key={repo.id} style={{ cursor: 'pointer' }}
                    onClick={() => window.location.href = `/repositories/${repo.id}`}>
                  <td style={{ padding: '12px 20px', color: '#6c63ff', fontWeight: 500 }}>
                    {repo.repoName}
                  </td>
                  <td style={{ padding: '12px 20px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <span style={{
                        color: getGradeColor(repo.latestHealthGrade),
                        fontWeight: 700, fontSize: '16px',
                      }}>
                        {repo.latestHealthGrade || '-'}
                      </span>
                      <span style={{ color: '#9ca3af', fontSize: '13px' }}>
                        {repo.latestHealthScore ? Math.round(repo.latestHealthScore) : '-'}
                      </span>
                    </div>
                  </td>
                  <td style={{ padding: '12px 20px', color: '#9ca3af', fontSize: '13px' }}>
                    {timeAgo(repo.lastAnalysisAt)}
                  </td>
                  <td style={{ padding: '12px 20px' }}>
                    <span style={{
                      color: repo.activeAlertCount > 0 ? '#ef4444' : '#10b981',
                      fontWeight: 500,
                    }}>
                      {repo.activeAlertCount || 0}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Alerts */}
      <AlertBanner alerts={alerts} />

      {error && (
        <div style={{
          marginTop: '16px', padding: '12px', backgroundColor: '#2d1215',
          border: '1px solid #ef4444', borderRadius: '6px', color: '#ef4444', fontSize: '13px',
        }}>
          {error}
          <button onClick={loadData} style={{
            marginLeft: '12px', color: '#6c63ff', background: 'none', border: 'none', cursor: 'pointer',
          }}>Retry</button>
        </div>
      )}
    </div>
  );
}

export default Dashboard;
