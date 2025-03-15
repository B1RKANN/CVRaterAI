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
}

// Analyze butonu işlevselliği
analyzeBtn.addEventListener('click', () => {
    if (file) {
        // Yükleme ekranına yönlendir
        window.location.href = 'loading.html';
    } else {
        alert('Lütfen önce bir CV dosyası yükleyin.');
    }
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

document.addEventListener('DOMContentLoaded', function() {
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