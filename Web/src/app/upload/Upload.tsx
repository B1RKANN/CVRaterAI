"use client";

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Header from '../components/Header';
import GradientSpots from '../components/GradientSpots';
import Particles from '../components/Particles';
import Link from 'next/link';
import Image from 'next/image';
import { useAuth } from '../context/AuthContext';
import { jwtDecode } from 'jwt-decode';

// Defining types for props and state
type FileInputProps = {
  file: File | null;
  isDragging: boolean;
  onDragOver: (e: React.DragEvent<HTMLDivElement>) => void;
  onDragLeave: (e: React.DragEvent<HTMLDivElement>) => void;
  onDrop: (e: React.DragEvent<HTMLDivElement>) => void;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
};

type InputFieldProps = {
  id: string;
  label: string;
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  placeholder: string;
  type?: string;
  isOptional?: boolean;
  icon: React.ReactNode;
  hint?: string;
};

type AnalyzeButtonProps = {
  onClick: () => void;
  isDisabled: boolean;
  isLoading: boolean;
};

type LoadingScreenProps = {
  isVisible: boolean;
};

// JWT Token type
interface DecodedToken {
  exp: number;
  sub: string;
  userId: number;
  role: string;
  iat: number;
}

// File Input Component
const FileInput = ({ file, isDragging, onDragOver, onDragLeave, onDrop, onChange }: FileInputProps) => (
  <div 
    className={`border-2 border-dashed rounded-xl p-8 mb-8 text-center cursor-pointer transition-all
      ${isDragging ? 'border-blue-500 bg-blue-900/30' : 'border-blue-700/50 hover:border-blue-500/70'}
      ${file ? 'bg-blue-900/20 border-blue-500/80' : ''}`}
    onDragOver={onDragOver}
    onDragLeave={onDragLeave}
    onDrop={onDrop}
    onClick={() => document.getElementById('file-upload')?.click()}
  >
    <input 
      type="file" 
      id="file-upload" 
      className="hidden" 
      onChange={onChange}
      accept=".pdf,.doc,.docx"
    />
    
    <div className="flex flex-col items-center justify-center py-4">
      <svg className="w-16 h-16 text-blue-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"></path>
      </svg>
      {file ? (
        <div>
          <p className="text-lg text-blue-300 font-medium">{file.name}</p>
          <p className="text-base text-blue-200/70 mt-2">Click or drag to change file</p>
        </div>
      ) : (
        <div>
          <p className="text-lg text-blue-100 font-medium">Drag & Drop Your CV Here</p>
          <p className="text-base text-blue-200/70 mt-2">or click to browse files</p>
          <p className="text-sm text-blue-300/50 mt-4 border border-blue-700/30 rounded-full px-4 py-1 inline-block">
            Supports PDF, DOC, DOCX
          </p>
        </div>
      )}
    </div>
  </div>
);

// Input Field Component
const InputField = ({ 
  id, 
  label, 
  value, 
  onChange, 
  placeholder, 
  type = "text", 
  isOptional = false, 
  icon,
  hint
}: InputFieldProps) => (
  <div className="mb-6">
    <label htmlFor={id} className="block text-base font-medium text-blue-200 mb-2">
      {label} {isOptional && <span className="text-blue-400/70 text-sm">(Optional)</span>}
    </label>
    <div className="relative">
      <div className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-blue-400">
        {icon}
      </div>
      <input 
        type={type}
        id={id} 
        className="w-full pl-10 pr-4 py-3 bg-blue-950/50 border border-blue-800/50 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-white placeholder-blue-400/70"
        placeholder={placeholder}
        value={value}
        onChange={onChange}
      />
    </div>
    {hint && (
      <p className="text-xs text-blue-300/70 mt-2">{hint}</p>
    )}
  </div>
);

// Analyze Button Component
const AnalyzeButton = ({ onClick, isDisabled, isLoading }: AnalyzeButtonProps) => (
  <button 
    className="w-full bg-gradient-to-r from-blue-500 to-blue-700 hover:from-blue-600 hover:to-blue-800 text-white font-bold py-4 rounded-lg transition-all shadow-lg hover:shadow-blue-500/30 disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center"
    onClick={onClick}
    disabled={isDisabled || isLoading}
  >
    {isLoading ? (
      <>
        <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        Processing...
      </>
    ) : isDisabled ? (
      "Please Upload a CV First"
    ) : (
      <>
        <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"></path>
        </svg>
        Analyze CV
      </>
    )}
  </button>
);

