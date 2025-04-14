import React from 'react';
import styles from './Modal.module.css'; // We'll create this CSS module next

const Modal = ({ isOpen, onClose, title, children }) => {
  if (!isOpen) return null;

  return (
    <div className={styles.modalOverlay} onClick={onClose}> {/* Close on overlay click */}
      <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}> {/* Prevent closing when clicking inside content */}
        <div className={styles.modalHeader}>
          <h2>{title}</h2>
          <button onClick={onClose} className={styles.closeButton}>×</button>
        </div>
        <div className={styles.modalBody}>
          {children}
        </div>
      </div>
    </div>
  );
};

export default Modal; 