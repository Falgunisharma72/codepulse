export const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', {
    month: 'short', day: 'numeric', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
};

export const timeAgo = (dateString) => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  const now = new Date();
  const seconds = Math.floor((now - date) / 1000);

  if (seconds < 60) return `${seconds}s ago`;
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  return `${days}d ago`;
};

export const getGradeColor = (grade) => {
  const colors = {
    A: '#10b981', B: '#3b82f6', C: '#f59e0b', D: '#f97316', F: '#ef4444',
  };
  return colors[grade] || '#6b7280';
};

export const getStatusColor = (status) => {
  const colors = {
    COMPLETED: '#10b981', RUNNING: '#3b82f6', QUEUED: '#f59e0b', FAILED: '#ef4444',
  };
  return colors[status] || '#6b7280';
};

export const formatNumber = (num) => {
  if (num === null || num === undefined) return 'N/A';
  return num.toLocaleString();
};

export const formatDelta = (value) => {
  if (!value) return null;
  const prefix = value > 0 ? '+' : '';
  return `${prefix}${value.toFixed(1)}`;
};
