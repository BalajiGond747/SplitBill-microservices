import { Link, useLocation, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import toast from "react-hot-toast";

import Button from "../../components/common/Button";
import Input from "../../components/common/Input";

import { login, googleLogin, getCurrentUser } from "../../api/authApi";

import {
  setAccessToken,
  setStoredUser,
  clearAuthStorage,
} from "../../utils/storage";

import { useAuthContext } from "../../context/AuthContext";

function loadGoogleIdentityScript() {
  return new Promise((resolve, reject) => {
    if (window.google?.accounts?.id) {
      resolve();
      return;
    }

    const existingScript = document.querySelector(
      'script[src="https://accounts.google.com/gsi/client"]'
    );

    if (existingScript) {
      existingScript.addEventListener("load", () => resolve(), {
        once: true,
      });

      existingScript.addEventListener(
        "error",
        () => reject(new Error("Unable to load Google Identity Services.")),
        { once: true }
      );

      return;
    }

    const script = document.createElement("script");

    script.src = "https://accounts.google.com/gsi/client";
    script.async = true;
    script.defer = true;

    script.onload = () => resolve();

    script.onerror = () =>
      reject(new Error("Unable to load Google Identity Services."));

    document.head.appendChild(script);
  });
}

function Login() {
  const navigate = useNavigate();
  const location = useLocation();

  const { updateUser } = useAuthContext();

  const [form, setForm] = useState({
    username: "",
    password: "",
  });

  const [errors, setErrors] = useState({
    username: "",
    password: "",
  });

  const [accountNotFound, setAccountNotFound] = useState(false);
  const [loading, setLoading] = useState(false);
  const [googleLoading, setGoogleLoading] = useState(false);

  const completeLogin = async (authData) => {
    if (!authData?.accessToken) {
      throw new Error("Login response did not contain an access token.");
    }

    setAccessToken(authData.accessToken);

    try {
      const response = await getCurrentUser();

      const currentUser = response?.data ?? response;

      if (!currentUser?.id) {
        throw new Error("Unable to verify authenticated user.");
      }

      setStoredUser(currentUser);
      updateUser(currentUser);

      const destination = location.state?.from?.pathname || "/dashboard";

      navigate(destination, {
        replace: true,
      });

      return currentUser;
    } catch (error) {
      clearAuthStorage();
      throw error;
    }
  };

  const handleGoogleLogin = async (response) => {
    if (!response?.credential) {
      toast.error("Google login failed. No credential received.");
      setGoogleLoading(false);
      return;
    }

    try {
      setGoogleLoading(true);

      const responseData = await googleLogin(response.credential);

      const authData = responseData?.data ?? responseData;

      await completeLogin(authData);

      toast.success("Google login successful");
    } catch (error) {
      console.error("Google login failed:", error);

      const message = error.response?.data?.message || "Google login failed.";

      toast.error(message);
    } finally {
      setGoogleLoading(false);
    }
  };

  const openGoogleLogin = () => {
    if (!window.google?.accounts?.id) {
      toast.error("Google login is not available yet. Please try again.");
      return;
    }

    setGoogleLoading(true);

    window.google.accounts.id.prompt((notification) => {
      if (notification.isNotDisplayed() || notification.isSkippedMoment()) {
        setGoogleLoading(false);

        toast.error("Google account selection could not be opened.");
      }
    });
  };

  useEffect(() => {
    let cancelled = false;

    const initializeGoogle = async () => {
      try {
        await loadGoogleIdentityScript();

        if (cancelled) {
          return;
        }

        const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;

        if (!clientId) {
          console.error("VITE_GOOGLE_CLIENT_ID is not configured.");
          return;
        }

        if (!window.google?.accounts?.id) {
          console.error("Google Identity Services is unavailable.");
          return;
        }

        window.google.accounts.id.initialize({
          client_id: clientId,
          callback: handleGoogleLogin,
          auto_select: false,
        });
      } catch (error) {
        console.error("Google Identity Services initialization failed:", error);
      }
    };

    initializeGoogle();

    return () => {
      cancelled = true;
    };
  }, []);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));

    setErrors((previous) => ({
      ...previous,
      [name]: "",
    }));

    setAccountNotFound(false);
  };

  const validate = () => {
    const validationErrors = {
      username: "",
      password: "",
    };

    if (!form.username.trim()) {
      validationErrors.username = "Username is required.";
    }

    if (!form.password) {
      validationErrors.password = "Password is required.";
    }

    setErrors(validationErrors);

    return !validationErrors.username && !validationErrors.password;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    setAccountNotFound(false);

    if (!validate()) {
      return;
    }

    setLoading(true);

    try {
      clearAuthStorage();

      const response = await login({
        username: form.username.trim(),
        password: form.password,
      });

      const authData = response?.data ?? response;

      await completeLogin(authData);

      toast.success("Login successful");
    } catch (error) {
      console.error("Login failed:", error);

      const status = error.response?.status;

      const message =
        error.response?.data?.message || error.message || "Login failed.";

      if (status === 404) {
        setAccountNotFound(true);

        toast.error("Account doesn't exist. Please register.");

        return;
      }

      if (status === 401) {
        setErrors((previous) => ({
          ...previous,
          password: "Incorrect password.",
        }));

        return;
      }

      if (status === 403) {
        toast.error(
          "Login was rejected by the server. Please check the authentication service."
        );

        return;
      }

      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 px-4 dark:bg-slate-950">
      <div className="w-full max-w-md">
        <div className="rounded-2xl border border-slate-200 bg-white p-8 shadow-sm dark:border-slate-800 dark:bg-slate-900">
          <div className="mb-8 text-center">
            <h1 className="text-3xl font-bold text-slate-900 dark:text-white">
              SplitBill
            </h1>

            <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">
              Sign in to manage your shared expenses
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-5" noValidate>
            <Input
              label="Username"
              name="username"
              value={form.username}
              onChange={handleChange}
              placeholder="Enter your username"
              autoComplete="username"
              error={errors.username}
            />

            <Input
              label="Password"
              name="password"
              type="password"
              value={form.password}
              onChange={handleChange}
              placeholder="Enter your password"
              autoComplete="current-password"
              error={errors.password}
            />

            {accountNotFound && (
              <div className="rounded-lg border border-red-200 bg-red-50 p-4 dark:border-red-900 dark:bg-red-950/40">
                <p className="text-sm text-red-700 dark:text-red-300">
                  This account doesn't exist.
                </p>

                <Link
                  to="/register"
                  className="mt-2 inline-block text-sm font-semibold text-red-700 underline hover:text-red-900 dark:text-red-300 dark:hover:text-red-200"
                >
                  Create an account
                </Link>
              </div>
            )}

            <Button
              type="submit"
              loading={loading}
              disabled={loading}
              className="w-full"
            >
              Sign in
            </Button>
          </form>

          <div className="my-6 flex items-center gap-3">
            <div className="h-px flex-1 bg-slate-200 dark:bg-slate-700" />

            <span className="text-xs text-slate-400">OR</span>

            <div className="h-px flex-1 bg-slate-200 dark:bg-slate-700" />
          </div>

          <button
            type="button"
            onClick={openGoogleLogin}
            disabled={googleLoading}
            className="flex h-11 w-full items-center justify-center gap-3 rounded-lg border border-slate-300 bg-white px-4 text-sm font-semibold text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60 dark:border-slate-700 dark:bg-slate-800 dark:text-white dark:hover:bg-slate-700"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" aria-hidden="true">
              <path
                fill="#4285F4"
                d="M21.35 12.23c0-.79-.07-1.55-.2-2.27H12v4.3h5.24a4.48 4.48 0 0 1-1.94 2.94v2.45h3.14c1.84-1.69 2.91-4.18 2.91-7.42Z"
              />
              <path
                fill="#34A853"
                d="M12 21.6c2.63 0 4.84-.87 6.45-2.35l-3.14-2.45c-.87.58-1.98.92-3.31.92-2.54 0-4.7-1.72-5.47-4.03H3.29v2.53A9.74 9.74 0 0 0 12 21.6Z"
              />
              <path
                fill="#FBBC05"
                d="M6.53 13.69A5.85 5.85 0 0 1 6.22 12c0-.59.11-1.16.31-1.69V7.78H3.29A9.73 9.73 0 0 0 2.27 12c0 1.57.38 3.05 1.02 4.22l3.24-2.53Z"
              />
              <path
                fill="#EA4335"
                d="M12 6.28c1.43 0 2.72.49 3.73 1.45l2.8-2.8C16.84 3.35 14.63 2.4 12 2.4a9.74 9.74 0 0 0-8.71 5.38l3.24 2.53C7.3 8 9.46 6.28 12 6.28Z"
              />
            </svg>

            <span>Login with Google</span>
          </button>

          {googleLoading && (
            <p className="mt-2 text-center text-xs text-slate-500 dark:text-slate-400">
              Signing in with Google...
            </p>
          )}

          <p className="mt-6 text-center text-sm text-slate-500 dark:text-slate-400">
            Don't have an account?{" "}
            <Link
              to="/register"
              className="font-semibold text-slate-900 underline dark:text-white"
            >
              Create one
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}

export default Login;
