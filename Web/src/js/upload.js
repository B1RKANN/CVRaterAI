// Drag and drop işlevselliği
const uploadBox = document.querySelector('.upload-box');
const uploadBtn = document.querySelector('.upload-btn');
const analyzeBtn = document.querySelector('.analyze-btn');
let file = null;

uploadBox.addEventListener('dragover', (e) => {
    e.preventDefault();
    uploadBox.style.borderColor = '#1a5cc7';
    uploadBox.classList.add('drag-over');
});

uploadBox.addEventListener('dragleave', () => {
    uploadBox.style.borderColor = 'rgba(255, 255, 255, 0.8)';
    uploadBox.classList.remove('drag-over');
});

uploadBox.addEventListener('drop', (e) => {
    e.preventDefault();
    uploadBox.style.borderColor = 'rgba(255, 255, 255, 0.8)';
    uploadBox.classList.remove('drag-over');
    const files = e.dataTransfer.files;
    if (files.length > 0) {
        file = files[0];
        showFileName(file.name);
    }
});

uploadBtn.addEventListener('click', () => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.pdf,.doc,.docx';
    input.onchange = (e) => {
        const files = e.target.files;
        if (files.length > 0) {
            file = files[0];
            showFileName(file.name);
        }
    };
    input.click();
});

function showFileName(fileName) {
    const h2 = uploadBox.querySelector('h2');
    h2.textContent = fileName;
    uploadBox.classList.add('file-selected');
    analyzeBtn.classList.add('active');
    
    // Dosya yüklendiğinde localStorage'a bilgi kaydet
    localStorage.setItem('cvFileUploaded', 'true');
    localStorage.setItem('cvFileName', fileName);
}

// Analyze butonu işlevselliği
analyzeBtn.addEventListener('click', async () => {
    if (file) {
        try {
            // Loading modal'ı göster
            const loadingModal = document.querySelector('#loadingModal');
            if (loadingModal) {
                loadingModal.style.display = 'flex';
                
                // İlerleme çubuğunu ve adımları sıfırla
                const progressFill = document.querySelector('.progress-fill');
                if (progressFill) {
                    progressFill.style.width = '0%';
                }
                
                const steps = document.querySelectorAll('.step');
                steps.forEach(step => {
                    step.classList.remove('active', 'completed');
                });
            }
            
            // Token'dan user ID'yi al
            const token = localStorage.getItem('jwt_token');
            if (!token) {
                alert('Oturum süreniz dolmuş. Lütfen tekrar giriş yapın.');
                if (loadingModal) loadingModal.style.display = 'none';
                return;
            }

            // Token'ı decode et ve user ID'yi al
            const userId = getUserIdFromToken(token);
            if (!userId) {
                alert('Kullanıcı bilgisi alınamadı. Lütfen tekrar giriş yapın.');
                if (loadingModal) loadingModal.style.display = 'none';
                return;
            }

            // İlerleme adımları için animasyonlu gösterim
            // 1. Adım: CV Verilerini Çıkarma
            updateProgress(20, 0);
            
            // Form verilerini al
            const formData = new FormData();
            formData.append('file', file);
            
            const requiredSkills = document.querySelector('.form-section textarea').value;
            const githubLink = document.querySelector('.form-section input[type="text"]').value;
            
            formData.append('jobRequirements', requiredSkills);
            if (githubLink) {
                formData.append('githubUrl', githubLink);
            }
            
             // Form verilerinin localStorage'da kalmamasını sağla
            localStorage.removeItem('requiredSkills');
            localStorage.removeItem('githubLink');
  
            // Form verilerini localStorage'a kaydet
            localStorage.setItem('requiredSkills', requiredSkills);
            localStorage.setItem('githubLink', githubLink);

            // 2. Adım: Beceri Analizi
            setTimeout(() => updateProgress(40, 1), 1000);
            
            // 3. Adım: Deneyim Değerlendirmesi
            setTimeout(() => updateProgress(60, 2), 2000);

            // 4. Adım: Uyumluluk Hesaplama
            setTimeout(() => updateProgress(80, 3), 3000);

            // API'ye istek gönder
            const response = await fetch(`/api/v1/cv-evaluation/evaluate/${userId}`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                body: formData
            });

            if (!response.ok) {
                throw new Error('API yanıtı başarısız: ' + response.status);
            }

            const result = await response.json();
            
            // Sonucu localStorage'a kaydet
            localStorage.setItem('evaluationResult', JSON.stringify(result));
            
            // 5. Adım: Rapor Oluşturma
            updateProgress(100, 4);
            
            // Kısa bir gecikme sonrası sonuç sayfasına yönlendir
            setTimeout(() => {
                window.location.href = 'result.html';
            }, 1000);
            
        } catch (error) {
            console.error('CV değerlendirme hatası:', error);
            alert('CV değerlendirme sırasında bir hata oluştu. Lütfen tekrar deneyin.');
            
            // Hata durumunda loading modal'ı kapat
            const loadingModal = document.querySelector('#loadingModal');
            if (loadingModal) {
                loadingModal.style.display = 'none';
            }
        }
    } else {
        // Dosya yüklenmemişse uyarı göster
        alert('Lütfen önce bir CV dosyası yükleyin.');
        
        // Upload box'ı vurgula
        uploadBox.style.borderColor = '#ff3860';
        uploadBox.classList.add('error');
        
        // 2 saniye sonra normal haline getir
        setTimeout(() => {
            uploadBox.style.borderColor = 'rgba(255, 255, 255, 0.8)';
            uploadBox.classList.remove('error');
        }, 2000);
    }
});

