import React, { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useUser } from '../context/UserContext';

const OAuth2Redirect = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useUser();
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        // Fetch user info from the backend OAuth endpoint
        const response = await fetch('https://astroglowfirebase-d2411.uc.r.appspot.com/api/user/user-info', {
          credentials: 'include' // Important to include cookies for OAuth session
        });

        if (!response.ok) {
          throw new Error();
        }

        const userData = await response.json();
        console.log('OAuth user data:', userData);

        if (!userData || Object.keys(userData).length === 0) {
          throw new Error();
        }

        // Extract user information (format depends on the OAuth provider)
        const userInfo = {
          userName: userData.userName || userData.name || userData.login || userData.email?.split('@')[0] || 'User',
          userEmail: userData.userEmail || userData.email || `${userData.login}@github.com` || 'unknown@email.com',
          // Use the numeric userId from the backend response
          userId: userData.userId || userData.id || Date.now().toString()
        };

        // Generate a simple session token (in a real app, this would be a JWT from the server)
        const simpleToken = btoa(userInfo.userEmail + ':' + new Date().getTime());

        // Login the user
        login(userInfo, simpleToken);

        // Check if we have a redirect location from the protected route
        const from = location.state?.from || '/home';
        navigate(from);
      } catch (error) {
        console.error('OAuth error:', error);
        navigate('/login');
      }
    };

    fetchUserInfo();
  }, [login, navigate, location.state]);

  if (loading) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        background: 'linear-gradient(135deg, #21295c, #1c1c3c)'
      }}>
        <div style={{
          padding: '20px',
          background: 'rgba(255, 255, 255, 0.1)',
          borderRadius: '10px',
          textAlign: 'center',
          color: 'white'
        }}>
          <h2>Authenticating...</h2>
          <p>Please wait while we complete your sign-in process.</p>
        </div>
      </div>
    );
  }

  return null;
};

export default OAuth2Redirect; 