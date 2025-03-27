'use client';

import Image from 'next/image';
import Link from 'next/link';

export default function FeaturesSection() {
  return (
    <section id="features" className="w-full py-8 md:py-12 px-4 md:px-16 bg-transparent relative z-20 overflow-hidden">
      {/* Background gradient spots */}
      <div className="absolute inset-0 z-0 overflow-hidden pointer-events-none">
        {/* Left side gradient spot */}
        <div 
          className="absolute top-1/4 left-0 w-[700px] h-[700px]" 
          style={{
            background: 'radial-gradient(circle, rgba(37, 99, 235, 0.7) 0%, rgba(59, 130, 246, 0.4) 30%, rgba(96, 165, 250, 0.1) 60%, transparent 80%)',
            borderRadius: '50%',
            filter: 'blur(70px)',
            transform: 'translate(-30%, -30%)'
          }}
        />
        
        {/* Right side gradient spot */}
        <div 
          className="absolute bottom-0 right-0 w-[600px] h-[600px]" 
          style={{
            background: 'radial-gradient(circle, rgba(59, 130, 246, 0.6) 0%, rgba(37, 99, 235, 0.3) 40%, rgba(96, 165, 250, 0.1) 70%, transparent 85%)',
            borderRadius: '50%',
            filter: 'blur(70px)',
            transform: 'translate(20%, 30%)'
          }}
        />
      </div>
      
      <div className="max-w-7xl mx-auto relative">
        {/* Section title */}
        <div className="text-center mb-2">
          <h2 className="text-3xl sm:text-4xl md:text-5xl font-bold text-white mb-1">Features</h2>
          <p className="text-base sm:text-lg text-blue-200 max-w-3xl mx-auto">
            Discover how our AI-powered tools can help you create the perfect CV and land your dream job
          </p>
        </div>
        
        {/* Mobile Feature Boxes - Only visible on small screens */}
        <div className="block md:hidden mt-8">
          <div className="space-y-4">
            <div className="bg-white text-gray-800 py-2.5 px-5 rounded-full shadow-lg transition-all hover:bg-blue-600 hover:text-white text-center mx-auto max-w-[280px]">
              <p className="font-semibold text-sm whitespace-nowrap">Personalized Recommendations</p>
            </div>
            
            <div className="bg-white text-gray-800 py-2.5 px-5 rounded-full shadow-lg transition-all hover:bg-blue-600 hover:text-white text-center mx-auto max-w-[280px]">
              <p className="font-semibold text-sm whitespace-nowrap">Smart Search Filters</p>
            </div>
            
            <div className="bg-white text-gray-800 py-2.5 px-5 rounded-full shadow-lg transition-all hover:bg-blue-600 hover:text-white text-center mx-auto max-w-[280px]">
              <p className="font-semibold text-sm whitespace-nowrap">Real-Time Availability Updates</p>
            </div>
            
            {/* Robot center image for mobile */}
            <div className="relative z-30 w-48 h-48 mx-auto my-6">
              <Image 
                src="/info.webp" 
                alt="Robot Assistant" 
                width={200} 
                height={200}
                className="w-full h-auto robot-float"
              />
              
              {/* Robot spotlight effect */}
              <div 
                className="absolute top-1/2 left-1/2 w-[250px] h-[250px] -z-10" 
                style={{
                  background: 'radial-gradient(circle, rgba(37, 99, 235, 0.8) 0%, rgba(59, 130, 246, 0.4) 30%, rgba(96, 165, 250, 0.1) 60%, transparent 80%)',
                  borderRadius: '50%',
                  filter: 'blur(40px)',
                  transform: 'translate(-50%, -50%)'
                }}
              />
            </div>
            
            <div className="bg-white text-gray-800 py-2.5 px-5 rounded-full shadow-lg transition-all hover:bg-blue-600 hover:text-white text-center mx-auto max-w-[280px]">
              <p className="font-semibold text-sm whitespace-nowrap">Predictive Pricing Insights</p>
            </div>
            
            <div className="bg-white text-gray-800 py-2.5 px-5 rounded-full shadow-lg transition-all hover:bg-blue-600 hover:text-white text-center mx-auto max-w-[280px]">
              <p className="font-semibold text-sm whitespace-nowrap">Virtual Property Tours</p>
            </div>
            
            <div className="bg-white text-gray-800 py-2.5 px-5 rounded-full shadow-lg transition-all hover:bg-blue-600 hover:text-white text-center mx-auto max-w-[280px]">
              <p className="font-semibold text-sm whitespace-nowrap">Efficient Leasing Process</p>
            </div>
          </div>
        </div>
        
        {/* Desktop Robot image with features - Only visible on medium screens and up */}
        <div className="hidden md:flex relative mx-auto max-w-5xl h-[400px] md:h-[500px] items-center justify-center">
          {/* Robot center image */}
          <div className="relative z-30 w-56 md:w-72 h-56 md:h-72">
            <Image 
              src="/info.webp" 
              alt="Robot Assistant" 
              width={300} 
              height={300}
              className="w-full h-auto robot-float"
            />
            
            {/* Robot spotlight effect */}
            <div 
              className="absolute top-1/2 left-1/2 w-[400px] h-[400px] -z-10" 
              style={{
                background: 'radial-gradient(circle, rgba(37, 99, 235, 0.8) 0%, rgba(59, 130, 246, 0.4) 30%, rgba(96, 165, 250, 0.1) 60%, transparent 80%)',
                borderRadius: '50%',
                filter: 'blur(60px)',
                transform: 'translate(-50%, -50%)'
              }}
            />
          </div>
          
          {/* Feature boxes positioned around the robot - Desktop only */}
          
          {/* Personalized Recommendations - Left top */}
          <div className="absolute left-0 top-[25%] md:top-[30%] transform -translate-y-1/2 z-20 bg-white text-gray-800 py-3 px-6 rounded-full shadow-lg cursor-pointer transition-all hover:bg-blue-600 hover:text-white hover:scale-105">
            <p className="font-semibold text-sm md:text-base whitespace-nowrap">Personalized Recommendations</p>
          </div>
          
          {/* Smart Search Filters - Left middle */}
          <div className="absolute left-[5%] md:left-[10%] top-1/2 transform -translate-y-1/2 z-20 bg-white text-gray-800 py-3 px-6 rounded-full shadow-lg cursor-pointer transition-all hover:bg-blue-600 hover:text-white hover:scale-105">
            <p className="font-semibold text-sm md:text-base whitespace-nowrap">Smart Search Filters</p>
          </div>
          
          {/* Real-Time Availability Updates - Left bottom */}
          <div className="absolute left-0 bottom-[25%] md:bottom-[30%] transform translate-y-1/2 z-20 bg-white text-gray-800 py-3 px-6 rounded-full shadow-lg cursor-pointer transition-all hover:bg-blue-600 hover:text-white hover:scale-105">
            <p className="font-semibold text-sm md:text-base whitespace-nowrap">Real-Time Availability Updates</p>
          </div>
          
          {/* Predictive Pricing Insights - Right top */}
          <div className="absolute right-0 top-[25%] md:top-[30%] transform -translate-y-1/2 z-20 bg-white text-gray-800 py-3 px-6 rounded-full shadow-lg cursor-pointer transition-all hover:bg-blue-600 hover:text-white hover:scale-105">
            <p className="font-semibold text-sm md:text-base whitespace-nowrap">Predictive Pricing Insights</p>
          </div>
          
          {/* Virtual Property Tours - Right middle */}
          <div className="absolute right-[5%] md:right-[10%] top-1/2 transform -translate-y-1/2 z-20 bg-white text-gray-800 py-3 px-6 rounded-full shadow-lg cursor-pointer transition-all hover:bg-blue-600 hover:text-white hover:scale-105">
            <p className="font-semibold text-sm md:text-base whitespace-nowrap">Virtual Property Tours</p>
          </div>
          
          {/* Efficient Leasing Process - Right bottom */}
          <div className="absolute right-0 bottom-[25%] md:bottom-[30%] transform translate-y-1/2 z-20 bg-white text-gray-800 py-3 px-6 rounded-full shadow-lg cursor-pointer transition-all hover:bg-blue-600 hover:text-white hover:scale-105">
            <p className="font-semibold text-sm md:text-base whitespace-nowrap">Efficient Leasing Process</p>
          </div>
        </div>
      </div>
      
      {/* Robot floating animation */}
      <style jsx global>{`
        @keyframes robotFloat {
          0% {
            transform: translateY(0px) rotate(0deg);
          }
          25% {
            transform: translateY(-7px) rotate(-5deg);
          }
          50% {
            transform: translateY(-15px) rotate(0deg);
          }
          75% {
            transform: translateY(-7px) rotate(5deg);
          }
          100% {
            transform: translateY(0px) rotate(0deg);
          }
        }
        
        .robot-float {
          animation: robotFloat 6s ease-in-out infinite;
          transform-origin: center center;
        }
      `}</style>
    </section>
  );
} 