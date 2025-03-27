"use client";

import Image from 'next/image';
import Link from 'next/link';
import { useState, useEffect } from 'react';

export default function Header() {
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  // Close menu when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as Element;
      if (isMenuOpen && !target.closest('#mobile-menu') && !target.closest('#menu-button')) {
        setIsMenuOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [isMenuOpen]);

  // Close mobile menu when resizing to desktop
  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth >= 768 && isMenuOpen) {
        setIsMenuOpen(false);
      }
    };
    
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, [isMenuOpen]);

  // Function to handle smooth scrolling to sections
  const scrollToSection = (e: React.MouseEvent, sectionId: string) => {
    e.preventDefault();
    
    // Close mobile menu if open
    if (isMenuOpen) {
      setIsMenuOpen(false);
    }
    
    // Check if we're on the homepage
    if (window.location.pathname === '/') {
      // If on homepage, scroll to the section
      const section = document.getElementById(sectionId);
      if (section) {
        // Get the section's position
        const sectionTop = section.getBoundingClientRect().top;
        // Account for the fixed header (80px height)
        const offsetPosition = sectionTop + window.pageYOffset - 80;
        
        // Scroll to the section with the offset
        window.scrollTo({
          top: offsetPosition,
          behavior: 'smooth'
        });
      }
    } else {
      // If not on homepage, navigate to homepage and then scroll to section
      window.location.href = `/#${sectionId}`;
    }
  };
  
  return (
    <header className="fixed top-0 left-0 right-0 flex justify-between items-center w-full px-4 md:px-16 h-20 bg-black/60 backdrop-blur-md z-50 transition-all duration-300">
      <Link href="/" className="flex items-center">
        <span className="text-xl sm:text-2xl md:text-[2.25rem] font-semibold text-white">
          CVRaterAI
        </span>
      </Link>
      
      <div className="flex items-center space-x-4 md:space-x-6">
        {/* Desktop Navigation */}
        <nav className="hidden md:flex items-center space-x-6">
          <Link href="/" className="text-white/80 hover:text-white font-medium transition-colors relative group">
            Home
            <span className="absolute bottom-0 left-0 w-0 h-0.5 bg-gradient-to-r from-blue-500 to-blue-700 group-hover:w-full transition-all duration-300"></span>
          </Link>
          <a 
            href="/#features" 
            onClick={(e) => scrollToSection(e, 'features')}
            className="text-white/80 hover:text-white font-medium transition-colors relative group cursor-pointer"
          >
            Features
            <span className="absolute bottom-0 left-0 w-0 h-0.5 bg-gradient-to-r from-blue-500 to-blue-700 group-hover:w-full transition-all duration-300"></span>
          </a>
          <a 
            href="/#pricing" 
            onClick={(e) => scrollToSection(e, 'pricing')}
            className="text-white/80 hover:text-white font-medium transition-colors relative group cursor-pointer"
          >
            Pricing
            <span className="absolute bottom-0 left-0 w-0 h-0.5 bg-gradient-to-r from-blue-500 to-blue-700 group-hover:w-full transition-all duration-300"></span>
          </a>
        </nav>
        
        {/* Desktop Buttons */}
        <div className="hidden md:flex items-center space-x-4">
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
        
        {/* Hamburger Menu Button */}
        <button 
          id="menu-button"
          className="md:hidden flex items-center justify-center w-10 h-10 text-white focus:outline-none"
          onClick={() => setIsMenuOpen(!isMenuOpen)}
          aria-label="Toggle navigation menu"
        >
          <svg 
            xmlns="http://www.w3.org/2000/svg" 
            className="h-6 w-6 transition-transform duration-300 ease-in-out"
            style={{ transform: isMenuOpen ? 'rotate(90deg)' : 'rotate(0)' }}
            fill="none" 
            viewBox="0 0 24 24" 
            stroke="currentColor"
          >
            {isMenuOpen ? (
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
            ) : (
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 6h16M4 12h16M4 18h16" />
            )}
          </svg>
        </button>
      </div>
      
      {/* Mobile Navigation Menu - With Animation */}
      <div 
        id="mobile-menu"
        className={`md:hidden fixed inset-0 top-20 bg-[#051029] z-40 flex flex-col transition-all duration-500 ease-in-out ${
          isMenuOpen ? 'opacity-100 translate-x-0' : 'opacity-0 translate-x-full pointer-events-none'
        }`}
        style={{ height: 'calc(100vh - 5rem)' }}
      >
        <div className="flex flex-col items-center justify-start pt-10 px-6 h-full overflow-y-auto">
          <div className="text-center mb-8">
            <h2 className="text-3xl font-bold text-white mb-2">CV Rater AI</h2>
            <p className="text-blue-300 text-sm">The Ultimate AI Tools</p>
          </div>
          
          <nav className="flex flex-col items-center space-y-6 w-full">
            <Link 
              href="/" 
              className="text-white text-xl font-medium py-2 transform transition-transform duration-300 hover:scale-105"
              onClick={() => setIsMenuOpen(false)}
            >
              Home
            </Link>
            <a 
              href="/#features" 
              className="text-white text-xl font-medium py-2 transform transition-transform duration-300 hover:scale-105"
              onClick={(e) => scrollToSection(e, 'features')}
            >
              Features
            </a>
            <a 
              href="/#pricing" 
              className="text-white text-xl font-medium py-2 transform transition-transform duration-300 hover:scale-105"
              onClick={(e) => scrollToSection(e, 'pricing')}
            >
              Pricing
            </a>
          </nav>
          
          <div className="mt-auto mb-10 w-full max-w-xs">
            <Link 
              href="/signin" 
              className="block text-center w-full text-white border border-blue-600 py-3 px-6 rounded-full font-medium my-4 hover:bg-blue-600/20 transition-colors"
              onClick={() => setIsMenuOpen(false)}
            >
              Sign In
            </Link>
            <Link 
              href="/download" 
              className="block text-center w-full bg-gradient-to-r from-blue-500 to-blue-700 text-white py-3 px-6 rounded-full font-medium hover:shadow-lg hover:shadow-blue-500/30 transition-all"
              onClick={() => setIsMenuOpen(false)}
            >
              Download
            </Link>
          </div>
        </div>
      </div>
    </header>
  );
}