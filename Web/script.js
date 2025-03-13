// Wait for the DOM to be fully loaded
document.addEventListener('DOMContentLoaded', () => {
    // Smooth scroll for navigation links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            document.querySelector(this.getAttribute('href')).scrollIntoView({
                behavior: 'smooth'
            });
        });
    });

    // Button hover effects with debounce
    const buttons = document.querySelectorAll('button, .download-btn');
    buttons.forEach(button => {
        let timeoutId;
        
        button.addEventListener('mouseenter', () => {
            clearTimeout(timeoutId);
            button.style.transform = 'translateY(-3px)';
            button.style.boxShadow = '0 6px 12px rgba(0, 0, 0, 0.2)';
        });

        button.addEventListener('mouseleave', () => {
            timeoutId = setTimeout(() => {
                button.style.transform = 'translateY(0)';
                button.style.boxShadow = '0 4px 8px rgba(0, 0, 0, 0.2)';
            }, 50);
        });
    });

    // Intersection Observer for scroll animations
    const observerOptions = {
        threshold: 0.2,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                observer.unobserve(entry.target); // Stop observing once animated
            }
        });
    }, observerOptions);

    // Observe elements that should animate on scroll
    const scrollAnimElements = document.querySelectorAll('.step-card, .fast-simple');
    scrollAnimElements.forEach(element => {
        observer.observe(element);
    });

    // Add active state to navigation links
    const navLinks = document.querySelectorAll('.nav-links a');
    navLinks.forEach(link => {
        link.addEventListener('click', () => {
            navLinks.forEach(l => l.classList.remove('active'));
            link.classList.add('active');
        });
    });

    // Optimize circle animations
    const circles = document.querySelectorAll('.circle');
    circles.forEach(circle => {
        circle.style.willChange = 'transform';
    });

    // Preload animations
    document.body.style.opacity = '1';

    const nav = document.querySelector('nav');
    
    // Initial check for page load
    if (window.scrollY > 0) {
        nav.classList.add('scrolled');
    }

    // Check on scroll
    window.addEventListener('scroll', () => {
        if (window.scrollY > 0) {
            nav.classList.add('scrolled');
        } else {
            nav.classList.remove('scrolled');
        }
    });
});

// Animate circles
const circles = document.querySelectorAll('.circle');
circles.forEach((circle, index) => {
    circle.style.animationDelay = `${index * 1}s`;
});

// Intersection Observer for fade-in animations
const observerOptions = {
    threshold: 0.1
};

const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.style.opacity = '1';
            entry.target.style.transform = 'translateY(0)';
        }
    });
}, observerOptions);

// Observe elements with fade-in animations
const fadeElements = document.querySelectorAll('.hero-section > *, .steps-section, .fast-simple');
fadeElements.forEach(element => {
    element.style.opacity = '0';
    element.style.transform = 'translateY(20px)';
    element.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
    observer.observe(element);
}); 