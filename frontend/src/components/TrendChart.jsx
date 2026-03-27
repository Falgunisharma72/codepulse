import React from 'react';
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, Area, AreaChart,
} from 'recharts';

function TrendChart({ data, metric = 'healthScore', title = 'Health Score Trend' }) {
  const colors = {
    healthScore: '#6c63ff',
    avgComplexity: '#f59e0b',
    totalLines: '#10b981',
    codeSmellCount: '#ef4444',
  };

  const formattedData = (data || []).map((point) => ({
    ...point,
    date: new Date(point.timestamp).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
  }));

  return (
    <div style={{
      backgroundColor: '#1a1a2e',
      border: '1px solid #2a2a4a',
      borderRadius: '8px',
      padding: '20px',
    }}>
      <h3 style={{ fontSize: '14px', fontWeight: 600, color: '#e0e0e0', marginBottom: '16px' }}>
        {title}
      </h3>
      <ResponsiveContainer width="100%" height={250}>
        <AreaChart data={formattedData}>
          <defs>
            <linearGradient id={`gradient-${metric}`} x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor={colors[metric]} stopOpacity={0.3} />
              <stop offset="95%" stopColor={colors[metric]} stopOpacity={0} />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke="#2a2a4a" />
          <XAxis dataKey="date" stroke="#6b7280" fontSize={12} />
          <YAxis stroke="#6b7280" fontSize={12} />
          <Tooltip
            contentStyle={{
              backgroundColor: '#1a1a2e',
              border: '1px solid #2a2a4a',
              borderRadius: '6px',
              fontSize: '12px',
            }}
          />
          <Area
            type="monotone"
            dataKey={metric}
            stroke={colors[metric]}
            fill={`url(#gradient-${metric})`}
            strokeWidth={2}
          />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
}

export default TrendChart;
