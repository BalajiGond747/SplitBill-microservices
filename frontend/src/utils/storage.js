export const getAccessToken = () => {
  return localStorage.getItem("splitbill_access_token");
};

export const setAccessToken = (token) => {
  localStorage.setItem("splitbill_access_token", token);
};

export const removeAccessToken = () => {
  localStorage.removeItem("splitbill_access_token");
};

export const getStoredUser = () => {
  const user = localStorage.getItem("splitbill_user");

  if (!user) {
    return null;
  }

  try {
    return JSON.parse(user);
  } catch {
    return null;
  }
};

export const setStoredUser = (user) => {
  localStorage.setItem("splitbill_user", JSON.stringify(user));
};

export const removeStoredUser = () => {
  localStorage.removeItem("splitbill_user");
};

export const clearAuthStorage = () => {
  localStorage.removeItem("splitbill_access_token");
  localStorage.removeItem("splitbill_user");
};
