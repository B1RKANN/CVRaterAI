// Wait for the DOM to be fully loaded
document.addEventListener('DOMContentLoaded', () => {
    // Mobil menü kurulumu
    setupMobileMenu();
    
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
                
                // Adım kartları için kademeli animasyon
                if (entry.target.classList.contains('step-card')) {
                    const index = Array.from(document.querySelectorAll('.step-card')).indexOf(entry.target);
                    entry.target.style.transitionDelay = `${index * 0.2}s`;
                }
                
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
            
            // Mobil menüyü kapat
            const navLinksElement = document.querySelector('.nav-links');
            if (navLinksElement && navLinksElement.classList.contains('mobile-menu')) {
                navLinksElement.classList.remove('active');
                document.querySelector('.menu-icon')?.classList.remove('active');
                document.body.style.overflow = '';
            }
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
            signInForm.addEventListener('submit', async function(e) {
                e.preventDefault();
                const email = this.querySelector('input[type="email"]').value;
                const password = this.querySelector('input[type="password"]').value;
                
                if (email && password) {
                    // Giriş işlemi için loading göster
                    const submitButton = this.querySelector('button[type="submit"]');
                    const originalText = submitButton.textContent;
                    submitButton.textContent = 'Giriş Yapılıyor...';
                    submitButton.disabled = true;
                    
                    try {
                        console.log('Attempting login with email:', email);
                        
                        // Giriş için authenticate-with-cookie endpoint'ine istek gönder
                        const responseData = await apiRequest('http://69.62.120.202:8080/auth/v2/authenticate-with-cookie', 'POST', {
                            email: email,
                            password: password
                        });
                        
                        console.log('Login successful!');
                        
                        // JWT token'ı yanıttan al
                        if (responseData && responseData.token) {
                            // Token'ı localStorage'a kaydet
                            localStorage.setItem('jwt_token', responseData.token);
                            console.log('JWT token saved to localStorage');
                        }
                        
                        // Kullanıcı bilgilerini localStorage'a kaydet
                        localStorage.setItem('userEmail', email);
                        localStorage.setItem('userName', responseData.name || 'User');
                        localStorage.setItem('userSurname', responseData.surname || 'Account');
                        localStorage.setItem('isLoggedIn', 'true');
                        
                        // Sign In/Up butonunu Profile butonu ile değiştir
                        updateNavButtons();
                        
                        // Modalı kapat
                        closeSignInModal();
                        
                        // Mevcut URL'yi kontrol et
                        const currentPath = window.location.pathname;
                        
                        // Eğer anasayfadaysak, upload sayfasına yönlendir
                        if (currentPath.includes('index.html') || currentPath.endsWith('/') || currentPath.endsWith('/Web/')) {
                            window.location.href = 'public/pages/upload.html';
                        } else {
                            // Diğer sayfalarda sayfayı yenile
                            window.location.reload();
                        }
                    } catch (error) {
                        console.error('Login error:', error);
                        alert('Giriş başarısız: ' + (error.message || 'Lütfen email ve şifrenizi kontrol edin.'));
                    } finally {
                        // Loading durumunu kaldır
                        submitButton.textContent = originalText;
                        submitButton.disabled = false;
                    }
                } else {
                    alert('Lütfen email ve şifre alanlarını doldurun!');
                }
            });
        }
        
        // Kayıt olma işlevselliği
        const signUpForm = document.querySelector('.sign-up-container form');
        if (signUpForm) {
            signUpForm.addEventListener('submit', async function(e) {
                e.preventDefault();
                const name = this.querySelector('input[type="text"]:nth-child(2)').value;
                const surname = this.querySelector('input[type="text"]:nth-child(3)').value;
                const email = this.querySelector('input[type="email"]').value;
                const password = this.querySelector('input[type="password"]').value;
                
                if (name && surname && email && password) {
                    // Kayıt işlemi için loading göster
                    const submitButton = this.querySelector('button.sign-up-btn');
                    const originalText = submitButton.textContent;
                    submitButton.textContent = 'Kayıt Yapılıyor...';
                    submitButton.disabled = true;
                    
                    try {
                        console.log('Attempting registration with email:', email);
                        
                        // Kayıt için register-with-cookie endpoint'ine istek gönder
                        const responseData = await apiRequest('http://69.62.120.202:8080/auth/register-with-cookie', 'POST', {
                            name: name,
                            email: email,
                            password: password
                        });
                        
                        console.log('Registration successful!');
                        
                        // JWT token'ı yanıttan al
                        if (responseData && responseData.token) {
                            // Token'ı localStorage'a kaydet
                            localStorage.setItem('jwt_token', responseData.token);
                            console.log('JWT token saved to localStorage');
                        }
                        
                        // Kullanıcı bilgilerini localStorage'a kaydet
                        localStorage.setItem('userEmail', email);
                        localStorage.setItem('userName', name);
                        localStorage.setItem('userSurname', surname);
                        localStorage.setItem('isLoggedIn', 'true');
                        
                        // Sign In/Up butonunu Profile butonu ile değiştir
                        updateNavButtons();
                        
                        // Modalı kapat
                        closeSignInModal();
                        
                        // Mevcut URL'yi kontrol et
                        const currentPath = window.location.pathname;
                        
                        // Eğer anasayfadaysak, upload sayfasına yönlendir
                        if (currentPath.includes('index.html') || currentPath.endsWith('/') || currentPath.endsWith('/Web/')) {
                            window.location.href = 'public/pages/upload.html';
                        } else {
                            // Diğer sayfalarda sayfayı yenile
                            window.location.reload();
                        }
                    } catch (error) {
                        console.error('Register error:', error);
                        alert('Kayıt başarısız: ' + (error.message || 'Lütfen bilgilerinizi kontrol edin.'));
                    } finally {
                        // Loading durumunu kaldır
                        submitButton.textContent = originalText;
                        submitButton.disabled = false;
                    }
                } else {
                    alert('Lütfen tüm alanları doldurun!');
                }
            });
        }
    }
    
    // Sayfa yüklendiğinde giriş durumunu kontrol et
    checkLoginStatus();

    // Adım kartları için animasyon sınıfları
    const stepCards = document.querySelectorAll('.step-card');
    stepCards.forEach((card, index) => {
        card.classList.add('fade-in-card');
        card.style.opacity = '0';
        card.style.transform = 'translateY(30px)';
        card.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        
        // Kademeli animasyon için gecikme ekle
        setTimeout(() => {
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, 300 + (index * 200));
    });
    
    // Fast and Simple başlığı için animasyon
    const fastSimple = document.querySelector('.fast-simple');
    if (fastSimple) {
        fastSimple.style.opacity = '0';
        fastSimple.style.transform = 'translateY(20px)';
        fastSimple.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        
        setTimeout(() => {
            fastSimple.style.opacity = '1';
            fastSimple.style.transform = 'translateY(0)';
        }, 200);
    }
});

