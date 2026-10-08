import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import toast from "react-hot-toast";

import { getGroupsByCreatedBy } from "../api/groupApi";

import { getExpensesByGroup } from "../api/expenseApi";

import { getBalancesByUser } from "../api/balanceApi";

import { getUserNotifications } from "../api/notificationApi";

import useAuth from "../hooks/useAuth";

function Dashboard() {
  const { user } = useAuth();

  const [loading, setLoading] = useState(true);

  const [groups, setGroups] = useState([]);

  const [expenses, setExpenses] = useState([]);

  const [balances, setBalances] = useState([]);

  const [notifications, setNotifications] = useState([]);

  useEffect(() => {
    const loadDashboard = async () => {
      if (!user?.id) return;

      try {
        setLoading(true);

        const groupResponse = await getGroupsByCreatedBy(user.id, {
          page: 0,
          size: 100,
          sortBy: "createdAt",
          sortDirection: "DESC",
        });

        const groupItems = groupResponse?.content || [];

        setGroups(groupItems);

        const expenseResponses = await Promise.all(
          groupItems.map(async (group) => {
            const response = await getExpensesByGroup(group.id, {
              page: 0,
              size: 100,
              sortBy: "expenseDate",
              sortDirection: "DESC",
            });

            return (response?.content || []).map((expense) => ({
              ...expense,

              groupId: group.id,

              groupName: group.name,
            }));
          })
        );

        const allExpenses = expenseResponses.flat();

        setExpenses(
          allExpenses.sort(
            (a, b) => new Date(b.expenseDate) - new Date(a.expenseDate)
          )
        );

        const [balanceResponse, notificationResponse] = await Promise.all([
          getBalancesByUser(user.id),

          getUserNotifications(user.id),
        ]);

        setBalances(Array.isArray(balanceResponse) ? balanceResponse : []);

        setNotifications(
          Array.isArray(notificationResponse) ? notificationResponse : []
        );
      } catch (error) {
        toast.error(
          error.response?.data?.message || "Unable to load dashboard."
        );
      } finally {
        setLoading(false);
      }
    };

    loadDashboard();
  }, [user?.id]);

  const totalExpenses = expenses.reduce(
    (sum, expense) => sum + Number(expense.amount || 0),
    0
  );

  const youOwe = balances
    .filter((balance) => Number(balance.fromUserId) === Number(user?.id))
    .reduce((sum, balance) => sum + Number(balance.amount || 0), 0);

  const youAreOwed = balances
    .filter((balance) => Number(balance.toUserId) === Number(user?.id))
    .reduce((sum, balance) => sum + Number(balance.amount || 0), 0);

  const unreadNotifications = notifications.filter(
    (notification) => !notification.read
  ).length;

  const cards = [
    {
      title: "Total Expenses",
      value: `₹${totalExpenses.toFixed(2)}`,
    },
    {
      title: "You Owe",
      value: `₹${youOwe.toFixed(2)}`,
    },
    {
      title: "You Are Owed",
      value: `₹${youAreOwed.toFixed(2)}`,
    },
    {
      title: "Groups",
      value: String(groups.length),
    },
  ];

  if (loading) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white">
            Dashboard
          </h1>

          <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
            Loading your SplitBill overview...
          </p>
        </div>

        <div className="rounded-xl border border-slate-200 bg-white p-8 text-center text-sm text-slate-500 dark:border-slate-800 dark:bg-slate-900">
          Loading...
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-white">
          Dashboard
        </h1>

        <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
          Welcome back, {user?.name || user?.username}.
        </p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {cards.map((card) => (
          <div
            key={card.title}
            className="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900"
          >
            <p className="text-sm text-slate-500 dark:text-slate-400">
              {card.title}
            </p>

            <p className="mt-2 text-2xl font-bold text-slate-900 dark:text-white">
              {card.value}
            </p>
          </div>
        ))}
      </div>

      {unreadNotifications > 0 && (
        <Link
          to="/notifications"
          className="block rounded-xl border border-slate-300 bg-slate-50 p-5 hover:bg-slate-100 dark:border-slate-700 dark:bg-slate-900 dark:hover:bg-slate-800"
        >
          <p className="font-semibold text-slate-900 dark:text-white">
            You have {unreadNotifications} unread notification
            {unreadNotifications !== 1 ? "s" : ""}
          </p>

          <p className="mt-1 text-sm text-slate-500">
            Open notifications to review them.
          </p>
        </Link>
      )}

      <div className="grid gap-6 lg:grid-cols-2">
        <div className="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900">
          <div className="flex items-center justify-between">
            <h2 className="font-semibold text-slate-900 dark:text-white">
              Recent expenses
            </h2>

            <Link
              to="/expenses"
              className="text-sm font-medium text-slate-600 hover:underline dark:text-slate-300"
            >
              View all
            </Link>
          </div>

          <div className="mt-4 space-y-3">
            {expenses.length === 0 ? (
              <p className="text-sm text-slate-500">No expenses yet.</p>
            ) : (
              expenses.slice(0, 5).map((expense) => (
                <div
                  key={expense.id}
                  className="flex items-center justify-between rounded-lg bg-slate-50 p-3 dark:bg-slate-950"
                >
                  <div className="min-w-0">
                    <p className="text-sm font-medium text-slate-900 dark:text-white">
                      {expense.description}
                    </p>

                    <p className="mt-0.5 text-xs font-medium text-slate-700 dark:text-slate-300">
                      {expense.groupName}
                    </p>

                    <p className="mt-0.5 text-xs text-slate-500">
                      {expense.expenseDate}
                    </p>
                  </div>

                  <p className="ml-4 whitespace-nowrap font-semibold text-slate-900 dark:text-white">
                    ₹{Number(expense.amount || 0).toFixed(2)}
                  </p>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900">
          <div className="flex items-center justify-between">
            <h2 className="font-semibold text-slate-900 dark:text-white">
              Groups
            </h2>

            <Link
              to="/groups"
              className="text-sm font-medium text-slate-600 hover:underline dark:text-slate-300"
            >
              Manage
            </Link>
          </div>

          <div className="mt-4 space-y-3">
            {groups.length === 0 ? (
              <p className="text-sm text-slate-500">No groups yet.</p>
            ) : (
              groups.slice(0, 5).map((group) => (
                <div
                  key={group.id}
                  className="rounded-lg bg-slate-50 p-3 dark:bg-slate-950"
                >
                  <p className="text-sm font-medium text-slate-900 dark:text-white">
                    {group.name}
                  </p>

                  <p className="mt-1 text-xs text-slate-500">
                    {group.description || "No description"}
                  </p>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