// Loading Screen Component
const LoadingScreen = ({ isVisible }: LoadingScreenProps) => {
  if (!isVisible) return null;
  
  return (
    <div className="fixed inset-0 flex items-center justify-center z-50 bg-black/80 backdrop-blur-md transition-all duration-300 overflow-hidden">
      {/* Background Particles */}
      <div className="absolute inset-0 z-0 overflow-hidden">
        <Particles />
      </div>
      
      <div className="bg-gradient-to-br from-blue-900/40 to-indigo-900/40 backdrop-blur-lg p-10 rounded-3xl border border-blue-500/30 shadow-[0_0_40px_rgba(59,130,246,0.3)] max-w-2xl w-full mx-4 relative z-10">
        <div className="flex flex-col items-center">
          {/* Robot Image with Glow Effect */}
          <div className="relative w-64 h-64 mx-auto mb-8 robot-analysis-container">
            <div className="absolute inset-0 bg-blue-500/20 rounded-full filter blur-xl scale-90 robot-glow-effect"></div>
            <Image
              src="/analyze.webp"
              alt="AI Analyzing"
              width={240}
              height={240}
              priority
              className="w-full h-auto drop-shadow-[0_0_20px_rgba(59,130,246,0.6)]"
            />
          </div>
          
          {/* Text Content */}
          <h2 className="text-3xl font-bold text-white mb-2 text-center">Analyzing Your CV</h2>
          <p className="text-blue-200 mb-8 text-center max-w-md mx-auto">
            Our advanced AI is processing your document to extract key insights and provide personalized recommendations.
          </p>
          
          {/* Progress Bar */}
          <div className="w-full max-w-md mx-auto mb-6">
            <div className="w-full h-2 bg-blue-900/50 rounded-full overflow-hidden">
              <div className="progress-bar h-full rounded-full"></div>
            </div>
          </div>
          
          {/* Processing Steps */}
          <div className="grid grid-cols-3 gap-4 w-full max-w-md mx-auto">
            <div className="text-center">
              <div className="w-10 h-10 mx-auto flex items-center justify-center rounded-full border border-blue-500/50 bg-blue-800/30 mb-2">
                <svg className="w-5 h-5 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                </svg>
              </div>
              <span className="text-xs text-blue-300">Scanning</span>
            </div>
            <div className="text-center">
              <div className="w-10 h-10 mx-auto flex items-center justify-center rounded-full border border-blue-500/50 bg-blue-800/30 mb-2">
                <svg className="w-5 h-5 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"></path>
                </svg>
              </div>
              <span className="text-xs text-blue-300">Analyzing</span>
            </div>
            <div className="text-center">
              <div className="w-10 h-10 mx-auto flex items-center justify-center rounded-full border border-blue-500/50 bg-blue-800/30 mb-2">
                <svg className="w-5 h-5 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"></path>
                </svg>
              </div>
              <span className="text-xs text-blue-300">Preparing Results</span>
            </div>
          </div>
        </div>
      </div>
      
      {/* Animation Styles */}
      <style jsx global>{`
        @keyframes robotAnalysis {
          0% {
            transform: translateY(0) rotate(0deg);
          }
          25% {
            transform: translateY(-10px) rotate(-3deg);
          }
          50% {
            transform: translateY(0) rotate(0deg);
          }
          75% {
            transform: translateY(-10px) rotate(3deg);
          }
          100% {
            transform: translateY(0) rotate(0deg);
          }
        }
        
        @keyframes glowPulse {
          0% {
            opacity: 0.5;
            transform: scale(0.9);
          }
          50% {
            opacity: 0.8;
            transform: scale(1);
          }
          100% {
            opacity: 0.5;
            transform: scale(0.9);
          }
        }
        
        @keyframes progressBar {
          0% {
            width: 10%;
            background: linear-gradient(to right, #3b82f6, #6366f1);
          }
          25% {
            width: 30%;
            background: linear-gradient(to right, #3b82f6, #6366f1);
          }
          50% {
            width: 60%;
            background: linear-gradient(to right, #3b82f6, #6366f1);
          }
          75% {
            width: 85%;
            background: linear-gradient(to right, #3b82f6, #6366f1);
          }
          90% {
            width: 95%;
            background: linear-gradient(to right, #3b82f6, #6366f1);
          }
          100% {
            width: 100%;
            background: linear-gradient(to right, #3b82f6, #6366f1);
          }
        }
        
        .robot-analysis-container {
          animation: robotAnalysis 5s ease-in-out infinite;
          transform-origin: center;
        }
        
        .robot-glow-effect {
          animation: glowPulse 3s ease-in-out infinite;
        }
        
        .progress-bar {
          animation: progressBar 3.5s ease-in-out forwards;
        }
        
        /* Customize particle styles for loading screen */
        .particle-container {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          pointer-events: none;
        }
        
        .particle {
          position: absolute;
          background: linear-gradient(to bottom right, rgba(59, 130, 246, 0.4), rgba(99, 102, 241, 0.3));
          border-radius: 50%;
          pointer-events: none;
        }
      `}</style>
    </div>
  );
};

