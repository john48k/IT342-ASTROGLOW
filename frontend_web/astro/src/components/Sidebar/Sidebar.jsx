import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import styles from './Sidebar.module.css';

const Sidebar = () => {
  const location = useLocation();

  return (
    <aside className={styles.sidebar}>
      <ul>
        <li>
          <Link 
            to="/home" 
            className={`${styles.sidebarLink} ${location.pathname === '/home' ? styles.active : ''}`}
          >
            Your Home
          </Link>
        </li>
        <li>
          <Link 
            to="/favorites" 
            className={`${styles.sidebarLink} ${location.pathname === '/favorites' ? styles.active : ''}`}
          >
            Favorites
          </Link>
        </li>
      </ul>
    </aside>
  );
};

export default Sidebar; 