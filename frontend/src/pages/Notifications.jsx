import { useEffect, useState } from "react";
import toast from "react-hot-toast";

import Button from "../components/common/Button";

import {
  getUserNotifications,
  markAllNotificationsAsRead,
  markNotificationAsRead,
} from "../api/notificationApi";

import useAuth from "../hooks/useAuth";

function Notifications() {
  const { user } = useAuth();

  const [notifications, setNotifications] = useState([]);

  const [loading, setLoading] = useState(true);

  const loadNotifications = async () => {
    if (!user?.id) return;

    try {
      setLoading(true);

      const response = await getUserNotifications(user.id);

      setNotifications(Array.isArray(response) ? response : []);
    } catch (error) {
      toast.error(
        error.response?.data?.message || "Failed to load notifications."
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadNotifications();
  }, [user?.id]);

  const handleRead = async (id) => {
    try {
      const updated = await markNotificationAsRead(id);

      setNotifications((previous) =>
        previous.map((notification) =>
          notification.id === id
            ? updated || {
                ...notification,
                read: true,
              }
            : notification
        )
      );
    } catch (error) {
      toast.error(
        error.response?.data?.message || "Unable to mark notification as read."
      );
    }
  };

  const handleReadAll = async () => {
    if (!user?.id) return;

    try {
      await markAllNotificationsAsRead(user.id);

      setNotifications((previous) =>
        previous.map((notification) => ({
          ...notification,
          read: true,
        }))
      );

      toast.success("All notifications marked as read.");
    } catch (error) {
      toast.error(
        error.response?.data?.message ||
          "Unable to mark all notifications as read."
      );
    }
  };

  const unreadCount = notifications.filter(
    (notification) => !notification.read
  ).length;

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white">
            Notifications
          </h1>

          <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
            Expense activity and updates.
          </p>
        </div>

        {unreadCount > 0 && (
          <Button variant="secondary" onClick={handleReadAll}>
            Mark all as read
          </Button>
        )}
      </div>

      {loading ? (
        <div className="rounded-xl border border-slate-200 bg-white p-6 text-sm text-slate-500 dark:border-slate-800 dark:bg-slate-900">
          Loading notifications...
        </div>
      ) : notifications.length === 0 ? (
        <div className="rounded-xl border border-dashed border-slate-300 bg-white p-8 text-center dark:border-slate-700 dark:bg-slate-900">
          <p className="font-medium text-slate-900 dark:text-white">
            No notifications
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          {notifications.map((notification) => (
            <div
              key={notification.id}
              className={`rounded-xl border p-5 ${
                notification.read
                  ? "border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900"
                  : "border-slate-300 bg-slate-50 dark:border-slate-700 dark:bg-slate-800"
              }`}
            >
              <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="font-semibold text-slate-900 dark:text-white">
                      {notification.title}
                    </h3>

                    {!notification.read && (
                      <span className="rounded-full bg-slate-800 px-2 py-0.5 text-xs text-white dark:bg-slate-200 dark:text-slate-900">
                        New
                      </span>
                    )}
                  </div>

                  <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">
                    {notification.message}
                  </p>

                  <p className="mt-2 text-xs text-slate-400">
                    {notification.type}
                    {notification.createdAt
                      ? ` · ${new Date(
                          notification.createdAt
                        ).toLocaleString()}`
                      : ""}
                  </p>
                </div>

                {!notification.read && (
                  <Button
                    variant="secondary"
                    onClick={() => handleRead(notification.id)}
                  >
                    Mark read
                  </Button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default Notifications;