// Icons
const SearchIcon = () => (
  <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
  </svg>
);

const GithubIcon = () => (
  <svg fill="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
    <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
  </svg>
);

// Main Upload Component
export default function Upload() {
  // State hooks
  const [file, setFile] = useState<File | null>(null);
  const [searchedFeatures, setSearchedFeatures] = useState('');
  const [githubLink, setGithubLink] = useState('');
  const [isDragging, setIsDragging] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  // Get auth context and router
  const { getToken } = useAuth();
  const router = useRouter();

  // Event handlers
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setFile(e.target.files[0]);
    }
  };

  const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      setFile(e.dataTransfer.files[0]);
    }
  };

  const handleAnalyze = async () => {
    if (!file) {
      setError("Please upload a CV file first");
      return;
    }
    
    try {
      // Set loading to true
      setIsLoading(true);
      setError(null);
      
      // Get token and extract user ID
      const token = await getToken();
      if (!token) {
        setError("Authentication required. Please sign in.");
        setIsLoading(false);
        router.push('/signin');
        return;
      }
      
      // Decode token to get userId
      const decodedToken = jwtDecode<DecodedToken>(token);
      const userId = decodedToken.userId;
      
      // Create FormData object
      const formData = new FormData();
      formData.append('file', file);
      formData.append('githubUrl', githubLink);
      formData.append('jobRequirements', searchedFeatures);
      
      // Send API request
      const response = await fetch(`/api/v1/cv-evaluation/evaluate/${userId}`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        },
        body: formData,
      });
      
      if (!response.ok) {
        throw new Error(`Error: ${response.status} ${response.statusText}`);
      }
      
      // Get response data
      const responseData = await response.json();
      
      // Store the response data in sessionStorage to access in Result page
      sessionStorage.setItem('evaluationResult', JSON.stringify(responseData));
      
      // Navigate to Result page
      router.push('/result');
      
    } catch (error) {
      console.error('Error analyzing CV:', error);
      setError(error instanceof Error ? error.message : "An unexpected error occurred");
    } finally {
      setIsLoading(false);
    }
  };

  // Feature input change handler
  const handleFeatureChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchedFeatures(e.target.value);
  };

  // Github link change handler
  const handleGithubChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setGithubLink(e.target.value);
  };

  return (
    <>
      <Header />
      <main className="min-h-screen w-full bg-black flex flex-col items-center justify-center px-4 py-12 pt-28 relative overflow-hidden">
        {/* Background Gradients */}
      <GradientSpots />
      
        {/* Main content wrapper */}
        <div className="w-full max-w-5xl mx-auto z-10">
          <div className="bg-gradient-to-br from-blue-900/30 to-indigo-900/30 backdrop-blur-sm p-8 rounded-2xl border border-blue-800/50 shadow-xl">
            <h2 className="text-2xl font-bold text-white mb-8 text-center">Upload Your CV</h2>
            
            {/* Error message */}
            {error && (
              <div className="bg-red-500/20 border border-red-500/50 text-red-200 p-4 mb-6 rounded-lg">
                {error}
              </div>
            )}
        
        {/* File Upload Area */}
            <FileInput 
              file={file}
              isDragging={isDragging}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
            onChange={handleFileChange}
            />
        
        {/* Searched Features Input */}
            <InputField 
            id="features" 
              label="Searched Features"
            value={searchedFeatures}
              onChange={handleFeatureChange}
              placeholder="Enter skills or qualifications you're looking for"
              icon={<SearchIcon />}
              hint="Example: React, Project Management, Leadership"
            />
            
            {/* GitHub Link Input */}
            <InputField 
            id="github" 
              label="GitHub Link"
              value={githubLink}
              onChange={handleGithubChange}
            placeholder="https://github.com/yourusername"
              type="url"
              isOptional={true}
              icon={<GithubIcon />}
            />
            
            {/* Analyze Button */}
            <AnalyzeButton 
              onClick={handleAnalyze}
              isDisabled={!file}
              isLoading={isLoading}
            />
            
            {/* Extra info */}
            <div className="mt-6 text-center">
              <p className="text-sm text-blue-300/70">
                Your data is kept private and secure.
                <Link href="/privacy" className="text-blue-400 hover:text-blue-300 ml-1">
                  Learn more
                </Link>
              </p>
            </div>
          </div>
        </div>
        
        {/* Loading Screen */}
        <LoadingScreen isVisible={isLoading} />
      </main>
    </>
  );
} 