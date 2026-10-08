import { Link } from "react-router-dom";
import toast from "react-hot-toast";

import ThemeSwitcher from "./ThemeSwitcher";
import Button from "../common/Button";
import useAuth from "../../hooks/useAuth";

function Header() {
  const { user, logout } = useAuth();

  const handleLogout = async () => {
    await logout();
    toast.success("Logged out successfully");
  };

  return (
    <header
      className="
        flex
        min-h-16
        items-center
        justify-between
        border-b
        px-4
        md:px-6
      "
      style={{
        backgroundColor: "var(--bg-card)",
        borderColor: "var(--border)",
      }}
    >
      <div>
        <p
          className="text-sm"
          style={{
            color: "var(--text-muted)",
          }}
        >
          Welcome back
        </p>

        <p
          className="font-semibold"
          style={{
            color: "var(--text-primary)",
          }}
        >
          {user?.name || user?.username || "User"}
        </p>
      </div>

      <div className="flex items-center gap-2">
        <Link
          to="/notifications"
          aria-label="Notifications"
          title="Notifications"
          className="theme-icon-button"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.8"
            className="h-5 w-5"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M14.857 17.082a23.848 23.848 0 0 0 5.454-1.31A8.967 8.967 0 0 1 18 9.75V9a6 6 0 0 0-12 0v.75a8.967 8.967 0 0 1-2.31 6.022 23.848 23.848 0 0 0 5.454 1.31m5.713 0a24.255 24.255 0 0 1-5.713 0m5.713 0a3 3 0 1 1-5.713 0"
            />
          </svg>
        </Link>

        <Link
          to="/profile"
          aria-label="Profile"
          title="Profile"
          className="theme-icon-button"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.8"
            className="h-5 w-5"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M15.75 6.75a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.5 20.118a7.5 7.5 0 0 1 15 0A17.933 17.933 0 0 1 12 21.75a17.933 17.933 0 0 1-7.5-1.632Z"
            />
          </svg>
        </Link>

        <ThemeSwitcher />

        <Button variant="ghost" onClick={handleLogout}>
          Logout
        </Button>
      </div>
    </header>
  );
}

export default Header;