// İlerleme çubuğu ve adım güncelleme fonksiyonu
function updateProgress(percentage, stepIndex) {
    const progressBar = document.querySelector('.progress-fill');
    if (progressBar) {
        progressBar.style.width = percentage + '%';
    }
    
    const progressText = document.querySelector('.progress-text');
    if (progressText) {
        progressText.textContent = percentage + '%';
    }
    
    const steps = document.querySelectorAll('.step');
    if (steps.length > 0) {
        // Önceki adımları tamamlandı olarak işaretle
        for (let i = 0; i < stepIndex; i++) {
            if (i < steps.length) {
                steps[i].classList.remove('active');
                steps[i].classList.add('completed');
            }
        }
        
        // Aktif adımı işaretle
        if (stepIndex < steps.length) {
            steps[stepIndex].classList.add('active');
        }
    }
}

// JWT token'dan user ID çıkarma
function getUserIdFromToken(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));

        const payload = JSON.parse(jsonPayload);
        return payload.userId || payload.user_id || payload.id || payload.sub;
    } catch (error) {
        console.error('Token decode hatası:', error);
        return null;
    }
}

// Sayfa yüklendiğinde dosya yükleme durumunu kontrol et
document.addEventListener('DOMContentLoaded', function() {
    // Giriş durumunu kontrol et
    checkLoginStatus();
    
    // Sayfayı temiz başlatmak için localStorage'daki CV ile ilgili bilgileri temizle
    localStorage.removeItem('cvFileUploaded');
    localStorage.removeItem('cvFileName');
    
    // Dosya yükleme alanını sıfırla
    const h2 = uploadBox.querySelector('h2');
    if (h2) {
        h2.textContent = 'Drag And Drop CV';
    }
    uploadBox.classList.remove('file-selected');
    analyzeBtn.classList.remove('active');
    
    // Form alanlarındaki default değerleri sıfırla
    document.querySelector('.form-section textarea').value = '';
    document.querySelector('.form-section input[type="text"]').value = '';
    
    // LocalStorage'daki ilgili değerleri temizle
    localStorage.removeItem('requiredSkills');
    localStorage.removeItem('githubLink');
});

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