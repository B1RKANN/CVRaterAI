'use client';

import { useEffect, useRef } from 'react';

interface Particle {
  x: number;
  y: number;
  size: number;
  speedX: number;
  speedY: number;
  element: HTMLDivElement;
}

export default function Particles() {
  const containerRef = useRef<HTMLDivElement>(null);
  const particlesRef = useRef<Particle[]>([]);
  
  useEffect(() => {
    if (!containerRef.current) return;
    
    const container = containerRef.current;
    const particleCount = 50;
    const particles: Particle[] = [];
    
    // Remove any existing particles
    container.innerHTML = '';
    
    // Create particles
    for (let i = 0; i < particleCount; i++) {
      const particle = document.createElement('div');
      particle.className = 'particle';
      
      const size = Math.random() * 15 + 5;
      const x = Math.random() * window.innerWidth;
      const y = Math.random() * window.innerHeight;
      const speedX = Math.random() * 2 - 1;
      const speedY = Math.random() * 2 - 1;
      
      particle.style.width = `${size}px`;
      particle.style.height = `${size}px`;
      particle.style.left = `${x}px`;
      particle.style.top = `${y}px`;
      particle.style.opacity = `${Math.random() * 0.6 + 0.2}`;
      particle.style.animation = `float ${Math.random() * 10 + 10}s linear infinite`;
      particle.style.animationDelay = `-${Math.random() * 10}s`;
      
      container.appendChild(particle);
      
      particles.push({
        x,
        y,
        size,
        speedX,
        speedY,
        element: particle
      });
    }
    
    particlesRef.current = particles;
    
    // Animation function
    let animationFrameId: number;
    
    const animate = () => {
      particlesRef.current.forEach(particle => {
        particle.x += particle.speedX;
        particle.y += particle.speedY;
        
        // Boundary check
        if (particle.x < 0 || particle.x > window.innerWidth) {
          particle.speedX *= -1;
        }
        
        if (particle.y < 0 || particle.y > window.innerHeight) {
          particle.speedY *= -1;
        }
        
        particle.element.style.left = `${particle.x}px`;
        particle.element.style.top = `${particle.y}px`;
      });
      
      animationFrameId = requestAnimationFrame(animate);
    };
    
    animate();
    
    return () => {
      cancelAnimationFrame(animationFrameId);
    };
  }, []);
  
  return (
    <div ref={containerRef} className="particle-container" />
  );
} 