// Giriş durumunu kontrol et ve butonları güncelle
function checkLoginStatus() {
    // JWT token'ı ve localStorage'dan giriş durumunu kontrol et
    const token = localStorage.getItem('jwt_token');
    const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
    console.log('Checking login status, isLoggedIn:', isLoggedIn, 'token exists:', !!token);
    
    if (isLoggedIn && token) {
        // Token'ın geçerliliğini kontrol et (isteğe bağlı)
        validateToken()
            .then(isValid => {
                if (isValid) {
                    // Token geçerli, profil butonunu güncelle
                    updateNavButtons();
                    
                    // Mobil menüde AnalyzingCV butonunun geçiş animasyonunu ayarla
                    const navGroup = document.querySelector('.nav-links .nav-group');
                    if (navGroup) {
                        const analyzingBtn = navGroup.querySelector('a[data-analyzing-cv]');
                        if (analyzingBtn) {
                            // Mobil menüde AnalyzingCV butonu için transition delay ekle
                            analyzingBtn.style.transitionDelay = '0.2s';
                        }
                    }
                    
                    // Aktif sayfayı kontrol et ve AnalyzingCV butonunu aktif yap
                    const currentPath = window.location.pathname;
                    if (currentPath.includes('upload.html')) {
                        const navLinks = document.querySelectorAll('.nav-links a');
                        navLinks.forEach(link => {
                            if (link.getAttribute('data-analyzing-cv') === 'true') {
                                link.classList.add('active');
                            } else {
                                link.classList.remove('active');
                            }
                        });
                    }
                } else {
                    // Token geçersiz, çıkış yap
                    logout();
                }
            })
            .catch(error => {
                console.error('Token validation error:', error);
                // Hata durumunda çıkış yap
                logout();
            });
    } else if (isLoggedIn) {
        // Token yok ama isLoggedIn true ise, profil butonunu güncelle
        // (Token cookie olarak saklanıyor olabilir)
        updateNavButtons();
    }
}

