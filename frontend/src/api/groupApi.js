import apiClient from "./axios";

function unwrap(response) {
  return response?.data?.data ?? response?.data ?? response;
}

export async function getGroups(params = {}) {
  const response = await apiClient.get("/api/v1/groups", { params });

  return unwrap(response);
}

export async function getGroupById(id) {
  const response = await apiClient.get(`/api/v1/groups/${id}`);

  return unwrap(response);
}

export async function searchGroups(name, params = {}) {
  const response = await apiClient.get("/api/v1/groups/search", {
    params: {
      name,
      ...params,
    },
  });

  return unwrap(response);
}

export async function getGroupsByCreatedBy(userId, params = {}) {
  const response = await apiClient.get(`/api/v1/groups/created-by/${userId}`, {
    params,
  });

  return unwrap(response);
}

export async function getActiveGroups(params = {}) {
  const response = await apiClient.get("/api/v1/groups/active", { params });

  return unwrap(response);
}

export async function createGroup(payload) {
  const response = await apiClient.post("/api/v1/groups", payload);

  return unwrap(response);
}

export async function updateGroup(id, payload) {
  const response = await apiClient.put(`/api/v1/groups/${id}`, payload);

  return unwrap(response);
}

export async function deactivateGroup(id) {
  const response = await apiClient.patch(`/api/v1/groups/${id}/deactivate`);

  return unwrap(response);
}

export async function activateGroup(id) {
  const response = await apiClient.patch(`/api/v1/groups/${id}/activate`);

  return unwrap(response);
}

/*
 * Group members
 */

export async function getGroupMembers(groupId) {
  const response = await apiClient.get(`/api/v1/groups/${groupId}/members`);

  return unwrap(response);
}

export async function addGroupMember(groupId, userId) {
  const response = await apiClient.post(`/api/v1/groups/${groupId}/members`, {
    userId,
  });

  return unwrap(response);
}

export async function removeGroupMember(groupId, userId) {
  const response = await apiClient.patch(
    `/api/v1/groups/${groupId}/members/${userId}/deactivate`
  );

  return unwrap(response);
}
