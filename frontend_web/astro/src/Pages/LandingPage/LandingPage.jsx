import "./LandingPage.css"
import { Music, Headphones, Radio, PlayCircle, Star, Users, Library, Zap } from "lucide-react"
import NavBar from "../../Components/NavBar/NavBar"
import { useNavigate } from "react-router-dom"

export default function LandingPage() {
    const navigate = useNavigate();

    return (
        <div className="LandingPageContainer">
            {/* Header Section */}
            <NavBar />
            <header className="LandingPageHeader">
                {/* <nav className="LandingPageNav">
                    <div className="LandingPageLogo">
                        <span className="LandingPageLogoText">AstroGlow</span>
                    </div>
                    <div className="LandingPageNavLinks">
                        <a href="#features">Features</a>
                        <a href="#about">About</a>
                        <a href="#services">Services</a>
                        <a href="#contact">Contact</a>
                    </div>
                    <button className="LandingPageButton">Get Started</button>
                </nav> */}
                <div className="LandingPageHero">
                    <h1 className="LandingPageTitle">AstroGlow</h1>
                    <p className="LandingPageSlogan">Music That Lights Up Your Universe.</p>
                    <div className="LandingPageCTA">
                        <button className="LandingPagePrimaryButton" onClick={() => navigate('/signup')}>
                            <PlayCircle className="LandingPageIcon" />
                            Sign Up Now
                        </button>
                        {/* <button className="LandingPageSecondaryButton">Learn More</button> */}
                    </div>
                </div>
            </header>

            {/* Features Section */}
            <section id="features" className="LandingPageSection LandingPageFeatures">
                <div className="LandingPageSectionContent">
                    <h2 className="LandingPageSectionTitle">Discover the Experience</h2>
                    <p className="LandingPageSectionDescription">
                        Seamlessly integrating various components and services to create a unified and efficient ecosystem for your
                        music journey.
                    </p>

                    <div className="LandingPageFeatureGrid">
                        <div className="LandingPageFeatureCard">
                            <div className="LandingPageFeatureIconWrapper">
                                <Music className="LandingPageFeatureIcon" />
                            </div>
                            <h3>Seamless Streaming</h3>
                            <p>High-quality music streaming with zero interruptions</p>
                        </div>

                        <div className="LandingPageFeatureCard">
                            <div className="LandingPageFeatureIconWrapper">
                                <Headphones className="LandingPageFeatureIcon" />
                            </div>
                            <h3>Personalized Experience</h3>
                            <p>Tailored recommendations based on your listening habits</p>
                        </div>

                        <div className="LandingPageFeatureCard">
                            <div className="LandingPageFeatureIconWrapper">
                                <Radio className="LandingPageFeatureIcon" />
                            </div>
                            <h3>Discover New Music</h3>
                            <p>Explore new artists and genres that match your taste</p>
                        </div>

                        <div className="LandingPageFeatureCard">
                            <div className="LandingPageFeatureIconWrapper">
                                <Library className="LandingPageFeatureIcon" />
                            </div>
                            <h3>Smart Libraries</h3>
                            <p>Organize your music collection with intelligent categorization</p>
                        </div>
                    </div>
                </div>
            </section>

            {/* About Section */}
            <section id="about" className="LandingPageSection LandingPageAbout">
                <div className="LandingPageSectionContent">
                    <div className="LandingPageAboutGrid">
                        <div className="LandingPageAboutInfo">
                            <h2 className="LandingPageSectionTitle">About AstroGlow</h2>
                            <p className="LandingPageAboutDescription">
                                Welcome to AstroGlow! This initiative focuses on seamlessly integrating various components and services
                                to create a unified and efficient ecosystem for managing and enhancing the user experience of a music
                                application.
                            </p>
                            <p className="LandingPageAboutDescription">
                                Our goal is to enable smooth interoperability between core features, such as music streaming, user
                                libraries, playlist management, and external services like third-party APIs, while ensuring reliability,
                                scalability, and performance.
                            </p>
                            <div className="LandingPageStats">
                                <div className="LandingPageStat">
                                    <span className="LandingPageStatNumber">10M+</span>
                                    <span className="LandingPageStatLabel">Songs</span>
                                </div>
                                <div className="LandingPageStat">
                                    <span className="LandingPageStatNumber">5M+</span>
                                    <span className="LandingPageStatLabel">Users</span>
                                </div>
                                <div className="LandingPageStat">
                                    <span className="LandingPageStatNumber">100K+</span>
                                    <span className="LandingPageStatLabel">Playlists</span>
                                </div>
                            </div>
                        </div>
                        <div className="LandingPageAboutImage">
                            <div className="LandingPageImagePlaceholder">
                                <Star className="LandingPageStarIcon" />
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            {/* Services Section */}
            <section id="services" className="LandingPageSection LandingPageServices">
                <div className="LandingPageSectionContent">
                    <h2 className="LandingPageSectionTitle">Our Services</h2>
                    <p className="LandingPageSectionDescription">
                        Enhancing your music experience with innovative features and services
                    </p>

                    <div className="LandingPageServicesGrid">
                        <div className="LandingPageServiceCard">
                            <Zap className="LandingPageServiceIcon" />
                            <h3>Fast Streaming</h3>
                            <p>Lightning-fast music streaming with minimal buffering</p>
                        </div>

                        <div className="LandingPageServiceCard">
                            <Users className="LandingPageServiceIcon" />
                            <h3>Social Sharing</h3>
                            <p>Share your favorite tracks and playlists with friends</p>
                        </div>

                        <div className="LandingPageServiceCard">
                            <Star className="LandingPageServiceIcon" />
                            <h3>Premium Sound</h3>
                            <p>High-definition audio quality for the ultimate experience</p>
                        </div>
                    </div>
                </div>
            </section>

            {/* Contact Section */}
            {/* <section id="contact" className="LandingPageSection LandingPageContact">
                <div className="LandingPageSectionContent">
                    <h2 className="LandingPageSectionTitle">Ready to Experience AstroGlow?</h2>
                    <p className="LandingPageSectionDescription">
                        Join us today and discover music that truly lights up your universe
                    </p>

                    <div className="LandingPageContactForm">
                        <div className="LandingPageFormGroup">
                            <input type="text" placeholder="Your Name" className="LandingPageInput" />
                        </div>
                        <div className="LandingPageFormGroup">
                            <input type="email" placeholder="Your Email" className="LandingPageInput" />
                        </div>
                        <button className="LandingPagePrimaryButton LandingPageFullWidth">Get Early Access</button>
                    </div>
                </div>
            </section> */}

            {/* Footer */}
            <footer className="LandingPageFooter">
                <div className="LandingPageFooterContent">
                    <div className="LandingPageFooterLogo">
                        <span className="LandingPageLogoText">AstroGlow</span>
                        <p className="LandingPageFooterTagline">Music That Lights Up Your Universe.</p>
                    </div>

                    <div className="LandingPageFooterLinks">
                        <div className="LandingPageFooterLinkGroup">
                            <h4>Company</h4>
                            <a href="#about">About</a>
                            <a href="#careers">Careers</a>
                            <a href="#press">Press</a>
                        </div>

                        <div className="LandingPageFooterLinkGroup">
                            <h4>Resources</h4>
                            <a href="#blog">Blog</a>
                            <a href="#help">Help Center</a>
                            <a href="#contact">Contact</a>
                        </div>

                        <div className="LandingPageFooterLinkGroup">
                            <h4>Legal</h4>
                            <a href="#terms">Terms</a>
                            <a href="#privacy">Privacy</a>
                            <a href="#cookies">Cookies</a>
                        </div>
                    </div>
                </div>

                <div className="LandingPageFooterBottom">
                    <p>&copy; {new Date().getFullYear()} AstroGlow. All rights reserved.</p>
                </div>
            </footer>
        </div>
    )
}
