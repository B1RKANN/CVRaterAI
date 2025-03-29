'use client';

import { useState, useEffect } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { FaUser, FaEnvelope, FaSignOutAlt } from 'react-icons/fa';
import Header from '../components/Header';
import GradientSpots from '../components/GradientSpots';
import { jwtDecode } from 'jwt-decode';
import Cookies from 'js-cookie';

export default function Profile() {
  const router = useRouter();
  // Default kullanıcı bilgileri
  const [userProfile, setUserProfile] = useState({
    firstName: '',
    lastName: '',
    email: '',
    credits: 0,
    maxCredits: 20,
    planType: ''
  });
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  // Kredi yüzdesini hesapla
  const creditPercentage = (userProfile.credits / userProfile.maxCredits) * 100;

  // Çıkış yapma fonksiyonu
  const handleLogout = () => {
    // Cookies'leri temizle
    Cookies.remove('token');
    Cookies.remove('refreshToken');
    
    // LocalStorage'i temizle
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    
    // Ana sayfaya yönlendir
    router.push('/');
  };

  // JWT token'dan kullanıcı ID'sini çıkarma fonksiyonu
  const getUserIdFromToken = () => {
    try {
      // Debug için tüm cookie'leri konsola yazdır
      console.log('Tüm cookies:', document.cookie);
      
      // Önce document cookies'den JWT token'ı almayı dene
      const cookies = document.cookie.split(';');
      console.log('Ayrıştırılmış cookies:', cookies);
      
      // 'token=' veya 'jwt=' veya 'accessToken=' ile başlayan cookie'yi ara
      const tokenCookie = cookies.find(cookie => 
        cookie.trim().startsWith('token=') || 
        cookie.trim().startsWith('jwt=') || 
        cookie.trim().startsWith('accessToken=')
      );
      
      let token = null;
      
      if (tokenCookie) {
        token = tokenCookie.split('=')[1];
        console.log('Cookie\'den token bulundu:', token);
      } else {
        // Cookie'de bulunamadıysa localStorage'dan almayı dene
        token = localStorage.getItem('token') || 
                localStorage.getItem('jwt') || 
                localStorage.getItem('accessToken');
                
        if (token) {
          console.log('LocalStorage\'dan token bulundu:', token);
        } else {
          // Geçici çözüm: Test için sabit bir ID kullan
          console.log('Token bulunamadı! Test amaçlı sabit ID kullanılacak');
          return 1; // Test için sabit bir kullanıcı ID'si kullanalım
        }
      }
      
      if (!token) {
        throw new Error('Token bulunamadı');
      }
      
      // Token'ı decode et
      console.log('Token decode ediliyor:', token);
      const decoded = jwtDecode(token) as any;
      console.log('Decode edilmiş token:', decoded);
      
      // ÖNEMLİ: Öncelikle userId alanını kontrol et
      // Token içinde userId varsa, o değeri kullan
      if (decoded.userId !== undefined) {
        console.log('Token içinden userId kullanılıyor:', decoded.userId);
        return Number(decoded.userId); // Sayısal değere dönüştür
      }
      
      // Eğer userId yoksa, diğer alanları kontrol et (id veya sub)
      // ID string olabileceğinden sayıya çevirmeyi dene
      let userId = decoded.id || 1; // Varsayılan olarak 1 kullan
      
      // ID sayısal değilse, sayısal değere çevirmeyi dene
      if (typeof userId === 'string' && !isNaN(Number(userId))) {
        userId = Number(userId);
      }
      
      console.log('Çıkarılan kullanıcı ID\'si:', userId, 'Tipi:', typeof userId);
      return userId;
      
    } catch (error) {
      console.error('Token çözümlenirken hata:', error);
      console.log('Test için sabit ID 1 kullanılıyor');
      return 1; // Hata durumunda test için sabit bir ID kullanalım
    }
  };

  // Sayfa yüklendiğinde kullanıcı bilgilerini API'den getir
  useEffect(() => {
    async function fetchUserProfile() {
      try {
        setIsLoading(true);
        setError('');
        
        // Token'dan kullanıcı ID'sini al
        const userId = getUserIdFromToken();
        
        if (!userId) {
          router.push('/signin');
          throw new Error('Kullanıcı bilgisi bulunamadı. Lütfen tekrar giriş yapın.');
        }
        
        // Token'ı al
        const token = localStorage.getItem('token') || 
                      document.cookie.split(';').find(cookie => cookie.trim().startsWith('token='))?.split('=')[1];
        
        if (!token) {
          router.push('/signin');
          throw new Error('Oturum bilgisi bulunamadı. Lütfen tekrar giriş yapın.');
        }
        
        // API'den kullanıcı bilgilerini çek (id integer olmalı)
        console.log(`Kullanıcı ID: ${userId}, ID Tipi: ${typeof userId}`);
        
        // URL'de email değil, sayısal ID kullanmalıyız
        const response = await fetch(`/api/v1/profile/${userId}`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}` // Token'ı Authorization header'ı ile gönder
          },
          credentials: 'include', // Cookie'lerdeki token bilgilerini göndermek için
        });
        
        if (!response.ok) {
          // Oturum kontrolü - 401 durumunda giriş sayfasına yönlendir
          if (response.status === 401) {
            router.push('/signin');
            throw new Error('Oturumunuz sona ermiş. Lütfen tekrar giriş yapın.');
          } else {
            throw new Error(`Kullanıcı bilgileri alınamadı: ${response.status}`);
          }
        }
        
        const data = await response.json();
        
        // API'den dönen verileri state'e kaydet
        if (data.status === 200 && data.payload) {
          const fullName = data.payload.name ? data.payload.name.split(' ') : ['', ''];
          
          setUserProfile({
            firstName: fullName[0] || 'İsimsiz',
            lastName: fullName.slice(1).join(' ') || 'Kullanıcı',
            email: data.payload.email || 'kullanici@ornek.com',
            credits: data.payload.userCredit || 0,
            maxCredits: 20, // Maksimum kredi sayısı sabit olarak düşünülebilir
            planType: data.payload.planType || 'FREE'
          });
        } else {
          throw new Error(data.errorMessage || 'Kullanıcı bilgileri alınamadı');
        }
        
      } catch (err: any) {
        console.error('Profil bilgileri yüklenirken hata:', err);
        setError(err.message || 'Profil bilgileri yüklenirken bir hata oluştu');
        
        // Demo amaçlı varsayılan değerler
        setUserProfile({
          firstName: 'John',
          lastName: 'Doe',
          email: 'john.doe@example.com',
          credits: 8,
          maxCredits: 20,
          planType: 'FREE'
        });
      } finally {
        setIsLoading(false);
      }
    }
    
    fetchUserProfile();
  }, [router]);

  return (
    <>
      <Header />
      <main className="min-h-screen w-full bg-black flex flex-col items-center justify-center px-4 py-12 pt-28 relative overflow-hidden">
        {/* Gradient Spots Component */}
        <GradientSpots />
        
        {/* Ana içerik */}
        <div className="w-full max-w-4xl mx-auto flex flex-col items-center z-10">
          <h1 className="text-3xl sm:text-4xl font-bold text-white mb-8">Your Profile</h1>
          
          {/* Hata mesajı */}
          {error && (
            <div className="w-full max-w-2xl mb-6 p-4 bg-red-500/20 border border-red-500/50 rounded-lg">
              <p className="text-red-200 text-sm">{error}</p>
            </div>
          )}
          
          {/* Yükleniyor durumu */}
          {isLoading ? (
            <div className="flex items-center justify-center p-12">
              <svg className="animate-spin h-10 w-10 text-blue-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
            </div>
          ) : (
            <div className="w-full bg-gradient-to-br from-blue-900/30 to-indigo-900/30 backdrop-blur-sm p-6 sm:p-8 rounded-2xl border border-blue-800/50 max-w-2xl">
              {/* Profil başlığı */}
              <div className="flex items-center justify-between mb-8">
                <h2 className="text-2xl font-bold text-white">Account Information</h2>
                <button 
                  className="bg-red-600/20 hover:bg-red-600/40 text-red-300 p-2.5 rounded-full transition-colors"
                  onClick={handleLogout}
                  title="Sign Out"
                >
                  <FaSignOutAlt className="h-5 w-5" />
                </button>
              </div>
              
              {/* Kullanıcı profil bilgileri */}
              <div className="space-y-6 mb-10">
                {/* Ad ve Soyad bilgisi */}
                <div className="flex flex-col sm:flex-row sm:space-x-4">
                  <div className="flex-1 mb-4 sm:mb-0">
                    <label className="block text-sm font-medium text-blue-200 mb-1.5">First Name</label>
                    <div className="bg-blue-950/50 px-4 py-3 rounded-lg border border-blue-800/50 flex items-center">
                      <FaUser className="h-5 w-5 text-blue-400 mr-3" />
                      <span className="text-white">{userProfile.firstName}</span>
                    </div>
                  </div>
                  <div className="flex-1">
                    <label className="block text-sm font-medium text-blue-200 mb-1.5">Last Name</label>
                    <div className="bg-blue-950/50 px-4 py-3 rounded-lg border border-blue-800/50 flex items-center">
                      <FaUser className="h-5 w-5 text-blue-400 mr-3" />
                      <span className="text-white">{userProfile.lastName}</span>
                    </div>
                  </div>
                </div>
                
                {/* Email bilgisi */}
                <div>
                  <label className="block text-sm font-medium text-blue-200 mb-1.5">Email Address</label>
                  <div className="bg-blue-950/50 px-4 py-3 rounded-lg border border-blue-800/50 flex items-center">
                    <FaEnvelope className="h-5 w-5 text-blue-400 mr-3" />
                    <span className="text-white">{userProfile.email}</span>
                  </div>
                </div>
              </div>
              
              {/* Kredi durumu */}
              <div className="mb-6">
                {/* Plan tipi bilgisi - Güncellenmiş, daha büyük ve sol tarafta */}
                <div className="mb-4">
                  <span className="text-sm text-blue-300 block mb-2">Current Plan</span>
                  <div className="inline-block">
                    <span className="text-2xl font-bold px-5 py-2 rounded-lg bg-gradient-to-r from-blue-600/40 to-indigo-600/40 text-white border border-blue-500/50 shadow-lg shadow-blue-900/20">
                      {userProfile.planType}
                    </span>
                  </div>
                </div>
                
                <div className="flex justify-between items-center mb-2 mt-6">
                  <h3 className="text-lg font-semibold text-white">Your Credits</h3>
                  <span className="text-blue-200">
                    {userProfile.credits} / {userProfile.maxCredits} credits
                  </span>
                </div>
                
                {/* Kredi ilerleme çubuğu */}
                <div className="h-4 bg-blue-950/70 rounded-full overflow-hidden border border-blue-900/60">
                  <div 
                    className="h-full bg-gradient-to-r from-blue-500 to-blue-700 rounded-full transition-all duration-500 ease-out"
                    style={{ width: `${creditPercentage}%` }}
                  ></div>
                </div>
                
                {/* Bilgi notu */}
                <p className="text-blue-300 text-sm mt-2">
                  These credits are used for premium CV rating services.
                </p>
              </div>
              
              {/* Kredi satın alma butonu */}
              <button className="w-full bg-gradient-to-r from-blue-500 to-blue-700 hover:from-blue-600 hover:to-blue-800 text-white py-3 px-4 rounded-lg font-medium transition-all flex items-center justify-center mt-4">
                Purchase More Credits
              </button>
            </div>
          )}
          
          {/* Alt bağlantılar */}
          <div className="mt-6 space-x-4 text-center">
            <Link href="/settings" className="text-blue-400 hover:text-blue-300 transition-colors">
              Account Settings
            </Link>
            <span className="text-blue-800">|</span>
            <Link href="/help" className="text-blue-400 hover:text-blue-300 transition-colors">
              Help Center
            </Link>
          </div>
        </div>
      </main>
    </>
  );
} 