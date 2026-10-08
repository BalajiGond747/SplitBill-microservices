import { NavLink } from "react-router-dom";

const navigation = [
  {
    label: "Dashboard",
    path: "/dashboard",
  },
  {
    label: "Groups",
    path: "/groups",
  },
  {
    label: "Expenses",
    path: "/expenses",
  },
  {
    label: "Balances",
    path: "/balances",
  },
];

function Sidebar() {
  return (
    <aside
      className="
        w-64
        shrink-0
        border-r
        px-0
      "
      style={{
        backgroundColor: "var(--bg-card)",
        borderColor: "var(--border)",
        color: "var(--text-primary)",
      }}
    >
      <div className="flex min-h-screen flex-col">
        <div
          className="border-b px-6 py-5"
          style={{
            borderColor: "var(--border)",
          }}
        >
          <h1
            className="text-xl font-bold"
            style={{
              color: "var(--text-primary)",
            }}
          >
            SplitBill
          </h1>

          <p
            className="mt-1 text-xs"
            style={{
              color: "var(--text-muted)",
            }}
          >
            Expense sharing made simple
          </p>
        </div>

        <nav className="flex-1 space-y-1 p-4">
          {navigation.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `
                  block
                  rounded-lg
                  px-3
                  py-2.5
                  text-sm
                  font-medium
                  transition-colors
                  ${isActive ? "sidebar-link-active" : "sidebar-link"}
                `
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </div>
    </aside>
  );
}

export default Sidebar;
