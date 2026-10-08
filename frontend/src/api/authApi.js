import apiClient from "./axios";

function unwrap(response) {
  return response?.data ?? response;
}

export async function register(payload) {
  const response = await apiClient.post("/api/v1/auth/register", payload);

  return unwrap(response);
}

export async function login(payload) {
  const response = await apiClient.post("/api/v1/auth/login", payload);

  return unwrap(response);
}

export async function getCurrentUser() {
  const response = await apiClient.get("/api/v1/auth/me");

  return unwrap(response);
}

export async function updateProfile(payload) {
  const response = await apiClient.put("/api/v1/auth/me", payload);

  return unwrap(response);
}

export async function changePassword(payload) {
  const response = await apiClient.post(
    "/api/v1/auth/change-password",
    payload
  );

  return unwrap(response);
}

export async function logout() {
  const response = await apiClient.post("/api/v1/auth/logout");

  return unwrap(response);
}

export async function googleLogin(credential) {
  const response = await apiClient.post("/api/v1/auth/google", {
    credential,
  });

  return unwrap(response);
}
