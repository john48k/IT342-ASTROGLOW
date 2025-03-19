# 🎵AstroGlow🎵

## Product Description

Welcome to AstroGlow! This initiative focuses on seamlessly integrating various components and services to create a unified and efficient ecosystem for managing and enhancing the user experience of a music application.

Our goal is to enable smooth interoperability between core features, such as music streaming, user libraries, playlist management, and external services like third-party APIs, while ensuring reliability, scalability, and performance.

Whether you're a developer looking to contribute, a stakeholder interested in the architecture, or a user curious about the system behind your favorite music experience, this documentation will guide you through the project's structure and functionalities.

## Table of Contents

- [Product Description](#product-description)
- [List of Features for Web Application](#list-of-features-for-web-application)
- [List of Features for Mobile Application](#list-of-features-for-mobile-application)
- [Objectives](#objectives)
- [Links](#links)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Running the Project](#running-the-project)
    - [Web Frontend](#web-frontend)
    - [Mobile Frontend](#mobile-frontend)
  - [Building the Project](#building-the-project)
    - [Web Frontend](#web-frontend-1)
    - [Mobile Frontend](#mobile-frontend-1)
  - [Running Tests](#running-tests) - [Web Frontend](#web-frontend-2) - [Mobile Frontend](#mobile-frontend-2)
  <!-- - [Contributing](#contributing)
- [License](#license) -->
- [Developers Profile](#developers-profile)

## 📝List of features for Web Application Features

- **Import Music**

  - Users can upload new music files from their devices.
  - The system automatically updates the music library when changes occur.
  - Users can delete songs they no longer wish to keep.

- **Offline Listening**

  - Users can download their favorite songs for offline playback.

- **Google Easy Sign-In**

  - One-tap sign-in using Google, eliminating complex account setups.

- **Favorite Songs**

  - Users can mark and organize favorite songs with a heart icon.
  - Advanced search allows quick song location.
  - Users can remove songs from their favorites.

- **Log In and Sign Up**

  - Users can create an account or log in with the option to use Google.

- **Play Button**
  - A simple play button allows quick playback of songs or playlists directly from the browser.

---

## 📝List of features for Mobile Application Features

- **Import Music**

  - Users can upload new music files from their devices.
  - The platform updates music files in the user's library automatically.
  - Users can delete songs they no longer wish to keep.

- **Offline Listening**

  - Users can download their favorite songs for offline playback.

- **Google Easy Sign-In**

  - One-tap sign-in using Google, avoiding complex setups.

- **Biometrics**

  - Users can unlock their accounts using their fingerprint for secure access.

- **Favorite Songs**

  - Users can mark and organize favorite songs with a heart icon.
  - Advanced search allows quick song location.
  - Users can remove songs from their favorites.

- **Log In and Sign Up**

  - Users can create an account or log in with the option to use Google.

- **Play Button**
  - A play button is accessible on any screen for instant playback while navigating the app.

## 📝List of features - Objectives

- Streamline data flow between different system components.
- Enhance system reliability and minimize latency.
- Implement scalable architecture to support growing user bases and content libraries.
- Ensure robust security measures for user data and content rights.

Feel free to explore the repository and contribute to building a better music experience for everyone!

## 🔗 Links

<!-- - [Figma] (https://www.figma.com/design/puqaUlMTznwdG5uTKPoPAP/IT342?node-id=0-1&t=ZkXVjLunc23z2BZm-1)
- [ClickUp] (https://app.clickup.com/9016724751/v/s/90162662327)
- [Diagrams] (https://drive.google.com/file/d/1zkfdnZg_mxy_cdmbxZGZMqQudcnYx7mH/view?usp=sharing)
- [SRS] (https://cebuinstituteoftechnology-my.sharepoint.com/:w:/g/personal/allenluis_mangoroban_cit_edu/Ecw2LrpiV8ROieKj0R3iVnwBfk2hTVy2F8NpNkhAccZNNQ?e=x8hCb7) -->

- [Figma](https://www.figma.com/design/puqaUlMTznwdG5uTKPoPAP/IT342?node-id=0-1&t=ZkXVjLunc23z2BZm-1)

- [ClickUp](https://app.clickup.com/9016724751/v/s/90162662327)

- [Diagrams](https://drive.google.com/file/d/1zkfdnZg_mxy_cdmbxZGZMqQudcnYx7mH/view?usp=sharing)

- [SRS](https://cebuinstituteoftechnology-my.sharepoint.com/:w:/g/personal/allenluis_mangoroban_cit_edu/Ecw2LrpiV8ROieKj0R3iVnwBfk2hTVy2F8NpNkhAccZNNQ?e=x8hCb7)

## Getting Started

### Prerequisites

Make sure you have the following installed on your machine:

- Node.js (v14 or later)
- npm (v6 or later)
- Gradle (v6 or later)
- Java Development Kit (JDK) (v11 or later)

### Installation

1. Clone the repository:

   ```sh
   git clone https://github.com/your-username/IT342-ASTROGLOW.git
   cd IT342-ASTROGLOW
   ```

2. Install dependencies for the web frontend:

   ```sh
   cd frontend_web/astro
   npm install
   ```

3. Install dependencies for the mobile frontend:
   ```sh
   cd ../../frontend_mobile/astroglow
   ./gradlew build
   ```

### Running the Project

#### Web Frontend

1. Navigate to the web frontend directory:

   ```sh
   cd frontend_web/astro
   ```

2. Start the development server:

   ```sh
   npm run dev
   ```

3. Open your browser and go to `http://localhost:3000`.

#### Mobile Frontend

1. Navigate to the mobile frontend directory:

   ```sh
   cd ../../frontend_mobile/astroglow
   ```

2. Run the app on an Android emulator or connected device:
   ```sh
   ./gradlew installDebug
   ```

### Building the Project

#### Web Frontend

1. Navigate to the web frontend directory:

   ```sh
   cd frontend_web/astro
   ```

2. Build the project:
   ```sh
   npm run build
   ```

#### Mobile Frontend

1. Navigate to the mobile frontend directory:

   ```sh
   cd ../../frontend_mobile/astroglow
   ```

2. Build the project:
   ```sh
   ./gradlew assembleRelease
   ```

### Running Tests

#### Web Frontend

1. Navigate to the web frontend directory:

   ```sh
   cd frontend_web/astro
   ```

2. Run the tests:
   ```sh
   npm test
   ```

#### Mobile Frontend

1. Navigate to the mobile frontend directory:

   ```sh
   cd ../../frontend_mobile/astroglow
   ```

2. Run the tests:
   ```sh
   ./gradlew test
   ```

<!-- ## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details. -->

## 👨‍💻Developers Profile

#### John Gabriel Cañal

- **Course & Year:** BSIT-3
- **GitHub:** [john48k](https://github.com/john48k)

#### Allen Luis S. Mangoroban

- **Course & Year:** BSIT-3
- **GitHub:** [Imo-sama](https://github.com/Imo-sama)

#### Cg M. Fernandez

- **Course & Year:** BSIT-3
- **GitHub:** [cg-del](https://github.com/cg-del)
