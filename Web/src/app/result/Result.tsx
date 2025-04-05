'use client';

import { useState, useEffect, useMemo, useCallback, memo, useRef } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import dynamic from 'next/dynamic';
import Header from '../components/Header';
import GradientSpots from '../components/GradientSpots';

// Dynamically import Particles with SSR disabled for better performance
const Particles = dynamic(() => import('../components/Particles'), { 
  ssr: false,
  loading: () => null
});

// Types for API response
interface EvaluationResponse {
  id: number;
  userId: number;
  fileName: string;
  fileType: string;
  githubUrl: string;
  jobRequirements: string;
  evaluationScore: number;
  evaluationResult: string;
  evaluationDate: string;
  fullName: string;
}

interface EvaluationResult {
  compatibilityStatus: number;
  userInformation: {
    name: string;
    surname: string;
    email: string;
    phone: string;
    skills: string;
  };
  explanation: string;
  skillRatings?: {
    language: string;
    percentage: number;
  }[] | null;
}

// Types for our component
interface Skill {
  name: string;
  percentage: number;
}

interface CVData {
  name: string;
  surname: string;
  email: string;
  phoneNumber: string;
  skills: string[];
  technicalSkills: Skill[];
  compatibilityScore: number;
  aiNote: string;
}

// Detect if device is mobile
const useMobileDetect = () => {
  const [isMobile, setIsMobile] = useState(false);
  
  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 768);
    };
    
    // Check on initial render
    checkMobile();
    
    // Add event listener for window resize
    window.addEventListener('resize', checkMobile);
    
    // Cleanup
    return () => window.removeEventListener('resize', checkMobile);
  }, []);
  
  return isMobile;
};

// Custom hook to observe element visibility
const useInView = (options = {}) => {
  const ref = useRef<HTMLDivElement>(null);
  const [isInView, setIsInView] = useState(false);

  useEffect(() => {
    if (!window.IntersectionObserver) {
      setIsInView(true);
      return;
    }

    const observer = new IntersectionObserver(([entry]) => {
      setIsInView(entry.isIntersecting);
    }, options);

    if (ref.current) {
      observer.observe(ref.current);
    }

    return () => {
      if (ref.current) {
        observer.unobserve(ref.current);
      }
    };
  }, [options]);

  return [ref, isInView] as const;
};

// Memoized Background Gradients Component
const BackgroundGradients = memo(({ isMobile }: { isMobile: boolean }) => (
  <div className="absolute inset-0 z-0 overflow-hidden pointer-events-none">
    {/* Simplified gradient spots for mobile */}
    <div 
      className="absolute bottom-0 left-1/4 w-[300px] h-[300px] md:w-[600px] md:h-[600px]" 
      style={{
        background: 'radial-gradient(circle, rgba(37, 99, 235, 0.6) 0%, rgba(59, 130, 246, 0.3) 30%, rgba(96, 165, 250, 0.1) 60%, transparent 80%)',
        borderRadius: '50%',
        filter: isMobile ? 'blur(30px)' : 'blur(70px)',
        transform: 'translate(-30%, 30%)',
        willChange: 'transform'
      }}
    />
    
    {!isMobile && (
      <div 
        className="absolute top-0 right-1/4 w-[500px] h-[500px]" 
        style={{
          background: 'radial-gradient(circle, rgba(59, 130, 246, 0.5) 0%, rgba(37, 99, 235, 0.2) 40%, rgba(96, 165, 250, 0.1) 70%, transparent 85%)',
          borderRadius: '50%',
          filter: 'blur(70px)',
          transform: 'translate(20%, -30%)',
          willChange: 'transform'
        }}
      />
    )}
  </div>
));
BackgroundGradients.displayName = 'BackgroundGradients';

