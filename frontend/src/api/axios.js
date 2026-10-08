import axios from "axios";

import { clearAuthStorage, getAccessToken } from "../utils/storage";

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",

  headers: {
    "Content-Type": "application/json",
  },
});

apiClient.interceptors.request.use(
  (config) => {
    const token = getAccessToken();

    if (token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

let redirectingToLogin = false;

apiClient.interceptors.response.use(
  (response) => {
    return response;
  },

  (error) => {
    const status = error.response?.status;

    const isLoginPage = window.location.pathname.includes("/login");

    if (status === 401 && !isLoginPage && !redirectingToLogin) {
      redirectingToLogin = true;

      clearAuthStorage();

      window.location.replace("/login");
    }

    return Promise.reject(error);
  }
);

export default apiClient;
