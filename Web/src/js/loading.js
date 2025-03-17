document.addEventListener('DOMContentLoaded', function() {
    // Dosya yüklenmiş mi kontrol et
    const fileUploaded = localStorage.getItem('cvFileUploaded') === 'true';
    
    if (!fileUploaded) {
        // Dosya yüklenmemişse upload sayfasına yönlendir
        alert('Lütfen önce bir CV dosyası yükleyin.');
        window.location.href = 'upload.html';
        return;
    }
    
    // Yükleme animasyonu ve ilerleme çubuğu
    let progress = 0;
    const progressBar = document.querySelector('.progress-fill');
    const progressText = document.querySelector('.progress-text');
    const steps = document.querySelectorAll('.step');
    
    // İlk adımı aktif yap
    setTimeout(() => {
        steps[0].classList.add('active');
    }, 500);
    
    // İlerleme çubuğunu güncelle
    function updateProgress(value) {
        progress = value;
        progressBar.style.width = `${progress}%`;
        progressText.textContent = `${progress}%`;
    }
    
    // Adımları güncelle
    function updateStep(stepIndex, status) {
        steps.forEach((step, index) => {
            // Önceki adımları tamamlandı olarak işaretle
            if (index < stepIndex) {
                step.classList.remove('active');
                step.classList.add('completed');
            }
            // Mevcut adımı aktif yap
            else if (index === stepIndex) {
                step.classList.remove('completed');
                step.classList.add('active');
            }
            // Sonraki adımları normal duruma getir
            else {
                step.classList.remove('active');
                step.classList.remove('completed');
            }
        });
    }
    
    // Simüle edilmiş yükleme süreci
    const simulateLoading = () => {
        // 1. Adım: CV verilerini çıkarma (0-20%)
        setTimeout(() => {
            updateStep(0, 'active');
            
            const interval1 = setInterval(() => {
                if (progress < 20) {
                    updateProgress(progress + 1);
                } else {
                    clearInterval(interval1);
                    
                    // 2. Adım: Becerileri analiz etme (20-40%)
                    updateStep(1, 'active');
                    
                    const interval2 = setInterval(() => {
                        if (progress < 40) {
                            updateProgress(progress + 1);
                        } else {
                            clearInterval(interval2);
                            
                            // 3. Adım: Deneyimi değerlendirme (40-60%)
                            updateStep(2, 'active');
                            
                            const interval3 = setInterval(() => {
                                if (progress < 60) {
                                    updateProgress(progress + 1);
                                } else {
                                    clearInterval(interval3);
                                    
                                    // 4. Adım: Uyumluluğu hesaplama (60-80%)
                                    updateStep(3, 'active');
                                    
                                    const interval4 = setInterval(() => {
                                        if (progress < 80) {
                                            updateProgress(progress + 1);
                                        } else {
                                            clearInterval(interval4);
                                            
                                            // 5. Adım: Rapor oluşturma (80-100%)
                                            updateStep(4, 'active');
                                            
                                            const interval5 = setInterval(() => {
                                                if (progress < 100) {
                                                    updateProgress(progress + 1);
                                                } else {
                                                    clearInterval(interval5);
                                                    
                                                    // Yükleme tamamlandı, sonuç sayfasına yönlendir
                                                    setTimeout(() => {
                                                        window.location.href = 'result.html';
                                                    }, 1000);
                                                }
                                            }, 100);
                                        }
                                    }, 100);
                                }
                            }, 100);
                        }
                    }, 100);
                }
            }, 100);
        }, 1000);
    };
    
    // Yükleme işlemini başlat
    simulateLoading();
    
    // Scroll effect for balloons
    window.addEventListener('scroll', () => {
        const scrollY = window.scrollY;
        const balloons = document.querySelectorAll('.balloon');
        
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
    
    // Giriş durumunu kontrol et
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