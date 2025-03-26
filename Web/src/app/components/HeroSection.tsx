'use client';

import Image from 'next/image';
import Link from 'next/link';

export default function HeroSection() {
  return (
    <section className="flex flex-col md:flex-row items-center justify-between w-full min-h-[calc(100vh-80px)] pt-28 pb-12 px-4 md:px-16 bg-transparent relative z-20">
      <div className="flex-1 flex justify-center mb-8 md:mb-0 order-2 md:order-1 relative z-30">
        <div className="relative w-[300px] md:w-[400px] robot-container">
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
      
      <div className="flex-1 text-white max-w-2xl order-1 md:order-2 relative z-30">
        <h1 className="text-6xl md:text-7xl font-bold mb-4">
          CV Rater AI
        </h1>
        <h2 className="text-3xl md:text-4xl font-medium mb-6">
          The Ultimate AI Tools
        </h2>
        <p className="text-lg mb-8">
        Analyze your CV with artificial intelligence, 
        <br /> discover your strengths and make better choices!
        </p>
        <Link 
          href="#"
          className="inline-block px-6 py-3 bg-gradient-to-r from-blue-500 to-blue-700 text-white font-medium rounded-full hover:opacity-90 transition-all duration-300 transform hover:scale-105 hover:shadow-[0_0_15px_rgba(59,130,246,0.5)] relative overflow-hidden group"
        >
          <span className="relative z-10">Generative AI</span>
        </Link>
      </div>
    </section>
  );
} 