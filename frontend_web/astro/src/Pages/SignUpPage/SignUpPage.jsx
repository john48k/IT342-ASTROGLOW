import './SignUpPage.css';
import '../../components/NavBar/NavBar.css'; // Import NavBar CSS
import NavBar from '../../components/NavBar/NavBar';

const SignUpPage = () => {
    return (
        <div className="SignUpPage">
            <NavBar />
            <div className="split-screen">
                <div className="left-side">
                    <div className='left-side-text'>
                        <h1 className='left-side-header'>Create your free account</h1>
                        <p className='left-side-sub-title'>All Your Sounds, All in One Place.</p>
                    </div>
                    {/* <img src="path/to/your/image.jpg" alt="Sign Up" className="full-size-image" /> */}
                </div>
                <div className="right-side">
                    <form className="signup-form">
                        <h2>Sign up to <span className="astro-glow-font">AstroGlow</span></h2>
                        <div className="form-group">
                            <label htmlFor="username">Username</label>
                            <input type="text" id="username" name="username" required />
                        </div>
                        <div className="form-group">
                            <label htmlFor="email">Email</label>
                            <input type="email" id="email" name="email" required />
                        </div>
                        <div className="form-group">
                            <label htmlFor="password">Password</label>
                            <input type="password" id="password" name="password" required />
                        </div>
                        <button type="submit" className="signup-button">Sign up</button>
                        <div className="google-login">
                            <a href="http://google.com/">Login with Google</a>
                        </div>
                        <div className="login-link">
                            <span>Already have an account? </span>
                            <a href="/login">Login</a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default SignUpPage;