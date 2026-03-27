import React from 'react';
import { Link, useLocation } from 'react-router-dom';

const styles = {
  nav: {
    backgroundColor: '#1a1a2e',
    borderBottom: '1px solid #2a2a4a',
    padding: '0 24px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    height: '60px',
  },
  logo: {
    fontSize: '20px',
    fontWeight: 700,
    color: '#6c63ff',
    textDecoration: 'none',
  },
  links: {
    display: 'flex',
    gap: '24px',
    alignItems: 'center',
  },
  link: {
    color: '#9ca3af',
    textDecoration: 'none',
    fontSize: '14px',
    fontWeight: 500,
    transition: 'color 0.2s',
  },
  activeLink: {
    color: '#6c63ff',
  },
  addBtn: {
    backgroundColor: '#6c63ff',
    color: 'white',
    border: 'none',
    padding: '8px 16px',
    borderRadius: '6px',
    fontSize: '13px',
    fontWeight: 600,
    cursor: 'pointer',
    textDecoration: 'none',
  },
};

function Navbar() {
  const location = useLocation();

  return (
    <nav style={styles.nav}>
      <Link to="/" style={styles.logo}>CodePulse</Link>
      <div style={styles.links}>
        <Link to="/" style={{
          ...styles.link,
          ...(location.pathname === '/' ? styles.activeLink : {}),
        }}>Dashboard</Link>
        <Link to="/add-repository" style={styles.addBtn}>+ Add Repo</Link>
      </div>
    </nav>
  );
}

export default Navbar;
