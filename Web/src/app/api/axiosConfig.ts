import axios, { AxiosResponse, AxiosError, InternalAxiosRequestConfig } from 'axios';
import Cookies from 'js-cookie';

// API temel URL
const API_URL = '';

// Axios instance oluşturma
const axiosInstance = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // CORS cookie paylaşımı için gerekli
});

// İstek öncesi interceptor - Auth header ekleme
axiosInstance.interceptors.request.use(
  async (config: InternalAxiosRequestConfig) => {
    const token = Cookies.get('token') || localStorage.getItem('token');
    
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// Cevap interceptor - Token refresh handling
axiosInstance.interceptors.response.use(
  (response: AxiosResponse) => {
    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
    
    // Token hatası var ve daha önce retry edilmemiş ise
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        // Refresh token'ı al
        const refreshToken = Cookies.get('refreshToken') || localStorage.getItem('refreshToken');
        
        if (!refreshToken) {
          // Refresh token yoksa login sayfasına yönlendir
          window.location.href = '/signin';
          return Promise.reject(error);
        }
        
        // Yeni token alma isteği
        const response = await axios.post(
          `${API_URL}/auth/v2/refreshToken-with-cookie`,
          { refreshToken },
          { 
            headers: { 'Content-Type': 'application/json' },
            withCredentials: true
          }
        );
        
        if (response.status === 200) {
          // Yeni tokenları kaydet
          const { token, refreshToken: newRefreshToken } = response.data;
          
          // Cookie ve localStorage'a kaydet
          Cookies.set('token', token, { path: '/', expires: 1/12 }); // 2 saat (1/12 gün)
          Cookies.set('refreshToken', newRefreshToken, { path: '/', expires: 7 }); // 7 gün
          
          // Yedek olarak localStorage'a da kaydet
          localStorage.setItem('token', token);
          localStorage.setItem('refreshToken', newRefreshToken);
          
          // Axios instances için auth header'ı güncelle
          axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
          
          // Önceki isteği tekrarla
          return axiosInstance(originalRequest);
        }
      } catch (refreshError) {
        console.error('Token refresh failed:', refreshError);
        
        // Token yenilenemezse oturumu kapat
        Cookies.remove('token');
        Cookies.remove('refreshToken');
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        
        // Login sayfasına yönlendir
        window.location.href = '/signin';
      }
    }
    
    return Promise.reject(error);
  }
);

export default axiosInstance; 