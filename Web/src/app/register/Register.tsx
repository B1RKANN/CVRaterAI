'use client';

import { useState } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { FaUser, FaEnvelope, FaLock, FaEye, FaEyeSlash } from 'react-icons/fa';
import Header from '../components/Header';
import GradientSpots from '../components/GradientSpots';

export default function Register() {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));

    // Şifreleri karşılaştırma kontrolü
    if (name === 'confirmPassword' || name === 'password') {
      if (name === 'confirmPassword' && formData.password !== value) {
        setError('Passwords do not match');
      } else if (name === 'password' && formData.confirmPassword && formData.confirmPassword !== value) {
        setError('Passwords do not match');
      } else {
        setError('');
      }
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Şifre kontrolü
    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    
    setIsLoading(true);
    setError('');
    
    try {
      // API'ye kayıt isteği gönder
      const response = await fetch('/auth/v2/register-with-cookie', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          name: `${formData.firstName} ${formData.lastName}`,
          email: formData.email,
          password: formData.password
        }),
        credentials: 'include' // Cookie'lerin saklanmasını sağlar
      });
      
      // Yanıtı JSON olarak işle
      const data = await response.json();
      
      if (!response.ok) {
        // Hata durumunu işle
        throw new Error(data.message || 'Kayıt sırasında bir hata oluştu');
      }
      
      // Başarılı kayıt sonrası işlemler
      console.log('Kayıt başarılı:', data);
      
      // Kullanıcıyı otomatik olarak giriş sayfasına yönlendir
      window.location.href = '/signin';
    } catch (err: any) {
      // Hata mesajını göster
      setError(err.message || 'Bir hata oluştu. Lütfen tekrar deneyin.');
      console.error('Kayıt hatası:', err);
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
        <div className="w-full max-w-6xl mx-auto flex flex-col-reverse lg:flex-row items-center justify-between gap-8 z-10">
          {/* Sol taraf: Form */}
          <div className="w-full lg:w-1/2 max-w-md">
            <div className="bg-gradient-to-br from-blue-900/30 to-indigo-900/30 backdrop-blur-sm p-6 sm:p-8 rounded-2xl border border-blue-800/50">
              <h2 className="text-2xl font-bold text-white mb-6">Create Account</h2>
              
              <form onSubmit={handleSubmit} className="space-y-4">
                {/* İsim ve Soyisim satırı */}
                <div className="grid grid-cols-2 gap-4">
                  {/* First name input */}
                  <div className="space-y-2">
                    <label htmlFor="firstName" className="block text-sm font-medium text-blue-200">
                      First Name
                    </label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <FaUser className="h-4 w-4 text-blue-400" />
                      </div>
                      <input
                        id="firstName"
                        name="firstName"
                        type="text"
                        required
                        value={formData.firstName}
                        onChange={handleChange}
                        className="bg-blue-950/50 block w-full pl-10 pr-3 py-3 border border-blue-800/50 rounded-lg text-white placeholder-blue-400/70 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                        placeholder="John"
                      />
                    </div>
                  </div>

                  {/* Last name input */}
                  <div className="space-y-2">
                    <label htmlFor="lastName" className="block text-sm font-medium text-blue-200">
                      Last Name
                    </label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <FaUser className="h-4 w-4 text-blue-400" />
                      </div>
                      <input
                        id="lastName"
                        name="lastName"
                        type="text"
                        required
                        value={formData.lastName}
                        onChange={handleChange}
                        className="bg-blue-950/50 block w-full pl-10 pr-3 py-3 border border-blue-800/50 rounded-lg text-white placeholder-blue-400/70 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                        placeholder="Doe"
                      />
                    </div>
                  </div>
                </div>
                
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
                      value={formData.email}
                      onChange={handleChange}
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
                      autoComplete="new-password"
                      required
                      value={formData.password}
                      onChange={handleChange}
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
                
                {/* Confirm Password input */}
                <div className="space-y-2">
                  <label htmlFor="confirmPassword" className="block text-sm font-medium text-blue-200">
                    Confirm Password
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                      <FaLock className="h-5 w-5 text-blue-400" />
                    </div>
                    <input
                      id="confirmPassword"
                      name="confirmPassword"
                      type={showConfirmPassword ? "text" : "password"}
                      autoComplete="new-password"
                      required
                      value={formData.confirmPassword}
                      onChange={handleChange}
                      className={`bg-blue-950/50 block w-full pl-10 pr-10 py-3 border ${error ? 'border-red-500' : 'border-blue-800/50'} rounded-lg text-white placeholder-blue-400/70 focus:outline-none focus:ring-2 ${error ? 'focus:ring-red-500 focus:border-red-500' : 'focus:ring-blue-500 focus:border-blue-500'}`}
                      placeholder="••••••••"
                    />
                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center">
                      <button
                        type="button"
                        onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                        className="text-blue-400 hover:text-blue-300 focus:outline-none"
                      >
                        {showConfirmPassword ? <FaEyeSlash className="h-5 w-5" /> : <FaEye className="h-5 w-5" />}
                      </button>
                    </div>
                  </div>
                  {error && <p className="text-red-500 text-sm mt-1">{error}</p>}
                </div>
                
                {/* Terms and conditions */}
                <div className="flex items-start">
                  <div className="flex items-center h-5">
                    <input
                      id="terms"
                      type="checkbox"
                      required
                      className="w-4 h-4 border border-blue-800 rounded bg-blue-950/50 focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                  <div className="ml-3 text-sm">
                    <label htmlFor="terms" className="text-blue-200">
                      I agree to the <Link href="/terms" className="text-blue-400 hover:text-blue-300">Terms of Service</Link> and <Link href="/privacy" className="text-blue-400 hover:text-blue-300">Privacy Policy</Link>
                    </label>
                  </div>
                </div>
                
                {/* Register button */}
                <button
                  type="submit"
                  disabled={isLoading || !!error}
                  className="w-full bg-gradient-to-r from-blue-500 to-blue-700 hover:from-blue-600 hover:to-blue-800 text-white py-3 px-4 rounded-lg font-medium transition-all flex items-center justify-center mt-6 disabled:opacity-70 disabled:cursor-not-allowed"
                >
                  {isLoading ? (
                    <svg className="animate-spin -ml-1 mr-2 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                  ) : null}
                  {isLoading ? 'Creating Account...' : 'Create Account'}
                </button>
              </form>
              
              {/* Sign in option */}
              <div className="mt-6 text-center">
                <span className="text-blue-200">Already have an account? </span>
                <Link 
                  href="/signin" 
                  className="text-blue-400 hover:text-blue-300 font-medium transition-colors"
                >
                  Sign in
                </Link>
              </div>
            </div>
          </div>
          
          {/* Sağ taraf: Bilgi ve robot resmi */}
          <div className="w-full lg:w-1/2 flex flex-col items-center lg:items-end text-center lg:text-right mb-8 lg:mb-0">
            <Link href="/" className="inline-block mb-8">
              <span className="text-3xl sm:text-4xl font-semibold text-white">
                CVRaterAI
              </span>
            </Link>
            
            <h1 className="text-3xl sm:text-4xl md:text-5xl font-bold text-white mb-4">
              Join Our Community
            </h1>
            
            <p className="text-base sm:text-lg text-blue-200 max-w-md mb-8">
              Create your account today and start using AI-powered tools to improve your CV and boost your career prospects.
            </p>
            
            <div className="relative w-64 sm:w-80 h-64 sm:h-80 robot-container">
              <Image 
                src="/security.png" 
                alt="Security Robot" 
                width={400} 
                height={400}
                priority
                className="w-full h-auto drop-shadow-[0_0_25px_rgba(59,130,246,0.3)] transform -scale-x-100"
              />
            </div>
          </div>
        </div>
        
        {/* Robot floating animation */}
        <style jsx global>{`
          @keyframes robotFloat {
            0% {
              transform: translateY(0px) rotate(0deg) scaleX(-1);
            }
            50% {
              transform: translateY(-15px) rotate(-2deg) scaleX(-1);
            }
            100% {
              transform: translateY(0px) rotate(0deg) scaleX(-1);
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