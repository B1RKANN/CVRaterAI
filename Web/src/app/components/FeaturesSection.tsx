'use client';

import Image from 'next/image';
import Link from 'next/link';

// Tekrar kullanılabilir bileşenler
const MobileFeatureItem = ({ text }: { text: string }) => (
  <div className="bg-white text-gray-800 py-2.5 px-5 rounded-full shadow-lg transition-all hover:bg-blue-600 hover:text-white text-center mx-auto max-w-[280px]">
    <p className="font-semibold text-sm whitespace-nowrap">{text}</p>
  </div>
);

// Masaüstü özellik öğesi bileşeni
const DesktopFeatureItem = ({ 
  text, 
  position 
}: { 
  text: string; 
  position: 'leftTop' | 'leftMiddle' | 'leftBottom' | 'rightTop' | 'rightMiddle' | 'rightBottom' 
}) => {
  // Konum sınıfları
  const positionClasses = {
    leftTop: "absolute left-0 top-[25%] md:top-[30%] transform -translate-y-1/2",
    leftMiddle: "absolute left-[5%] md:left-[10%] top-1/2 transform -translate-y-1/2",
    leftBottom: "absolute left-0 bottom-[25%] md:bottom-[30%] transform translate-y-1/2",
    rightTop: "absolute right-0 top-[25%] md:top-[30%] transform -translate-y-1/2",
    rightMiddle: "absolute right-[5%] md:right-[10%] top-1/2 transform -translate-y-1/2",
    rightBottom: "absolute right-0 bottom-[25%] md:bottom-[30%] transform translate-y-1/2"
  };
  
  return (
    <div className={`${positionClasses[position]} z-20 bg-white text-gray-800 py-3 px-6 rounded-full shadow-lg cursor-pointer transition-all hover:bg-blue-600 hover:text-white hover:scale-105`}>
      <p className="font-semibold text-sm md:text-base whitespace-nowrap">{text}</p>
    </div>
  );
};

// Arkaplan gradyanları bileşeni
const BackgroundGradients = () => (
  <div className="absolute inset-0 z-0 overflow-hidden pointer-events-none">
    {/* Sol taraf gradyan noktası */}
    <div 
      className="absolute top-1/4 left-0 w-[700px] h-[700px]" 
      style={{
        background: 'radial-gradient(circle, rgba(37, 99, 235, 0.7) 0%, rgba(59, 130, 246, 0.4) 30%, rgba(96, 165, 250, 0.1) 60%, transparent 80%)',
        borderRadius: '50%',
        filter: 'blur(70px)',
        transform: 'translate(-30%, -30%)'
      }}
    />
    
    {/* Sağ taraf gradyan noktası */}
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
);

// Özelliklerin array şeklinde tutulması, yönetimini kolaylaştırır
const featureItems = [
  "Personalized Recommendations",
  "Smart Search Filters",
  "Real-Time Availability Updates",
  "Predictive Pricing Insights",
  "Virtual Property Tours",
  "Efficient Leasing Process"
];

// Robot spot ışık efekti
const RobotSpotlight = ({ isMobile = false }) => {
  const size = isMobile ? "[250px]" : "[400px]";
  
  return (
    <div 
      className={`absolute top-1/2 left-1/2 w-${size} h-${size} -z-10`} 
      style={{
        background: 'radial-gradient(circle, rgba(37, 99, 235, 0.8) 0%, rgba(59, 130, 246, 0.4) 30%, rgba(96, 165, 250, 0.1) 60%, transparent 80%)',
        borderRadius: '50%',
        filter: `blur(${isMobile ? '40px' : '60px'})`,
        transform: 'translate(-50%, -50%)'
      }}
    />
  );
};

export default function FeaturesSection() {
  return (
    <section id="features" className="w-full py-8 md:py-12 px-4 md:px-16 bg-transparent relative z-20 overflow-hidden">
      <BackgroundGradients />
      
      <div className="max-w-7xl mx-auto relative">
        {/* Bölüm başlığı */}
        <div className="text-center mb-2">
          <h2 className="text-3xl sm:text-4xl md:text-5xl font-bold text-white mb-1">Features</h2>
          <p className="text-base sm:text-lg text-blue-200 max-w-3xl mx-auto">
            Discover how our AI-powered tools can help you create the perfect CV and land your dream job
          </p>
        </div>
        
        {/* Mobil Özellik Kutuları - Sadece küçük ekranlarda görünür */}
        <div className="block md:hidden mt-8">
          <div className="space-y-4">
            {featureItems.slice(0, 3).map((feature, index) => (
              <MobileFeatureItem key={`mobile-feature-${index}`} text={feature} />
            ))}
            
            {/* Robot merkez görseli - mobil */}
            <div className="relative z-30 w-48 h-48 mx-auto my-6">
              <Image 
                src="/info.webp" 
                alt="Robot Assistant" 
                width={200} 
                height={200}
                className="w-full h-auto robot-float"
              />
              
              {/* Robot spot ışık efekti */}
              <RobotSpotlight isMobile={true} />
            </div>
            
            {featureItems.slice(3).map((feature, index) => (
              <MobileFeatureItem key={`mobile-feature-${index + 3}`} text={feature} />
            ))}
          </div>
        </div>
        
        {/* Masaüstü Robot görseli ve özellikleri - Sadece orta ve üstü ekranlarda görünür */}
        <div className="hidden md:flex relative mx-auto max-w-5xl h-[400px] md:h-[500px] items-center justify-center">
          {/* Robot merkez görseli */}
          <div className="relative z-30 w-56 md:w-72 h-56 md:h-72">
            <Image 
              src="/info.webp" 
              alt="Robot Assistant" 
              width={300} 
              height={300}
              className="w-full h-auto robot-float"
            />
            
            {/* Robot spot ışık efekti */}
            <RobotSpotlight />
          </div>
          
          {/* Robotun etrafına konumlandırılmış özellik kutuları - Sadece masaüstü */}
          <DesktopFeatureItem text={featureItems[0]} position="leftTop" />
          <DesktopFeatureItem text={featureItems[1]} position="leftMiddle" />
          <DesktopFeatureItem text={featureItems[2]} position="leftBottom" />
          <DesktopFeatureItem text={featureItems[3]} position="rightTop" />
          <DesktopFeatureItem text={featureItems[4]} position="rightMiddle" />
          <DesktopFeatureItem text={featureItems[5]} position="rightBottom" />
        </div>
      </div>
      
      {/* Robot yüzdürme animasyonu */}
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