// Common animations for all pages
document.addEventListener('DOMContentLoaded', () => {
    // Wait a short time to ensure the page is fully loaded
    setTimeout(() => {
        // Intersection Observer for fade-in animations
        const observerOptions = {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px'
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        // Apply animations to main content sections
        const mainSections = document.querySelectorAll('main > div, .features-section, .pricing-container, .loading-content, .results-flex-container, .container, .user-details, .credit-section');
        mainSections.forEach(element => {
            element.style.opacity = '0';
            element.style.transform = 'translateY(20px)';
            element.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
            observer.observe(element);
        });

        // Apply staggered animations to headings
        const headings = document.querySelectorAll('h1, h2, h3, .subtitle');
        headings.forEach((element, index) => {
            element.style.opacity = '0';
            element.style.transform = 'translateY(20px)';
            element.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
            element.style.transitionDelay = `${Math.min(index * 0.1, 0.5)}s`;
            observer.observe(element);
        });

        // Apply staggered animations to paragraphs
        const paragraphs = document.querySelectorAll('p, .info-item, .detail-row');
        paragraphs.forEach((element, index) => {
            element.style.opacity = '0';
            element.style.transform = 'translateY(20px)';
            element.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
            element.style.transitionDelay = `${Math.min(0.2 + index * 0.1, 0.7)}s`;
            observer.observe(element);
        });

        // Apply staggered animations to buttons
        const buttons = document.querySelectorAll('.hero-buttons button, .get-started-btn, .buy-now, .analyze-btn, .upload-btn');
        buttons.forEach((element, index) => {
            element.style.opacity = '0';
            element.style.transform = 'translateY(20px)';
            element.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
            element.style.transitionDelay = `${Math.min(0.3 + index * 0.1, 0.8)}s`;
            observer.observe(element);
        });

        // Apply staggered animations to cards and other elements
        const cards = document.querySelectorAll('.feature-card, .pricing-card, .step-card, .skills-container, .user-info, .details-card, .credit-card, .upload-box, .form-section, .input-group, .step, .skill-item, .compatibility');
        cards.forEach((element, index) => {
            element.style.opacity = '0';
            element.style.transform = 'translateY(20px)';
            element.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
            element.style.transitionDelay = `${Math.min(0.1 + (index % 5) * 0.1, 0.6)}s`;
            observer.observe(element);
        });
    }, 100); // Short delay to ensure DOM is fully processed
}); 