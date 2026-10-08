import apiClient from "./axios";

function unwrap(response) {
  return response?.data?.data ?? response?.data ?? response;
}

export async function createSettlement(payload) {
  const response = await apiClient.post("/api/v1/settlements", payload);

  return unwrap(response);
}

export async function getSettlementById(id) {
  const response = await apiClient.get(`/api/v1/settlements/${id}`);

  return unwrap(response);
}

export async function getSettlementsByGroup(groupId) {
  const response = await apiClient.get(`/api/v1/settlements/group/${groupId}`);

  return unwrap(response);
}

export async function getSettlementsByUser(userId) {
  const response = await apiClient.get(`/api/v1/settlements/user/${userId}`);

  return unwrap(response);
}
