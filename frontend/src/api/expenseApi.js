import apiClient from "./axios";

function unwrap(response) {
  return response?.data?.data ?? response?.data ?? response;
}

export async function getExpenseById(id) {
  const response = await apiClient.get(`/api/v1/expenses/${id}`);

  return unwrap(response);
}

export async function getExpensesByGroup(groupId, params = {}) {
  const response = await apiClient.get(`/api/v1/expenses/group/${groupId}`, {
    params: {
      page: 0,
      size: 100,
      sortBy: "expenseDate",
      sortDirection: "DESC",
      ...params,
    },
  });

  return unwrap(response);
}

export async function createExpense(payload) {
  const response = await apiClient.post("/api/v1/expenses", payload);

  return unwrap(response);
}

export async function updateExpense(id, payload) {
  const response = await apiClient.put(`/api/v1/expenses/${id}`, payload);

  return unwrap(response);
}

export async function deleteExpense(id) {
  const response = await apiClient.delete(`/api/v1/expenses/${id}`);

  return unwrap(response);
}
