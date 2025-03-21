import styles from "./LoginPage.module.css";

const LoginPage = () => {
  return (
    <div className={styles.LoginPage}>
      <section className={styles.container}>
        <div className={styles["login-container"]}>
          <div className={`${styles.circle} ${styles["circle-one"]}`}></div>
          <div className={styles["form-container"]}>
            {/* <img
              src="https://raw.githubusercontent.com/hicodersofficial/glassmorphism-login-form/master/assets/illustration.png"
              alt="illustration"
              className={styles.illustration}
            /> */}
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
            <div className={`${styles["register-forget"]} ${styles.opacity}`}>
              <a className={styles["register-hover"]} href="/signup">
                REGISTER
              </a>
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
