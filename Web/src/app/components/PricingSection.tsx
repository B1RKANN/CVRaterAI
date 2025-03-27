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

// Özellik listesi öğesi bileşeni
const FeatureListItem = ({ text, included, disabled = false }: { text: string; included: boolean; disabled?: boolean }) => (
  <li className={`flex items-start ${disabled ? 'opacity-50' : ''}`}>
    {included ? <CheckIcon /> : <XMarkIcon />}
    <span className="text-blue-100">{text}</span>
  </li>
);

// Arkaplan gradyanları bileşeni
const BackgroundGradients = () => (
  <div className="absolute inset-0 z-0 overflow-hidden pointer-events-none">
    {/* Sol taraf gradyan noktası */}
    <div 
      className="absolute bottom-0 left-1/4 w-[600px] h-[600px]" 
      style={{
        background: 'radial-gradient(circle, rgba(37, 99, 235, 0.6) 0%, rgba(59, 130, 246, 0.3) 30%, rgba(96, 165, 250, 0.1) 60%, transparent 80%)',
        borderRadius: '50%',
        filter: 'blur(70px)',
        transform: 'translate(-30%, 30%)'
      }}
    />
    
    {/* Sağ taraf gradyan noktası */}
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
);

// Fiyatlandırma kartı bileşeni
interface PricingPlan {
  tier: string;
  price: string;
  period: string;
  description: string;
  features: Array<{text: string; included: boolean}>;
  buttonText: string;
  buttonLink: string;
  isPopular?: boolean;
  isDisabled?: boolean;
  cardClasses?: string;
  buttonClasses?: string;
  textColorClass?: string;
}

const PricingCard = ({
  tier,
  price,
  period,
  description,
  features,
  buttonText,
  buttonLink,
  isPopular = false,
  isDisabled = false,
  cardClasses = "bg-gradient-to-br from-blue-900/30 to-indigo-900/30 backdrop-blur-sm p-5 sm:p-6 md:p-8 rounded-2xl border border-blue-800/50 hover:border-blue-600/50",
  buttonClasses = "block w-full py-2.5 md:py-3 px-4 md:px-6 text-center bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors",
  textColorClass = "text-blue-100"
}: PricingPlan) => (
  <div className={`${cardClasses} transition-all flex flex-col h-full ${isPopular ? 'relative md:transform md:scale-105 shadow-xl shadow-blue-500/20' : ''}`}>
    {isPopular && (
      <div className="absolute top-0 right-6 -translate-y-1/2 bg-gradient-to-r from-blue-500 to-indigo-500 px-3 py-1 rounded-full text-xs font-semibold text-white">
        Most Popular
      </div>
    )}
    
    <div className="mb-4 md:mb-6">
      <span className="text-blue-400 font-semibold tracking-wider uppercase text-xs sm:text-sm">{tier}</span>
      <div className="mt-2 flex items-baseline">
        <span className="text-3xl sm:text-4xl font-bold text-white">{price}</span>
        {period && <span className="text-base sm:text-lg text-blue-300 ml-1">{period}</span>}
      </div>
      <p className="text-sm sm:text-base text-blue-200 mt-2 sm:mt-3">{description}</p>
    </div>
    
    <div className={`border-t ${isPopular ? 'border-blue-600/50' : 'border-blue-800/50'} py-4 md:py-6 mb-4 md:mb-6`}>
      <ul className="space-y-3 md:space-y-4 text-sm sm:text-base">
        {features.map((feature, index) => (
          <FeatureListItem
            key={`feature-${tier}-${index}`}
            text={feature.text}
            included={feature.included}
            disabled={!feature.included && isDisabled}
          />
        ))}
      </ul>
    </div>
    
    <div className="mt-auto">
      {isDisabled ? (
        <button 
          disabled
          className="block w-full py-2.5 md:py-3 px-4 md:px-6 text-center bg-blue-900/70 text-blue-200 font-medium rounded-lg cursor-not-allowed"
        >
          {buttonText}
        </button>
      ) : (
        <Link 
          href={buttonLink} 
          className={buttonClasses}
        >
          {buttonText}
        </Link>
      )}
    </div>
  </div>
);

export default function PricingSection() {
  // Plan verileri
  const pricingPlans: PricingPlan[] = [
    {
      tier: "Free",
      price: "$0",
      period: "/month",
      description: "Perfect for getting started with CVRaterAI",
      features: [
        { text: "20 Credits per week", included: true },
        { text: "Basic CV analysis", included: true },
        { text: "Standard templates", included: true },
        { text: "Premium features", included: false },
        { text: "Priority support", included: false }
      ],
      buttonText: "Get Started",
      buttonLink: "/register"
    },
    {
      tier: "Pro",
      price: "$9.99",
      period: "/month",
      description: "For professionals looking to maximize their potential",
      features: [
        { text: "Unlimited Credits", included: true },
        { text: "Advanced CV analysis", included: true },
        { text: "Premium templates", included: true },
        { text: "Industry-specific suggestions", included: true },
        { text: "Priority email support", included: true }
      ],
      buttonText: "Get Started",
      buttonLink: "/register/pro",
      isPopular: true,
      cardClasses: "bg-gradient-to-br from-blue-700/40 to-indigo-700/40 backdrop-blur-sm p-5 sm:p-6 md:p-8 rounded-2xl border border-blue-500/50 hover:border-blue-400/50",
      buttonClasses: "block w-full py-2.5 md:py-3 px-4 md:px-6 text-center bg-gradient-to-r from-blue-500 to-blue-700 hover:from-blue-600 hover:to-blue-800 text-white font-medium rounded-lg transition-all hover:shadow-lg hover:shadow-blue-500/30",
      textColorClass: "text-blue-50"
    },
    {
      tier: "Business",
      price: "Coming Soon",
      period: "",
      description: "Enterprise-grade solutions for your business",
      features: [
        { text: "Team member accounts", included: true },
        { text: "Admin dashboard", included: true },
        { text: "Custom branding", included: true },
        { text: "API access", included: true },
        { text: "Dedicated account manager", included: true }
      ],
      buttonText: "Notify Me",
      buttonLink: "#",
      isDisabled: true
    }
  ];

  return (
    <section id="pricing" className="w-full py-12 md:py-16 px-4 md:px-16 bg-transparent relative z-20 overflow-hidden">
      <BackgroundGradients />
      
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
          {pricingPlans.map((plan, index) => (
            <PricingCard key={`pricing-plan-${index}`} {...plan} />
          ))}
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