import apiClient from "./axios";

function unwrap(response) {
  return response?.data?.data ?? response?.data ?? response;
}

export async function getUserNotifications(userId) {
  const response = await apiClient.get(`/api/v1/notifications/user/${userId}`);

  return unwrap(response);
}

export async function getUnreadNotifications(userId) {
  const response = await apiClient.get(
    `/api/v1/notifications/user/${userId}/unread`
  );

  return unwrap(response);
}

export async function markNotificationAsRead(notificationId) {
  const response = await apiClient.patch(
    `/api/v1/notifications/${notificationId}/read`
  );

  return unwrap(response);
}

export async function markAllNotificationsAsRead(userId) {
  const response = await apiClient.patch(
    `/api/v1/notifications/user/${userId}/read-all`
  );

  return unwrap(response);
}
