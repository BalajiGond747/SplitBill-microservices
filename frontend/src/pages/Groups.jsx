import { useEffect, useMemo, useState } from "react";
import toast from "react-hot-toast";

import Button from "../components/common/Button";
import Input from "../components/common/Input";

import {
  activateGroup,
  addGroupMember,
  createGroup,
  deactivateGroup,
  getGroupMembers,
  getGroupsByCreatedBy,
  removeGroupMember,
  updateGroup,
} from "../api/groupApi";

import { getUsers } from "../api/userApi";

import useAuth from "../hooks/useAuth";

function Groups() {
  const { user } = useAuth();

  const [groups, setGroups] = useState([]);
  const [users, setUsers] = useState([]);

  const [loading, setLoading] = useState(true);
  const [usersLoading, setUsersLoading] = useState(true);

  const [saving, setSaving] = useState(false);
  const [memberLoading, setMemberLoading] = useState(false);

  const [editingId, setEditingId] = useState(null);

  const [members, setMembers] = useState([]);

  const [memberSearch, setMemberSearch] = useState("");

  const [form, setForm] = useState({
    name: "",
    description: "",
  });

  const loadGroups = async () => {
    if (!user?.id) return;

    try {
      setLoading(true);

      const response = await getGroupsByCreatedBy(user.id, {
        page: 0,
        size: 100,
        sortBy: "createdAt",
        sortDirection: "DESC",
      });

      setGroups(response?.content || []);
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to load groups.");
    } finally {
      setLoading(false);
    }
  };

  const loadUsers = async () => {
    try {
      setUsersLoading(true);

      const response = await getUsers({
        active: true,
        page: 0,
        size: 100,
        sort: "name,asc",
      });

      setUsers(response?.content || []);
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to load users.");
    } finally {
      setUsersLoading(false);
    }
  };

  useEffect(() => {
    if (!user?.id) return;

    loadGroups();
    loadUsers();
  }, [user?.id]);

  const loadMembers = async (groupId) => {
    try {
      setMemberLoading(true);

      const response = await getGroupMembers(groupId);

      setMembers(Array.isArray(response) ? response : []);
    } catch (error) {
      toast.error(
        error.response?.data?.message || "Failed to load group members."
      );

      setMembers([]);
    } finally {
      setMemberLoading(false);
    }
  };

  const handleChange = (event) => {
    setForm((previous) => ({
      ...previous,
      [event.target.name]: event.target.value,
    }));
  };

  const resetForm = () => {
    setForm({
      name: "",
      description: "",
    });

    setEditingId(null);
    setMembers([]);
    setMemberSearch("");
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!form.name.trim()) {
      toast.error("Group name is required.");
      return;
    }

    setSaving(true);

    try {
      if (editingId) {
        await updateGroup(editingId, {
          name: form.name,
          description: form.description,
        });

        toast.success("Group updated successfully.");
      } else {
        const memberUserIds = selectedUsers.map((member) => member.id);

        await createGroup({
          name: form.name,
          description: form.description,
          createdBy: user.id,
          memberUserIds,
        });

        toast.success("Group created successfully.");
      }

      resetForm();

      await loadGroups();
    } catch (error) {
      toast.error(error.response?.data?.message || "Unable to save group.");
    } finally {
      setSaving(false);
    }
  };

  const handleEdit = async (group) => {
    setEditingId(group.id);

    setForm({
      name: group.name || "",
      description: group.description || "",
    });

    setMemberSearch("");

    await loadMembers(group.id);

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  };

  const handleAddMember = async (userId) => {
    if (!editingId) return;

    try {
      setMemberLoading(true);

      await addGroupMember(editingId, userId);

      toast.success("Member added successfully.");

      await loadMembers(editingId);
    } catch (error) {
      toast.error(error.response?.data?.message || "Unable to add member.");
    } finally {
      setMemberLoading(false);
    }
  };

  const handleRemoveMember = async (userId) => {
    if (!editingId) return;

    if (userId === user.id) {
      toast.error("The group creator cannot be removed.");
      return;
    }

    if (!window.confirm("Remove this member from the group?")) {
      return;
    }

    try {
      setMemberLoading(true);

      await removeGroupMember(editingId, userId);

      toast.success("Member removed from group.");

      await loadMembers(editingId);
    } catch (error) {
      toast.error(error.response?.data?.message || "Unable to remove member.");
    } finally {
      setMemberLoading(false);
    }
  };

  const handleDeactivate = async (id) => {
    if (!window.confirm("Deactivate this group?")) {
      return;
    }

    try {
      await deactivateGroup(id);

      toast.success("Group deactivated.");

      await loadGroups();
    } catch (error) {
      toast.error(
        error.response?.data?.message || "Unable to deactivate group."
      );
    }
  };

  const handleActivate = async (id) => {
    if (!window.confirm("Activate this group?")) {
      return;
    }

    try {
      await activateGroup(id);

      toast.success("Group activated.");

      await loadGroups();
    } catch (error) {
      toast.error(error.response?.data?.message || "Unable to activate group.");
    }
  };

  const currentMemberUsers = useMemo(() => {
    return members
      .map((member) => {
        const matchedUser = users.find(
          (candidate) => candidate.id === member.userId
        );

        return {
          ...member,
          user: matchedUser,
        };
      })
      .filter((member) => member.user);
  }, [members, users]);

  const currentMemberIds = useMemo(() => {
    return new Set(members.map((member) => member.userId));
  }, [members]);

  const availableUsers = useMemo(() => {
    const search = memberSearch.trim().toLowerCase();

    return users
      .filter((candidate) => !currentMemberIds.has(candidate.id))
      .filter((candidate) => candidate.id !== user?.id)
      .filter((candidate) => {
        if (!search) {
          return true;
        }

        return (
          candidate.name?.toLowerCase().includes(search) ||
          candidate.username?.toLowerCase().includes(search) ||
          candidate.email?.toLowerCase().includes(search)
        );
      });
  }, [users, currentMemberIds, memberSearch, user?.id]);

  const [selectedMemberIds, setSelectedMemberIds] = useState([]);

  const selectedUsers = useMemo(() => {
    return users.filter((candidate) =>
      selectedMemberIds.includes(candidate.id)
    );
  }, [users, selectedMemberIds]);

  const toggleCreateMember = (userId) => {
    setSelectedMemberIds((previous) => {
      if (previous.includes(userId)) {
        return previous.filter((id) => id !== userId);
      }

      return [...previous, userId];
    });
  };

  const handleCancel = () => {
    resetForm();
    setSelectedMemberIds([]);
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-white">
          Groups
        </h1>

        <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
          Create and manage your expense groups.
        </p>
      </div>

      <form
        onSubmit={handleSubmit}
        className="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900"
      >
        <div className="mb-5 flex items-center justify-between">
          <h2 className="text-lg font-semibold text-slate-900 dark:text-white">
            {editingId ? "Edit group" : "Create group"}
          </h2>

          {editingId && (
            <Button type="button" variant="ghost" onClick={handleCancel}>
              Cancel
            </Button>
          )}
        </div>

        <div className="grid gap-4 md:grid-cols-2">
          <Input
            label="Group name"
            name="name"
            value={form.name}
            onChange={handleChange}
            placeholder="Trip to Goa"
            maxLength={100}
            required
          />

          <Input
            label="Description"
            name="description"
            value={form.description}
            onChange={handleChange}
            placeholder="Friends trip expenses"
            maxLength={500}
          />
        </div>

        {!editingId && (
          <div className="mt-6">
            <div className="mb-3">
              <h3 className="text-sm font-semibold text-slate-900 dark:text-white">
                Add members
              </h3>

              <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">
                You are automatically added as the group creator.
              </p>
            </div>

            <Input
              label="Search users"
              value={memberSearch}
              onChange={(event) => setMemberSearch(event.target.value)}
              placeholder="Search by name, username or email"
            />

            {selectedUsers.length > 0 && (
              <div className="mt-4">
                <p className="mb-2 text-xs font-medium text-slate-500 dark:text-slate-400">
                  Selected members
                </p>

                <div className="flex flex-wrap gap-2">
                  {selectedUsers.map((member) => (
                    <button
                      key={member.id}
                      type="button"
                      onClick={() => toggleCreateMember(member.id)}
                      className="rounded-full bg-slate-100 px-3 py-1.5 text-sm text-slate-700 hover:bg-slate-200 dark:bg-slate-800 dark:text-slate-200 dark:hover:bg-slate-700"
                    >
                      {member.name} ×
                    </button>
                  ))}
                </div>
              </div>
            )}

            <div className="mt-4 max-h-64 overflow-y-auto rounded-lg border border-slate-200 dark:border-slate-700">
              {usersLoading ? (
                <div className="p-4 text-sm text-slate-500">
                  Loading users...
                </div>
              ) : availableUsers.length === 0 ? (
                <div className="p-4 text-sm text-slate-500">
                  No users found.
                </div>
              ) : (
                availableUsers.map((candidate) => {
                  const selected = selectedMemberIds.includes(candidate.id);

                  return (
                    <label
                      key={candidate.id}
                      className="flex cursor-pointer items-center gap-3 border-b border-slate-100 px-4 py-3 last:border-b-0 hover:bg-slate-50 dark:border-slate-800 dark:hover:bg-slate-800"
                    >
                      <input
                        type="checkbox"
                        checked={selected}
                        onChange={() => toggleCreateMember(candidate.id)}
                        className="h-4 w-4 rounded border-slate-300 text-slate-700 focus:ring-slate-500"
                      />

                      <div className="min-w-0">
                        <p className="text-sm font-medium text-slate-900 dark:text-white">
                          {candidate.name}
                        </p>

                        <p className="truncate text-xs text-slate-500 dark:text-slate-400">
                          @{candidate.username}
                          {" · "}
                          {candidate.email}
                        </p>
                      </div>
                    </label>
                  );
                })
              )}
            </div>

            <p className="mt-2 text-xs text-slate-500 dark:text-slate-400">
              {selectedMemberIds.length} member
              {selectedMemberIds.length === 1 ? "" : "s"} selected
            </p>
          </div>
        )}

        {editingId && (
          <div className="mt-6 space-y-6">
            <div>
              <div className="mb-3">
                <h3 className="text-sm font-semibold text-slate-900 dark:text-white">
                  Group members
                </h3>

                <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">
                  Manage who belongs to this group.
                </p>
              </div>

              {memberLoading ? (
                <div className="rounded-lg border border-slate-200 p-4 text-sm text-slate-500 dark:border-slate-700">
                  Loading members...
                </div>
              ) : currentMemberUsers.length === 0 ? (
                <div className="rounded-lg border border-dashed border-slate-300 p-4 text-sm text-slate-500 dark:border-slate-700">
                  No members found.
                </div>
              ) : (
                <div className="space-y-2">
                  {currentMemberUsers.map((member) => {
                    const isCreator = member.user.id === user?.id;

                    return (
                      <div
                        key={member.userId}
                        className="flex items-center justify-between rounded-lg border border-slate-200 px-4 py-3 dark:border-slate-700"
                      >
                        <div className="min-w-0">
                          <p className="text-sm font-medium text-slate-900 dark:text-white">
                            {member.user.name}
                          </p>

                          <p className="truncate text-xs text-slate-500 dark:text-slate-400">
                            @{member.user.username}
                          </p>
                        </div>

                        {isCreator ? (
                          <span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-medium text-slate-600 dark:bg-slate-800 dark:text-slate-300">
                            Creator
                          </span>
                        ) : (
                          <Button
                            type="button"
                            variant="danger"
                            disabled={memberLoading}
                            onClick={() => handleRemoveMember(member.userId)}
                          >
                            Remove
                          </Button>
                        )}
                      </div>
                    );
                  })}
                </div>
              )}
            </div>

            <div>
              <div className="mb-3">
                <h3 className="text-sm font-semibold text-slate-900 dark:text-white">
                  Add member
                </h3>

                <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">
                  Search for another user to add to this group.
                </p>
              </div>

              <Input
                label="Search users"
                value={memberSearch}
                onChange={(event) => setMemberSearch(event.target.value)}
                placeholder="Search by name, username or email"
              />

              <div className="mt-3 max-h-56 overflow-y-auto rounded-lg border border-slate-200 dark:border-slate-700">
                {usersLoading ? (
                  <div className="p-4 text-sm text-slate-500">
                    Loading users...
                  </div>
                ) : availableUsers.length === 0 ? (
                  <div className="p-4 text-sm text-slate-500">
                    No users available to add.
                  </div>
                ) : (
                  availableUsers.map((candidate) => (
                    <div
                      key={candidate.id}
                      className="flex items-center justify-between border-b border-slate-100 px-4 py-3 last:border-b-0 dark:border-slate-800"
                    >
                      <div className="min-w-0">
                        <p className="text-sm font-medium text-slate-900 dark:text-white">
                          {candidate.name}
                        </p>

                        <p className="truncate text-xs text-slate-500 dark:text-slate-400">
                          @{candidate.username}
                          {" · "}
                          {candidate.email}
                        </p>
                      </div>

                      <Button
                        type="button"
                        variant="secondary"
                        disabled={memberLoading}
                        onClick={() => handleAddMember(candidate.id)}
                      >
                        Add
                      </Button>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>
        )}

        <div className="mt-5">
          <Button type="submit" loading={saving}>
            {editingId ? "Update group" : "Create group"}
          </Button>
        </div>
      </form>

      <div className="space-y-3">
        {loading ? (
          <div className="rounded-xl border border-slate-200 bg-white p-6 text-sm text-slate-500 dark:border-slate-800 dark:bg-slate-900">
            Loading groups...
          </div>
        ) : groups.length === 0 ? (
          <div className="rounded-xl border border-dashed border-slate-300 bg-white p-8 text-center dark:border-slate-700 dark:bg-slate-900">
            <p className="font-medium text-slate-900 dark:text-white">
              No groups yet
            </p>

            <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
              Create your first group above.
            </p>
          </div>
        ) : (
          groups.map((group) => (
            <div
              key={group.id}
              className="flex flex-col gap-4 rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900 md:flex-row md:items-center md:justify-between"
            >
              <div>
                <div className="flex items-center gap-3">
                  <h3 className="font-semibold text-slate-900 dark:text-white">
                    {group.name}
                  </h3>

                  <span
                    className={`rounded-full px-2.5 py-1 text-xs font-medium ${
                      group.active
                        ? "bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300"
                        : "bg-red-100 text-red-700 dark:bg-red-950 dark:text-red-300"
                    }`}
                  >
                    {group.active ? "Active" : "Inactive"}
                  </span>
                </div>

                <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
                  {group.description || "No description"}
                </p>
              </div>

              <div className="flex gap-2">
                {group.active ? (
                  <>
                    <Button
                      variant="secondary"
                      onClick={() => handleEdit(group)}
                    >
                      Edit
                    </Button>

                    <Button
                      variant="danger"
                      onClick={() => handleDeactivate(group.id)}
                    >
                      Deactivate
                    </Button>
                  </>
                ) : (
                  <Button
                    variant="secondary"
                    onClick={() => handleActivate(group.id)}
                  >
                    Activate
                  </Button>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default Groups;
