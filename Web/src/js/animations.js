// Common animations for all pages
document.addEventListener('DOMContentLoaded', () => {
    // Kesinlikle mobil butonların gizli olduğundan emin ol
    const mobileButtons = document.querySelector('.mobile-buttons');
    if (mobileButtons) {
        mobileButtons.style.display = 'none';
        mobileButtons.style.opacity = '0';
    }
    
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

        // Special animation for pricing page title and subtitle
        const pricingTitle = document.querySelector('.pricing-container h1');
        const pricingSubtitle = document.querySelector('.pricing-container .subtitle');
        
        if (pricingTitle) {
            pricingTitle.style.opacity = '0';
            pricingTitle.style.transform = 'translateY(30px)';
            pricingTitle.style.transition = 'opacity 0.7s ease, transform 0.7s ease';
            
            setTimeout(() => {
                pricingTitle.style.opacity = '1';
                pricingTitle.style.transform = 'translateY(0)';
            }, 300);
        }
        
        if (pricingSubtitle) {
            pricingSubtitle.style.opacity = '0';
            pricingSubtitle.style.transform = 'translateY(30px)';
            pricingSubtitle.style.transition = 'opacity 0.7s ease, transform 0.7s ease';
            
            setTimeout(() => {
                pricingSubtitle.style.opacity = '1';
                pricingSubtitle.style.transform = 'translateY(0)';
            }, 500);
        }
        
        // Special animation for features page title and subtitle
        const featuresTitle = document.querySelector('.features-section h1');
        const featuresSubtitle = document.querySelector('.features-section .subtitle');
        
        if (featuresTitle) {
            featuresTitle.style.opacity = '0';
            featuresTitle.style.transform = 'translateY(30px)';
            featuresTitle.style.transition = 'opacity 0.7s ease, transform 0.7s ease';
            
            setTimeout(() => {
                featuresTitle.style.opacity = '1';
                featuresTitle.style.transform = 'translateY(0)';
            }, 300);
        }
        
        if (featuresSubtitle) {
            featuresSubtitle.style.opacity = '0';
            featuresSubtitle.style.transform = 'translateY(30px)';
            featuresSubtitle.style.transition = 'opacity 0.7s ease, transform 0.7s ease';
            
            setTimeout(() => {
                featuresSubtitle.style.opacity = '1';
                featuresSubtitle.style.transform = 'translateY(0)';
            }, 500);
        }
        
        // Special animation for profile page title and subtitle
        const profileTitles = document.querySelectorAll('.profile-section h1, .profile-section h2, .user-info h2');
        
        if (profileTitles.length > 0) {
            profileTitles.forEach((title, index) => {
                title.style.opacity = '0';
                title.style.transform = 'translateY(30px)';
                title.style.transition = 'opacity 0.7s ease, transform 0.7s ease';
                
                setTimeout(() => {
                    title.style.opacity = '1';
                    title.style.transform = 'translateY(0)';
                }, 300 + (index * 200));
            });
        }

        // Apply staggered animations to headings (except those we've just given special animations)
        const headings = document.querySelectorAll('h1:not(.pricing-container h1):not(.features-section h1):not(.profile-section h1):not(.profile-section h2):not(.user-info h2), h3, .subtitle:not(.pricing-container .subtitle):not(.features-section .subtitle)');
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
        const cards = document.querySelectorAll('.details-card, .credit-card, .upload-box, .form-section, .input-group, .step, .skill-item, .compatibility');
        cards.forEach((element, index) => {
            element.style.opacity = '0';
            element.style.transform = 'translateY(20px)';
            element.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
            element.style.transitionDelay = `${Math.min(0.1 + (index % 5) * 0.1, 0.6)}s`;
            observer.observe(element);
        });
        
        // Special animation for feature cards - similar to step cards in home page
        const featureCards = document.querySelectorAll('.feature-card');
        if (featureCards.length > 0) {
            featureCards.forEach((card, index) => {
                card.classList.add('fade-in-card');
                card.style.opacity = '0';
                card.style.transform = 'translateY(30px)';
                card.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
                
                // Kademeli animasyon için gecikme ekle
                setTimeout(() => {
                    card.style.opacity = '1';
                    card.style.transform = 'translateY(0)';
                }, 300 + (index * 200));
            });
        }
        
        // Special animation for pricing cards - similar to step cards in home page
        const pricingCards = document.querySelectorAll('.pricing-card');
        if (pricingCards.length > 0) {
            pricingCards.forEach((card, index) => {
                card.classList.add('fade-in-card');
                card.style.opacity = '0';
                card.style.transform = 'translateY(30px)';
                card.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
                
                // Kademeli animasyon için gecikme ekle
                setTimeout(() => {
                    card.style.opacity = '1';
                    card.style.transform = 'translateY(0)';
                }, 300 + (index * 200));
            });
        }
        
        // Home page step cards animation
        const stepCards = document.querySelectorAll('.step-card');
        if (stepCards.length > 0) {
            stepCards.forEach((card, index) => {
                card.classList.add('fade-in-card');
                card.style.opacity = '0';
                card.style.transform = 'translateY(30px)';
                card.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
                
                // Kademeli animasyon için gecikme ekle
                setTimeout(() => {
                    card.style.opacity = '1';
                    card.style.transform = 'translateY(0)';
                }, 300 + (index * 200));
            });
        }
    }, 50); // Daha kısa bir süre
}); 