// Memoized Skill Bar Component
const SkillBar = memo(({ skill, percentage }: { skill: string; percentage: number }) => {
  const isMobile = useMobileDetect();
  const [ref, isInView] = useInView({ threshold: 0.1 });
  
  // For mobile, use a lighter animation approach
  if (isMobile) {
    return (
      <div ref={ref} className="mb-4">
        <div className="flex justify-between mb-1">
          <span className="text-sm font-medium text-white">{skill}</span>
          <span className="text-sm font-medium text-blue-300">%{percentage}</span>
        </div>
        <div className="w-full bg-blue-900/50 rounded-full h-2.5 overflow-hidden">
          <div 
            className={`h-2.5 rounded-full ${isInView ? 'mobile-bar-fill' : ''}`} 
            style={{ 
              width: isInView ? `${percentage}%` : '0%',
              background: 'linear-gradient(to right, #3b82f6, #6366f1)'
            }}
          ></div>
        </div>
      </div>
    );
  }
  
  // Desktop version with animations
  return (
    <div ref={ref} className="mb-4">
      <div className="flex justify-between mb-1">
        <span className="text-sm font-medium text-white">{skill}</span>
        <span className="text-sm font-medium text-blue-300">%{percentage}</span>
      </div>
      <div className="w-full bg-blue-900/50 rounded-full h-2.5 overflow-hidden">
        <div 
          className="h-2.5 rounded-full skill-progress" 
          style={{ 
            width: 0,
            background: 'linear-gradient(to right, #3b82f6, #6366f1)',
            '--target-width': `${percentage}%`
          } as React.CSSProperties}
        ></div>
      </div>
    </div>
  );
});
SkillBar.displayName = 'SkillBar';

// Memoized Compatibility Progress Component with CSS Animation
const CompatibilityProgress = memo(({ score }: { score: number }) => {
  const [ref, isInView] = useInView({ threshold: 0.1 });
  const [isAnimating, setIsAnimating] = useState(false);
  const isMobile = useMobileDetect();
  
  useEffect(() => {
    if (isInView && !isAnimating) {
      setIsAnimating(true);
    }
  }, [isInView, isAnimating]);
  
  const getColorClass = () => {
    if (score < 40) return 'progress-red';
    if (score < 70) return 'progress-yellow';
    return 'progress-green';
  };
  
  // For mobile, use a lighter animation approach
  if (isMobile) {
    return (
      <div ref={ref} className="mb-8">
        <h3 className="text-xl font-semibold text-white mb-4">Compatibility Status</h3>
        <div className="p-4 bg-gradient-to-br from-blue-900/30 to-indigo-900/30 backdrop-blur-sm rounded-xl border border-blue-800/50">
          <div className="flex justify-between mb-2">
            <span className="text-sm font-medium text-blue-300">Job Match Score</span>
            <div className="flex items-center">
              <span className={`text-lg font-bold text-white ${isInView ? 'mobile-counter-animate' : ''}`} data-value={score}>
                0%
              </span>
            </div>
          </div>
          <div className="w-full bg-blue-900/70 rounded-full h-4 mb-2 overflow-hidden">
            <div 
              className={`h-4 rounded-full flex items-center justify-end px-2 ${getColorClass()} ${isInView ? 'mobile-bar-fill' : ''}`}
              style={{ 
                width: isInView ? `${score}%` : '0%',
                transitionDelay: '0.1s'
              }}
            >
              {score >= 25 && (
                <span className={`text-xs font-bold text-white whitespace-nowrap ${isInView ? 'mobile-text-fade' : 'opacity-0'}`}>
                  {score}% Match
                </span>
              )}
            </div>
          </div>
          <div className="flex justify-between text-xs text-blue-400">
            <span>Not Compatible</span>
            <span>Partially Compatible</span>
            <span>Highly Compatible</span>
          </div>
        </div>
      </div>
    );
  }
  
  // Desktop version with animations
  return (
    <div ref={ref} className="mb-8">
      <h3 className="text-xl font-semibold text-white mb-4">Compatibility Status</h3>
      <div className="p-4 bg-gradient-to-br from-blue-900/30 to-indigo-900/30 backdrop-blur-sm rounded-xl border border-blue-800/50">
        <div className="flex justify-between mb-2">
          <span className="text-sm font-medium text-blue-300">Job Match Score</span>
          <div className="flex items-center">
            <span className="text-lg font-bold text-white progress-counter" data-target={score}>
              {isAnimating ? '0' : '0'}%
            </span>
          </div>
        </div>
        <div className="w-full bg-blue-900/70 rounded-full h-4 mb-2 overflow-hidden">
          <div 
            className={`h-4 rounded-full flex items-center justify-end px-2 ${isAnimating ? `progress-bar ${getColorClass()}` : ''}`}
            style={{ 
              width: 0,
              '--target-width': `${score}%`
            } as React.CSSProperties}
          >
            {score >= 25 && (
              <span className="text-xs font-bold text-white whitespace-nowrap absolute right-2 opacity-0 match-text">
                {score}% Match
              </span>
            )}
          </div>
        </div>
        <div className="flex justify-between text-xs text-blue-400">
          <span>Not Compatible</span>
          <span>Partially Compatible</span>
          <span>Highly Compatible</span>
        </div>
      </div>
    </div>
  );
});
CompatibilityProgress.displayName = 'CompatibilityProgress';

