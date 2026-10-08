import { Link, useNavigate } from "react-router-dom";

import { useState } from "react";

import toast from "react-hot-toast";

import Button from "../../components/common/Button";
import Input from "../../components/common/Input";

import { register } from "../../api/authApi";

function Register() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    name: "",
    email: "",
    username: "",
    password: "",
  });

  const [errors, setErrors] = useState({
    name: "",
    email: "",
    username: "",
    password: "",
  });

  const [loading, setLoading] = useState(false);

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
  };

  const validate = () => {
    const validationErrors = {
      name: "",
      email: "",
      username: "",
      password: "",
    };

    const name = form.name.trim();
    const email = form.email.trim();
    const username = form.username.trim();
    const password = form.password;

    if (!name) {
      validationErrors.name = "Name is required.";
    } else if (name.length > 100) {
      validationErrors.name = "Name must not exceed 100 characters.";
    }

    if (!email) {
      validationErrors.email = "Email is required.";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      validationErrors.email = "Please enter a valid email address.";
    }

    if (!username) {
      validationErrors.username = "Username is required.";
    } else if (username.length < 3 || username.length > 50) {
      validationErrors.username =
        "Username must be between 3 and 50 characters.";
    }

    if (!password) {
      validationErrors.password = "Password is required.";
    } else if (password.length < 8) {
      validationErrors.password = "Password must be at least 8 characters.";
    }

    setErrors(validationErrors);

    return !Object.values(validationErrors).some(Boolean);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!validate()) {
      return;
    }

    setLoading(true);

    try {
      await register({
        name: form.name.trim(),
        email: form.email.trim(),
        username: form.username.trim(),
        password: form.password,
      });

      toast.success("Account created successfully. Please sign in.");

      navigate("/login", {
        replace: true,
      });
    } catch (error) {
      const status = error.response?.status;
      const message = error.response?.data?.message || "Registration failed.";

      /*
       * Backend duplicate-resource response.
       */
      if (status === 409) {
        const lowerMessage = message.toLowerCase();

        if (lowerMessage.includes("email")) {
          setErrors((previous) => ({
            ...previous,
            email: message,
          }));
        } else if (lowerMessage.includes("username")) {
          setErrors((previous) => ({
            ...previous,
            username: message,
          }));
        } else {
          toast.error(message);
        }

        return;
      }

      /*
       * Backend validation response.
       */
      if (status === 400 && error.response?.data?.validationErrors) {
        setErrors((previous) => ({
          ...previous,
          ...error.response.data.validationErrors,
        }));

        return;
      }

      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 px-4 py-8 dark:bg-slate-950">
      <div className="w-full max-w-md">
        <div className="rounded-2xl border border-slate-200 bg-white p-8 shadow-sm dark:border-slate-800 dark:bg-slate-900">
          <div className="mb-8 text-center">
            <h1 className="text-3xl font-bold text-slate-900 dark:text-white">
              Create account
            </h1>

            <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">
              Start splitting expenses with your group
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-5" noValidate>
            <Input
              label="Name"
              name="name"
              value={form.name}
              onChange={handleChange}
              placeholder="Your full name"
              maxLength={100}
              error={errors.name}
            />

            <Input
              label="Email"
              name="email"
              type="email"
              value={form.email}
              onChange={handleChange}
              placeholder="you@example.com"
              maxLength={150}
              error={errors.email}
            />

            <Input
              label="Username"
              name="username"
              value={form.username}
              onChange={handleChange}
              placeholder="Choose a username"
              minLength={3}
              maxLength={50}
              error={errors.username}
            />

            <Input
              label="Password"
              name="password"
              type="password"
              value={form.password}
              onChange={handleChange}
              placeholder="Minimum 8 characters"
              minLength={8}
              error={errors.password}
            />

            <Button type="submit" loading={loading} className="w-full">
              Create account
            </Button>
          </form>

          <p className="mt-6 text-center text-sm text-slate-500 dark:text-slate-400">
            Already have an account?{" "}
            <Link
              to="/login"
              className="font-semibold text-slate-800 hover:underline dark:text-slate-200"
            >
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}

export default Register;
