"use client";

import Image from 'next/image';
import Link from 'next/link';

export default function Header() {
  return (
    <header className="fixed top-0 left-0 right-0 flex justify-between items-center w-full px-4 md:px-16 h-20 bg-black/60 backdrop-blur-md z-50 transition-all duration-300">
      <Link href="/" className="flex items-center">
        <span className="text-[2.25rem] font-semibold text-white">
          CVRaterAI
        </span>
      </Link>
      
      <div className="flex items-center space-x-6">
        <nav className="hidden md:flex items-center space-x-6">
          <Link href="/" className="text-white/80 hover:text-white font-medium transition-colors relative group">
            Home
            <span className="absolute bottom-0 left-0 w-0 h-0.5 bg-gradient-to-r from-blue-500 to-blue-700 group-hover:w-full transition-all duration-300"></span>
          </Link>
          <Link href="/pricing" className="text-white/80 hover:text-white font-medium transition-colors relative group">
            Pricing
            <span className="absolute bottom-0 left-0 w-0 h-0.5 bg-gradient-to-r from-blue-500 to-blue-700 group-hover:w-full transition-all duration-300"></span>
          </Link>
          <Link href="/features" className="text-white/80 hover:text-white font-medium transition-colors relative group">
            Features
            <span className="absolute bottom-0 left-0 w-0 h-0.5 bg-gradient-to-r from-blue-500 to-blue-700 group-hover:w-full transition-all duration-300"></span>
          </Link>
        </nav>
        
        <div className="flex items-center space-x-4">
          <Link 
            href="/signin" 
            className="text-white/90 hover:text-white border border-blue-600 px-5 py-2 rounded-full font-medium transition-all hover:bg-blue-600/20"
          >
            Sign In
          </Link>
          <Link 
            href="/download" 
            className="bg-gradient-to-r from-blue-500 to-blue-700 text-white px-5 py-2 rounded-full font-medium hover:shadow-lg hover:shadow-blue-500/30 transition-all"
          >
            Download
          </Link>
        </div>
      </div>
    </header>
  );
}