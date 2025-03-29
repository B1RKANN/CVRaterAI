'use client';

import { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { useRouter } from 'next/navigation';
import Cookies from 'js-cookie';
import { jwtDecode } from 'jwt-decode';

// Token yapısı için interface
interface DecodedToken {
  exp: number;
  sub: string;
  userId: number;
  role: string;
  iat: number;
}

// Kullanıcı veri tipini tanımlama
interface User {
  userId: number;
  email: string;
  name: string;
  role: string;
}

// Context value tipini tanımlama
interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<boolean>;
  logout: () => void;
  getToken: () => Promise<string | null>;
}

// Context oluşturma
const AuthContext = createContext<AuthContextType | null>(null);

// Provider bileşeni
export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const router = useRouter();

  // Token'ın geçerli olup olmadığını kontrol etme
  const isTokenValid = (token: string): boolean => {
    try {
      const decodedToken = jwtDecode<DecodedToken>(token);
      // Token süresi dolmuş mu kontrol et (exp süresi saniye cinsinden)
      const currentTime = Math.floor(Date.now() / 1000);
      return decodedToken.exp > currentTime;
    } catch (error) {
      console.error('Token validation error:', error);
      return false;
    }
  };

  // Refresh token kullanarak yeni token alma
  const refreshToken = async (): Promise<boolean> => {
    try {
      const storedRefreshToken = Cookies.get('refreshToken') || localStorage.getItem('refreshToken');
      
      if (!storedRefreshToken) {
        throw new Error('No refresh token available');
      }

      const response = await fetch('/auth/v2/refreshToken-with-cookie', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          refreshToken: storedRefreshToken
        }),
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error(`Refresh token failed: ${response.status}`);
      }

      const data = await response.json();
      
      // Yeni tokenleri kaydet
      const { token, refreshToken: newRefreshToken, userId, email, name, role } = data;
      
      // Cookie ve localStorage'a kaydet
      Cookies.set('token', token, { path: '/', expires: 1/12 }); // 2 saat (1/12 gün)
      Cookies.set('refreshToken', newRefreshToken, { path: '/', expires: 7 }); // 7 gün
      
      // Yedek olarak localStorage'a da kaydet
      localStorage.setItem('token', token);
      localStorage.setItem('refreshToken', newRefreshToken);
      
      // Kullanıcı bilgilerini güncelle
      setUser({ userId, email, name, role });
      setIsAuthenticated(true);
      
      return true;
    } catch (error) {
      console.error('Token refresh error:', error);
      // Refresh token da geçersizse çıkış yap
      logout();
      return false;
    }
  };

  // Geçerli token almak için
  const getToken = async (): Promise<string | null> => {
    const token = Cookies.get('token') || localStorage.getItem('token');
    
    if (!token) {
      return null;
    }
    
    // Token geçerliliğini kontrol et
    if (isTokenValid(token)) {
      return token;
    }
    
    // Token geçersizse, refresh token ile yenilemeyi dene
    const isRefreshed = await refreshToken();
    if (isRefreshed) {
      return Cookies.get('token') || localStorage.getItem('token');
    }
    
    return null;
  };

  // Giriş fonksiyonu
  const login = async (email: string, password: string): Promise<boolean> => {
    setIsLoading(true);
    
    try {
      const response = await fetch('/auth/v2/authenticate-with-cookie', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
        credentials: 'include',
      });
      
      if (!response.ok) {
        throw new Error(`Login failed: ${response.status}`);
      }
      
      const data = await response.json();
      
      // Token ve kullanıcı bilgilerini kaydet
      const { token, refreshToken, userId, role, name } = data;
      
      // Cookie ve localStorage'a kaydet
      Cookies.set('token', token, { path: '/', expires: 1/12 }); // 2 saat (1/12 gün)
      Cookies.set('refreshToken', refreshToken, { path: '/', expires: 7 }); // 7 gün
      
      // Yedek olarak localStorage'a da kaydet
      localStorage.setItem('token', token);
      localStorage.setItem('refreshToken', refreshToken);
      
      // Kullanıcı bilgilerini güncelle
      setUser({ userId, email, name, role });
      setIsAuthenticated(true);
      
      return true;
    } catch (error) {
      console.error('Login error:', error);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  // Çıkış fonksiyonu
  const logout = () => {
    // Cookie ve localStorage'dan tokenleri temizle
    Cookies.remove('token');
    Cookies.remove('refreshToken');
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    
    // Kullanıcı durumunu sıfırla
    setUser(null);
    setIsAuthenticated(false);
    
    // Giriş sayfasına yönlendir
    router.push('/signin');
  };

  // İlk yüklemede token kontrolü
  useEffect(() => {
    const initAuth = async () => {
      setIsLoading(true);
      
      try {
        const token = await getToken();
        
        if (token) {
          // Token geçerliyse veya yenilendiyse kullanıcı bilgilerini ayarla
          const decodedToken = jwtDecode<DecodedToken>(token);
          
          // Kullanıcı bilgilerini API'den alabilir veya token içinden çıkarabilirsiniz
          // Burada basit olarak token içinden çıkarıyoruz
          setUser({
            userId: decodedToken.userId,
            email: decodedToken.sub,
            name: localStorage.getItem('userName') || 'User', // Bu bilgiyi daha önce kaydetmiş olmalısınız
            role: decodedToken.role
          });
          
          setIsAuthenticated(true);
        } else {
          // Token yoksa veya geçersizse state'i sıfırla
          setUser(null);
          setIsAuthenticated(false);
        }
      } catch (error) {
        console.error('Auth initialization error:', error);
        setUser(null);
        setIsAuthenticated(false);
      } finally {
        setIsLoading(false);
      }
    };
    
    initAuth();
  }, []);

  // Context değerlerini sağla
  const contextValue: AuthContextType = {
    user,
    isLoading,
    isAuthenticated,
    login,
    logout,
    getToken
  };

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
};

// Context hook
export const useAuth = () => {
  const context = useContext(AuthContext);
  
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  
  return context;
}; 