/**
 * GovLICA Auth Module
 * JWT token management + nav bar updates
 */
const Auth = {
    TOKEN_KEY: 'govlica_token',
    USER_KEY: 'govlica_user',

    getToken() {
        return localStorage.getItem(this.TOKEN_KEY);
    },

    getUser() {
        const raw = localStorage.getItem(this.USER_KEY);
        return raw ? JSON.parse(raw) : null;
    },

    setAuth(token, user) {
        localStorage.setItem(this.TOKEN_KEY, token);
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    },

    clear() {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
    },

    isLoggedIn() {
        return !!this.getToken();
    },

    logout() {
        this.clear();
        location.href = '/';
    },

    /**
     * Authenticated fetch wrapper — adds Authorization header if token exists
     */
    async fetch(url, options = {}) {
        const token = this.getToken();
        if (token) {
            options.headers = options.headers || {};
            options.headers['Authorization'] = 'Bearer ' + token;
        }
        return fetch(url, options);
    },

    /**
     * Update the nav bar with login/user info
     */
    updateNav() {
        const navLinks = document.querySelector('.nav-links');
        if (!navLinks) return;

        // Remove existing auth elements
        navLinks.querySelectorAll('.auth-nav').forEach(el => el.remove());

        if (this.isLoggedIn()) {
            const user = this.getUser();
            const nickname = user ? user.nickname : 'User';

            const mypage = document.createElement('a');
            mypage.href = '/mypage';
            mypage.className = 'auth-nav';
            mypage.textContent = nickname;

            const logoutBtn = document.createElement('a');
            logoutBtn.href = '#';
            logoutBtn.className = 'auth-nav';
            logoutBtn.textContent = 'Logout';
            logoutBtn.onclick = (e) => {
                e.preventDefault();
                Auth.logout();
            };

            navLinks.appendChild(mypage);
            navLinks.appendChild(logoutBtn);
        } else {
            const loginLink = document.createElement('a');
            loginLink.href = '/user/login';
            loginLink.className = 'auth-nav';
            loginLink.textContent = 'Login';
            navLinks.appendChild(loginLink);
        }
    }
};

// Auto-update nav on page load
document.addEventListener('DOMContentLoaded', () => Auth.updateNav());
