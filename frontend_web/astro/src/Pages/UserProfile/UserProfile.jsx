import React, { useState, useEffect, useRef } from 'react';
import styles from './UserProfile.module.css';
import NavBar from '../../components/NavBar/NavBar';
import { Link, useNavigate } from 'react-router-dom';
import { useUser } from '../../context/UserContext';

export const UserProfile = () => {
    const { user, logout } = useUser();
    const navigate = useNavigate();
    const [isEditingName, setIsEditingName] = useState(false);
    const [isChangingPassword, setIsChangingPassword] = useState(false);
    const [updateSuccess, setUpdateSuccess] = useState('');
    const [updateError, setUpdateError] = useState('');
    const [nameForm, setNameForm] = useState({ userName: '' });
    const [passwordForm, setPasswordForm] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
    });
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [deleteStep, setDeleteStep] = useState('confirmation'); // 'confirmation' or 'password'
    const [deletePassword, setDeletePassword] = useState('');
    const [deleteError, setDeleteError] = useState('');
    const [showCurrentPassword, setShowCurrentPassword] = useState(false);
    const [showNewPassword, setShowNewPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [showDeletePassword, setShowDeletePassword] = useState(false);
    const [passwordValidation, setPasswordValidation] = useState({
        length: false,
        upperCase: false,
        lowerCase: false,
        number: false,
        special: false,
        match: false
    });
    const [profileImage, setProfileImage] = useState(null);
    const [profileImageUrl, setProfileImageUrl] = useState('');
    const [isUploadingImage, setIsUploadingImage] = useState(false);
    const fileInputRef = useRef(null);

    useEffect(() => {
        // Redirect to login if no user is logged in
        if (!user) {
            navigate('/login');
        } else {
            // Initialize form with current username
            setNameForm({ userName: user.userName || '' });
            
            // Fetch the user's profile picture from the server
            fetchProfilePicture();
        }
    }, [user, navigate]);

    const fetchProfilePicture = async () => {
        if (!user || !user.userId) return;
        
        try {
            const response = await fetch(`http://localhost:8080/api/user/profile-picture/${user.userId}`, {
                method: 'GET',
            });
            
            if (!response.ok) {
                throw new Error('Failed to fetch profile picture');
            }
            
            const data = await response.json();
            
            if (data.status === 'success' && data.profilePicture) {
                setProfileImageUrl(data.profilePicture);
            }
        } catch (error) {
            console.error('Error fetching profile picture:', error);
            // Don't show error to user as this is not critical
        }
    };

    // Validate password requirements as user types
    useEffect(() => {
        const { newPassword, confirmPassword } = passwordForm;
        
        setPasswordValidation({
            length: newPassword.length >= 8,
            upperCase: /[A-Z]/.test(newPassword),
            lowerCase: /[a-z]/.test(newPassword),
            number: /[0-9]/.test(newPassword),
            special: /[@$!%*?&]/.test(newPassword),
            match: newPassword === confirmPassword && newPassword !== ''
        });
    }, [passwordForm.newPassword, passwordForm.confirmPassword]);

    const handleLogoutClick = () => {
        logout();
        navigate('/login');
    };

    const handleNameChange = (e) => {
        setNameForm({ ...nameForm, [e.target.name]: e.target.value });
    };

    const handlePasswordChange = (e) => {
        setPasswordForm({ ...passwordForm, [e.target.name]: e.target.value });
    };

    const validatePassword = (password) => {
        // At least 8 characters, one uppercase, one lowercase, one number, one special character
        const regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
        return regex.test(password);
    };

    const handleImageClick = () => {
        fileInputRef.current.click();
    };

    const handleImageChange = async (e) => {
        if (e.target.files && e.target.files[0]) {
            const file = e.target.files[0];
            
            // Check if file is an image
            if (!file.type.match('image.*')) {
                setUpdateError('Please select an image file');
                return;
            }
            
            // Check file size (limit to 2MB)
            if (file.size > 2 * 1024 * 1024) {
                setUpdateError('Image size should be less than 2MB');
                return;
            }
            
            try {
                setIsUploadingImage(true);
                
                // Convert image file to base64 string
                const reader = new FileReader();
                reader.onload = async (event) => {
                    const base64Image = event.target.result;
                    
                    // Upload the image to the server
                    const response = await fetch(`http://localhost:8080/api/user/update-profile-picture/${user.userId}`, {
                        method: 'PUT',
                        headers: {
                            'Content-Type': 'application/json',
                        },
                        body: JSON.stringify({
                            profilePicture: base64Image
                        }),
                    });
                    
                    if (!response.ok) {
                        const errorData = await response.json();
                        throw new Error(errorData.message || 'Failed to upload profile picture');
                    }
                    
                    // Update the UI with the new image
                    setProfileImageUrl(base64Image);
                    setUpdateSuccess('Profile picture updated successfully');
                    setTimeout(() => setUpdateSuccess(''), 3000);
                    setIsUploadingImage(false);
                };
                
                reader.readAsDataURL(file);
                setProfileImage(file);
                
            } catch (error) {
                console.error('Error uploading profile picture:', error);
                setUpdateError(error.message || 'Failed to upload profile picture');
                setIsUploadingImage(false);
            }
        }
    };

    const handleRemoveImage = async () => {
        try {
            setIsUploadingImage(true);
            
            // Send request to clear the profile picture
            const response = await fetch(`http://localhost:8080/api/user/update-profile-picture/${user.userId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    profilePicture: '' // Empty string to clear the image
                }),
            });
            
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to remove profile picture');
            }
            
            // Clear the image from UI
            setProfileImageUrl('');
            setProfileImage(null);
            setUpdateSuccess('Profile picture removed');
            setTimeout(() => setUpdateSuccess(''), 3000);
            
        } catch (error) {
            console.error('Error removing profile picture:', error);
            setUpdateError(error.message || 'Failed to remove profile picture');
        } finally {
            setIsUploadingImage(false);
        }
    };

    const handleSubmitNameChange = async (e) => {
        e.preventDefault();
        setUpdateSuccess('');
        setUpdateError('');

        if (!nameForm.userName.trim()) {
            setUpdateError('Username cannot be empty');
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/api/user/putUser/${user.userId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userName: nameForm.userName
                }),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to update name');
            }

            const updatedUser = await response.json();
            
            // Update the user in context
            // We need to maintain all current user fields and just update the name
            const updatedUserInfo = {
                ...user,
                userName: updatedUser.userName
            };
            
            // This would typically be done through the context, but for simplicity,
            // we can update localStorage directly and refresh the page
            localStorage.setItem('user', JSON.stringify(updatedUserInfo));
            
            setUpdateSuccess('Name updated successfully');
            setIsEditingName(false);
            
            // Refresh the page after a short delay to update the user context
            setTimeout(() => window.location.reload(), 1000);
        } catch (error) {
            console.error('Error updating name:', error);
            setUpdateError(error.message || 'Failed to update name');
        }
    };

    const handleSubmitPasswordChange = async (e) => {
        e.preventDefault();
        setUpdateSuccess('');
        setUpdateError('');

        // Validate password
        if (!passwordForm.currentPassword.trim()) {
            setUpdateError('Current password is required');
            return;
        }

        if (passwordForm.newPassword !== passwordForm.confirmPassword) {
            setUpdateError('New passwords do not match');
            return;
        }

        if (!validatePassword(passwordForm.newPassword)) {
            setUpdateError('Password must be at least 8 characters and include uppercase, lowercase, number, and special character');
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/api/user/putUser/${user.userId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userPassword: passwordForm.newPassword
                }),
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Failed to update password');
            }

            setUpdateSuccess(data.message || 'Password updated successfully');
            setIsChangingPassword(false);
            setPasswordForm({
                currentPassword: '',
                newPassword: '',
                confirmPassword: ''
            });
        } catch (error) {
            console.error('Error updating password:', error);
            setUpdateError(error.message || 'Failed to update password');
        }
    };

    const handleDeleteAccount = async (e) => {
        e.preventDefault();
        setDeleteError('');

        if (!deletePassword.trim()) {
            setDeleteError('Please enter your password to confirm account deletion');
            return;
        }

        try {
            // First verify the password is correct
            const verifyResponse = await fetch(`http://localhost:8080/api/user/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userEmail: user.userEmail,
                    userPassword: deletePassword
                }),
            });

            if (!verifyResponse.ok) {
                setDeleteError('Incorrect password. Account deletion canceled.');
                return;
            }

            // If password is correct, proceed with deletion
            const deleteResponse = await fetch(`http://localhost:8080/api/user/deleteUser/${user.userId}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                },
            });

            if (!deleteResponse.ok) {
                const errorData = await deleteResponse.json();
                throw new Error(errorData.message || 'Failed to delete account');
            }

            // Log out and redirect to login page
            logout();
            navigate('/login');
        } catch (error) {
            console.error('Error deleting account:', error);
            setDeleteError(error.message || 'Failed to delete account');
        }
    };

    // Modified part for delete account
    const handleOpenDeleteModal = () => {
        setShowDeleteModal(true);
        setDeleteStep('confirmation');
        setIsEditingName(false);
        setIsChangingPassword(false);
        setDeleteError('');
    };

    const handleCloseDeleteModal = () => {
        setShowDeleteModal(false);
        setDeleteStep('confirmation');
        setDeletePassword('');
        setDeleteError('');
    };

    const handleProceedToPassword = () => {
        setDeleteStep('password');
    };

    // Close modal if escape key is pressed
    useEffect(() => {
        const handleEsc = (event) => {
            if (event.key === 'Escape') {
                handleCloseDeleteModal();
            }
        };
        window.addEventListener('keydown', handleEsc);
        
        return () => {
            window.removeEventListener('keydown', handleEsc);
        };
    }, []);

    // Close modal when clicking outside of it
    const modalContentRef = useRef(null);
    
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (showDeleteModal && modalContentRef.current && !modalContentRef.current.contains(event.target)) {
                handleCloseDeleteModal();
            }
        };
        
        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [showDeleteModal]);

    if (!user) {
        return <div>Loading...</div>;
    }

    return (
        <div className={styles.userInfoPage}>
            {/* Keep the existing navbar */}
            <NavBar />

            <div className={styles.container}>
                {/* Sidebar */}
                <aside className={styles.sidebar}>
                    <ul>
                        <div className={styles.libraryHeader}>
                            <img className={styles.libraryLogo} src="library-music.png" alt="" />
                            <p>Your Library</p>
                        </div>
                        <Link to="/home" className="">Your Home</Link>
                        <br />
                        <Link to="/home" className="">Favorites</Link>
                    </ul>
                </aside>

                {/* Main content area */}
                <main className={styles.mainContent}>
                    <div className={styles.profileCard}>
                        {/* Profile Image Upload */}
                        <div 
                            className={`${styles.profileAvatar} ${isUploadingImage ? styles.uploading : ''}`}
                            onClick={handleImageClick}
                            title="Click to change profile picture"
                        >
                            {profileImageUrl ? (
                                <img 
                                    src={profileImageUrl} 
                                    alt="Profile" 
                                    className={styles.avatarImage} 
                                />
                            ) : (
                                user.userName ? user.userName.charAt(0).toUpperCase() : 'U'
                            )}
                            <div className={styles.avatarOverlay}>
                                <span>{isUploadingImage ? 'Uploading...' : 'Change Photo'}</span>
                            </div>
                            <input 
                                type="file" 
                                ref={fileInputRef} 
                                onChange={handleImageChange} 
                                accept="image/*" 
                                className={styles.fileInput}
                                disabled={isUploadingImage} 
                            />
                            {isUploadingImage && <div className={styles.spinnerOverlay}><div className={styles.spinner}></div></div>}
                        </div>

                        {profileImageUrl && (
                            <button 
                                className={styles.removeImageButton} 
                                onClick={handleRemoveImage}
                                title="Remove profile picture"
                                disabled={isUploadingImage}
                            >
                                {isUploadingImage ? 'Removing...' : 'Remove Photo'}
                            </button>
                        )}

                        <h1 className={styles.welcomeTitle}>
                            Welcome, {user.userName || 'User'}
                        </h1>
                        <p className={styles.emailText}>{user.userEmail}</p>

                        {/* Success and error messages */}
                        {updateSuccess && <div className={styles.successMessage}>{updateSuccess}</div>}
                        {updateError && <div className={styles.errorMessage}>{updateError}</div>}

                        {/* Edit Name Form */}
                        {isEditingName ? (
                            <form onSubmit={handleSubmitNameChange} className={styles.editForm}>
                                <div className={styles.formGroup}>
                                    <label htmlFor="userName">New Name</label>
                                    <input
                                        type="text"
                                        id="userName"
                                        name="userName"
                                        value={nameForm.userName}
                                        onChange={handleNameChange}
                                        required
                                        className={styles.formInput}
                                        autoFocus
                                    />
                                </div>
                                <div className={styles.buttonGroup}>
                                    <button type="submit" className={styles.saveButton}>
                                        Save
                                    </button>
                                    <button
                                        type="button"
                                        onClick={() => setIsEditingName(false)}
                                        className={styles.cancelButton}
                                    >
                                        Cancel
                                    </button>
                                </div>
                            </form>
                        ) : (
                            <div className={styles.inputBoxContainer}>
                                <div className={styles.inputBoxLabel}>Username</div>
                                <div 
                                    className={styles.inputBox}
                                    onClick={() => {
                                        setIsEditingName(true);
                                        setIsChangingPassword(false);
                                    }}
                                >
                                    <span>{user.userName}</span>
                                    <button className={styles.editIconButton}>
                                        <span className={styles.editIcon}>✎</span>
                                        Edit
                                    </button>
                                </div>
                            </div>
                        )}

                        {/* Change Password Form */}
                        {isChangingPassword ? (
                            <form onSubmit={handleSubmitPasswordChange} className={styles.editForm}>
                                <div className={styles.formGroup}>
                                    <label htmlFor="currentPassword">Current Password</label>
                                    <div className={styles.passwordInputContainer}>
                                        <input
                                            type={showCurrentPassword ? "text" : "password"}
                                            id="currentPassword"
                                            name="currentPassword"
                                            value={passwordForm.currentPassword}
                                            onChange={handlePasswordChange}
                                            required
                                            className={styles.formInput}
                                            autoFocus
                                        />
                                        <button 
                                            type="button" 
                                            className={styles.togglePasswordButton}
                                            onClick={() => setShowCurrentPassword(!showCurrentPassword)}
                                            aria-label={showCurrentPassword ? "Hide password" : "Show password"}
                                        >
                                            {showCurrentPassword ? (
                                                <svg
                                                    xmlns="http://www.w3.org/2000/svg"
                                                    width="20"
                                                    height="20"
                                                    viewBox="0 0 24 24"
                                                    fill="none"
                                                    stroke="currentColor"
                                                    strokeWidth="2"
                                                    strokeLinecap="round"
                                                    strokeLinejoin="round"
                                                >
                                                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                                                    <line x1="1" y1="1" x2="23" y2="23"></line>
                                                </svg>
                                            ) : (
                                                <svg
                                                    xmlns="http://www.w3.org/2000/svg"
                                                    width="20"
                                                    height="20"
                                                    viewBox="0 0 24 24"
                                                    fill="none"
                                                    stroke="currentColor"
                                                    strokeWidth="2"
                                                    strokeLinecap="round"
                                                    strokeLinejoin="round"
                                                >
                                                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                                                    <circle cx="12" cy="12" r="3"></circle>
                                                </svg>
                                            )}
                                        </button>
                                    </div>
                                </div>
                                <div className={styles.formGroup}>
                                    <label htmlFor="newPassword">New Password</label>
                                    <div className={styles.passwordInputContainer}>
                                        <input
                                            type={showNewPassword ? "text" : "password"}
                                            id="newPassword"
                                            name="newPassword"
                                            value={passwordForm.newPassword}
                                            onChange={handlePasswordChange}
                                            required
                                            className={styles.formInput}
                                        />
                                        <button 
                                            type="button" 
                                            className={styles.togglePasswordButton}
                                            onClick={() => setShowNewPassword(!showNewPassword)}
                                            aria-label={showNewPassword ? "Hide password" : "Show password"}
                                        >
                                            {showNewPassword ? (
                                                <svg
                                                    xmlns="http://www.w3.org/2000/svg"
                                                    width="20"
                                                    height="20"
                                                    viewBox="0 0 24 24"
                                                    fill="none"
                                                    stroke="currentColor"
                                                    strokeWidth="2"
                                                    strokeLinecap="round"
                                                    strokeLinejoin="round"
                                                >
                                                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                                                    <line x1="1" y1="1" x2="23" y2="23"></line>
                                                </svg>
                                            ) : (
                                                <svg
                                                    xmlns="http://www.w3.org/2000/svg"
                                                    width="20"
                                                    height="20"
                                                    viewBox="0 0 24 24"
                                                    fill="none"
                                                    stroke="currentColor"
                                                    strokeWidth="2"
                                                    strokeLinecap="round"
                                                    strokeLinejoin="round"
                                                >
                                                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                                                    <circle cx="12" cy="12" r="3"></circle>
                                                </svg>
                                            )}
                                        </button>
                                    </div>
                                </div>
                                <div className={styles.formGroup}>
                                    <label htmlFor="confirmPassword">Confirm New Password</label>
                                    <div className={styles.passwordInputContainer}>
                                        <input
                                            type={showConfirmPassword ? "text" : "password"}
                                            id="confirmPassword"
                                            name="confirmPassword"
                                            value={passwordForm.confirmPassword}
                                            onChange={handlePasswordChange}
                                            required
                                            className={styles.formInput}
                                        />
                                        <button 
                                            type="button" 
                                            className={styles.togglePasswordButton}
                                            onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                            aria-label={showConfirmPassword ? "Hide password" : "Show password"}
                                        >
                                            {showConfirmPassword ? (
                                                <svg
                                                    xmlns="http://www.w3.org/2000/svg"
                                                    width="20"
                                                    height="20"
                                                    viewBox="0 0 24 24"
                                                    fill="none"
                                                    stroke="currentColor"
                                                    strokeWidth="2"
                                                    strokeLinecap="round"
                                                    strokeLinejoin="round"
                                                >
                                                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                                                    <line x1="1" y1="1" x2="23" y2="23"></line>
                                                </svg>
                                            ) : (
                                                <svg
                                                    xmlns="http://www.w3.org/2000/svg"
                                                    width="20"
                                                    height="20"
                                                    viewBox="0 0 24 24"
                                                    fill="none"
                                                    stroke="currentColor"
                                                    strokeWidth="2"
                                                    strokeLinecap="round"
                                                    strokeLinejoin="round"
                                                >
                                                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                                                    <circle cx="12" cy="12" r="3"></circle>
                                                </svg>
                                            )}
                                        </button>
                                    </div>
                                </div>
                                <div className={styles.passwordRequirements}>
                                    <p>Password Requirements:</p>
                                    <ul>
                                        <li className={passwordValidation.length ? styles.validRequirement : styles.invalidRequirement}>
                                            {passwordValidation.length ? "✓" : "○"} Be at least 8 characters long
                                        </li>
                                        <li className={passwordValidation.upperCase ? styles.validRequirement : styles.invalidRequirement}>
                                            {passwordValidation.upperCase ? "✓" : "○"} Include at least one uppercase letter
                                        </li>
                                        <li className={passwordValidation.lowerCase ? styles.validRequirement : styles.invalidRequirement}>
                                            {passwordValidation.lowerCase ? "✓" : "○"} Include at least one lowercase letter
                                        </li>
                                        <li className={passwordValidation.number ? styles.validRequirement : styles.invalidRequirement}>
                                            {passwordValidation.number ? "✓" : "○"} Include at least one number
                                        </li>
                                        <li className={passwordValidation.special ? styles.validRequirement : styles.invalidRequirement}>
                                            {passwordValidation.special ? "✓" : "○"} Include at least one special character (@$!%*?&)
                                        </li>
                                        <li className={passwordValidation.match ? styles.validRequirement : styles.invalidRequirement}>
                                            {passwordValidation.match ? "✓" : "○"} Passwords match
                                        </li>
                                    </ul>
                                </div>
                                <div className={styles.buttonGroup}>
                                    <button 
                                        type="submit" 
                                        className={styles.saveButton}
                                        disabled={!passwordValidation.length || 
                                                !passwordValidation.upperCase || 
                                                !passwordValidation.lowerCase || 
                                                !passwordValidation.number || 
                                                !passwordValidation.special || 
                                                !passwordValidation.match}
                                    >
                                        Change Password
                                    </button>
                                    <button
                                        type="button"
                                        onClick={() => setIsChangingPassword(false)}
                                        className={styles.cancelButton}
                                    >
                                        Cancel
                                    </button>
                                </div>
                            </form>
                        ) : (
                            <div className={styles.inputBoxContainer}>
                                <div className={styles.inputBoxLabel}>Password</div>
                                <div 
                                    className={styles.inputBox}
                                    onClick={() => {
                                        setIsChangingPassword(true);
                                        setIsEditingName(false);
                                    }}
                                >
                                    <span>••••••••••</span>
                                    <button className={styles.editIconButton}>
                                        <span className={styles.editIcon}>✎</span>
                                        Change
                                    </button>
                                </div>
                            </div>
                        )}

                        {/* Delete Account Button */}
                        <button
                            className={styles.deleteAccountButton}
                            onClick={handleOpenDeleteModal}
                        >
                            Delete Account
                        </button>

                        <button className={styles.logoutButton} onClick={handleLogoutClick}>
                            Logout
                        </button>
                    </div>
                </main>
            </div>

            {/* Delete Account Modal */}
            {showDeleteModal && (
                <div className={styles.modalOverlay}>
                    <div className={styles.modalContent} ref={modalContentRef}>
                        {deleteStep === 'confirmation' ? (
                            <div className={styles.modalBody}>
                                <h3 className={styles.deleteTitle}>Are you sure?</h3>
                                <p className={styles.deleteWarning}>
                                    Are you sure you want to delete your account? This action cannot be undone.
                                </p>
                                <div className={styles.buttonGroup}>
                                    <button 
                                        className={styles.deleteButton} 
                                        onClick={handleProceedToPassword}
                                    >
                                        Yes, delete my account
                                    </button>
                                    <button 
                                        className={styles.cancelButton} 
                                        onClick={handleCloseDeleteModal}
                                    >
                                        No, keep my account
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <div className={styles.modalBody}>
                                <h3 className={styles.deleteTitle}>Delete Account</h3>
                                <p className={styles.deleteWarning}>
                                    Warning: This action cannot be undone. All your data will be permanently deleted.
                                </p>
                                
                                {deleteError && <div className={styles.errorMessage}>{deleteError}</div>}
                                
                                <form onSubmit={handleDeleteAccount} className={styles.deleteForm}>
                                    <div className={styles.formGroup}>
                                        <label htmlFor="deletePassword">Enter your password to confirm</label>
                                        <div className={styles.passwordInputContainer}>
                                            <input
                                                type={showDeletePassword ? "text" : "password"}
                                                id="deletePassword"
                                                value={deletePassword}
                                                onChange={(e) => setDeletePassword(e.target.value)}
                                                required
                                                className={styles.formInput}
                                                autoFocus
                                            />
                                            <button 
                                                type="button" 
                                                className={styles.togglePasswordButton}
                                                onClick={() => setShowDeletePassword(!showDeletePassword)}
                                                aria-label={showDeletePassword ? "Hide password" : "Show password"}
                                            >
                                                {showDeletePassword ? (
                                                    <svg
                                                        xmlns="http://www.w3.org/2000/svg"
                                                        width="20"
                                                        height="20"
                                                        viewBox="0 0 24 24"
                                                        fill="none"
                                                        stroke="currentColor"
                                                        strokeWidth="2"
                                                        strokeLinecap="round"
                                                        strokeLinejoin="round"
                                                    >
                                                        <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                                                        <line x1="1" y1="1" x2="23" y2="23"></line>
                                                    </svg>
                                                ) : (
                                                    <svg
                                                        xmlns="http://www.w3.org/2000/svg"
                                                        width="20"
                                                        height="20"
                                                        viewBox="0 0 24 24"
                                                        fill="none"
                                                        stroke="currentColor"
                                                        strokeWidth="2"
                                                        strokeLinecap="round"
                                                        strokeLinejoin="round"
                                                    >
                                                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                                                        <circle cx="12" cy="12" r="3"></circle>
                                                    </svg>
                                                )}
                                            </button>
                                        </div>
                                    </div>
                                    <div className={styles.buttonGroup}>
                                        <button type="submit" className={styles.deleteButton}>
                                            Confirm Delete
                                        </button>
                                        <button
                                            type="button"
                                            onClick={handleCloseDeleteModal}
                                            className={styles.cancelButton}
                                        >
                                            Cancel
                                        </button>
                                    </div>
                                </form>
                            </div>
                        )}
                        <button className={styles.modalCloseButton} onClick={handleCloseDeleteModal}>
                            ×
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default UserProfile;
