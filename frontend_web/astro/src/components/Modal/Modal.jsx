import React from "react";
import styles from "./Modal.module.css";

const Modal = ({ isOpen, onClose, onConfirm, title, message }) => {
  if (isOpen !== true) return null;

  // Close modal when clicking on backdrop
  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return (
    <div className={styles.modalOverlay} onClick={handleBackdropClick}>
      <div className={styles.modalContent}>
        <button 
          className={styles.closeButton} 
          onClick={onClose}
          aria-label="Close modal"
        >
          ×
        </button>
        
        <h2 className={styles.modalTitle}>{title}</h2>
        <div className={styles.modalMessage}>{message}</div>
        
        <div className={styles.modalButtons}>
          <button
            className={`${styles.modalButton} ${styles.cancelButton}`}
            onClick={onClose}
          >
            Cancel
          </button>
          <button
            className={`${styles.modalButton} ${styles.confirmButton}`}
            onClick={onConfirm}
          >
            Confirm
          </button>
        </div>
      </div>
    </div>
  );
};

export default Modal;
