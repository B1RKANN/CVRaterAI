document.addEventListener('DOMContentLoaded', () => {
    // Hamburger butonu ve sidebar
    const hamburgerBtn = document.querySelector('.hamburger-btn');
    const sidebar = document.getElementById('sidebar');
    
    // Hamburger butonuna tıklama olayı
    if (hamburgerBtn && sidebar) {
        hamburgerBtn.addEventListener('click', function() {
            // Toggle active class
            this.classList.toggle('active');
            sidebar.classList.toggle('active');
            
            // Sayfada scroll'u engelle veya serbest bırak
            if (sidebar.classList.contains('active')) {
                document.body.style.overflow = 'hidden';
            } else {
                document.body.style.overflow = '';
            }
        });
        
        // Sidebar dışına tıklanınca kapat
        document.addEventListener('click', function(event) {
            if (sidebar.classList.contains('active')) {
                // Tıklama sidebar veya hamburger butonu dışında mı?
                if (!sidebar.contains(event.target) && !hamburgerBtn.contains(event.target)) {
                    sidebar.classList.remove('active');
                    hamburgerBtn.classList.remove('active');
                    document.body.style.overflow = '';
                }
            }
        });
    }

    // Button hover effects
    const buttons = document.querySelectorAll('button:not(.hamburger-btn)');
    buttons.forEach(button => {
        button.addEventListener('mouseover', () => {
            if (button.classList.contains('profile-btn')) {
                button.style.backgroundColor = '#f0f0f0';
            } else if (button.classList.contains('download-btn')) {
                button.style.backgroundColor = '#002d70';
            }
        });
        
        button.addEventListener('mouseout', () => {
            if (button.classList.contains('profile-btn')) {
                button.style.backgroundColor = 'white';
            } else if (button.classList.contains('download-btn')) {
                button.style.backgroundColor = '#003B93';
            }
        });
    });

    // History item hover effects
    const historyItems = document.querySelectorAll('.history-item');
    historyItems.forEach(item => {
        item.addEventListener('mouseover', () => {
            item.style.backgroundColor = 'rgba(255, 255, 255, 0.1)';
        });
        
        item.addEventListener('mouseout', () => {
            item.style.backgroundColor = 'transparent';
        });
    });
    
    // Kullanıcı bilgilerini göster
    displayUserInfo();
    
    // Giriş durumunu kontrol et
    checkLoginStatus();
});

// Kullanıcı bilgilerini göster
function displayUserInfo() {
    const userEmail = localStorage.getItem('userEmail');
    if (userEmail) {
        const userEmailElement = document.getElementById('userEmail');
        if (userEmailElement) {
            userEmailElement.textContent = userEmail;
        }
    } else {
        // Kullanıcı giriş yapmamışsa ana sayfaya yönlendir
        window.location.href = '../../index.html';
    }
}

// Giriş durumunu kontrol et
function checkLoginStatus() {
    const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
    if (!isLoggedIn) {
        // Kullanıcı giriş yapmamışsa ana sayfaya yönlendir
        window.location.href = '../../index.html';
    }
}

// Çıkış yapma fonksiyonu
function logout() {
    localStorage.removeItem('isLoggedIn');
    localStorage.removeItem('userEmail');
    window.location.href = '../../index.html';
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