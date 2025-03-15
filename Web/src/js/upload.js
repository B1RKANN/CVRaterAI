// Drag and drop işlevselliği
const uploadBox = document.querySelector('.upload-box');
const uploadBtn = document.querySelector('.upload-btn');

uploadBox.addEventListener('dragover', (e) => {
    e.preventDefault();
    uploadBox.style.borderColor = '#1a5cc7';
});

uploadBox.addEventListener('dragleave', () => {
    uploadBox.style.borderColor = 'rgba(255, 255, 255, 0.8)';
});

uploadBox.addEventListener('drop', (e) => {
    e.preventDefault();
    uploadBox.style.borderColor = 'rgba(255, 255, 255, 0.8)';
    const files = e.dataTransfer.files;
    handleFiles(files);
});

uploadBtn.addEventListener('click', () => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.pdf,.doc,.docx';
    input.onchange = (e) => {
        const files = e.target.files;
        handleFiles(files);
    };
    input.click();
});

function handleFiles(files) {
    // Burada dosya yükleme işlemlerini gerçekleştirebilirsiniz
    console.log('Yüklenen dosyalar:', files);
}

// Analyze butonu işlevselliği
const analyzeBtn = document.querySelector('.analyze-btn');

analyzeBtn.addEventListener('click', () => {
    // Analiz işlemlerini burada gerçekleştirebilirsiniz
    console.log('CV analizi başlatıldı');
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