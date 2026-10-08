import apiClient from "./axios";

function unwrap(response) {
  return response?.data?.data ?? response?.data ?? response;
}

export async function createPaymentOrder(payload) {
  const response = await apiClient.post("/api/v1/payments/orders", payload);

  return unwrap(response);
}

export async function verifyPayment(payload) {
  const response = await apiClient.post("/api/v1/payments/verify", payload);

  return unwrap(response);
}

export async function getPaymentById(id) {
  const response = await apiClient.get(`/api/v1/payments/${id}`);

  return unwrap(response);
}

export async function getPaymentsByUser(userId) {
  const response = await apiClient.get(`/api/v1/payments/user/${userId}`);

  return unwrap(response);
}

export async function getPaymentsByGroup(groupId) {
  const response = await apiClient.get(`/api/v1/payments/group/${groupId}`);

  return unwrap(response);
}
