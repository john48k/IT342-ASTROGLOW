import { useSelector } from 'react-redux';
import { Route, Routes } from 'react-router-dom';

import { Searchbar, Sidebar, MusicPlayer, TopPlay } from './components';
import { ArtistDetails, TopArtists, AroundYou, Search, SongDetails, TopCharts } from './Pages';

import AboutPage from "./Pages/AboutPage/AboutPage";
import HomePage from "./Pages/HomePage/HomePage";
import LandingPage from "./Pages/LandingPage/LandingPage";
import LoginPage from "./Pages/LoginPage/LoginPage";
import SignUpPage from "./Pages/SignUpPage/SignUpPage";
import UserProfilePage from "./Pages/UserProfile/UserProfile";
import { UserProvider } from "./context/UserContext";

const App = () => {
  const { activeSong } = useSelector((state) => state.player);

  return (
    <UserProvider>
      <div className="relative flex">
        <Sidebar />
        <div className="flex-1 flex flex-col bg-gradient-to-br from-black to-[#121286]">
          <Searchbar />

          <div className="px-6 h-[calc(100vh-72px)] overflow-y-scroll hide-scrollbar flex xl:flex-row flex-col-reverse">
            <div className="flex-1 h-fit pb-40">
              <Routes>
                {/* Existing Routes */}
                <Route path="/" element={<LandingPage />} />
                <Route path="/home" element={<HomePage />} />
                <Route path="/about" element={<AboutPage />} />
                <Route path="/signup" element={<SignUpPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/profile" element={<UserProfilePage />} />

                {/* New Routes */}
                <Route path="/" element={<Discover />} />
                <Route path="/top-artists" element={<TopArtists />} />
                <Route path="/top-charts" element={<TopCharts />} />
                <Route path="/around-you" element={<AroundYou />} />
                <Route path="/artists/:id" element={<ArtistDetails />} />
                <Route path="/songs/:songid" element={<SongDetails />} />
                <Route path="/search/:searchTerm" element={<Search />} />
              </Routes>
            </div>
            <div className="xl:sticky relative top-0 h-fit">
              <TopPlay />
            </div>
          </div>
        </div>

        {activeSong?.title && (
          <div className="absolute h-28 bottom-0 left-0 right-0 flex animate-slideup bg-gradient-to-br from-white/10 to-[#2a2a80] backdrop-blur-lg rounded-t-3xl z-10">
            <MusicPlayer />
          </div>
        )}
      </div>
    </UserProvider>
  );
};

export default App;