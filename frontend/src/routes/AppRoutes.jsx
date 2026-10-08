import { Navigate, Route, Routes } from "react-router-dom";

import ProtectedRoute from "../components/auth/ProtectedRoute";
import AppLayout from "../components/layout/AppLayout";

import Login from "../pages/auth/Login";
import Register from "../pages/auth/Register";

import Dashboard from "../pages/Dashboard";
import Groups from "../pages/Groups";
import Expenses from "../pages/Expenses";
import Balances from "../pages/Balances";
import Settlements from "../pages/Settlements";
import Payments from "../pages/Payments";
import Notifications from "../pages/Notifications";
import Profile from "../pages/Profile";
import NotFound from "../pages/NotFound";

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />

      <Route path="/login" element={<Login />} />

      <Route path="/register" element={<Register />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/dashboard" element={<Dashboard />} />

          <Route path="/groups" element={<Groups />} />

          <Route path="/expenses" element={<Expenses />} />

          <Route path="/balances" element={<Balances />} />

          <Route path="/settlements" element={<Settlements />} />

          <Route path="/payments" element={<Payments />} />

          <Route path="/notifications" element={<Notifications />} />

          <Route path="/profile" element={<Profile />} />
        </Route>
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

export default AppRoutes;
