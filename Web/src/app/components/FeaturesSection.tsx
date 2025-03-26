'use client';

import Image from 'next/image';
import Link from 'next/link';

export default function FeaturesSection() {
  return (
    <section className="w-full py-20 px-4 md:px-16 bg-transparent relative z-20 overflow-hidden">
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
        <div className="text-center mb-16">
          <h2 className="text-4xl md:text-5xl font-bold text-white mb-4">Our Features</h2>
          <p className="text-lg text-blue-200 max-w-3xl mx-auto">
            Discover how our AI-powered tools can help you create the perfect CV and land your dream job
          </p>
        </div>
        
        {/* Features grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 relative z-30">
          {/* Feature 1 */}
          <div className="bg-gradient-to-br from-blue-900/40 to-indigo-900/40 backdrop-blur-sm p-6 rounded-xl border border-blue-800/50 hover:border-blue-600/50 transition-all">
            <div className="w-14 h-14 bg-blue-600 rounded-lg mb-4 flex items-center justify-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
            <h3 className="text-xl font-bold text-white mb-2">CV Analysis</h3>
            <p className="text-blue-200">
              Our AI analyzes your CV in seconds, providing detailed feedback and suggestions for improvement.
            </p>
          </div>
          
          {/* Feature 2 */}
          <div className="bg-gradient-to-br from-blue-900/40 to-indigo-900/40 backdrop-blur-sm p-6 rounded-xl border border-blue-800/50 hover:border-blue-600/50 transition-all">
            <div className="w-14 h-14 bg-blue-600 rounded-lg mb-4 flex items-center justify-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
            <h3 className="text-xl font-bold text-white mb-2">Skill Optimization</h3>
            <p className="text-blue-200">
              Identify key skills that are in demand for your industry and tailor your CV accordingly.
            </p>
          </div>
          
          {/* Feature 3 */}
          <div className="bg-gradient-to-br from-blue-900/40 to-indigo-900/40 backdrop-blur-sm p-6 rounded-xl border border-blue-800/50 hover:border-blue-600/50 transition-all">
            <div className="w-14 h-14 bg-blue-600 rounded-lg mb-4 flex items-center justify-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
              </svg>
            </div>
            <h3 className="text-xl font-bold text-white mb-2">Smart Suggestions</h3>
            <p className="text-blue-200">
              Get intelligent recommendations to enhance your work experience descriptions and achievements.
            </p>
          </div>
        </div>
        
        {/* Featured image and info section */}
        <div className="mt-20 flex flex-col md:flex-row items-center">
          <div className="w-full md:w-1/2 mb-8 md:mb-0 relative">
            <div className="relative z-30 flex justify-center">
              <Image 
                src="/info.webp" 
                alt="Robot Assistant" 
                width={200} 
                height={200}
                className="w-3/5 h-auto mx-auto robot-float"
              />
              
              {/* Robot floating animation */}
              <style jsx global>{`
                @keyframes robotFloat {
                  0% {
                    transform: translateX(0px) rotate(0deg);
                  }
                  25% {
                    transform: translateX(-10px) rotate(-3deg);
                  }
                  50% {
                    transform: translateY(-5px) rotate(0deg);
                  }
                  75% {
                    transform: translateX(10px) rotate(3deg);
                  }
                  100% {
                    transform: translateX(0px) rotate(0deg);
                  }
                }
                
                .robot-float {
                  animation: robotFloat 6s ease-in-out infinite;
                  transform-origin: center center;
                }
              `}</style>
            </div>
            
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
          
          <div className="w-full md:w-1/2 md:pl-12">
            <h3 className="text-3xl font-bold text-white mb-5">Real-time CV Analysis</h3>
            <p className="text-lg text-blue-100 mb-6">
              Our advanced AI algorithm analyzes your CV in real-time, providing instant feedback on structure, content, 
              and presentation. Identify strengths and weaknesses to make your CV stand out from the competition.
            </p>
            
            <ul className="space-y-3 mb-8">
              {['Instant scoring system', 'Keyword optimization', 'ATS compatibility check', 'Industry-specific recommendations'].map((item, index) => (
                <li key={index} className="flex items-start">
                  <svg className="h-6 w-6 text-blue-400 mr-2 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                  </svg>
                  <span className="text-blue-100">{item}</span>
                </li>
              ))}
            </ul>
            
            <Link 
              href="/features"
              className="inline-flex items-center px-5 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors"
            >
              Learn More
              <svg className="ml-2 h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M14 5l7 7m0 0l-7 7m7-7H3" />
              </svg>
            </Link>
          </div>
        </div>
      </div>
    </section>
  );
} 