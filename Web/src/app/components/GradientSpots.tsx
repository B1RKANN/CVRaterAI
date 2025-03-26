export default function GradientSpots() {
  return (
    <div className="absolute inset-0 z-1 overflow-hidden pointer-events-none">
      {/* Gradient spot behind the robot (left side) */}
      <div 
        className="absolute top-1/3 left-0 w-[600px] h-[600px]" 
        style={{
          background: 'radial-gradient(circle, rgba(37, 99, 235, 0.9) 0%, rgba(59, 130, 246, 0.6) 30%, rgba(96, 165, 250, 0.2) 60%, transparent 80%)',
          borderRadius: '50%',
          filter: 'blur(60px)',
          transform: 'translate(-30%, -50%)'
        }}
      />
      
      {/* Gradient spot in the upper right corner */}
      <div 
        className="absolute top-0 right-0 w-[500px] h-[500px]" 
        style={{
          background: 'radial-gradient(circle, rgba(59, 130, 246, 0.8) 0%, rgba(37, 99, 235, 0.5) 40%, rgba(96, 165, 250, 0.2) 70%, transparent 85%)',
          borderRadius: '50%',
          filter: 'blur(60px)',
          transform: 'translate(20%, -20%)'
        }}
      />
    </div>
  );
} 