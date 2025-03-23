document.addEventListener('DOMContentLoaded', function() {
    // API yanıtını kontrol et
    const evaluationResultStr = localStorage.getItem('evaluationResult');
    if (!evaluationResultStr) {
        // Sonuç bulunamadıysa upload sayfasına yönlendir
        alert('Değerlendirme sonucu bulunamadı. Lütfen tekrar CV yükleyin.');
        window.location.href = 'upload.html';
        return;
    }

    try {
        const evaluationResult = JSON.parse(evaluationResultStr);
        const resultData = JSON.parse(evaluationResult.evaluationResult);

        // Kullanıcı bilgilerini doldur
        const userInfo = resultData.userInformation;
        
        // Label'lara göre doğru elementleri bul
        const infoItems = document.querySelectorAll('.info-item');
        infoItems.forEach(item => {
            const label = item.querySelector('.info-label');
            const value = item.querySelector('.info-value');
            
            if (label && value) {
                const labelText = label.textContent.trim();
                
                switch(labelText) {
                    case 'Name :':
                        if (userInfo.name) value.textContent = userInfo.name;
                        break;
                    case 'Surname :':
                        if (userInfo.surname) value.textContent = userInfo.surname;
                        break;
                    case 'Email :':
                        if (userInfo.email) value.textContent = userInfo.email;
                        break;
                    case 'Phone Number :':
                        if (userInfo.phone) value.textContent = userInfo.phone;
                        break;
                    case 'Skills :':
                        if (userInfo.skills) {
                            value.innerHTML = ''; // Mevcut içeriği temizle
                            userInfo.skills.split(',').forEach(skill => {
                                if (skill.trim()) {
                                    const p = document.createElement('p');
                                    p.textContent = skill.trim();
                                    value.appendChild(p);
                                }
                            });
                        }
                        break;
                }
            }
        });

        // Compatibility bar'ı doldur
        const compatibilityBar = document.getElementById('compatibility-bar');
        if (compatibilityBar) {
            const compatibilityScore = evaluationResult.evaluationScore;
            compatibilityBar.style.width = `${compatibilityScore}%`;
            
            // Compatibility yüzdesini başlık ile bar arasına göster
            const percentageDisplay = document.createElement('div');
            percentageDisplay.className = 'compatibility-percentage-header';
            percentageDisplay.textContent = `${compatibilityScore}%`;
            
            const compatibilitySection = document.querySelector('.compatibility');
            const compatibilityContainer = document.querySelector('.compatibility-container');
            
            if (compatibilitySection && compatibilityContainer) {
                compatibilitySection.insertBefore(percentageDisplay, compatibilityContainer);
            }
        }

        // Skill barlarını doldur
        const skillsContainer = document.querySelector('.skills-container');
        if (skillsContainer && resultData.skillRatings) {
            skillsContainer.innerHTML = ''; // Mevcut içeriği temizle

            resultData.skillRatings.forEach(skill => {
                if (skill.language && typeof skill.percentage === 'number') {
                    const skillItem = document.createElement('div');
                    skillItem.className = 'skill-item';
                    skillItem.innerHTML = `
                        <span class="skill-name">${skill.language}</span>
                        <div class="skill-bar-container">
                            <div class="skill-bar" style="width: ${skill.percentage}%"></div>
                        </div>
                        <span class="skill-percent">%${skill.percentage}</span>
                    `;
                    skillsContainer.appendChild(skillItem);
                }
            });
        }

    } catch (error) {
        console.error('Sonuç verisi işlenirken hata:', error);
        console.error('Hata detayları:', error.stack);
        alert('Sonuç verisi işlenirken bir hata oluştu. Detaylar için konsolu kontrol edin.');
    }

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
            const profileBtn = document.createElement('button');
            profileBtn.className = 'sign-in-btn';
            profileBtn.textContent = 'PROFILE';
            
            const currentPath = window.location.pathname;
            let profilePath = '';
            
            if (currentPath.includes('index.html') || currentPath.endsWith('/') || currentPath.endsWith('/Web/')) {
                profilePath = 'public/pages/profile.html';
            } 
            else if (currentPath.includes('/pages/')) {
                profilePath = 'profile.html';
            }
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
    
    if (scrollY > 0) {
        balloons.forEach((balloon, index) => {
            const moveX = (index % 2 === 0) ? scrollY * 0.05 : -scrollY * 0.05;
            const moveY = (index % 3 === 0) ? scrollY * 0.03 : -scrollY * 0.02;
            balloon.style.transform = `translate(${moveX}px, ${moveY}px)`;
        });
    } else {
        balloons.forEach(balloon => {
            balloon.style.transform = 'translate(0, 0)';
        });
    }
});