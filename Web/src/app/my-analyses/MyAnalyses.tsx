'use client';

import { useState, useEffect, useCallback } from 'react';
import Header from '../components/Header';
import GradientSpots from '../components/GradientSpots';
import Particles from '../components/Particles';
import { useAuth } from '../context/AuthContext';
import { useRouter } from 'next/navigation';
import Image from 'next/image';
import Link from 'next/link';
import { FaChartBar, FaHistory, FaStar, FaFileAlt, FaCalendarAlt, FaCheckCircle, FaTimesCircle, FaSearch } from 'react-icons/fa';

interface Analysis {
  id: string;
  date: string;
  cvName: string;
  score: number;
  status: 'completed' | 'processing' | 'failed';
  // İstatistikler için ek alanlar
  jobTitle?: string;
  industry?: string;
  matchPercentage?: number;
}

// Tab tipi
type TabType = 'analyses' | 'statistics';

// LoadingScreen Component (basit spinner tarzı)
const LoadingScreen = ({ isVisible }: { isVisible: boolean }) => {
  if (!isVisible) return null;
  
  return (
    <div className="fixed inset-0 flex items-center justify-center z-50 bg-black/70 backdrop-blur-sm">
      <div className="flex flex-col items-center">
        <div className="animate-spin rounded-full h-16 w-16 border-4 border-blue-500 border-t-transparent mb-4"></div>
        <p className="text-white font-medium">Loading...</p>
      </div>
    </div>
  );
};

