import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
} from "react";

import { getCurrentUser } from "../api/authApi";

import {
  getAccessToken,
  getStoredUser,
  removeAccessToken,
  removeStoredUser,
  setStoredUser,
} from "../utils/storage";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => getStoredUser());
  const [loading, setLoading] = useState(() => Boolean(getAccessToken()));

  const authVersion = useRef(0);

  const updateUser = useCallback((nextUser) => {
    authVersion.current += 1;

    setUser(nextUser);

    if (nextUser) {
      setStoredUser(nextUser);
    } else {
      removeStoredUser();
    }

    setLoading(false);
  }, []);

  const logout = useCallback(() => {
    authVersion.current += 1;

    removeAccessToken();
    removeStoredUser();

    setUser(null);
    setLoading(false);
  }, []);

  useEffect(() => {
    const token = getAccessToken();

    if (!token) {
      setUser(null);
      setLoading(false);
      return;
    }

    const requestVersion = authVersion.current;
    let cancelled = false;

    const restoreSession = async () => {
      try {
        const response = await getCurrentUser();

        if (cancelled || requestVersion !== authVersion.current) {
          return;
        }

        const currentUser = response?.data ?? response;

        if (currentUser) {
          setUser(currentUser);
          setStoredUser(currentUser);
        }

        setLoading(false);
      } catch (error) {
        if (cancelled || requestVersion !== authVersion.current) {
          return;
        }

        const status = error?.response?.status;

        if (status === 401) {
          removeAccessToken();
          removeStoredUser();
          setUser(null);
        } else {
          const storedUser = getStoredUser();

          if (storedUser) {
            setUser(storedUser);
          }
        }

        setLoading(false);
      }
    };

    restoreSession();

    return () => {
      cancelled = true;
    };
  }, []);

  const value = {
    user,
    loading,
    isAuthenticated: Boolean(getAccessToken()),
    updateUser,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuthContext() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuthContext must be used inside AuthProvider.");
  }

  return context;
}
