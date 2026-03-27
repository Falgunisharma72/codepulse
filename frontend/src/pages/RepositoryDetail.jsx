import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  getRepository, getLatestMetrics, getMetricsTrend,
  getHotspots, getAnalyses, getRepoAlerts, triggerAnalysis,
} from '../api/codepulseApi';
import HealthScoreBadge from '../components/HealthScoreBadge';
import MetricCard from '../components/MetricCard';
import TrendChart from '../components/TrendChart';
import FileHotspotTable from '../components/FileHotspotTable';
import AnalysisTimeline from '../components/AnalysisTimeline';
import AlertBanner from '../components/AlertBanner';

function RepositoryDetail() {
  const { id } = useParams();
  const [repo, setRepo] = useState(null);
  const [metrics, setMetrics] = useState(null);
  const [trend, setTrend] = useState([]);
  const [hotspots, setHotspots] = useState([]);
  const [analyses, setAnalyses] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('healthScore');

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 30000);
    return () => clearInterval(interval);
  }, [id]);

  const loadData = async () => {
    try {
      const now = new Date().toISOString();
      const thirtyDaysAgo = new Date(Date.now() - 30 * 86400000).toISOString();

      const [repoRes, metricsRes, trendRes, hotspotsRes, analysesRes, alertsRes] = await Promise.all([
        getRepository(id),
        getLatestMetrics(id).catch(() => ({ data: null })),
        getMetricsTrend(id, thirtyDaysAgo, now).catch(() => ({ data: { dataPoints: [] } })),
        getHotspots(id).catch(() => ({ data: [] })),
        getAnalyses(id).catch(() => ({ data: { content: [] } })),
        getRepoAlerts(id).catch(() => ({ data: [] })),
      ]);

      setRepo(repoRes.data);
      setMetrics(metricsRes.data);
      setTrend(trendRes.data?.dataPoints || []);
      setHotspots(hotspotsRes.data || []);
      setAnalyses(analysesRes.data?.content || analysesRes.data || []);
      setAlerts(alertsRes.data || []);
    } catch (err) {
      console.error('Failed to load data:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleTriggerAnalysis = async () => {
    try {
      await triggerAnalysis(id);
      setTimeout(loadData, 2000);
    } catch (err) {
      console.error('Failed to trigger analysis:', err);
    }
  };

  if (loading) {
    return <div style={{ color: '#6b7280', textAlign: 'center', padding: '60px' }}>Loading...</div>;
  }

  const tabs = [
    { key: 'healthScore', label: 'Health Score' },
    { key: 'avgComplexity', label: 'Complexity' },
    { key: 'totalLines', label: 'Lines of Code' },
  ];

  return (
    <div>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '24px' }}>
        <Link to="/" style={{ color: '#6b7280', fontSize: '14px' }}>&larr; Back</Link>
        <h1 style={{ fontSize: '24px', fontWeight: 700, color: '#e0e0e0', flex: 1 }}>
          {repo?.repoName}
        </h1>
        {metrics && (
          <HealthScoreBadge
            score={metrics.overallHealthScore}
            grade={metrics.healthGrade}
            size="large"
          />
        )}
        <button onClick={handleTriggerAnalysis} style={{
          backgroundColor: '#6c63ff', color: 'white', border: 'none', padding: '8px 16px',
          borderRadius: '6px', fontSize: '13px', fontWeight: 600, cursor: 'pointer',
        }}>
          Analyze Now
        </button>
        <Link to={`/repositories/${id}/settings`} style={{
          color: '#9ca3af', fontSize: '13px', padding: '8px 12px',
          border: '1px solid #2a2a4a', borderRadius: '6px',
        }}>
          Settings
        </Link>
      </div>

      {/* Metric Cards */}
      <div style={{ display: 'flex', gap: '16px', marginBottom: '24px', flexWrap: 'wrap' }}>
        <MetricCard title="Files" value={metrics?.totalFiles} />
        <MetricCard title="Avg Complexity" value={metrics?.avgCyclomaticComplexity?.toFixed(1)} />
        <MetricCard title="Max Complexity" value={metrics?.maxCyclomaticComplexity?.toFixed(0)} />
        <MetricCard title="Test Coverage" value={metrics?.testCoverageEstimate?.toFixed(0)} unit="%" />
      </div>

      {/* Alerts */}
      {alerts.length > 0 && (
        <div style={{ marginBottom: '24px' }}>
          <AlertBanner alerts={alerts} />
        </div>
      )}

      {/* Trend Chart with Tabs */}
      <div style={{ marginBottom: '24px' }}>
        <div style={{ display: 'flex', gap: '8px', marginBottom: '12px' }}>
          {tabs.map((tab) => (
            <button key={tab.key} onClick={() => setActiveTab(tab.key)} style={{
              padding: '6px 14px', borderRadius: '4px', border: 'none', fontSize: '12px',
              fontWeight: 500, cursor: 'pointer',
              backgroundColor: activeTab === tab.key ? '#6c63ff' : '#2a2a4a',
              color: activeTab === tab.key ? 'white' : '#9ca3af',
            }}>
              {tab.label}
            </button>
          ))}
        </div>
        <TrendChart data={trend} metric={activeTab} title={tabs.find(t => t.key === activeTab)?.label + ' Trend'} />
      </div>

      {/* Hotspots and Timeline side by side */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
        <FileHotspotTable files={hotspots} />
        <AnalysisTimeline analyses={analyses} repoId={id} />
      </div>
    </div>
  );
}

export default RepositoryDetail;
