document.addEventListener('DOMContentLoaded', function() {
    // Dosya yüklenmiş mi kontrol et
    const fileUploaded = localStorage.getItem('cvFileUploaded') === 'true';
    
    if (!fileUploaded) {
        // Dosya yüklenmemişse upload sayfasına yönlendir
        alert('Lütfen önce bir CV dosyası yükleyin.');
        window.location.href = 'upload.html';
        return;
    }
    
    // Kullanıcı bilgilerini doldur
    const fileName = localStorage.getItem('cvFileName');
    if (fileName) {
        // Kullanıcı adını dosya adından çıkar (örnek: "John_Doe_CV.pdf" -> "John")
        const nameMatch = fileName.split('_')[0] || fileName.split('.')[0];
        if (nameMatch) {
            const nameElement = document.querySelector('.info-item:nth-child(1) .info-value');
            if (nameElement) {
                nameElement.textContent = nameMatch;
            }
        }
    }
    
    // Aranan özellikleri doldur
    const requiredSkills = localStorage.getItem('requiredSkills');
    if (requiredSkills) {
        const skillsElement = document.querySelector('.skills-info .info-value');
        if (skillsElement) {
            // Aranan özellikleri virgülle ayırıp paragraf olarak ekle
            const skillsArray = requiredSkills.split(',');
            skillsElement.innerHTML = '';
            
            skillsArray.forEach(skill => {
                if (skill.trim()) {
                    const p = document.createElement('p');
                    p.textContent = skill.trim() + ',';
                    skillsElement.appendChild(p);
                }
            });
        }
    }
    
    // Beceri değerleri (görseldeki değerlerle tanımlanmış)
    const skills = {
        java: { target: 60, current: 0 },
        html: { target: 40, current: 0 },
        css: { target: 35, current: 0 },
        python: { target: 90, current: 0 },
        cpp: { target: 55, current: 0 },
        csharp: { target: 75, current: 0 },
        php: { target: 10, current: 0 }
    };

    // Uyumluluk durumu (%60'a ayarlanacak)
    let compatibility = {
        target: 60,
        current: 60
    };

    // Beceri çubuğunu güncelleme
    function updateSkillBar(skill, value) {
        const bar = document.getElementById(`${skill}-bar`);
        const percent = document.getElementById(`${skill}-percent`);
        
        if (bar && percent) {
            bar.style.width = `${value}%`;
            percent.textContent = `%${value}`;
        }
    }

    // Uyumluluk çubuğunu güncelleme
    function updateCompatibilityBar(value) {
        const bar = document.getElementById('compatibility-bar');
        
        if (bar) {
            bar.style.width = `${value}%`;
            
            // Yüzde göstergesini ekle veya güncelle
            let percentageDisplay = document.querySelector('.compatibility-percentage');
            if (!percentageDisplay) {
                percentageDisplay = document.createElement('div');
                percentageDisplay.className = 'compatibility-percentage';
                bar.parentElement.appendChild(percentageDisplay);
            }
            
            percentageDisplay.textContent = `%${value}`;
        }
    }

    // Sayfa yüklendiğinde
    function initPage() {
        // Programlama dillerinden 3 tanesini doldur
        const skillsToFill = ['python', 'csharp', 'java'];
        
        skillsToFill.forEach(skill => {
            updateSkillBar(skill, skills[skill].target);
        });
        
        // Compatibility barını doldur
        updateCompatibilityBar(compatibility.current);
    }
    
    // Sayfayı başlat
    initPage();
    
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