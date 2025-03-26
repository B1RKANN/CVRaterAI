import Header from './components/Header';
import HeroSection from './components/HeroSection';
import FeaturesSection from './components/FeaturesSection';
import Particles from './components/Particles';
import GradientSpots from './components/GradientSpots';

export default function Home() {
  return (
    <main className="min-h-screen bg-black relative overflow-hidden pt-20">
      <Particles />
      <GradientSpots />
      <Header />
      <div className="relative z-20">
        <HeroSection />
        <FeaturesSection />
      </div>
    </main>
  );
}
