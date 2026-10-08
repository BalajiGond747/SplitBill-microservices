import apiClient from "./axios";

function unwrap(response) {
  return response?.data?.data ?? response?.data ?? response;
}

export async function getBalancesByGroup(groupId) {
  const response = await apiClient.get(`/api/v1/balances/group/${groupId}`);

  return unwrap(response);
}

export async function getBalancesByUser(userId) {
  const response = await apiClient.get(`/api/v1/balances/user/${userId}`);

  return unwrap(response);
}
