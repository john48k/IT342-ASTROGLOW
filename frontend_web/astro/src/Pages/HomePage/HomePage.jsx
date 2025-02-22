import { Bell, HelpCircle, Home, LayoutGrid, MessageSquare, Search, Settings2, Target } from "lucide-react"
import { useState } from 'react'

// btw i used tailwind on this for the styling @cg

export const HomePage = () => {
    const [activeTab, setActiveTab] = useState("viewed")
    return (
        <div className="min-h-screen bg-[#ffffff]">
            {/* Top Navigation */}
            <header className="flex items-center px-4 h-14 border-b">
                <div className="flex items-center gap-2">
                    <button className="p-2 hover:bg-gray-100 rounded-md">
                        <LayoutGrid className="h-4 w-4" />
                    </button>
                    {/* <a href="#" className="flex items-center gap-1 text-[#2684ff] font-medium">

                        <svg viewBox="0 0 32 32" className="h-6 w-6" fill="#2684ff">
                            <path d="M15.3 3.3c-.5-.5-1.1-.3-1.4.4-.2.4-.4 1.2-.4 1.8 0 .5-.1 1-.1 1 0 .1-.9-.2-2-.7-1.1-.4-2.1-.8-2.3-.8-.8 0-1 .4-.7 1.4.2.5.7 1.3 1.2 1.7.5.4.9.8.9.8-.1.1-.8 0-1.8-.2-1.5-.3-1.7-.3-2.2.2-.6.6-.6.7 0 1.8.4.6 1.1 1.3 1.6 1.6.5.3 1 .6 1 .6 0 .1-.4.3-1 .6-.5.3-1.2 1-1.6 1.6-.6 1.1-.6 1.2 0 1.8.5.5.7.5 2.2.2.9-.2 1.7-.3 1.8-.2 0 0-.4.4-.9.8-.5.4-1 1.2-1.2 1.7-.3 1-.1 1.4.7 1.4.2 0 1.2-.4 2.3-.8 1.1-.5 2-.8 2-.7 0 0 .1.5.1 1 0 .6.2 1.4.4 1.8.3.7.9.9 1.4.4.3-.3.6-1 .7-1.6.1-.6.2-1.1.3-1.1.1-.1.5.3.9.8.4.5 1.1.9 1.6 1 .7.1.9-.1.9-.8 0-.5-.3-1.3-.7-1.8-.4-.5-.7-1-.7-1 0-.1.5-.1 1.1-.1.6 0 1.4-.2 1.8-.5.6-.5.6-.8-.1-1.3-.4-.3-1.2-.6-1.8-.7-.6-.1-1.1-.2-1.1-.2 0-.1.3-.6.7-1 .4-.5.7-1.3.7-1.8 0-.7-.2-.9-.9-.8-.5.1-1.2.5-1.6 1-.4.5-.8.9-.9.8-.1 0-.2-.5-.3-1.1-.1-.6-.4-1.3-.7-1.6z"></path>
                        </svg>
                        AstroGlow
                    </a> */}

                    <a href="#" className="flex items-center gap-1 text-[#2684ff] font-medium">
                        <img src="./src/assets/images/AstroGlow-logo.png" alt="Profile" className="h-6 w-6 rounded-full" />
                        AstroGlow
                    </a>
                </div>
                <div className="flex-1 mx-4">
                    <div className="relative">
                        <Search className="absolute left-2 top-2.5 h-4 w-4 text-gray-400" />
                        <input
                            type="text"
                            placeholder="Search"
                            className="w-full pl-8 pr-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-[#2684ff] focus:border-transparent"
                        />
                    </div>
                </div>
                <div className="flex items-center gap-2">
                    <button className="p-2 hover:bg-gray-100 rounded-md">
                        <Bell className="h-4 w-4" />
                    </button>
                    <button className="p-2 hover:bg-gray-100 rounded-md">
                        <HelpCircle className="h-4 w-4" />
                    </button>
                    <button className="p-2 hover:bg-gray-100 rounded-md">
                        <Settings2 className="h-4 w-4" />
                    </button>
                    {/* <span className="h-6 w-6 flex items-center justify-center rounded-full bg-[#0052cc] text-white text-sm">
                        1
                    </span> */}
                    {/* <span className="h-6 w-6 flex items-center justify-center rounded-full bg-[#0052cc] text-white text-sm">
                        1
                    </span> */}
                    <span className="h-6 w-6 flex items-center justify-center rounded-full bg-[#0052cc] text-white text-sm">
                        <img src="./src/assets/images/cg-pfp.jpg" alt="Cg Profile" className="h-full w-full rounded-full" />
                    </span>
                </div>
            </header>
            {/* This is the side bar i repeat this is the side bar bruhhhhh  */}
            <div className="flex">
                {/* Sidebar */}
                <aside className="w-64 border-r min-h-[calc(100vh-3.5rem)] p-4">
                    <nav className="space-y-2">
                        <a
                            href="#"
                            className="flex items-center gap-2 px-3 py-2 text-sm font-medium rounded-md bg-blue-50 text-[#2684ff]"
                        >
                            <Home className="h-4 w-4" />
                            Your home
                        </a>
                        <a
                            href="#"
                            className="flex items-center gap-2 px-3 py-2 text-sm font-medium rounded-md text-[#6b6e76] hover:bg-gray-100"
                        >
                            <MessageSquare className="h-4 w-4" />
                            Recent
                        </a>
                        <a
                            href="#"
                            className="flex items-center gap-2 px-3 py-2 text-sm font-medium rounded-md text-[#6b6e76] hover:bg-gray-100"
                        >
                            <Target className="h-4 w-4" />
                            Goals
                        </a>
                    </nav>
                </aside>

                {/* Main Content */}
                <main className="flex-1 p-8">
                    <h1 className="text-2xl font-semibold mb-8">G&apos;day, John | CG | ALLEN</h1>

                    <div className="space-y-8">
                        <section>
                            <div className="flex items-center justify-between mb-4">
                                <h2 className="text-lg font-semibold">Your apps</h2>
                                <button className="text-[#2684ff] hover:underline">Show all</button>
                            </div>
                            <div className="grid grid-cols-5 gap-4">
                                {["Trello", "Jira", "Account settings", "AstroGlow Support", "AstroGlow Community"].map((app) => (
                                    <div
                                        key={app}
                                        className="p-6 text-center border rounded-lg hover:shadow-md transition-shadow cursor-pointer"
                                    >
                                        <div className="w-12 h-12 mx-auto mb-4 bg-gray-100 rounded-md flex items-center justify-center">
                                            <Settings2 className="h-6 w-6 text-gray-600" />
                                        </div>
                                        <p className="text-sm font-medium">{app}</p>
                                    </div>
                                ))}
                            </div>
                        </section>

                        <section>
                            <h2 className="text-lg font-semibold mb-4">Other products you can try</h2>
                            <div className="grid grid-cols-3 gap-4">
                                {["Jira Product Discovery", "Confluence", "Jira Service Management"].map((product) => (
                                    <div key={product} className="p-6 border rounded-lg hover:shadow-md transition-shadow">
                                        <div className="w-12 h-12 mb-4 bg-gray-100 rounded-md flex items-center justify-center">
                                            <Settings2 className="h-6 w-6 text-gray-600" />
                                        </div>
                                        <p className="font-medium mb-2">{product}</p>
                                        <button className="px-3 py-1 text-sm border rounded-md hover:bg-gray-50">Try</button>
                                    </div>
                                ))}
                            </div>
                        </section>

                        <section>
                            <h2 className="text-lg font-semibold mb-4">Recent</h2>
                            <div className="border-b">
                                <div className="flex gap-4">
                                    <button
                                        onClick={() => setActiveTab("worked")}
                                        className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${activeTab === "worked"
                                            ? "border-[#2684ff] text-[#2684ff]"
                                            : "border-transparent text-gray-600 hover:border-gray-300"
                                            }`}
                                    >
                                        Worked on
                                    </button>
                                    <button
                                        onClick={() => setActiveTab("viewed")}
                                        className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${activeTab === "viewed"
                                            ? "border-[#2684ff] text-[#2684ff]"
                                            : "border-transparent text-gray-600 hover:border-gray-300"
                                            }`}
                                    >
                                        Viewed
                                    </button>
                                </div>
                            </div>
                            <div className="mt-8 text-center">
                                <img

                                    src="./src/assets/images/spotify-placeholder.png"

                                    alt="Spotify Place Holder"
                                    width={300}
                                    height={300}
                                    className="mx-auto mb-4"
                                />
                                <h3 className="text-lg font-semibold mb-2">Looks like you&apos;re new here</h3>
                                <p className="text-[#6b6e76]">Your work will appear here as you use your AstroGlow products</p>
                            </div>
                        </section>
                    </div>
                </main>
            </div>
        </div>
    )
}