import { Link } from "react-router-dom";
import styles from "./LoginPage.module.css";

const LoginPage = () => {
  const handleGoogleLogin = () => {
    window.location.href = "http://localhost:8080/oauth2/authorization/google";
  };

  const handleGithubLogin = () => {
    window.location.href = "http://localhost:8080/oauth2/authorization/github";
  };

  return (
    <div className={styles.LoginPage}>
      <section className={styles.container}>
        <div className={styles["login-container"]}>
          <div className={`${styles.circle} ${styles["circle-one"]}`}></div>
          <div className={styles["form-container"]}>
            <img
              src="login-character.png"
              alt="login-character"
              className={styles.illustration}
            />
            <h1 className={styles.opacity}>LOGIN</h1>
            <form>
              <input type="text" placeholder="EMAIL" />
              <input type="password" placeholder="PASSWORD" />
              <button className={styles.opacity}>SUBMIT</button>
            </form>

            <div className={`${styles["social-login"]} ${styles.opacity}`}>
              <p>Or login with</p>
              <div className={styles["social-buttons"]}>
                <button
                  onClick={handleGoogleLogin}
                  className={`${styles["social-button"]} ${styles["google-button"]}`}
                >
                  <img
                    src="/google-icon.png"
                    alt="Google"
                    className={styles["social-icon"]}
                  />
                  Google
                </button>
                <button
                  onClick={handleGithubLogin}
                  className={`${styles["social-button"]} ${styles["github-button"]}`}
                >
                  <img
                    src="/github-icon.png"
                    alt="GitHub"
                    className={`${styles["social-icon"]} ${styles["github-icon"]}`}
                  />
                  GitHub
                </button>
              </div>
            </div>

            <div className={`${styles["register-forget"]} ${styles.opacity}`}>
              <Link className={styles["register-hover"]} to="/signup">
                REGISTER
              </Link>
            </div>
          </div>
          <div className={`${styles.circle} ${styles["circle-two"]}`}></div>
        </div>
        <div className={styles["theme-btn-container"]}></div>
      </section>
    </div>
  );
};

export default LoginPage;
