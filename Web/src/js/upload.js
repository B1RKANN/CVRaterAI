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