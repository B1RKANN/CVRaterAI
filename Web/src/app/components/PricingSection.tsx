'use client';

import Link from 'next/link';

// Simple SVG icons as components
const CheckIcon = () => (
  <svg className="h-5 w-5 text-blue-400 mr-2 sm:mr-3 flex-shrink-0 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
    <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
  </svg>
);

const XMarkIcon = () => (
  <svg className="h-5 w-5 text-blue-400 mr-2 sm:mr-3 flex-shrink-0 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
    <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
  </svg>
);

export default function PricingSection() {
  return (
    <section id="pricing" className="w-full py-12 md:py-16 px-4 md:px-16 bg-transparent relative z-20 overflow-hidden">
      {/* Background gradient spots */}
      <div className="absolute inset-0 z-0 overflow-hidden pointer-events-none">
        {/* Left side gradient spot */}
        <div 
          className="absolute bottom-0 left-1/4 w-[600px] h-[600px]" 
          style={{
            background: 'radial-gradient(circle, rgba(37, 99, 235, 0.6) 0%, rgba(59, 130, 246, 0.3) 30%, rgba(96, 165, 250, 0.1) 60%, transparent 80%)',
            borderRadius: '50%',
            filter: 'blur(70px)',
            transform: 'translate(-30%, 30%)'
          }}
        />
        
        {/* Right side gradient spot */}
        <div 
          className="absolute top-0 right-1/4 w-[500px] h-[500px]" 
          style={{
            background: 'radial-gradient(circle, rgba(59, 130, 246, 0.5) 0%, rgba(37, 99, 235, 0.2) 40%, rgba(96, 165, 250, 0.1) 70%, transparent 85%)',
            borderRadius: '50%',
            filter: 'blur(70px)',
            transform: 'translate(20%, -30%)'
          }}
        />
      </div>
      
      <div className="max-w-7xl mx-auto relative">
        {/* Section title */}
        <div className="text-center mb-8 md:mb-12">
          <h2 className="text-3xl sm:text-4xl md:text-5xl font-bold text-white mb-2 md:mb-3">Pricing Plans</h2>
          <p className="text-base sm:text-lg text-blue-200 max-w-3xl mx-auto">
            Choose the perfect plan for your needs and start improving your CV today
          </p>
        </div>
        
        {/* Pricing cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 lg:gap-10">
          
          {/* FREE Tier */}
          <div className="bg-gradient-to-br from-blue-900/30 to-indigo-900/30 backdrop-blur-sm p-5 sm:p-6 md:p-8 rounded-2xl border border-blue-800/50 hover:border-blue-600/50 transition-all flex flex-col h-full">
            <div className="mb-4 md:mb-6">
              <span className="text-blue-400 font-semibold tracking-wider uppercase text-xs sm:text-sm">Free</span>
              <div className="mt-2 flex items-baseline">
                <span className="text-3xl sm:text-4xl font-bold text-white">$0</span>
                <span className="text-base sm:text-lg text-blue-300 ml-1">/month</span>
              </div>
              <p className="text-sm sm:text-base text-blue-200 mt-2 sm:mt-3">Perfect for getting started with CVRaterAI</p>
            </div>
            
            <div className="border-t border-blue-800/50 py-4 md:py-6 mb-4 md:mb-6">
              <ul className="space-y-3 md:space-y-4 text-sm sm:text-base">
                <li className="flex items-start">
                  <CheckIcon />
                  <span className="text-blue-100">20 Credits per week</span>
                </li>
                <li className="flex items-start">
                  <CheckIcon />
                  <span className="text-blue-100">Basic CV analysis</span>
                </li>
                <li className="flex items-start">
                  <CheckIcon />
                  <span className="text-blue-100">Standard templates</span>
                </li>
                <li className="flex items-start opacity-50">
                  <XMarkIcon />
                  <span className="text-blue-100">Premium features</span>
                </li>
                <li className="flex items-start opacity-50">
                  <XMarkIcon />
                  <span className="text-blue-100">Priority support</span>
                </li>
              </ul>
            </div>
            
            <div className="mt-auto">
              <Link 
                href="/register" 
                className="block w-full py-2.5 md:py-3 px-4 md:px-6 text-center bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors"
              >
                Get Started
              </Link>
            </div>
          </div>
          
          {/* PRO Tier */}
          <div className="bg-gradient-to-br from-blue-700/40 to-indigo-700/40 backdrop-blur-sm p-5 sm:p-6 md:p-8 rounded-2xl border border-blue-500/50 hover:border-blue-400/50 transition-all flex flex-col h-full relative md:transform md:scale-105 shadow-xl shadow-blue-500/20">
            {/* Popular badge */}
            <div className="absolute top-0 right-6 -translate-y-1/2 bg-gradient-to-r from-blue-500 to-indigo-500 px-3 py-1 rounded-full text-xs font-semibold text-white">
              Most Popular
            </div>
            
            <div className="mb-4 md:mb-6">
              <span className="text-blue-300 font-semibold tracking-wider uppercase text-xs sm:text-sm">Pro</span>
              <div className="mt-2 flex items-baseline">
                <span className="text-3xl sm:text-4xl font-bold text-white">$9.99</span>
                <span className="text-base sm:text-lg text-blue-300 ml-1">/month</span>
              </div>
              <p className="text-sm sm:text-base text-blue-100 mt-2 sm:mt-3">For professionals looking to maximize their potential</p>
            </div>
            
            <div className="border-t border-blue-600/50 py-4 md:py-6 mb-4 md:mb-6">
              <ul className="space-y-3 md:space-y-4 text-sm sm:text-base">
                <li className="flex items-start">
                  <CheckIcon />
                  <span className="text-blue-50">Unlimited Credits</span>
                </li>
                <li className="flex items-start">
                  <CheckIcon />
                  <span className="text-blue-50">Advanced CV analysis</span>
                </li>
                <li className="flex items-start">
                  <CheckIcon />
                  <span className="text-blue-50">Premium templates</span>
                </li>
                <li className="flex items-start">
                  <CheckIcon />
                  <span className="text-blue-50">Industry-specific suggestions</span>
                </li>
                <li className="flex items-start">
                  <CheckIcon />
                  <span className="text-blue-50">Priority email support</span>
                </li>
              </ul>
            </div>
            
            <div className="mt-auto">
              <Link 
                href="/register/pro" 
                className="block w-full py-2.5 md:py-3 px-4 md:px-6 text-center bg-gradient-to-r from-blue-500 to-blue-700 hover:from-blue-600 hover:to-blue-800 text-white font-medium rounded-lg transition-all hover:shadow-lg hover:shadow-blue-500/30"
              >
                Get Started
              </Link>
            </div>
          </div>
          
          {/* BUSINESS Tier */}
          <div className="bg-gradient-to-br from-blue-900/30 to-indigo-900/30 backdrop-blur-sm p-5 sm:p-6 md:p-8 rounded-2xl border border-blue-800/50 hover:border-blue-600/50 transition-all flex flex-col h-full">
            <div className="mb-4 md:mb-6">
              <span className="text-blue-400 font-semibold tracking-wider uppercase text-xs sm:text-sm">Business</span>
              <div className="mt-2 flex items-baseline">
                <span className="text-2xl sm:text-3xl md:text-4xl font-bold text-white">Coming Soon</span>
              </div>
              <p className="text-sm sm:text-base text-blue-200 mt-2 sm:mt-3">Enterprise-grade solutions for your business</p>
            </div>
            
            <div className="border-t border-blue-800/50 py-4 md:py-6 mb-4 md:mb-6">
              <ul className="space-y-3 md:space-y-4 text-sm sm:text-base">
                <li className="flex items-start">
                  <CheckIcon />
                  <span className="text-blue-100">Team member accounts</span>
                </li>
                <li className="flex items-start">
                  <CheckIcon />
                  <span className="text-blue-100">Admin dashboard</span>
                </li>
                <li className="flex items-start">
                  <CheckIcon />
                  <span className="text-blue-100">Custom branding</span>
                </li>
                <li className="flex items-start">
                  <CheckIcon />
                  <span className="text-blue-100">API access</span>
                </li>
                <li className="flex items-start">
                  <CheckIcon />
                  <span className="text-blue-100">Dedicated account manager</span>
                </li>
              </ul>
            </div>
            
            <div className="mt-auto">
              <button 
                disabled
                className="block w-full py-2.5 md:py-3 px-4 md:px-6 text-center bg-blue-900/70 text-blue-200 font-medium rounded-lg cursor-not-allowed"
              >
                Notify Me
              </button>
            </div>
          </div>
          
        </div>
        
        {/* Testimonial or additional info */}
        <div className="mt-10 md:mt-16 text-center">
          <p className="text-sm sm:text-base text-blue-300 max-w-2xl mx-auto">
            Join thousands of professionals who have improved their job prospects with CVRaterAI. Cancel anytime.
          </p>
        </div>
      </div>
    </section>
  );
} 