export default function MyAnalyses() {
  const [analyses, setAnalyses] = useState<Analysis[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isFetchingResult, setIsFetchingResult] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<TabType>('analyses');
  const [searchTerm, setSearchTerm] = useState('');
  const { getToken, isAuthenticated } = useAuth();
  const router = useRouter();

  useEffect(() => {
    // Oturum açık değilse giriş sayfasına yönlendir
    if (!isAuthenticated) {
      router.push('/signin');
      return;
    }
    
    // Analizleri getir
    fetchAnalyses();
  }, [isAuthenticated, router]);

  const fetchAnalyses = async () => {
    try {
      setIsLoading(true);
      const token = await getToken();
      
      if (!token) {
        throw new Error('Kimlik doğrulama hatası. Lütfen tekrar giriş yapın.');
      }
      
      // Token bilgilerini konsola yazdır
      console.log('Token:', token);
      
      try {
        // Token'ı parçalara ayır
        const tokenParts = token.split('.');
        console.log('Token parçaları:', tokenParts);
        
        if (tokenParts.length >= 2) {
          // Base64'ü decode et
          const base64Payload = tokenParts[1];
          console.log('Base64 payload:', base64Payload);
          
          // Base64'ü decode ederek JSON'a çevir
          const decodedPayload = atob(base64Payload);
          console.log('Decoded payload:', decodedPayload);
          
          // JSON'ı parse et
          const tokenData = JSON.parse(decodedPayload);
          console.log('Token veri yapısı:', tokenData);
          
          // userId'yi direk kullan
          const userId = 1; // Sabit kullanıcı ID'si
          console.log('Kullanılan userId:', userId);
          
          // API isteği
          const response = await fetch(`http://69.62.120.202:8080/api/v1/cv-evaluation/user/${userId}`, {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          });
          
          if (!response.ok) {
            throw new Error(`Hata: ${response.status} ${response.statusText}`);
          }
          
          const data = await response.json();
          console.log('API yanıtı:', data);
          
          // Gelen veriyi uygulama için gerekli formata dönüştür
          const enhancedData = data.map((item: any) => ({
            id: item.id.toString(),
            date: item.date,
            cvName: item.fullName,
            score: Math.floor(Math.random() * 40) + 60, // Demo için rastgele skor
            status: 'completed',
            jobTitle: 'Software Developer', // Varsayılan değer
            industry: 'Information Technology', // Varsayılan değer
            matchPercentage: Math.floor(Math.random() * 40) + 60, // Demo için rastgele eşleşme yüzdesi
          }));
          
          setAnalyses(enhancedData);
        } else {
          console.error('Geçersiz token formatı:', token);
          throw new Error('Geçersiz token formatı');
        }
      } catch (parseError: any) {
        console.error('Token parse hatası:', parseError);
        throw new Error(`Token ayrıştırma hatası: ${parseError.message}`);
      }
    } catch (err: any) {
      console.error('Analizler getirilirken hata:', err);
      setError(err.message || 'Analizler yüklenirken bir hata oluştu');
    } finally {
      setIsLoading(false);
    }
  };

  // Skordan renk hesaplama
  const getScoreColor = (score: number) => {
    if (score >= 80) return 'text-green-500';
    if (score >= 60) return 'text-yellow-500';
    return 'text-red-500';
  };

  // Arama filtreleme
  const filteredAnalyses = analyses.filter(analysis => {
    const searchTermLower = searchTerm.toLowerCase();
    return (
      (analysis.cvName?.toLowerCase() || '').includes(searchTermLower) ||
      (analysis.jobTitle?.toLowerCase() || '').includes(searchTermLower) ||
      (analysis.industry?.toLowerCase() || '').includes(searchTermLower)
    );
  });

  // İstatistik verileri hesaplama
  const statisticsData = {
    totalAnalyses: analyses.length,
    completedAnalyses: analyses.filter(a => a.status === 'completed').length,
    failedAnalyses: analyses.filter(a => a.status === 'failed').length,
    averageScore: analyses.length > 0 
      ? Math.round(analyses.reduce((sum, a) => sum + a.score, 0) / analyses.length) 
      : 0,
    bestScore: analyses.length > 0 
      ? Math.max(...analyses.map(a => a.score)) 
      : 0,
    latestAnalysis: analyses.length > 0 
      ? analyses.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())[0].date 
      : 'N/A',
  };

  // Tab içerik bileşenleri
  const TabButton = ({ type, label, icon }: { type: TabType, label: string, icon: React.ReactNode }) => (
    <button
      className={`flex items-center space-x-2 px-5 py-3 rounded-xl font-medium transition-all ${
        activeTab === type 
          ? 'bg-blue-800/50 text-white border border-blue-700'
          : 'bg-transparent text-blue-300 hover:text-white hover:bg-blue-900/30'
      }`}
      onClick={() => setActiveTab(type)}
    >
      {icon}
      <span>{label}</span>
    </button>
  );

  // Sayfa başlığı
  const PageTitle = () => (
    <div className="flex flex-col sm:flex-row items-center justify-between mb-8">
      <h1 className="text-2xl sm:text-3xl font-bold text-white mb-4 sm:mb-0">My Analyses</h1>
      
      <Link 
        href="/upload" 
        className="flex items-center space-x-2 bg-gradient-to-r from-blue-500 to-blue-700 text-white px-4 py-2 rounded-full hover:shadow-lg hover:shadow-blue-500/30 transition-all"
      >
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
        </svg>
        <span>New Analysis</span>
      </Link>
    </div>
  );

  // Tabs
  const Tabs = () => (
    <div className="flex space-x-3 mb-6">
      <TabButton 
        type="analyses" 
        label="Analyses" 
        icon={<FaHistory className="w-5 h-5" />} 
      />
      <TabButton 
        type="statistics" 
        label="Statistics" 
        icon={<FaChartBar className="w-5 h-5" />} 
      />
    </div>
  );
  
  // Statistics view
  const StatisticsView = () => (
    <div>
      <h2 className="text-xl font-semibold text-white mb-6">CV Analysis Statistics</h2>
      
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
        {/* Total Analyses */}
        <div className="bg-blue-900/20 border border-blue-800/50 rounded-xl p-5">
          <div className="flex items-center mb-2">
            <FaFileAlt className="text-blue-400 mr-3 h-5 w-5" />
            <h3 className="text-white font-medium">Total Analyses</h3>
          </div>
          <p className="text-3xl font-bold text-white">{statisticsData.totalAnalyses}</p>
        </div>
        
        {/* Average Score */}
        <div className="bg-blue-900/20 border border-blue-800/50 rounded-xl p-5">
          <div className="flex items-center mb-2">
            <FaStar className="text-yellow-400 mr-3 h-5 w-5" />
            <h3 className="text-white font-medium">Average Score</h3>
          </div>
          <p className="text-3xl font-bold text-white">{statisticsData.averageScore}%</p>
        </div>
        
        {/* Best Score */}
        <div className="bg-blue-900/20 border border-blue-800/50 rounded-xl p-5">
          <div className="flex items-center mb-2">
            <FaCheckCircle className="text-green-500 mr-3 h-5 w-5" />
            <h3 className="text-white font-medium">Best Score</h3>
          </div>
          <p className="text-3xl font-bold text-white">{statisticsData.bestScore}%</p>
        </div>
        
        {/* Successful Analyses */}
        <div className="bg-blue-900/20 border border-blue-800/50 rounded-xl p-5">
          <div className="flex items-center mb-2">
            <FaCheckCircle className="text-green-500 mr-3 h-5 w-5" />
            <h3 className="text-white font-medium">Completed Analyses</h3>
          </div>
          <p className="text-3xl font-bold text-white">{statisticsData.completedAnalyses}</p>
        </div>
        
        {/* Failed Analyses */}
        <div className="bg-blue-900/20 border border-blue-800/50 rounded-xl p-5">
          <div className="flex items-center mb-2">
            <FaTimesCircle className="text-red-500 mr-3 h-5 w-5" />
            <h3 className="text-white font-medium">Failed Analyses</h3>
          </div>
          <p className="text-3xl font-bold text-white">{statisticsData.failedAnalyses}</p>
        </div>
        
        {/* Latest Analysis */}
        <div className="bg-blue-900/20 border border-blue-800/50 rounded-xl p-5">
          <div className="flex items-center mb-2">
            <FaCalendarAlt className="text-blue-400 mr-3 h-5 w-5" />
            <h3 className="text-white font-medium">Latest Analysis</h3>
          </div>
          <p className="text-lg font-medium text-white">{statisticsData.latestAnalysis}</p>
        </div>
      </div>
      
      {analyses.length > 0 ? (
        <div className="mt-4 text-center">
          <Link 
            href="/upload" 
            className="inline-flex items-center space-x-2 bg-gradient-to-r from-blue-500 to-blue-700 text-white px-5 py-2 rounded-full hover:shadow-lg hover:shadow-blue-500/30 transition-all"
          >
            <span>Get Even Better Scores</span>
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path>
            </svg>
          </Link>
        </div>
      ) : (
        <div className="text-center py-8">
          <p className="text-blue-300 mb-4">No analysis data available yet.</p>
          <Link 
            href="/upload" 
            className="inline-flex items-center space-x-2 bg-gradient-to-r from-blue-500 to-blue-700 text-white px-5 py-2 rounded-full hover:shadow-lg hover:shadow-blue-500/30 transition-all"
          >
            <span>Start Your First Analysis</span>
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path>
            </svg>
          </Link>
        </div>
      )}
    </div>
  );
  
  // Analiz sonuçlarını getir ve sonuç sayfasına yönlendir
  const fetchAnalysisResult = useCallback(async (analysisId: string) => {
    try {
      setIsFetchingResult(true);
      const token = await getToken();
      
      if (!token) {
        throw new Error('Kimlik doğrulama hatası. Lütfen tekrar giriş yapın.');
      }
      
      // API isteği
      const response = await fetch(`/api/v1/cv-evaluation/evaluate/${analysisId}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (!response.ok) {
        throw new Error(`Hata: ${response.status} ${response.statusText}`);
      }
      
      const data = await response.json();
      console.log('Analiz sonucu:', data);
      
      // Result sayfasında kullanılmak üzere sessionStorage'a kaydet
      sessionStorage.setItem('evaluationResult', JSON.stringify(data));
      
      // Sonuç sayfasına yönlendir
      router.push('/result');
    } catch (err: any) {
      console.error('Analiz sonucu alınırken hata:', err);
      setError(err.message || 'Analiz sonucu yüklenirken bir hata oluştu');
      setIsFetchingResult(false);
    }
  }, [getToken, router]);

  // Analyses List with Search
  const AnalysesListView = () => (
    <div>
      {/* Search Bar */}
      <div className="relative mb-6">
        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
          <FaSearch className="h-5 w-5 text-blue-400" />
        </div>
        <input
          type="text"
          className="bg-blue-950/50 border border-blue-800/50 text-white block w-full pl-10 pr-3 py-3 rounded-lg placeholder-blue-400/70 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          placeholder="Search analyses by name, job title, industry..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>
      
      {filteredAnalyses.length === 0 ? (
        <div className="text-center py-8 border border-blue-800/30 rounded-xl bg-blue-900/10 p-6">
          <p className="text-blue-300 mb-2">No matching analyses found.</p>
          {searchTerm && (
            <button 
              onClick={() => setSearchTerm('')}
              className="text-blue-400 hover:text-blue-300 underline mt-2"
            >
              Clear search
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredAnalyses.map((analysis) => (
            <div 
              key={analysis.id} 
              onClick={() => fetchAnalysisResult(analysis.id)}
              className="block bg-blue-900/20 hover:bg-blue-900/30 border border-blue-800/50 rounded-xl p-4 transition-all hover:shadow-md hover:shadow-blue-500/10 cursor-pointer"
            >
              <div className="flex justify-between items-start mb-3">
                <h3 className="font-medium text-white truncate pr-2">{analysis.cvName}</h3>
                <span className={`text-sm font-bold ${getScoreColor(analysis.score)}`}>
                  {analysis.score}%
                </span>
              </div>
              
              <div className="mb-3">
                <p className="text-sm text-blue-300">
                  <span className="font-medium">Job:</span> {analysis.jobTitle}
                </p>
                <p className="text-sm text-blue-300">
                  <span className="font-medium">Industry:</span> {analysis.industry}
                </p>
              </div>
              
              <div className="flex justify-between items-center text-sm">
                <span className="text-blue-300">{analysis.date}</span>
                
                {analysis.status === 'completed' ? (
                  <span className="px-2 py-1 bg-green-900/30 text-green-400 rounded-full text-xs">
                    Completed
                  </span>
                ) : analysis.status === 'processing' ? (
                  <span className="px-2 py-1 bg-yellow-900/30 text-yellow-400 rounded-full text-xs flex items-center">
                    <span className="animate-pulse mr-1">●</span>
                    Processing
                  </span>
                ) : (
                  <span className="px-2 py-1 bg-red-900/30 text-red-400 rounded-full text-xs">
                    Failed
                  </span>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );

  // Boş durum ekranı
  const EmptyState = () => (
    <div className="text-center py-16">
      <div className="w-24 h-24 mx-auto mb-6 opacity-50">
        <Image src="/empty-folder.png" alt="No analyses" width={96} height={96} />
      </div>
      <h3 className="text-xl font-medium text-white mb-2">No Analyses Yet</h3>
      <p className="text-blue-300 mb-6">Upload your first CV to get started with AI analysis</p>
      <Link 
        href="/upload" 
        className="inline-flex items-center space-x-2 bg-gradient-to-r from-blue-500 to-blue-700 text-white px-5 py-2 rounded-full hover:shadow-lg hover:shadow-blue-500/30 transition-all"
      >
        <span>Start Your First Analysis</span>
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path>
        </svg>
      </Link>
    </div>
  );

  return (
    <>
      <Header />
      <main className="min-h-screen w-full bg-black flex flex-col items-center justify-start px-4 py-12 pt-28 relative overflow-hidden">
        {/* Background Gradient Spots */}
        <GradientSpots />
        
        {/* Background Particles */}
        <div className="absolute inset-0 pointer-events-none">
          <Particles />
        </div>
        
        <div className="w-full max-w-6xl mx-auto z-10">
          <div className="bg-gradient-to-br from-blue-900/30 to-indigo-900/30 backdrop-blur-sm p-6 sm:p-8 rounded-2xl border border-blue-800/50 shadow-xl">
            <PageTitle />
            
            {isLoading ? (
              <div className="flex justify-center py-12">
                <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
              </div>
            ) : error ? (
              <div className="bg-red-500/20 border border-red-500/50 text-red-200 p-4 rounded-lg">
                {error}
              </div>
            ) : analyses.length === 0 ? (
              <EmptyState />
            ) : (
              <>
                <Tabs />
                {activeTab === 'analyses' ? <AnalysesListView /> : <StatisticsView />}
              </>
            )}
          </div>
        </div>
        
        {/* Loading Screen */}
        <LoadingScreen isVisible={isFetchingResult} />
      </main>
    </>
  );
} 