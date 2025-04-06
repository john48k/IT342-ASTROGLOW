import { Error, Loader, SongCard } from '../../components';
import { genres } from '../../assets/constants';
import { Link } from "react-router-dom";

export const HomePage = () => {
    console.log(genres);
    return (
        <div className="flex flex-col">
            <div className='w-full flex justify-between items-center sm:flex-row flex-col mt-4 mb-10'>
                <h1 className='font-bold text-red-500 text-6xl'>Discover</h1>

                <select />
            </div>
        </div >
    );
};

export default HomePage;