// Memoized AI Note Component
const AINote = memo(({ note, isMobile }: { note: string; isMobile: boolean }) => {
  const [ref, isInView] = useInView({ threshold: 0.1 });
  
  return (
    <div ref={ref} className="mb-8">
      <h3 className="text-xl font-semibold text-white mb-4">AI Analysis Note</h3>
      <div className="p-5 bg-gradient-to-br from-red-900/30 to-red-800/20 backdrop-blur-sm rounded-xl border border-red-800/50 flex flex-col md:flex-row items-center md:items-start text-center md:text-left gap-6">
        <div className="mb-4 md:mb-0 flex-shrink-0 relative robot-float">
          <div className="absolute inset-0 bg-red-500/30 rounded-full filter blur-xl scale-90 robot-glow"></div>
          <Image 
            src="/result.png" 
            alt="AI Analysis Robot" 
            width={120}
            height={120}
            className="relative z-10 w-28 h-28 object-contain drop-shadow-[0_0_15px_rgba(220,38,38,0.5)]"
            priority
          />
        </div>
        <div className="flex-1">
          <h4 className="text-lg font-medium text-red-300 mb-2">Feedback from AI</h4>
          <p className="text-white/90">{note}</p>
        </div>
      </div>
    </div>
  );
});
AINote.displayName = 'AINote';

