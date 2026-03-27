import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Dashboard from './pages/Dashboard';
import RepositoryDetail from './pages/RepositoryDetail';
import AnalysisDetail from './pages/AnalysisDetail';
import AddRepository from './pages/AddRepository';
import Settings from './pages/Settings';

function App() {
  return (
    <Router>
      <div style={{ minHeight: '100vh', backgroundColor: '#0f0f23' }}>
        <Navbar />
        <main style={{ maxWidth: '1200px', margin: '0 auto', padding: '24px' }}>
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/repositories/:id" element={<RepositoryDetail />} />
            <Route path="/repositories/:repoId/analyses/:runId" element={<AnalysisDetail />} />
            <Route path="/add-repository" element={<AddRepository />} />
            <Route path="/repositories/:id/settings" element={<Settings />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
