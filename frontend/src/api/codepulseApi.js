import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

// Response interceptor to unwrap ApiResponse
api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const message = error.response?.data?.error?.message || error.message;
    return Promise.reject(new Error(message));
  }
);

// Repository APIs
export const getRepositories = () => api.get('/repositories');
export const getRepository = (id) => api.get(`/repositories/${id}`);
export const createRepository = (data) => api.post('/repositories', data);
export const updateRepository = (id, data) => api.put(`/repositories/${id}`, data);
export const deleteRepository = (id) => api.delete(`/repositories/${id}`);
export const triggerAnalysis = (id) => api.post(`/repositories/${id}/analyze`);

// Analysis APIs
export const getAnalyses = (repoId, page = 0, size = 10) =>
  api.get(`/repositories/${repoId}/analyses`, { params: { page, size } });
export const getAnalysis = (repoId, runId) =>
  api.get(`/repositories/${repoId}/analyses/${runId}`);
export const getAnalysisFiles = (repoId, runId) =>
  api.get(`/repositories/${repoId}/analyses/${runId}/files`);
export const getLatestAnalysis = (repoId) =>
  api.get(`/repositories/${repoId}/analyses/latest`);

// Metrics APIs
export const getLatestMetrics = (repoId) =>
  api.get(`/repositories/${repoId}/metrics/latest`);
export const getMetricsTrend = (repoId, from, to) =>
  api.get(`/repositories/${repoId}/metrics/trend`, { params: { from, to } });
export const getHotspots = (repoId) =>
  api.get(`/repositories/${repoId}/metrics/hotspots`);

// Alert APIs
export const getRepoAlerts = (repoId) =>
  api.get(`/repositories/${repoId}/alerts`);
export const getAlertSummary = () => api.get('/alerts/summary');
export const resolveAlert = (id) => api.put(`/alerts/${id}/resolve`);

// Threshold APIs
export const getThresholds = (repoId) =>
  api.get(`/repositories/${repoId}/thresholds`);
export const updateThresholds = (repoId, data) =>
  api.put(`/repositories/${repoId}/thresholds`, data);
export const resetThresholds = (repoId) =>
  api.post(`/repositories/${repoId}/thresholds/reset`);

// Dashboard APIs
export const getDashboardSummary = () => api.get('/dashboard/summary');

export default api;
