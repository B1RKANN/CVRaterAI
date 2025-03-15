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

    // Sign In/Up Modal Functionality
    const signInModal = document.getElementById('signInModal');
    if (signInModal) {
        const container = document.getElementById('container');
        const signUpButton = document.getElementById('signUp');
        const signInButton = document.getElementById('signIn');

        // Toggle between sign up and sign in panels
        if (signUpButton) {
            signUpButton.addEventListener('click', () => {
                container.classList.add('right-panel-active');
            });
        }

        if (signInButton) {
            signInButton.addEventListener('click', () => {
                container.classList.remove('right-panel-active');
            });
        }

        // Close modal when clicking outside of it
        window.addEventListener('click', (event) => {
            if (event.target === signInModal) {
                closeSignInModal();
            }
        });

        // Close modal with escape key
        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape' && signInModal.style.display === 'block') {
                closeSignInModal();
            }
        });
        
        // Giriş yapma işlevselliği
        const signInForm = document.querySelector('.sign-in-container form');
        if (signInForm) {
            signInForm.addEventListener('submit', function(e) {
                e.preventDefault();
                const email = this.querySelector('input[type="email"]').value;
                const password = this.querySelector('input[type="password"]').value;
                
                // Test kullanıcı bilgileri kontrolü
                if (email === 'x@gmail.com' && password === '123') {
                    // Giriş başarılı
                    localStorage.setItem('isLoggedIn', 'true');
                    localStorage.setItem('userEmail', email);
                    
                    // Sign In/Up butonunu Profile butonu ile değiştir
                    updateNavButtons();
                    
                    // Modalı kapat
                    closeSignInModal();
                } else {
                    alert('Hatalı email veya şifre! Lütfen tekrar deneyin.');
                }
            });
        }
    }
    
    // Sayfa yüklendiğinde giriş durumunu kontrol et
    checkLoginStatus();
});

// Giriş durumunu kontrol et ve butonları güncelle
function checkLoginStatus() {
    const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
    if (isLoggedIn) {
        updateNavButtons();
    }
}

// Butonları güncelle
function updateNavButtons() {
    const navButtons = document.querySelector('.nav-buttons');
    if (navButtons) {
        const signInBtn = navButtons.querySelector('.sign-in-btn');
        if (signInBtn) {
            // Sign In/Up butonunu Profile butonu ile değiştir
            const profileBtn = document.createElement('button');
            profileBtn.className = 'sign-in-btn';
            profileBtn.textContent = 'PROFILE';
            
            // Mevcut URL'ye göre doğru yönlendirme yolunu belirle
            const currentPath = window.location.pathname;
            let profilePath = '';
            
            // Eğer ana sayfadaysak
            if (currentPath.includes('index.html') || currentPath.endsWith('/') || currentPath.endsWith('/Web/')) {
                profilePath = 'public/pages/profile.html';
            } 
            // Eğer zaten pages klasöründeysek
            else if (currentPath.includes('/pages/')) {
                profilePath = 'profile.html';
            }
            // Diğer durumlar için
            else {
                profilePath = 'public/pages/profile.html';
            }
            
            profileBtn.onclick = function() {
                window.location.href = profilePath;
            };
            
            navButtons.replaceChild(profileBtn, signInBtn);
        }
    }
}

// Animate circles
const circles = document.querySelectorAll('.circle');
circles.forEach((circle, index) => {
    circle.style.animationDelay = `${index * 1}s`;
});

// Animate balloons
const balloons = document.querySelectorAll('.balloon');
balloons.forEach((balloon, index) => {
    balloon.style.animationDelay = `${index * 2}s`;
});

// Scroll effect for balloons
window.addEventListener('scroll', () => {
    const scrollY = window.scrollY;
    
    // Only apply effect if scrolled
    if (scrollY > 0) {
        balloons.forEach((balloon, index) => {
            // Different movement for each balloon
            const moveX = (index % 2 === 0) ? scrollY * 0.05 : -scrollY * 0.05;
            const moveY = (index % 3 === 0) ? scrollY * 0.03 : -scrollY * 0.02;
            
            // Apply transform with both the float animation and scroll movement
            balloon.style.transform = `translate(${moveX}px, ${moveY}px)`;
        });
    } else {
        // Reset transform when back at top
        balloons.forEach(balloon => {
            balloon.style.transform = 'translate(0, 0)';
        });
    }
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

// Modal Functions
function openSignInModal() {
    const modal = document.getElementById('signInModal');
    if (modal) {
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden'; // Prevent scrolling when modal is open
    }
}

function closeSignInModal() {
    const modal = document.getElementById('signInModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = ''; // Re-enable scrolling
    }
}

// Çıkış yapma fonksiyonu
function logout() {
    localStorage.removeItem('isLoggedIn');
    localStorage.removeItem('userEmail');
    window.location.reload();
} 