// Memoized User Information Component
const UserInformation = memo(({ userData }: { userData: Pick<CVData, 'name' | 'surname' | 'email' | 'phoneNumber' | 'skills'> }) => {
  const [ref, isInView] = useInView({ threshold: 0.1 });
  
  return (
    <div ref={ref} className={`bg-gradient-to-br from-blue-900/40 to-indigo-900/40 backdrop-blur-sm p-6 rounded-2xl border border-blue-500/30 shadow-md md:shadow-[0_0_40px_rgba(59,130,246,0.2)] ${isInView ? 'fade-in-element' : 'opacity-0'}`}>
      <h2 className="text-2xl font-bold text-white mb-6">User Information</h2>
      
      <div className="space-y-4">
        <div className="grid grid-cols-3 gap-2">
          <div className="col-span-1 text-blue-300">Name:</div>
          <div className="col-span-2 text-white font-medium">{userData.name}</div>
        </div>
        
        <div className="grid grid-cols-3 gap-2">
          <div className="col-span-1 text-blue-300">Surname:</div>
          <div className="col-span-2 text-white font-medium">{userData.surname}</div>
        </div>
        
        <div className="grid grid-cols-3 gap-2">
          <div className="col-span-1 text-blue-300">Email:</div>
          <div className="col-span-2 text-white font-medium">{userData.email}</div>
        </div>
        
        <div className="grid grid-cols-3 gap-2">
          <div className="col-span-1 text-blue-300">Phone Number:</div>
          <div className="col-span-2 text-white font-medium">{userData.phoneNumber}</div>
        </div>
        
        <div className="pt-4 border-t border-blue-800/50">
          <div className="text-blue-300 mb-3">Skills:</div>
          <ul className="list-disc pl-6 space-y-2">
            {userData.skills.map((skill, index) => (
              <li key={index} className="text-white">{skill}</li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
});
UserInformation.displayName = 'UserInformation';

// Memoized Technical Skills Component
const TechnicalSkills = memo(({ skills }: { skills: Skill[] }) => {
  const [ref, isInView] = useInView({ threshold: 0.1 });
  
  return (
    <div ref={ref} className={`bg-gradient-to-br from-blue-900/40 to-indigo-900/40 backdrop-blur-sm p-6 rounded-2xl border border-blue-500/30 shadow-md md:shadow-[0_0_40px_rgba(59,130,246,0.2)] ${isInView ? 'fade-in-element' : 'opacity-0'}`}>
      <h2 className="text-2xl font-bold text-white mb-6">Technical Skills</h2>
      
      <div className="space-y-5">
        {skills.map((skill, index) => (
          <SkillBar key={index} skill={skill.name} percentage={skill.percentage} />
        ))}
      </div>
    </div>
  );
});
TechnicalSkills.displayName = 'TechnicalSkills';

// Action Buttons Component
const ActionButtons = memo(() => {
  const router = useRouter();
  
  const handleNavigation = useCallback((path: string) => (e: React.MouseEvent) => {
    e.preventDefault();
    router.push(path);
  }, [router]);
  
  return (
    <div className="flex flex-col sm:flex-row gap-4 justify-center mt-8">
      <button 
        onClick={handleNavigation('/profile')}
        className="px-6 py-3 bg-gradient-to-r from-blue-500 to-blue-700 text-white font-medium rounded-lg hover:opacity-90 transition-all duration-300 text-center"
      >
        Back to Dashboard
      </button>
      <button 
        onClick={handleNavigation('/upload')}
        className="px-6 py-3 bg-gradient-to-r from-indigo-500 to-indigo-700 text-white font-medium rounded-lg hover:opacity-90 transition-all duration-300 text-center"
      >
        Upload New CV
      </button>
    </div>
  );
});
ActionButtons.displayName = 'ActionButtons';

export default function Result() {
  const isMobile = useMobileDetect();
  const router = useRouter();
  
  // State to store the evaluation data
  const [cvData, setCvData] = useState<CVData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Fetch data from sessionStorage on component mount
  useEffect(() => {
    try {
      const storedData = sessionStorage.getItem('evaluationResult');
      
      if (!storedData) {
        setError('No evaluation data found. Please upload a CV first.');
        setIsLoading(false);
        return;
      }
      
      const evaluationResponse: EvaluationResponse = JSON.parse(storedData);
      const evaluationResult: EvaluationResult = JSON.parse(evaluationResponse.evaluationResult);
      
      // Parse skills from string to array
      const skillsArray = evaluationResult.userInformation.skills
        .split(',')
        .map(skill => skill.trim())
        .filter(skill => skill.length > 0);
      
      // Map API data to component data structure
      const formattedData: CVData = {
        name: evaluationResult.userInformation.name,
        surname: evaluationResult.userInformation.surname,
        email: evaluationResult.userInformation.email,
        phoneNumber: evaluationResult.userInformation.phone,
        skills: skillsArray,
        technicalSkills: evaluationResult.skillRatings 
          ? evaluationResult.skillRatings.map(skill => ({
              name: skill.language,
              percentage: skill.percentage
            }))
          : [],
        compatibilityScore: evaluationResult.compatibilityStatus,
        aiNote: evaluationResult.explanation
      };
      
      setCvData(formattedData);
      setIsLoading(false);
    } catch (err) {
      console.error('Error parsing evaluation data:', err);
      setError('Error loading results. Please try again.');
      setIsLoading(false);
    }
  }, []);
  
  // Counter animation setup
  useEffect(() => {
    if (!cvData) return;
    
    // Initialize counter animation when elements are loaded
    const counters = document.querySelectorAll('.progress-counter');
    const mobileCounters = document.querySelectorAll('.mobile-counter-animate');
    
    // Handle desktop counters
    if (counters.length) {
      counters.forEach(counter => {
        const target = parseInt(counter.getAttribute('data-target') || '0', 10);
        
        // Simple counter with minimal DOM operations
        if (counter.classList.contains('animate-counter')) {
          return; // Skip if already animated
        }
        
        counter.classList.add('animate-counter');
        let startTimestamp: number | null = null;
        const duration = 1500; // ms
        
        function step(timestamp: number) {
          if (!startTimestamp) startTimestamp = timestamp;
          
          const elapsed = timestamp - startTimestamp;
          const progress = Math.min(elapsed / duration, 1);
          
          if (counter.textContent !== null) {
            counter.textContent = `${Math.floor(progress * target)}%`;
          }
          
          if (progress < 1) {
            window.requestAnimationFrame(step);
          }
        }
        
        const observer = new IntersectionObserver(
          (entries) => {
            if (entries[0].isIntersecting) {
              window.requestAnimationFrame(step);
              observer.disconnect();
            }
          },
          { threshold: 0.1 }
        );
        
        observer.observe(counter as Element);
      });
    }
    
    // Handle mobile counters - simplified approach
    if (mobileCounters.length) {
      mobileCounters.forEach(counter => {
        const target = counter.getAttribute('data-value');
        if (target) {
          setTimeout(() => {
            counter.textContent = `${target}%`;
          }, 800); // Delay to match transition
        }
      });
    }
  }, [cvData]);
  
  // Redirect to upload page if no data and not loading
  useEffect(() => {
    if (!isLoading && error) {
      const timeoutId = setTimeout(() => {
        router.push('/upload');
      }, 3000);
      
      return () => clearTimeout(timeoutId);
    }
  }, [isLoading, error, router]);

  // Show loading state
  if (isLoading) {
    return (
      <>
        <Header />
        <main className="min-h-screen w-full bg-black flex flex-col items-center justify-center px-4 py-12 pt-28 relative overflow-hidden">
          <GradientSpots />
          <div className="w-full max-w-6xl mx-auto z-10 mb-12 flex flex-col items-center justify-center">
            <div className="animate-spin w-16 h-16 border-4 border-blue-500 border-t-transparent rounded-full mb-8"></div>
            <h2 className="text-2xl font-bold text-white">Loading your results...</h2>
          </div>
        </main>
      </>
    );
  }
  
  // Show error state
  if (error || !cvData) {
    return (
      <>
        <Header />
        <main className="min-h-screen w-full bg-black flex flex-col items-center justify-center px-4 py-12 pt-28 relative overflow-hidden">
          <GradientSpots />
          <div className="w-full max-w-6xl mx-auto z-10 mb-12 flex flex-col items-center justify-center">
            <div className="bg-red-500/20 border border-red-500/50 text-red-200 p-6 rounded-xl mb-8">
              <h2 className="text-2xl font-bold mb-4">Error Loading Results</h2>
              <p>{error || 'No evaluation data found. Please upload a CV first.'}</p>
              <p className="mt-4 text-sm opacity-70">Redirecting to upload page...</p>
            </div>
            <button
              onClick={() => router.push('/upload')}
              className="px-6 py-3 bg-gradient-to-r from-blue-500 to-blue-700 text-white font-medium rounded-lg hover:opacity-90 transition-all duration-300"
            >
              Return to Upload
            </button>
          </div>
        </main>
      </>
    );
  }

  return (
    <>
      <Header />
      <main className="min-h-screen w-full bg-black flex flex-col items-center justify-center px-4 py-12 pt-28 relative overflow-hidden">
        {/* Background Elements - Conditionally rendered based on device */}
        <GradientSpots />
        {!isMobile && (
          <div className="absolute inset-0 z-0 opacity-40">
            <Particles />
          </div>
        )}
        <BackgroundGradients isMobile={isMobile} />
        
        {/* Main Content */}
        <div className="w-full max-w-6xl mx-auto z-10 mb-12">
          <div className="text-center mb-10">
            <h1 className="text-3xl md:text-5xl font-bold text-white mb-4">Your CV Analysis Results</h1>
            <p className="text-blue-300 max-w-3xl mx-auto">
              Here's our comprehensive analysis of your CV. We've highlighted your strengths and identified areas for improvement.
            </p>
          </div>
          
          {/* Results Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            <UserInformation userData={cvData} />
            <TechnicalSkills skills={cvData.technicalSkills} />
          </div>
          
          {/* Compatibility Score */}
          <div className="mt-8">
            <CompatibilityProgress score={cvData.compatibilityScore} />
          </div>
          
          {/* AI Note */}
          <div>
            <AINote note={cvData.aiNote} isMobile={isMobile} />
          </div>
          
          {/* Action Buttons */}
          <ActionButtons />
        </div>
      </main>
      
      {/* Animation Styles with optimized CSS-based animations */}
      <style jsx global>{`
        /* Optimized animations */
        .fade-in-element {
          animation: fadeIn 0.5s ease-out forwards;
          will-change: opacity, transform;
        }
        
        @keyframes fadeIn {
          from {
            opacity: 0;
            transform: translateY(10px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }
        
        /* Progress bar animations */
        .progress-bar {
          animation: progressWidth 1.5s ease-out forwards;
          will-change: width;
        }
        
        .match-text {
          animation: fadeIn 0.3s ease-out 1.2s forwards;
        }
        
        .progress-red {
          background: linear-gradient(to right, #ef4444, #b91c1c);
        }
        
        .progress-yellow {
          background: linear-gradient(to right, #f59e0b, #d97706);
        }
        
        .progress-green {
          background: linear-gradient(to right, #10b981, #059669);
        }
        
        @keyframes progressWidth {
          from { width: 0; }
          to { width: var(--target-width, 0); }
        }
        
        /* Robot animation */
        .animate-robot {
          animation: robotFloat 4s ease-in-out infinite;
        }
        
        @keyframes robotFloat {
          0% {
            transform: translateY(0);
          }
          50% {
            transform: translateY(-8px);
          }
          100% {
            transform: translateY(0);
          }
        }
        
        .robot-float {
          animation: robotFloat 4s ease-in-out infinite;
          transform-origin: center bottom;
        }
        
        .robot-glow {
          animation: glowPulse 3s ease-in-out infinite;
        }
        
        @keyframes glowPulse {
          0%, 100% {
            opacity: 0.3;
            transform: scale(0.9);
          }
          50% {
            opacity: 0.5;
            transform: scale(1.05);
          }
        }
        
        /* Skill progress animations */
        .skill-progress {
          animation: progressWidth 1s ease-out forwards;
          animation-delay: 0.2s;
        }
        
        /* Mobile-specific animations */
        .mobile-bar-fill {
          transition: width 0.8s ease-out;
        }
        
        .mobile-text-fade {
          opacity: 0;
          animation: simpleFade 0.5s ease-out 0.7s forwards;
        }
        
        @keyframes simpleFade {
          from { opacity: 0; }
          to { opacity: 1; }
        }
      `}</style>
    </>
  );
} 