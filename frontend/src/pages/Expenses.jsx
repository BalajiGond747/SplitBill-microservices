import { useEffect, useMemo, useState } from "react";

import toast from "react-hot-toast";

import Button from "../components/common/Button";
import Input from "../components/common/Input";

import { getGroupsByCreatedBy, getGroupMembers } from "../api/groupApi";

import { getUsers } from "../api/userApi";

import {
  createExpense,
  deleteExpense,
  getExpensesByGroup,
  updateExpense,
} from "../api/expenseApi";

import useAuth from "../hooks/useAuth";

function Expenses() {
  const { user } = useAuth();

  const [groups, setGroups] = useState([]);

  const [selectedGroupId, setSelectedGroupId] = useState("");

  const [groupMembers, setGroupMembers] = useState([]);

  const [users, setUsers] = useState([]);

  const [membersLoading, setMembersLoading] = useState(false);

  const [expenses, setExpenses] = useState([]);

  const [loading, setLoading] = useState(false);

  const [saving, setSaving] = useState(false);

  const [editingId, setEditingId] = useState(null);

  const [form, setForm] = useState({
    amount: "",
    description: "",
    expenseDate: new Date().toISOString().slice(0, 10),
    splitType: "EQUAL",
  });

  const [paidBy, setPaidBy] = useState("");

  const [splits, setSplits] = useState([]);

  const selectedGroup = useMemo(
    () => groups.find((group) => String(group.id) === String(selectedGroupId)),
    [groups, selectedGroupId]
  );

  const memberUsers = useMemo(() => {
    return groupMembers
      .map((member) => {
        const memberUser = users.find(
          (candidate) => Number(candidate.id) === Number(member.userId)
        );

        return {
          ...member,
          user: memberUser,
        };
      })
      .filter((member) => member.user);
  }, [groupMembers, users]);

  const selectedParticipantIds = useMemo(
    () => new Set(splits.map((split) => Number(split.userId))),
    [splits]
  );

  const availableMembers = useMemo(
    () =>
      memberUsers.filter(
        (member) => !selectedParticipantIds.has(Number(member.userId))
      ),
    [memberUsers, selectedParticipantIds]
  );

  const totalPercentage = useMemo(
    () =>
      splits.reduce((total, split) => total + Number(split.percentage || 0), 0),
    [splits]
  );

  const totalSplitAmount = useMemo(
    () => splits.reduce((total, split) => total + Number(split.amount || 0), 0),
    [splits]
  );

  const equalSplitAmount = useMemo(() => {
    if (form.splitType !== "EQUAL") {
      return 0;
    }

    if (!form.amount || splits.length === 0) {
      return 0;
    }

    return Number(form.amount) / splits.length;
  }, [form.amount, form.splitType, splits.length]);

  const loadGroups = async () => {
    if (!user?.id) {
      return;
    }

    try {
      const response = await getGroupsByCreatedBy(user.id, {
        page: 0,
        size: 100,
        sortBy: "createdAt",
        sortDirection: "DESC",
      });

      const items = response?.content || [];

      setGroups(items);

      if (items.length > 0 && !selectedGroupId) {
        setSelectedGroupId(String(items[0].id));
      }

      await loadExpenses(items);
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to load groups.");
    }
  };

  const loadUsers = async () => {
    try {
      const response = await getUsers({
        active: true,
        page: 0,
        size: 100,
        sort: "name,asc",
      });

      setUsers(response?.content || []);
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to load users.");
    }
  };

  const loadGroupMembers = async (groupId) => {
    if (!groupId) {
      setGroupMembers([]);
      return;
    }

    try {
      setMembersLoading(true);

      const response = await getGroupMembers(groupId);

      const members = Array.isArray(response) ? response : [];

      setGroupMembers(members.filter((member) => member.active !== false));
    } catch (error) {
      setGroupMembers([]);

      toast.error(
        error.response?.data?.message || "Failed to load group members."
      );
    } finally {
      setMembersLoading(false);
    }
  };

  const loadExpenses = async (groupItems = groups) => {
    if (!groupItems || groupItems.length === 0) {
      setExpenses([]);
      return;
    }

    try {
      setLoading(true);

      const responses = await Promise.all(
        groupItems.map(async (group) => {
          const response = await getExpensesByGroup(group.id);

          return (response?.content || []).map((expense) => ({
            ...expense,
            groupId: group.id,
            groupName: group.name,
          }));
        })
      );

      const allExpenses = responses
        .flat()
        .sort((a, b) => new Date(b.expenseDate) - new Date(a.expenseDate));

      setExpenses(allExpenses);
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to load expenses.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!user?.id) {
      return;
    }

    loadGroups();
    loadUsers();
  }, [user?.id]);

  useEffect(() => {
    if (!selectedGroupId) {
      setGroupMembers([]);
      setPaidBy("");
      setSplits([]);

      return;
    }

    loadGroupMembers(selectedGroupId);

    setEditingId(null);

    setPaidBy("");

    setSplits([]);
  }, [selectedGroupId]);

  useEffect(() => {
    if (!user?.id || groupMembers.length === 0) {
      return;
    }

    const currentUserIsMember = groupMembers.some(
      (member) => Number(member.userId) === Number(user.id)
    );

    if (currentUserIsMember && !paidBy) {
      setPaidBy(String(user.id));
    }
  }, [user?.id, groupMembers, paidBy]);

  const resetForm = () => {
    setForm({
      amount: "",
      description: "",
      expenseDate: new Date().toISOString().slice(0, 10),
      splitType: "EQUAL",
    });

    setPaidBy("");

    setSplits([]);

    setEditingId(null);
  };

  const handleFormChange = (event) => {
    const { name, value } = event.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const addParticipant = (userId) => {
    if (!userId) {
      return;
    }

    const numericUserId = Number(userId);

    if (splits.some((split) => Number(split.userId) === numericUserId)) {
      return;
    }

    setSplits((previous) => [
      ...previous,
      {
        userId: numericUserId,
        amount: "",
        percentage: "",
      },
    ]);
  };

  const removeParticipant = (userId) => {
    setSplits((previous) =>
      previous.filter((split) => Number(split.userId) !== Number(userId))
    );
  };

  const handleSplitChange = (userId, field, value) => {
    setSplits((previous) =>
      previous.map((split) =>
        Number(split.userId) === Number(userId)
          ? {
              ...split,
              [field]: value,
            }
          : split
      )
    );
  };

  const buildSplits = () => {
    return splits.map((split) => {
      const item = {
        userId: Number(split.userId),
      };

      if (form.splitType === "EQUAL") {
        item.amount = Number(equalSplitAmount.toFixed(2));
      }

      if (form.splitType === "EXACT") {
        item.amount = Number(split.amount || 0);
      }

      if (form.splitType === "PERCENTAGE") {
        item.percentage = Number(split.percentage || 0);
      }

      return item;
    });
  };

  const validateForm = () => {
    if (!selectedGroupId) {
      toast.error("Select a group first.");

      return false;
    }

    if (membersLoading) {
      toast.error("Please wait for group members to load.");

      return false;
    }

    if (groupMembers.length === 0) {
      toast.error("This group has no active members.");

      return false;
    }

    if (!paidBy) {
      toast.error("Select who paid for this expense.");

      return false;
    }

    if (!form.amount || Number(form.amount) <= 0) {
      toast.error("Amount must be greater than zero.");

      return false;
    }

    if (!form.description.trim()) {
      toast.error("Description is required.");

      return false;
    }

    if (splits.length === 0) {
      toast.error("Add at least one participant.");

      return false;
    }

    const memberIds = new Set(
      groupMembers.map((member) => Number(member.userId))
    );

    const invalidParticipant = splits.some(
      (split) => !memberIds.has(Number(split.userId))
    );

    if (invalidParticipant) {
      toast.error("Every participant must be a member of the selected group.");

      return false;
    }

    if (form.splitType === "EXACT") {
      const hasInvalidAmount = splits.some(
        (split) => Number(split.amount) <= 0
      );

      if (hasInvalidAmount) {
        toast.error("Every participant must have a valid amount.");

        return false;
      }

      if (Math.abs(totalSplitAmount - Number(form.amount)) > 0.01) {
        toast.error("Exact split amounts must equal the expense amount.");

        return false;
      }
    }

    if (form.splitType === "PERCENTAGE") {
      const hasInvalidPercentage = splits.some(
        (split) => Number(split.percentage) <= 0
      );

      if (hasInvalidPercentage) {
        toast.error("Every participant must have a valid percentage.");

        return false;
      }

      if (Math.abs(totalPercentage - 100) > 0.01) {
        toast.error("Percentage splits must total 100.");

        return false;
      }
    }

    return true;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!validateForm()) {
      return;
    }

    setSaving(true);

    try {
      const payload = {
        amount: Number(form.amount),

        description: form.description.trim(),

        expenseDate: form.expenseDate,

        splitType: form.splitType,

        paidBy: Number(paidBy),

        splits: buildSplits(),
      };

      if (editingId) {
        await updateExpense(editingId, payload);

        toast.success("Expense updated successfully.");
      } else {
        await createExpense({
          groupId: Number(selectedGroupId),

          ...payload,
        });

        toast.success("Expense created successfully.");
      }

      resetForm();

      await loadExpenses(groups);
    } catch (error) {
      toast.error(error.response?.data?.message || "Unable to save expense.");
    } finally {
      setSaving(false);
    }
  };

  const handleEdit = (expense) => {
    if (expense.groupId) {
      setSelectedGroupId(String(expense.groupId));
    }

    setEditingId(expense.id);

    setForm({
      amount: String(expense.amount ?? ""),

      description: expense.description || "",

      expenseDate: expense.expenseDate || new Date().toISOString().slice(0, 10),

      splitType: expense.splitType || "EQUAL",
    });

    setPaidBy(String(expense.paidBy || ""));

    setSplits(
      (expense.splits || []).map((split) => ({
        userId: split.userId,

        amount: split.amount ?? "",

        percentage: split.percentage ?? "",
      }))
    );

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this expense?")) {
      return;
    }

    try {
      await deleteExpense(id);

      toast.success("Expense deleted.");

      await loadExpenses(groups);
    } catch (error) {
      toast.error(error.response?.data?.message || "Unable to delete expense.");
    }
  };

  const getUserName = (userId) => {
    const matchedUser = users.find(
      (candidate) => Number(candidate.id) === Number(userId)
    );

    return matchedUser?.name || matchedUser?.username || "Unknown member";
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-white">
          Expenses
        </h1>

        <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
          Create and manage shared expenses.
        </p>
      </div>

      <div className="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900">
        <div className="mb-5">
          <label className="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">
            Group
          </label>

          <select
            value={selectedGroupId}
            onChange={(event) => setSelectedGroupId(event.target.value)}
            className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 dark:border-slate-600 dark:bg-slate-900 dark:text-white md:max-w-md"
          >
            <option value="">Select group</option>

            {groups.map((group) => (
              <option key={group.id} value={group.id}>
                {group.name}
              </option>
            ))}
          </select>

          {selectedGroup && (
            <p className="mt-2 text-xs text-slate-500 dark:text-slate-400">
              {memberUsers.length} active member
              {memberUsers.length === 1 ? "" : "s"}
            </p>
          )}
        </div>

        <div className="border-t border-slate-200 pt-5 dark:border-slate-800">
          <div className="mb-5 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-slate-900 dark:text-white">
              {editingId ? "Edit expense" : "Add expense"}
            </h2>

            {editingId && (
              <Button type="button" variant="ghost" onClick={resetForm}>
                Cancel
              </Button>
            )}
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid gap-4 md:grid-cols-3">
              <Input
                label="Amount"
                name="amount"
                type="number"
                min="0.01"
                step="0.01"
                value={form.amount}
                onChange={handleFormChange}
                placeholder="3000"
                required
              />

              <Input
                label="Description"
                name="description"
                value={form.description}
                onChange={handleFormChange}
                placeholder="Goa Hotel"
                maxLength={255}
                required
              />

              <Input
                label="Expense date"
                name="expenseDate"
                type="date"
                value={form.expenseDate}
                onChange={handleFormChange}
                required
              />
            </div>

            <div>
              <label className="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">
                Paid by
              </label>

              <select
                value={paidBy}
                onChange={(event) => setPaidBy(event.target.value)}
                disabled={!selectedGroupId || membersLoading}
                className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 dark:border-slate-600 dark:bg-slate-900 dark:text-white md:max-w-md"
              >
                <option value="">
                  {membersLoading ? "Loading members..." : "Select who paid"}
                </option>

                {memberUsers.map((member) => (
                  <option key={member.userId} value={member.userId}>
                    {member.user.name}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">
                Split type
              </label>

              <select
                name="splitType"
                value={form.splitType}
                onChange={handleFormChange}
                className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 dark:border-slate-600 dark:bg-slate-900 dark:text-white md:max-w-md"
              >
                <option value="EQUAL">Equal</option>

                <option value="EXACT">Exact amounts</option>

                <option value="PERCENTAGE">Percentage</option>
              </select>
            </div>

            {selectedGroupId && (
              <div>
                <label className="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">
                  Split between
                </label>

                <select
                  value=""
                  disabled={membersLoading || availableMembers.length === 0}
                  onChange={(event) => {
                    addParticipant(event.target.value);
                  }}
                  className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 dark:border-slate-600 dark:bg-slate-900 dark:text-white"
                >
                  <option value="">
                    {membersLoading
                      ? "Loading members..."
                      : availableMembers.length === 0
                      ? "All members added"
                      : "Add participant"}
                  </option>

                  {availableMembers.map((member) => (
                    <option key={member.userId} value={member.userId}>
                      {member.user.name}
                    </option>
                  ))}
                </select>

                {splits.length > 0 && (
                  <div className="mt-4 space-y-3">
                    {splits.map((split) => (
                      <div
                        key={split.userId}
                        className="rounded-lg border border-slate-200 p-4 dark:border-slate-700"
                      >
                        <div className="flex items-center justify-between gap-3">
                          <p className="font-medium text-slate-900 dark:text-white">
                            {getUserName(split.userId)}
                          </p>

                          <Button
                            type="button"
                            variant="ghost"
                            onClick={() => removeParticipant(split.userId)}
                          >
                            Remove
                          </Button>
                        </div>

                        {form.splitType === "EXACT" && (
                          <div className="mt-3">
                            <Input
                              label="Amount"
                              type="number"
                              min="0.01"
                              step="0.01"
                              value={split.amount}
                              onChange={(event) =>
                                handleSplitChange(
                                  split.userId,
                                  "amount",
                                  event.target.value
                                )
                              }
                            />
                          </div>
                        )}

                        {form.splitType === "PERCENTAGE" && (
                          <div className="mt-3">
                            <Input
                              label="Percentage"
                              type="number"
                              min="0.01"
                              max="100"
                              step="0.01"
                              value={split.percentage}
                              onChange={(event) =>
                                handleSplitChange(
                                  split.userId,
                                  "percentage",
                                  event.target.value
                                )
                              }
                            />
                          </div>
                        )}

                        {form.splitType === "EQUAL" && (
                          <p className="mt-2 text-sm text-slate-500">
                            ₹{equalSplitAmount.toFixed(2)}
                          </p>
                        )}
                      </div>
                    ))}
                  </div>
                )}

                {form.splitType === "PERCENTAGE" && (
                  <div className="mt-2 flex items-center justify-between">
                    <span className="text-sm text-slate-500 dark:text-slate-400">
                      Assigned
                    </span>

                    <span
                      className={`font-semibold ${
                        Math.abs(totalPercentage - 100) <= 0.01
                          ? "text-slate-900 dark:text-white"
                          : "text-red-600"
                      }`}
                    >
                      {totalPercentage.toFixed(2)}%
                    </span>
                  </div>
                )}
              </div>
            )}

            <Button
              type="submit"
              loading={saving}
              disabled={!selectedGroupId || membersLoading}
            >
              {editingId ? "Update expense" : "Create expense"}
            </Button>
          </form>
        </div>
      </div>

      <div className="space-y-3">
        {loading ? (
          <div className="rounded-xl border border-slate-200 bg-white p-6 text-sm text-slate-500 dark:border-slate-800 dark:bg-slate-900">
            Loading expenses...
          </div>
        ) : expenses.length === 0 ? (
          <div className="rounded-xl border border-dashed border-slate-300 bg-white p-8 text-center dark:border-slate-700 dark:bg-slate-900">
            <p className="font-medium text-slate-900 dark:text-white">
              No expenses
            </p>

            <p className="mt-1 text-sm text-slate-500">
              Create an expense for any of your groups.
            </p>
          </div>
        ) : (
          expenses.map((expense) => (
            <div
              key={expense.id}
              className="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900"
            >
              <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
                <div>
                  <h3 className="font-semibold text-slate-900 dark:text-white">
                    {expense.description}
                  </h3>

                  <p className="mt-1 text-sm font-medium text-slate-700 dark:text-slate-300">
                    {expense.groupName}
                  </p>

                  <p className="mt-1 text-sm text-slate-500">
                    {expense.expenseDate}
                    {" · "}
                    Paid by{" "}
                    <span className="font-medium">
                      {getUserName(expense.paidBy)}
                    </span>
                  </p>

                  <p className="mt-3 text-2xl font-bold text-slate-900 dark:text-white">
                    ₹{Number(expense.amount).toFixed(2)}
                  </p>

                  <p className="mt-1 text-xs text-slate-400">
                    {expense.splitType} split
                  </p>
                </div>

                <div className="flex gap-2">
                  <Button
                    variant="secondary"
                    onClick={() => handleEdit(expense)}
                  >
                    Edit
                  </Button>

                  <Button
                    variant="danger"
                    onClick={() => handleDelete(expense.id)}
                  >
                    Delete
                  </Button>
                </div>
              </div>

              <div className="mt-4 border-t border-slate-200 pt-4 dark:border-slate-800">
                <p className="mb-2 text-sm font-medium text-slate-700 dark:text-slate-300">
                  Split details
                </p>

                <div className="grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
                  {(expense.splits || []).map((split) => (
                    <div
                      key={split.id}
                      className="rounded-lg bg-slate-50 p-3 dark:bg-slate-950"
                    >
                      <p className="text-sm font-medium text-slate-900 dark:text-white">
                        {getUserName(split.userId)}
                      </p>

                      <p className="mt-1 text-xs text-slate-500">
                        ₹{Number(split.amount || 0).toFixed(2)}
                        {split.percentage != null && ` · ${split.percentage}%`}
                      </p>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default Expenses;
