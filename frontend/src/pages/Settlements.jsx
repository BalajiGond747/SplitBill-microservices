import { useEffect, useState } from "react";
import toast from "react-hot-toast";

import Button from "../components/common/Button";
import Input from "../components/common/Input";

import { createSettlement, getSettlementsByUser } from "../api/settlementApi";

import {
  getGroupsByCreatedBy,
  getGroupById,
  getGroupMembers,
} from "../api/groupApi";

import { getUsers } from "../api/userApi";

import useAuth from "../hooks/useAuth";

function Settlements() {
  const { user } = useAuth();

  const [settlements, setSettlements] = useState([]);

  const [groups, setGroups] = useState([]);

  const [members, setMembers] = useState([]);

  const [users, setUsers] = useState([]);

  const [groupNames, setGroupNames] = useState({});

  const [loading, setLoading] = useState(true);

  const [groupsLoading, setGroupsLoading] = useState(true);

  const [membersLoading, setMembersLoading] = useState(false);

  const [saving, setSaving] = useState(false);

  const [form, setForm] = useState({
    groupId: "",
    fromUserId: "",
    toUserId: "",
    amount: "",
    note: "",
  });

  const loadSettlements = async () => {
    if (!user?.id) return;

    try {
      setLoading(true);

      const response = await getSettlementsByUser(user.id);

      const settlementList = Array.isArray(response) ? response : [];

      setSettlements(settlementList);

      const uniqueGroupIds = [
        ...new Set(
          settlementList
            .map((settlement) => settlement.groupId)
            .filter(Boolean)
            .map(Number)
        ),
      ];

      if (uniqueGroupIds.length > 0) {
        const results = await Promise.all(
          uniqueGroupIds.map(async (groupId) => {
            try {
              const group = await getGroupById(groupId);

              return {
                id: groupId,
                name: group?.name || "Unknown group",
              };
            } catch {
              return {
                id: groupId,
                name: "Unknown group",
              };
            }
          })
        );

        const names = {};

        results.forEach((group) => {
          names[group.id] = group.name;
        });

        setGroupNames(names);
      }
    } catch (error) {
      toast.error(
        error.response?.data?.message || "Failed to load settlements."
      );
    } finally {
      setLoading(false);
    }
  };

  const loadGroups = async () => {
    if (!user?.id) return;

    try {
      setGroupsLoading(true);

      const response = await getGroupsByCreatedBy(user.id, {
        page: 0,
        size: 100,
        sortBy: "createdAt",
        sortDirection: "DESC",
      });

      const groupList = response?.content || [];

      setGroups(groupList);

      const names = {};

      groupList.forEach((group) => {
        names[group.id] = group.name;
      });

      setGroupNames((previous) => ({
        ...previous,
        ...names,
      }));
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to load groups.");
    } finally {
      setGroupsLoading(false);
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

  useEffect(() => {
    if (!user?.id) return;

    loadSettlements();
    loadGroups();
    loadUsers();
  }, [user?.id]);

  const loadMembers = async (groupId) => {
    if (!groupId) {
      setMembers([]);
      return;
    }

    try {
      setMembersLoading(true);

      const response = await getGroupMembers(Number(groupId));

      setMembers(Array.isArray(response) ? response : []);
    } catch (error) {
      toast.error(
        error.response?.data?.message || "Failed to load group members."
      );

      setMembers([]);
    } finally {
      setMembersLoading(false);
    }
  };

  const handleGroupChange = async (event) => {
    const groupId = event.target.value;

    setForm((previous) => ({
      ...previous,
      groupId,
      fromUserId: "",
      toUserId: "",
    }));

    await loadMembers(groupId);
  };

  const handleChange = (event) => {
    setForm((previous) => ({
      ...previous,
      [event.target.name]: event.target.value,
    }));
  };

  const getUserName = (userId) => {
    const numericId = Number(userId);

    if (numericId === Number(user?.id)) {
      return user?.name || user?.username || "You";
    }

    const foundUser = users.find((item) => Number(item.id) === numericId);

    if (foundUser) {
      return foundUser.name || foundUser.username || "Unknown user";
    }

    const foundMember = members.find(
      (member) => Number(member.userId) === numericId
    );

    if (foundMember?.user) {
      return (
        foundMember.user.name || foundMember.user.username || "Unknown user"
      );
    }

    return "Unknown user";
  };

  const groupUsers = members
    .filter((member) => member?.user)
    .map((member) => ({
      id: member.userId,
      name: member.user.name || member.user.username || "Unknown user",
      username: member.user.username || "",
    }));

  const availableToUsers = groupUsers.filter(
    (member) => Number(member.id) !== Number(form.fromUserId)
  );

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!form.groupId || !form.fromUserId || !form.toUserId || !form.amount) {
      toast.error("Fill all required settlement fields.");
      return;
    }

    if (Number(form.fromUserId) === Number(form.toUserId)) {
      toast.error("From and To users must be different.");
      return;
    }

    if (Number(form.amount) <= 0) {
      toast.error("Settlement amount must be greater than zero.");
      return;
    }

    setSaving(true);

    try {
      await createSettlement({
        groupId: Number(form.groupId),

        fromUserId: Number(form.fromUserId),

        toUserId: Number(form.toUserId),

        amount: Number(form.amount),

        note: form.note.trim() || null,
      });

      toast.success("Settlement created successfully.");

      setForm({
        groupId: "",
        fromUserId: "",
        toUserId: "",
        amount: "",
        note: "",
      });

      setMembers([]);

      await loadSettlements();
    } catch (error) {
      toast.error(
        error.response?.data?.message || "Unable to create settlement."
      );
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-white">
          Settlements
        </h1>

        <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
          Record money transfers between group members.
        </p>
      </div>

      <form
        onSubmit={handleSubmit}
        className="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900"
      >
        <h2 className="mb-5 text-lg font-semibold text-slate-900 dark:text-white">
          Record settlement
        </h2>

        <div className="grid gap-4 md:grid-cols-2">
          <div>
            <label className="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">
              Group
            </label>

            <select
              name="groupId"
              value={form.groupId}
              onChange={handleGroupChange}
              disabled={groupsLoading || saving}
              required
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 outline-none transition focus:border-slate-500 dark:border-slate-700 dark:bg-slate-950 dark:text-white"
            >
              <option value="">
                {groupsLoading ? "Loading groups..." : "Select a group"}
              </option>

              {groups.map((group) => (
                <option key={group.id} value={group.id}>
                  {group.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">
              From
            </label>

            <select
              name="fromUserId"
              value={form.fromUserId}
              onChange={handleChange}
              disabled={!form.groupId || membersLoading || saving}
              required
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 outline-none transition focus:border-slate-500 disabled:cursor-not-allowed disabled:opacity-60 dark:border-slate-700 dark:bg-slate-950 dark:text-white"
            >
              <option value="">
                {!form.groupId
                  ? "Select a group first"
                  : membersLoading
                  ? "Loading members..."
                  : "Select member"}
              </option>

              {groupUsers.map((member) => (
                <option key={member.id} value={member.id}>
                  {Number(member.id) === Number(user?.id)
                    ? `${member.name} (You)`
                    : member.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">
              To
            </label>

            <select
              name="toUserId"
              value={form.toUserId}
              onChange={handleChange}
              disabled={
                !form.groupId || !form.fromUserId || membersLoading || saving
              }
              required
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 outline-none transition focus:border-slate-500 disabled:cursor-not-allowed disabled:opacity-60 dark:border-slate-700 dark:bg-slate-950 dark:text-white"
            >
              <option value="">
                {!form.fromUserId
                  ? "Select From user first"
                  : availableToUsers.length === 0
                  ? "No other members"
                  : "Select member"}
              </option>

              {availableToUsers.map((member) => (
                <option key={member.id} value={member.id}>
                  {member.name}
                </option>
              ))}
            </select>
          </div>

          <Input
            label="Amount"
            name="amount"
            type="number"
            min="0.01"
            step="0.01"
            value={form.amount}
            onChange={handleChange}
            required
          />

          <div className="md:col-span-2">
            <Input
              label="Note"
              name="note"
              value={form.note}
              onChange={handleChange}
              maxLength={500}
              placeholder="Dinner settlement"
            />
          </div>
        </div>

        <div className="mt-4">
          <Button
            type="submit"
            loading={saving}
            disabled={membersLoading || groupUsers.length < 2}
          >
            Record settlement
          </Button>
        </div>
      </form>

      <div className="space-y-3">
        {loading ? (
          <div className="rounded-xl border border-slate-200 bg-white p-6 text-sm text-slate-500 dark:border-slate-800 dark:bg-slate-900">
            Loading settlements...
          </div>
        ) : settlements.length === 0 ? (
          <div className="rounded-xl border border-dashed border-slate-300 bg-white p-8 text-center dark:border-slate-700 dark:bg-slate-900">
            <p className="font-medium text-slate-900 dark:text-white">
              No settlements
            </p>

            <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
              Your settlement history will appear here.
            </p>
          </div>
        ) : (
          settlements.map((settlement) => {
            const fromName = getUserName(settlement.fromUserId);

            const toName = getUserName(settlement.toUserId);

            const groupName =
              groupNames[Number(settlement.groupId)] || "Unknown group";

            return (
              <div
                key={settlement.id}
                className="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900"
              >
                <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                  <div>
                    <p className="font-semibold text-slate-900 dark:text-white">
                      {fromName}
                      {" → "}
                      {toName}
                    </p>

                    <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
                      {groupName}
                    </p>

                    {settlement.note && (
                      <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">
                        {settlement.note}
                      </p>
                    )}
                  </div>

                  <div className="text-left md:text-right">
                    <p className="text-xl font-bold text-slate-900 dark:text-white">
                      ₹{Number(settlement.amount || 0).toFixed(2)}
                    </p>

                    <p className="mt-1 text-xs text-slate-400">
                      {settlement.settlementDate
                        ? new Date(settlement.settlementDate).toLocaleString()
                        : ""}
                    </p>
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}

export default Settlements;
