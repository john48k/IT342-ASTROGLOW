import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUser } from '../context/UserContext';

const OAuth2Redirect = () => {
  const navigate = useNavigate();
  const { login } = useUser();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        // Fetch user info from the backend OAuth endpoint
        const response = await fetch('http://localhost:8080/api/user/user-info', {
          credentials: 'include' // Important to include cookies for OAuth session
        });

        if (!response.ok) {
          throw new Error('Failed to get user information');
        }

        const userData = await response.json();
        console.log('OAuth user data:', userData);

        if (!userData || Object.keys(userData).length === 0) {
          throw new Error('No user data received');
        }

        // Extract user information (format depends on the OAuth provider)
        const userInfo = {
          userName: userData.name || userData.login || userData.email?.split('@')[0] || 'User',
          userEmail: userData.email || `${userData.login}@github.com` || 'unknown@email.com',
          // We don't get or store the password for OAuth users
          userId: userData.sub || userData.id || Date.now().toString()
        };

        // Generate a simple session token (in a real app, this would be a JWT from the server)
        const simpleToken = btoa(userInfo.userEmail + ':' + new Date().getTime());
        
        // Login the user
        login(userInfo, simpleToken);
        setLoading(false);
        
        // Redirect to home page
        navigate('/home');
      } catch (err) {
        console.error('Error fetching OAuth user data:', err);
        setError(err.message);
        setLoading(false);
        // Redirect to login page on error
        setTimeout(() => navigate('/login'), 3000);
      }
    };

    fetchUserInfo();
  }, [login, navigate]);

  if (loading) {
    return (
      <div style={{ 
        display: 'flex', 
        flexDirection: 'column',
        alignItems: 'center', 
        justifyContent: 'center', 
        height: '100vh',
        background: 'linear-gradient(45deg, #1e0030, #3a0068)',
        color: 'white',
        fontFamily: 'Arial, sans-serif'
      }}>
        <div style={{
          width: '50px',
          height: '50px',
          border: '5px solid rgba(255, 255, 255, 0.3)',
          borderTop: '5px solid #8c52ff',
          borderRadius: '50%',
          animation: 'spin 1s linear infinite',
          marginBottom: '20px'
        }}></div>
        <h2>Logging you in...</h2>
        <style>{`
          @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
        `}</style>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ 
        display: 'flex', 
        flexDirection: 'column',
        alignItems: 'center', 
        justifyContent: 'center', 
        height: '100vh',
        background: 'linear-gradient(45deg, #1e0030, #3a0068)',
        color: 'white',
        fontFamily: 'Arial, sans-serif',
        padding: '0 20px',
        textAlign: 'center'
      }}>
        <h2>Login Error</h2>
        <p>{error}</p>
        <p>Redirecting to login page...</p>
      </div>
    );
  }

  return null;
};

export default OAuth2Redirect; 