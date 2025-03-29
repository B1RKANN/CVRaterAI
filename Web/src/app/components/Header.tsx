"use client";

import Image from 'next/image';
import Link from 'next/link';
import { useState, useEffect } from 'react';

// Navigasyon bağlantısı bileşeni
interface NavLinkProps {
  href: string;
  onClick?: (e: React.MouseEvent) => void;
  children: React.ReactNode;
  className?: string;
}

const NavLink = ({ href, onClick, children, className = "" }: NavLinkProps) => (
  <a 
    href={href} 
    onClick={onClick}
    className={`text-white/80 hover:text-white font-medium transition-colors relative group cursor-pointer ${className}`}
  >
    {children}
    <span className="absolute bottom-0 left-0 w-0 h-0.5 bg-gradient-to-r from-blue-500 to-blue-700 group-hover:w-full transition-all duration-300"></span>
  </a>
);

// Ana menü bağlantısı tipleri
interface NavigationLink {
  name: string;
  href: string;
  sectionId: string | null;
  isScrollable: boolean;
}

// Ana menü bağlantıları 
const baseNavigationLinks: NavigationLink[] = [
  { name: 'Home', href: '/#hero', sectionId: 'hero', isScrollable: true },
  { name: 'Features', href: '/#features', sectionId: 'features', isScrollable: true },
  { name: 'Pricing', href: '/#pricing', sectionId: 'pricing', isScrollable: true }
];

// Giriş yapılınca eklenecek navigasyon linki
const authenticatedLinks: NavigationLink[] = [
  { name: 'CV Analysis', href: '/upload', sectionId: null, isScrollable: false }
];

export default function Header() {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [navigationLinks, setNavigationLinks] = useState<NavigationLink[]>(baseNavigationLinks);

  // Kullanıcının giriş durumunu kontrol et
  useEffect(() => {
    // Tokeni localStorage veya cookie'den kontrol et
    const token = localStorage.getItem('token') || 
                  document.cookie.split(';').find(cookie => 
                    cookie.trim().startsWith('token='))?.split('=')[1];
    
    // Token varsa kullanıcı giriş yapmış demektir
    const loggedIn = !!token;
    setIsLoggedIn(loggedIn);
    
    // Kullanıcı giriş yapmışsa, CV Analysis linkini ekle
    if (loggedIn) {
      setNavigationLinks([...baseNavigationLinks, ...authenticatedLinks]);
    } else {
      setNavigationLinks(baseNavigationLinks);
    }
  }, []);

  // Dışarı tıklandığında menüyü kapat
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

  // Masaüstü boyutuna geçildiğinde mobil menüyü kapat
  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth >= 768 && isMenuOpen) {
        setIsMenuOpen(false);
      }
    };
    
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, [isMenuOpen]);

  // Bölümlere yumuşak kaydırma fonksiyonu
  const scrollToSection = (e: React.MouseEvent, sectionId: string | null) => {
    e.preventDefault();
    
    // Mobil menü açıksa kapat
    if (isMenuOpen) {
      setIsMenuOpen(false);
    }
    
    // SectionId yoksa işlem yapma
    if (!sectionId) return;
    
    // Ana sayfada olup olmadığımızı kontrol et
    if (window.location.pathname === '/') {
      // Ana sayfadaysak, bölüme kaydır
      const section = document.getElementById(sectionId);
      if (section) {
        // Bölümün konumunu al
        const sectionTop = section.getBoundingClientRect().top;
        // Sabit başlığı hesaba kat (80px yükseklik)
        const offsetPosition = sectionTop + window.pageYOffset - 80;
        
        // Belirtilen offsetle bölüme kaydır
        window.scrollTo({
          top: offsetPosition,
          behavior: 'smooth'
        });
      }
    } else {
      // Ana sayfada değilsek, ana sayfaya git ve bölüme kaydır
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
        {/* Masaüstü Navigasyonu */}
        <nav className="hidden md:flex items-center space-x-6">
          {navigationLinks.map((link) => (
            link.isScrollable ? (
              <NavLink 
                key={link.name}
                href={link.href} 
                onClick={(e) => scrollToSection(e, link.sectionId!)}
              >
                {link.name}
              </NavLink>
            ) : (
              <Link 
                key={link.name}
                href={link.href} 
                className="text-white/80 hover:text-white font-medium transition-colors relative group"
              >
                {link.name}
                <span className="absolute bottom-0 left-0 w-0 h-0.5 bg-gradient-to-r from-blue-500 to-blue-700 group-hover:w-full transition-all duration-300"></span>
              </Link>
            )
          ))}
        </nav>
        
        {/* Masaüstü Butonları */}
        <div className="hidden md:flex items-center space-x-4">
          {isLoggedIn ? (
            <Link 
              href="/profile" 
              className="text-white/90 hover:text-white border border-blue-600 px-5 py-2 rounded-full font-medium transition-all hover:bg-blue-600/20"
            >
              Profile
            </Link>
          ) : (
            <Link 
              href="/signin" 
              className="text-white/90 hover:text-white border border-blue-600 px-5 py-2 rounded-full font-medium transition-all hover:bg-blue-600/20"
            >
              Sign In
            </Link>
          )}
          <Link 
            href="/download" 
            className="bg-gradient-to-r from-blue-500 to-blue-700 text-white px-5 py-2 rounded-full font-medium hover:shadow-lg hover:shadow-blue-500/30 transition-all"
          >
            Download
          </Link>
        </div>
        
        {/* Hamburger Menü Butonu */}
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
      
      {/* Mobil Navigasyon Menüsü - Animasyonlu */}
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
            {navigationLinks.map((link) => (
              link.isScrollable ? (
                <a 
                  key={link.name}
                  href={link.href} 
                  className="text-white text-xl font-medium py-2 transform transition-transform duration-300 hover:scale-105"
                  onClick={(e) => scrollToSection(e, link.sectionId!)}
                >
                  {link.name}
                </a>
              ) : (
                <Link 
                  key={link.name}
                  href={link.href} 
                  className="text-white text-xl font-medium py-2 transform transition-transform duration-300 hover:scale-105"
                  onClick={() => setIsMenuOpen(false)}
                >
                  {link.name}
                </Link>
              )
            ))}
          </nav>
          
          <div className="mt-auto mb-10 w-full max-w-xs">
            {isLoggedIn ? (
              <Link 
                href="/profile" 
                className="block text-center w-full text-white border border-blue-600 py-3 px-6 rounded-full font-medium my-4 hover:bg-blue-600/20 transition-colors"
                onClick={() => setIsMenuOpen(false)}
              >
                Profile
              </Link>
            ) : (
              <Link 
                href="/signin" 
                className="block text-center w-full text-white border border-blue-600 py-3 px-6 rounded-full font-medium my-4 hover:bg-blue-600/20 transition-colors"
                onClick={() => setIsMenuOpen(false)}
              >
                Sign In
              </Link>
            )}
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