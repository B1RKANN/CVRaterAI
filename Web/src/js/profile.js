document.addEventListener('DOMContentLoaded', () => {
    const menuIcon = document.querySelector('.menu-icon');
    const sidebar = document.getElementById('sidebar');

    // Toggle sidebar
    menuIcon.addEventListener('click', () => {
        menuIcon.classList.toggle('active');
        sidebar.classList.toggle('active');
    });

    // Button hover effects
    const buttons = document.querySelectorAll('button');
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
}); 