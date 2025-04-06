import { Error, Loader, SongCard } from '../../components';
import { genres } from '../../assets/constants';
import { Link } from "react-router-dom";

export const HomePage = () => {
    console.log(genres);
    return (
        // <div className="flex flex-col bg-blue-900 min-h-screen" >
        <div className="flex flex-col bg-gradient-to-br from-black to-purple-800 min-h-screen">
            <div className='w-full flex justify-between items-center sm:flex-row flex-col mt-4 mb-10'>

                <select />
            </div>
        </div >
    );
};

export default HomePage;
