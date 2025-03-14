document.addEventListener('DOMContentLoaded', function() {
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
});