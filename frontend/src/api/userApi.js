import apiClient from "./axios";

function unwrap(response) {
  return response?.data?.data ?? response?.data ?? response;
}

export async function getUsers(params = {}) {
  const response = await apiClient.get("/api/v1/users", {
    params,
  });

  return unwrap(response);
}
