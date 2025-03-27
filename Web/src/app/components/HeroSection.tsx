'use client';

import Image from 'next/image';
import Link from 'next/link';

export default function HeroSection() {
  return (
    <section className="flex flex-col md:flex-row items-center justify-between w-full min-h-[calc(100vh-80px)] pt-16 md:pt-28 pb-8 md:pb-12 px-4 md:px-16 bg-transparent relative z-20">
      <div className="flex-1 flex justify-center mb-8 md:mb-0 order-2 md:order-1 relative z-30">
        <div className="relative w-[250px] md:w-[300px] lg:w-[400px] robot-container">
          <Image 
            src="/Chatbot-Hero-Image-1.webp" 
            alt="AI Robot" 
            width={400} 
            height={500}
            priority
            className="max-w-full h-auto"
          />
          
          {/* Robot container animation */}
          <style jsx global>{`
            @keyframes floatAnimation {
              0% {
                transform: translateY(0px);
              }
              50% {
                transform: translateY(-15px);
              }
              100% {
                transform: translateY(0px);
              }
            }
            
            .robot-container {
              animation: floatAnimation 4s ease-in-out infinite;
            }
          `}</style>
        </div>
      </div>
      
      <div className="flex-1 text-white max-w-2xl order-1 md:order-2 relative z-30 text-center md:text-left">
        <h1 className="text-4xl sm:text-5xl md:text-6xl lg:text-7xl font-bold mb-2 md:mb-4">
          CV Rater AI
        </h1>
        <h2 className="text-2xl sm:text-3xl md:text-4xl font-medium mb-4 md:mb-6">
          The Ultimate AI Tools
        </h2>
        <p className="text-base md:text-lg mb-6 md:mb-8">
          Analyze your CV with artificial intelligence, 
          <span className="hidden md:inline"><br /></span> discover your strengths and make better choices!
        </p>
        <Link 
          href="#"
          className="inline-block px-5 sm:px-6 py-2.5 sm:py-3 bg-gradient-to-r from-blue-500 to-blue-700 text-white font-medium rounded-full hover:opacity-90 transition-all duration-300 transform hover:scale-105 hover:shadow-[0_0_15px_rgba(59,130,246,0.5)] relative overflow-hidden group"
        >
          <span className="relative z-10">Generative AI</span>
        </Link>
      </div>
    </section>
  );
} 