// Token'ın geçerliliğini kontrol et
async function validateToken() {
    try {
        // Token doğrulama endpoint'ine istek gönder
        const response = await apiRequest('http://69.62.120.202:8080/auth/validate', 'GET');
        return true; // Başarılı yanıt alındıysa token geçerli
    } catch (error) {
        console.error('Token validation error:', error);
        return false; // Hata durumunda token geçersiz
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
            let uploadPath = '';
            
            // Eğer ana sayfadaysak
            if (currentPath.includes('index.html') || currentPath.endsWith('/') || currentPath.endsWith('/Web/')) {
                profilePath = 'public/pages/profile.html';
                uploadPath = 'public/pages/upload.html';
            } 
            // Eğer zaten pages klasöründeysek
            else if (currentPath.includes('/pages/')) {
                profilePath = 'profile.html';
                uploadPath = 'upload.html';
            }
            // Diğer durumlar için
            else {
                profilePath = 'public/pages/profile.html';
                uploadPath = 'public/pages/upload.html';
            }
            
            profileBtn.onclick = function() {
                window.location.href = profilePath;
            };
            
            navButtons.replaceChild(profileBtn, signInBtn);
            
            // AnalyzingCV butonunu nav-group içine ekle
            const navGroup = document.querySelector('.nav-links .nav-group');
            if (navGroup) {
                // Önce mevcut bir AnalyzingCV butonu var mı kontrol et
                const existingAnalyzingBtn = navGroup.querySelector('a[data-analyzing-cv]');
                if (!existingAnalyzingBtn) {
                    const analyzingBtn = document.createElement('a');
                    analyzingBtn.href = uploadPath;
                    analyzingBtn.textContent = 'AnalyzingCV';
                    analyzingBtn.setAttribute('data-analyzing-cv', 'true');
                    
                    // Aktif sayfayı kontrol et
                    if (currentPath.includes('upload.html')) {
                        analyzingBtn.classList.add('active');
                        // Diğer butonlardan active sınıfını kaldır
                        navGroup.querySelectorAll('a').forEach(link => {
                            if (link !== analyzingBtn) {
                                link.classList.remove('active');
                            }
                        });
                    }
                    
                    navGroup.appendChild(analyzingBtn);
                }
            }
        }
    }
}

