import './LoginPage.css'

const LoginPage = () => {
    return (
        <div className="LoginPage">
            <section className="container">
                <div className="login-container">
                    <div className="circle circle-one"></div>
                    <div className="form-container">
                        <img src="https://raw.githubusercontent.com/hicodersofficial/glassmorphism-login-form/master/assets/illustration.png" alt="illustration" className="illustration" />
                        <h1 className="opacity">LOGIN</h1>
                        <form>
                            <input type="text" placeholder="EMAIL" />
                            <input type="password" placeholder="PASSWORD" />
                            <button className="opacity">SUBMIT</button>
                        </form>
                        <div className="register-forget opacity">
                            <a className='register-hover' href="/signup">REGISTER</a>
                        </div>
                    </div>
                    <div className="circle circle-two"></div>
                </div>
                <div className="theme-btn-container"></div>
            </section>
        </div>
    );
};

export default LoginPage;