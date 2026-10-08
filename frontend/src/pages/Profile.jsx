import { useEffect, useState } from "react";
import toast from "react-hot-toast";

import Button from "../components/common/Button";
import Input from "../components/common/Input";

import { changePassword, getCurrentUser, updateProfile } from "../api/authApi";

import useAuth from "../hooks/useAuth";

function Profile() {
  const { user, updateUser } = useAuth();

  const [profile, setProfile] = useState({
    name: "",
    email: "",
    username: "",
  });

  const [passwordForm, setPasswordForm] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  const [profileLoading, setProfileLoading] = useState(false);

  const [passwordLoading, setPasswordLoading] = useState(false);

  useEffect(() => {
    if (!user) return;

    setProfile({
      name: user.name || "",
      email: user.email || "",
      username: user.username || "",
    });
  }, [user]);

  const handleProfileChange = (event) => {
    setProfile((previous) => ({
      ...previous,
      [event.target.name]: event.target.value,
    }));
  };

  const handlePasswordChange = (event) => {
    setPasswordForm((previous) => ({
      ...previous,
      [event.target.name]: event.target.value,
    }));
  };

  const saveProfile = async (event) => {
    event.preventDefault();

    setProfileLoading(true);

    try {
      const updated = await updateProfile(profile);

      const updatedUser = updated?.user || updated?.data || updated;

      updateUser(updatedUser);

      toast.success("Profile updated successfully.");
    } catch (error) {
      toast.error(error.response?.data?.message || "Unable to update profile.");
    } finally {
      setProfileLoading(false);
    }
  };

  const savePassword = async (event) => {
    event.preventDefault();

    if (passwordForm.newPassword.length < 8) {
      toast.error("New password must be at least 8 characters.");
      return;
    }

    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      toast.error("New password and confirmation do not match.");
      return;
    }

    setPasswordLoading(true);

    try {
      await changePassword(passwordForm);

      setPasswordForm({
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      });

      toast.success("Password changed successfully.");
    } catch (error) {
      toast.error(
        error.response?.data?.message || "Unable to change password."
      );
    } finally {
      setPasswordLoading(false);
    }
  };

  const refreshProfile = async () => {
    try {
      const currentUser = await getCurrentUser();

      updateUser(currentUser);

      toast.success("Profile refreshed.");
    } catch {
      toast.error("Unable to refresh profile.");
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-white">
          Profile
        </h1>

        <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
          Manage your account information and password.
        </p>
      </div>

      <form
        onSubmit={saveProfile}
        className="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900"
      >
        <div className="mb-5 flex items-center justify-between">
          <h2 className="text-lg font-semibold text-slate-900 dark:text-white">
            Profile information
          </h2>

          <Button type="button" variant="ghost" onClick={refreshProfile}>
            Refresh
          </Button>
        </div>

        <div className="grid gap-4 md:grid-cols-2">
          <Input
            label="Name"
            name="name"
            value={profile.name}
            onChange={handleProfileChange}
            maxLength={100}
            required
          />

          <Input
            label="Email"
            name="email"
            type="email"
            value={profile.email}
            onChange={handleProfileChange}
            maxLength={150}
            required
          />

          <Input
            label="Username"
            name="username"
            value={profile.username}
            onChange={handleProfileChange}
            minLength={3}
            maxLength={50}
            required
          />
        </div>

        <div className="mt-4">
          <Button type="submit" loading={profileLoading}>
            Save profile
          </Button>
        </div>
      </form>

      <form
        onSubmit={savePassword}
        className="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900"
      >
        <h2 className="mb-5 text-lg font-semibold text-slate-900 dark:text-white">
          Change password
        </h2>

        <div className="space-y-4">
          <Input
            label="Current password"
            name="currentPassword"
            type="password"
            value={passwordForm.currentPassword}
            onChange={handlePasswordChange}
            autoComplete="current-password"
            required
          />

          <Input
            label="New password"
            name="newPassword"
            type="password"
            value={passwordForm.newPassword}
            onChange={handlePasswordChange}
            minLength={8}
            autoComplete="new-password"
            required
          />

          <Input
            label="Confirm new password"
            name="confirmPassword"
            type="password"
            value={passwordForm.confirmPassword}
            onChange={handlePasswordChange}
            minLength={8}
            autoComplete="new-password"
            required
          />
        </div>

        <div className="mt-4">
          <Button type="submit" loading={passwordLoading}>
            Change password
          </Button>
        </div>
      </form>

      <div className="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900">
        <h2 className="text-lg font-semibold text-slate-900 dark:text-white">
          Account
        </h2>

        <div className="mt-4 grid gap-4 text-sm md:grid-cols-2">
          <div>
            <p className="text-slate-500 dark:text-slate-400">Status</p>

            <p className="mt-1 font-medium text-slate-900 dark:text-white">
              {user?.active ? "Active" : "Inactive"}
            </p>
          </div>

          <div>
            <p className="text-slate-500 dark:text-slate-400">Username</p>

            <p className="mt-1 font-medium text-slate-900 dark:text-white">
              {user?.username || "—"}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Profile;
