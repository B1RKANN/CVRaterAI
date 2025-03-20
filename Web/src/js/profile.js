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
    
    // Kullanıcı profil verilerini getir
    fetchUserProfile();
    
    // CV değerlendirme geçmişini getir
    fetchCVEvaluationHistory();
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

// Kullanıcı profil verilerini API'den getir
async function fetchUserProfile() {
    try {
        // JWT token'dan kullanıcı ID'sini çıkar
        const token = localStorage.getItem('jwt_token');
        if (!token) {
            console.error('Token bulunamadı');
            return;
        }
        
        // Token'ı decode et ve user ID'yi al
        const userId = getUserIdFromToken(token);
        if (!userId) {
            console.error('Token içinden kullanıcı ID\'si alınamadı');
            return;
        }
        
        console.log('Kullanıcı ID:', userId);
        
        // API'ye istek gönder
        const response = await fetch(`http://69.62.120.202:8080/api/v1/profile/${userId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        
        const data = await response.json();
        
        if (data.status === 200 && data.payload) {
            // Kullanıcı verilerini HTML'e yansıt
            updateUserProfile(data.payload);
        } else {
            console.error('Profil verileri alınamadı:', data.errorMessage);
        }
    } catch (error) {
        console.error('Profil verilerini getirme hatası:', error);
    }
}

// JWT token'dan user ID çıkarma
function getUserIdFromToken(token) {
    try {
        // Token'ın payload kısmını al (ikinci kısım)
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));

        // Payload'ı JSON olarak parse et
        const payload = JSON.parse(jsonPayload);
        console.log('Token payload:', payload);
        
        // JWT içindeki id'yi bul - backend yapısına göre farklı alanlar kontrol edilmeli
        // Burada sadece user id'sini döndürmek istiyoruz, email değil
        return payload.userId || payload.user_id || payload.id || payload.sub;
    } catch (error) {
        console.error('Token decode hatası:', error);
        return null;
    }
}

// Kullanıcı profilini güncelleme
function updateUserProfile(userData) {
    // Kullanıcı adı güncelleme
    const userNameElement = document.getElementById('userName');
    if (userNameElement) {
        userNameElement.textContent = userData.name;
    }
    
    // Email güncelleme
    const userEmailElement = document.getElementById('userEmail');
    if (userEmailElement) {
        userEmailElement.textContent = userData.email;
    }
    
    // Plan türü güncelleme
    const planElement = document.querySelector('.plan');
    if (planElement) {
        planElement.textContent = userData.planType;
    }
    
    // Kullanım kredisi güncelleme
    const usageElement = document.querySelector('.usage');
    if (usageElement) {
        usageElement.textContent = `${userData.userCredit}/20`;
    }
    
    // Progress bar güncelleme
    const progressElement = document.querySelector('.progress');
    if (progressElement) {
        // Maksimum kredi 20 olduğu için, yüzdeliği hesaplayalım
        const percentage = (userData.userCredit / 20) * 100;
        progressElement.style.width = `${percentage}%`;
    }
    
    // Kullanıcı verilerini localStorage'a kaydedelim
    localStorage.setItem('userName', userData.name);
    localStorage.setItem('userEmail', userData.email);
    localStorage.setItem('userCredit', userData.userCredit);
    localStorage.setItem('planType', userData.planType);
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

// CV değerlendirme geçmişini getir
async function fetchCVEvaluationHistory() {
    try {
        // JWT token'dan kullanıcı ID'sini çıkar
        const token = localStorage.getItem('jwt_token');
        if (!token) {
            console.error('Token bulunamadı');
            return;
        }
        
        // Token'ı decode et ve user ID'yi al
        const userId = getUserIdFromToken(token);
        if (!userId) {
            console.error('Token içinden kullanıcı ID\'si alınamadı');
            return;
        }
        
        // API'ye istek gönder
        const response = await fetch(`http://69.62.120.202:8080/api/v1/cv-evaluation/user/${userId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        
        const evaluations = await response.json();
        
        if (Array.isArray(evaluations)) {
            // Değerlendirme ID'lerini localStorage'a kaydet
            const evaluationIds = evaluations.map(eval => eval.id);
            localStorage.setItem('evaluationIds', JSON.stringify(evaluationIds));
            
            // Sidebar'ı güncelle
            updateSidebarHistory(evaluations);
        } else {
            console.error('CV değerlendirme geçmişi alınamadı');
        }
    } catch (error) {
        console.error('CV değerlendirme geçmişi getirme hatası:', error);
    }
}

// Sidebar geçmişini güncelle
function updateSidebarHistory(evaluations) {
    const sidebar = document.getElementById('sidebar');
    if (!sidebar) return;
    
    // Sidebar'ı temizle
    sidebar.innerHTML = '';
    
    // Değerlendirmeleri gruplara ayır
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);
    const lastWeek = new Date(today);
    lastWeek.setDate(lastWeek.getDate() - 7);
    const lastMonth = new Date(today);
    lastMonth.setDate(lastMonth.getDate() - 30);
    
    // Zaman gruplarını oluştur
    const timeGroups = {
        today: { title: 'Today', items: [] },
        yesterday: { title: 'Yesterday', items: [] },
        lastWeek: { title: 'Last 7 Days', items: [] },
        lastMonth: { title: 'Last 30 Days', items: [] }
    };
    
    // Her değerlendirmeyi uygun gruba ekle
    evaluations.forEach(evaluation => {
        const historyItem = document.createElement('div');
        historyItem.className = 'history-item';
        historyItem.textContent = evaluation.fullName;
        historyItem.dataset.evaluationId = evaluation.id; // ID'yi data attribute olarak sakla
        
        // Hover efektlerini ekle
        historyItem.addEventListener('mouseover', () => {
            historyItem.style.backgroundColor = 'rgba(255, 255, 255, 0.1)';
        });
        
        historyItem.addEventListener('mouseout', () => {
            historyItem.style.backgroundColor = 'transparent';
        });
        
        // Tüm değerlendirmeleri şimdilik Today grubuna ekle
        // Not: Gerçek tarih bilgisi API'den gelmediği için hepsini Today'de gösteriyoruz
        timeGroups.today.items.push(historyItem);
    });
    
    // Grupları sidebar'a ekle
    Object.values(timeGroups).forEach(group => {
        if (group.items.length > 0) {
            const groupDiv = document.createElement('div');
            groupDiv.className = 'time-group';
            
            const groupTitle = document.createElement('h3');
            groupTitle.textContent = group.title;
            groupDiv.appendChild(groupTitle);
            
            group.items.forEach(item => {
                groupDiv.appendChild(item);
            });
            
            sidebar.appendChild(groupDiv);
        }
    });
} 