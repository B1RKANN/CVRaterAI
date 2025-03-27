'use client';

import { useState } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { FaEnvelope, FaLock, FaEye, FaEyeSlash } from 'react-icons/fa';
import Header from '../components/Header';
import GradientSpots from '../components/GradientSpots';

export default function SignIn() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');
    
    try {
      // API isteği gönder
      const response = await fetch('http://69.62.120.202:8080/auth/v2/authenticate-with-cookie', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email,
          password
        }),
        credentials: 'include', // Cookie'lerin gönderilmesi ve alınması için
      });
      
      if (!response.ok) {
        // HTTP hata durumları
        if (response.status === 401) {
          throw new Error('Email veya şifre hatalı');
        } else {
          throw new Error(`Giriş yapılamadı: ${response.status}`);
        }
      }
      
      // Response'u JSON olarak parse et
      const data = await response.json();
      
      // Token ve refreshToken'ı kontrol et
      if (!data.token || !data.refreshToken) {
        throw new Error('Geçersiz token bilgileri');
      }
      
      // Token'ı hem cookie hem de localStorage'a kaydet (yedek olarak)
      // Bazı tarayıcılar veya durumlar için cookie çalışmazsa localStorage yedek olacak
      localStorage.setItem('token', data.token);
      localStorage.setItem('refreshToken', data.refreshToken);
      
      // Eğer cookie'ler sunucu tarafından otomatik ayarlanmadıysa (CORS sorunlarından dolayı)
      // Biz manuel olarak cookie ayarlayalım (tarayıcı içinde kullanım için)
      document.cookie = `token=${data.token}; path=/; max-age=86400; SameSite=Lax`;
      document.cookie = `refreshToken=${data.refreshToken}; path=/; max-age=604800; SameSite=Lax`;
      
      console.log('Token ve refreshToken kaydedildi:', data.token, data.refreshToken);
      
      // Giriş başarılı, Profile sayfasına yönlendir
      router.push('/profile');
      
    } catch (err: any) {
      console.error('Giriş hatası:', err);
      setError(err.message || 'Giriş sırasında bir hata oluştu');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      <Header />
      <main className="min-h-screen w-full bg-black flex flex-col items-center justify-center px-4 py-12 pt-28 relative overflow-hidden">
        {/* Gradient Spots Component */}
        <GradientSpots />
        
        {/* Ana içerik */}
        <div className="w-full max-w-6xl mx-auto flex flex-col lg:flex-row items-center justify-between gap-8 z-10">
          {/* Sol taraf: Bilgi ve robot resmi */}
          <div className="w-full lg:w-1/2 flex flex-col items-center lg:items-start text-center lg:text-left mb-8 lg:mb-0">
            <Link href="/" className="inline-block mb-8">
              <span className="text-3xl sm:text-4xl font-semibold text-white">
                CVRaterAI
              </span>
            </Link>
            
            <h1 className="text-3xl sm:text-4xl md:text-5xl font-bold text-white mb-4">
              Welcome Back
            </h1>
            
            <p className="text-base sm:text-lg text-blue-200 max-w-md mb-8">
              Sign in to access your account and continue improving your CV with AI-powered tools.
            </p>
            
            <div className="relative w-64 sm:w-80 h-64 sm:h-80 robot-container">
              <Image 
                src="/security.png" 
                alt="Security Robot" 
                width={400} 
                height={400}
                priority
                className="w-full h-auto drop-shadow-[0_0_25px_rgba(59,130,246,0.3)]"
              />
            </div>
          </div>
          
          {/* Sağ taraf: Giriş formu */}
          <div className="w-full lg:w-1/2 max-w-md">
            <div className="bg-gradient-to-br from-blue-900/30 to-indigo-900/30 backdrop-blur-sm p-6 sm:p-8 rounded-2xl border border-blue-800/50">
              <h2 className="text-2xl font-bold text-white mb-6">Sign In</h2>
              
              {error && (
                <div className="mb-4 p-3 bg-red-500/20 border border-red-500/50 rounded-lg">
                  <p className="text-red-200 text-sm">{error}</p>
                </div>
              )}
              
              <form onSubmit={handleSubmit} className="space-y-5">
                {/* Email input */}
                <div className="space-y-2">
                  <label htmlFor="email" className="block text-sm font-medium text-blue-200">
                    Email Address
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                      <FaEnvelope className="h-5 w-5 text-blue-400" />
                    </div>
                    <input
                      id="email"
                      name="email"
                      type="email"
                      autoComplete="email"
                      required
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      className="bg-blue-950/50 block w-full pl-10 pr-3 py-3 border border-blue-800/50 rounded-lg text-white placeholder-blue-400/70 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                      placeholder="your.email@example.com"
                    />
                  </div>
                </div>
                
                {/* Password input */}
                <div className="space-y-2">
                  <label htmlFor="password" className="block text-sm font-medium text-blue-200">
                    Password
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                      <FaLock className="h-5 w-5 text-blue-400" />
                    </div>
                    <input
                      id="password"
                      name="password"
                      type={showPassword ? "text" : "password"}
                      autoComplete="current-password"
                      required
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      className="bg-blue-950/50 block w-full pl-10 pr-10 py-3 border border-blue-800/50 rounded-lg text-white placeholder-blue-400/70 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                      placeholder="••••••••"
                    />
                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center">
                      <button
                        type="button"
                        onClick={() => setShowPassword(!showPassword)}
                        className="text-blue-400 hover:text-blue-300 focus:outline-none"
                      >
                        {showPassword ? <FaEyeSlash className="h-5 w-5" /> : <FaEye className="h-5 w-5" />}
                      </button>
                    </div>
                  </div>
                </div>
                
                {/* Forgot password link */}
                <div className="flex justify-end">
                  <Link 
                    href="/forgot-password"
                    className="text-sm text-blue-400 hover:text-blue-300 transition-colors"
                  >
                    Forgot your password?
                  </Link>
                </div>
                
                {/* Sign in button */}
                <button
                  type="submit"
                  disabled={isLoading}
                  className="w-full bg-gradient-to-r from-blue-500 to-blue-700 hover:from-blue-600 hover:to-blue-800 text-white py-3 px-4 rounded-lg font-medium transition-all flex items-center justify-center"
                >
                  {isLoading ? (
                    <svg className="animate-spin -ml-1 mr-2 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                  ) : null}
                  {isLoading ? 'Signing In...' : 'Sign In'}
                </button>
                
                {/* Alternative sign in options */}
                <div className="relative flex items-center justify-center my-6">
                  <div className="absolute inset-0 flex items-center">
                    <div className="w-full border-t border-blue-800/60"></div>
                  </div>
                  <div className="relative bg-transparent px-4">
                    <span className="text-sm text-blue-300">or continue with</span>
                  </div>
                </div>
                
                <div className="grid grid-cols-3 gap-3">
                  <button
                    type="button"
                    className="bg-blue-950/70 hover:bg-blue-900/50 border border-blue-800/50 py-2 px-4 rounded-lg flex items-center justify-center transition-all"
                  >
                    <svg className="h-5 w-5 text-white" aria-hidden="true" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M12.0003 2C6.47731 2 2.00031 6.477 2.00031 12C2.00031 16.991 5.65731 21.128 10.4373 21.879V14.89H7.89831V12H10.4373V9.797C10.4373 7.291 11.9323 5.907 14.2153 5.907C15.3103 5.907 16.4543 6.102 16.4543 6.102V8.562H15.1923C13.9503 8.562 13.5633 9.333 13.5633 10.124V12H16.3363L15.8933 14.89H13.5633V21.879C18.3433 21.129 22.0003 16.99 22.0003 12C22.0003 6.477 17.5233 2 12.0003 2Z"></path>
                    </svg>
                  </button>
                  
                  <button
                    type="button"
                    className="bg-blue-950/70 hover:bg-blue-900/50 border border-blue-800/50 py-2 px-4 rounded-lg flex items-center justify-center transition-all"
                  >
                    <svg className="h-5 w-5 text-white" aria-hidden="true" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M12.0003 2C6.47731 2 2.00031 6.477 2.00031 12C2.00031 17.523 6.47731 22 12.0003 22C17.5233 22 22.0003 17.523 22.0003 12C22.0003 6.477 17.5233 2 12.0003 2ZM18.7033 8.68C18.7203 8.85 18.7203 9.02 18.7203 9.19C18.7203 13.552 15.4033 18.593 9.39231 18.593C7.63931 18.593 6.01731 18.079 4.64731 17.192C4.90431 17.222 5.15231 17.237 5.41731 17.237C6.88431 17.237 8.22931 16.741 9.29431 15.897C7.91731 15.87 6.75931 14.97 6.37731 13.739C6.58131 13.769 6.78531 13.784 6.99731 13.784C7.29431 13.784 7.59131 13.754 7.86431 13.694C6.43931 13.404 5.35931 12.131 5.35931 10.588V10.548C5.76131 10.767 6.21731 10.907 6.70331 10.922C5.85531 10.342 5.30031 9.37 5.30031 8.269C5.30031 7.674 5.45931 7.125 5.74131 6.65C7.27331 8.528 9.55031 9.73 12.0903 9.865C12.0403 9.646 12.0153 9.417 12.0153 9.187C12.0153 7.46 13.4143 6.062 15.1433 6.062C16.0393 6.062 16.8473 6.447 17.4173 7.064C18.1183 6.929 18.7933 6.677 19.3923 6.32C19.1733 7.032 18.7043 7.673 18.0883 8.095C18.6863 8.02 19.2693 7.848 19.8093 7.6C19.3923 8.251 18.8673 8.826 18.2673 9.295C18.7033 8.68 18.7033 8.68 18.7033 8.68Z"></path>
                    </svg>
                  </button>
                  
                  <button
                    type="button"
                    className="bg-blue-950/70 hover:bg-blue-900/50 border border-blue-800/50 py-2 px-4 rounded-lg flex items-center justify-center transition-all"
                  >
                    <svg className="h-5 w-5 text-white" aria-hidden="true" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M11.9999 2C6.47691 2 1.99991 6.477 1.99991 12C1.99991 16.42 4.86791 20.17 8.83791 21.5C9.33791 21.58 9.52191 21.27 9.52191 21C9.52191 20.77 9.51391 20.14 9.50991 19.31C6.72691 19.91 6.13991 17.97 6.13991 17.97C5.68491 16.81 5.02791 16.5 5.02791 16.5C4.12191 15.88 5.09391 15.9 5.09391 15.9C6.09991 15.97 6.62791 16.93 6.62791 16.93C7.52191 18.45 8.97091 18.01 9.53991 17.76C9.61991 17.11 9.85991 16.67 10.1289 16.42C7.82791 16.17 5.41791 15.31 5.41791 11.5C5.41791 10.39 5.81191 9.49 6.64791 8.79C6.55591 8.54 6.20891 7.5 6.74691 6.15C6.74691 6.15 7.58591 5.88 9.49991 7.17C10.2959 6.95 11.1499 6.84 11.9999 6.84C12.8499 6.84 13.7039 6.95 14.4999 7.17C16.4139 5.88 17.2529 6.15 17.2529 6.15C17.7909 7.5 17.4439 8.54 17.3519 8.79C18.1899 9.49 18.5799 10.39 18.5799 11.5C18.5799 15.32 16.1659 16.16 13.8579 16.41C14.1959 16.72 14.4919 17.33 14.4919 18.26C14.4919 19.6 14.4819 20.68 14.4819 21C14.4819 21.27 14.6609 21.59 15.1759 21.5C19.1379 20.16 21.9999 16.42 21.9999 12C21.9999 6.477 17.5229 2 11.9999 2Z"></path>
                    </svg>
                  </button>
                </div>
              </form>
              
              {/* Sign up option */}
              <div className="mt-8 text-center">
                <span className="text-blue-200">Don't have an account? </span>
                <Link 
                  href="/register" 
                  className="text-blue-400 hover:text-blue-300 font-medium transition-colors"
                >
                  Create account
                </Link>
              </div>
            </div>
          </div>
        </div>
        
        {/* Robot floating animation */}
        <style jsx global>{`
          @keyframes robotFloat {
            0% {
              transform: translateY(0px) rotate(0deg);
            }
            50% {
              transform: translateY(-15px) rotate(2deg);
            }
            100% {
              transform: translateY(0px) rotate(0deg);
            }
          }
          
          .robot-container {
            animation: robotFloat 5s ease-in-out infinite;
            transform-origin: center center;
          }
        `}</style>
      </main>
    </>
  );
} 