import "./SignUpPage.css";
import NavBar from "../../components/NavBar/NavBar";
import { Link } from "react-router-dom"; // Added import

const SignUpPage = () => {
  return (
    <div className="SignUpPage">
      <div className="split-screen">
        <div className="left-side">
          <div className="left-side-text">
            <h1 className="left-side-header">Create your free account</h1>
            <p className="left-side-sub-title">
              All Your Sounds, All in One Place.
            </p>
            <div className="astro-pic">
              <img src="/astro-pic.png" alt="Astro character" />
            </div>
          </div>
        </div>
        <div className="right-side">
          <form className="signup-form">
            <h2>
              Sign up to <span className="astro-glow-font">AstroGlow</span>
            </h2>
            <div className="form-group">
              <label htmlFor="email">Email</label>
              <input type="email" id="email" name="email" required />
            </div>
            <div className="form-group">
              <label htmlFor="password">Password</label>
              <input type="password" id="password" name="password" required />
            </div>
            <button type="submit" className="signup-button">
              Sign up
            </button>
            <div className="signup-google-login">
              <a href="http://google.com/">Login with Google</a>
            </div>
            <div className="signup-login-link">
              <span className="already">Already have an account? </span>
              <Link to="/login">Login</Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default SignUpPage;
