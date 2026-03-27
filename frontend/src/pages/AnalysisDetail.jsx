import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getAnalysis, getAnalysisFiles } from '../api/codepulseApi';
import FileHotspotTable from '../components/FileHotspotTable';
import { formatDate, getStatusColor } from '../utils/formatters';

function AnalysisDetail() {
  const { repoId, runId } = useParams();
  const [analysis, setAnalysis] = useState(null);
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadData = async () => {
      try {
        const [analysisRes, filesRes] = await Promise.all([
          getAnalysis(repoId, runId),
          getAnalysisFiles(repoId, runId).catch(() => ({ data: [] })),
        ]);
        setAnalysis(analysisRes.data);
        setFiles(filesRes.data || []);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, [repoId, runId]);

  if (loading) {
    return <div style={{ color: '#6b7280', textAlign: 'center', padding: '60px' }}>Loading...</div>;
  }

  if (!analysis) {
    return <div style={{ color: '#ef4444', textAlign: 'center', padding: '60px' }}>Analysis not found</div>;
  }

  return (
    <div>
      <Link to={`/repositories/${repoId}`} style={{ color: '#6b7280', fontSize: '14px' }}>
        &larr; Back to Repository
      </Link>

      <div style={{
        backgroundColor: '#1a1a2e',
        border: '1px solid #2a2a4a',
        borderRadius: '8px',
        padding: '24px',
        marginTop: '16px',
        marginBottom: '24px',
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <h1 style={{ fontSize: '20px', fontWeight: 700, color: '#e0e0e0', marginBottom: '8px' }}>
              Analysis Run #{analysis.id}
            </h1>
            <div style={{ fontSize: '13px', color: '#9ca3af' }}>
              <span style={{ fontFamily: 'monospace', color: '#6c63ff' }}>
                {analysis.commitSha?.substring(0, 7)}
              </span>
              {' '} {analysis.commitMessage}
            </div>
          </div>
          <span style={{
            padding: '4px 12px',
            borderRadius: '12px',
            fontSize: '12px',
            fontWeight: 600,
            color: getStatusColor(analysis.status),
            backgroundColor: `${getStatusColor(analysis.status)}20`,
          }}>
            {analysis.status}
          </span>
        </div>

        <div style={{
          display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '16px', marginTop: '20px',
          fontSize: '13px',
        }}>
          <div>
            <div style={{ color: '#6b7280' }}>Author</div>
            <div style={{ color: '#e0e0e0', fontWeight: 500 }}>{analysis.author}</div>
          </div>
          <div>
            <div style={{ color: '#6b7280' }}>Branch</div>
            <div style={{ color: '#e0e0e0', fontWeight: 500 }}>{analysis.branch}</div>
          </div>
          <div>
            <div style={{ color: '#6b7280' }}>Started</div>
            <div style={{ color: '#e0e0e0' }}>{formatDate(analysis.startedAt)}</div>
          </div>
          <div>
            <div style={{ color: '#6b7280' }}>Completed</div>
            <div style={{ color: '#e0e0e0' }}>{formatDate(analysis.completedAt)}</div>
          </div>
        </div>
      </div>

      <FileHotspotTable files={files} />
    </div>
  );
}

export default AnalysisDetail;