// Mobil menü kurulumu
function setupMobileMenu() {
    const menuIcon = document.querySelector('.menu-icon');
    const navLinks = document.querySelector('.nav-links');
    const mobileButtons = document.querySelector('.mobile-buttons');
    const navGroup = document.querySelector('.nav-links .nav-group');
    
    if (menuIcon && navLinks) {
        // Mobil menü sınıfını ekle
        navLinks.classList.add('mobile-menu');
        
        // Menü ikonuna tıklama olayı ekle
        menuIcon.addEventListener('click', () => {
            menuIcon.classList.toggle('active');
            navLinks.classList.toggle('active');
            
            // Mobil butonları göster/gizle
            if (mobileButtons) {
                if (navLinks.classList.contains('active')) {
                    mobileButtons.classList.add('active');
                    setTimeout(() => {
                        mobileButtons.style.display = 'flex';
                    }, 100);
                } else {
                    mobileButtons.classList.remove('active');
                    setTimeout(() => {
                        mobileButtons.style.display = 'none';
                    }, 400);
                }
            }
            
            // Nav grup animasyonu
            if (navGroup) {
                if (navLinks.classList.contains('active')) {
                    navGroup.style.opacity = '0';
                    navGroup.style.transform = 'scale(0.9)';
                    setTimeout(() => {
                        navGroup.style.opacity = '1';
                        navGroup.style.transform = 'scale(1)';
                    }, 100);
                }
            }
            
            // Sayfa kaydırmayı engelle/serbest bırak
            if (navLinks.classList.contains('active')) {
                document.body.style.overflow = 'hidden';
            } else {
                document.body.style.overflow = '';
            }
        });
        
        // Sayfa yüklendiğinde mobil butonları gizle
        if (mobileButtons) {
            mobileButtons.style.display = 'none';
            mobileButtons.classList.remove('active');
            
            // Mobil butonlara tıklama olayları ekle
            const mobileSignInBtn = mobileButtons.querySelector('.sign-in-btn');
            if (mobileSignInBtn) {
                mobileSignInBtn.addEventListener('click', () => {
                    openSignInModal();
                    // Mobil menüyü kapat
                    navLinks.classList.remove('active');
                    menuIcon.classList.remove('active');
                    document.body.style.overflow = '';
                    mobileButtons.classList.remove('active');
                    setTimeout(() => {
                        mobileButtons.style.display = 'none';
                    }, 400);
                });
            }
        }
        
        // Nav grup stillerini ayarla
        if (navGroup) {
            navGroup.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
        }
        
        // Menü linklerine tıklandığında menüyü kapat
        const navLinkElements = navLinks.querySelectorAll('a');
        navLinkElements.forEach(link => {
            link.addEventListener('click', () => {
                navLinks.classList.remove('active');
                menuIcon.classList.remove('active');
                document.body.style.overflow = '';
                
                if (mobileButtons) {
                    mobileButtons.classList.remove('active');
                    setTimeout(() => {
                        mobileButtons.style.display = 'none';
                    }, 400);
                }
            });
        });
        
        // Kullanıcı giriş yapmışsa AnalyzingCV butonunun animasyonunu ayarla
        const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
        if (isLoggedIn) {
            const analyzingBtn = navGroup.querySelector('a[data-analyzing-cv]');
            if (analyzingBtn) {
                // Mobil menü animasyonlarını AnalyzingCV butonu için de ayarla
                analyzingBtn.style.opacity = '0';
                analyzingBtn.style.transform = 'translateY(-20px)';
                
                // Aktif sınıfı ekle
                navLinks.addEventListener('transitionend', () => {
                    if (navLinks.classList.contains('active')) {
                        setTimeout(() => {
                            analyzingBtn.style.opacity = '0.9';
                            analyzingBtn.style.transform = 'translateY(0)';
                        }, 200);
                    }
                });
            }
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

// Sign In Modal Functions
function openSignInModal() {
    const modal = document.getElementById('signInModal');
    if (modal) {
        modal.style.display = 'block';
    }
}

function closeSignInModal() {
    const modal = document.getElementById('signInModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

// Get Started butonuna tıklandığında kullanıcının giriş durumuna göre farklı davranacak fonksiyon
function handleGetStarted() {
    const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
    
    if (isLoggedIn) {
        // Kullanıcı giriş yapmışsa upload sayfasına yönlendir
        window.location.href = 'public/pages/upload.html';
    } else {
        // Kullanıcı giriş yapmamışsa giriş modalını aç
        openSignInModal();
    }
}

// Logout function
async function logout() {
    try {
        console.log('Attempting to logout...');
        
        // Backend'e çıkış isteği gönder
        try {
            await apiRequest('http://69.62.120.202:8080/auth/logout', 'POST');
            console.log('Logout request successful');
        } catch (error) {
            console.error('Logout request error:', error);
            // Hata olsa bile devam et
        }
        
        // Cookie'leri temizle
        document.cookie.split(";").forEach(function(c) {
            document.cookie = c.replace(/^ +/, "").replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/");
        });
        
        // localStorage'dan kullanıcı bilgilerini ve token'ı temizle
        localStorage.removeItem('jwt_token');
        localStorage.removeItem('isLoggedIn');
        localStorage.removeItem('userEmail');
        localStorage.removeItem('userName');
        localStorage.removeItem('userSurname');
        
        console.log('Logout successful!');
        
        // Ana sayfaya yönlendir
        const currentPath = window.location.pathname;
        if (currentPath.includes('/pages/')) {
            window.location.href = '../../index.html';
        } else {
            window.location.href = 'index.html';
        }
    } catch (error) {
        console.error('Logout error:', error);
        alert('Çıkış yapılırken bir hata oluştu. Lütfen tekrar deneyin.');
    }
}

// API istekleri için yardımcı fonksiyon
async function apiRequest(url, method, data) {
    try {
        // JWT token'ı al
        const token = localStorage.getItem('jwt_token');
        
        console.log(`Sending ${method} request to ${url} with data:`, data);
        
        // İstek ayarlarını oluştur
        const requestOptions = {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
                'Access-Control-Allow-Credentials': 'true'
            },
            credentials: 'include', // Cookie'leri gönder
            mode: 'cors' // CORS modunu açıkça belirt
        };
        
        // Token varsa Authorization header'ı ekle
        if (token) {
            requestOptions.headers['Authorization'] = `Bearer ${token}`;
        }
        
        // Data varsa body'ye ekle
        if (data) {
            requestOptions.body = JSON.stringify(data);
        }
        
        // İsteği gönder
        const response = await fetch(url, requestOptions);
        
        console.log(`Response status: ${response.status}`);
        
        // Yanıt metnini al
        let responseText = '';
        try {
            responseText = await response.text();
            console.log('Response text:', responseText);
        } catch (textError) {
            console.error('Error reading response text:', textError);
        }
        
        // Yanıt başarısız ise hata fırlat
        if (!response.ok) {
            let errorMessage = `API request failed: ${response.status}`;
            
            // Yanıt metni varsa ve JSON ise parse et
            if (responseText) {
                try {
                    const errorData = JSON.parse(responseText);
                    if (errorData.message) {
                        errorMessage = errorData.message;
                    }
                } catch (jsonError) {
                    // JSON parse edilemezse yanıt metnini kullan
                    if (responseText.length < 100) { // Çok uzun değilse
                        errorMessage += ` - ${responseText}`;
                    }
                }
            }
            
            throw new Error(errorMessage);
        }
        
        // Yanıt boş ise boş bir obje döndür
        if (!responseText || responseText.trim() === '') {
            return {};
        }
        
        // Yanıt JSON ise parse et
        try {
            const responseData = JSON.parse(responseText);
            return responseData;
        } catch (jsonError) {
            console.log('Response is not JSON, returning as text');
            return { message: responseText };
        }
    } catch (error) {
        console.error('API request error:', error);
        throw error;
    }
} 