import { useEffect, useState } from "react";
import toast from "react-hot-toast";

import Button from "../components/common/Button";

import { getBalancesByUser } from "../api/balanceApi";

import { getGroupById } from "../api/groupApi";

import { getUsers } from "../api/userApi";

import { createSettlement } from "../api/settlementApi";

import { createPaymentOrder, verifyPayment } from "../api/paymentApi";

import useAuth from "../hooks/useAuth";

function loadRazorpayScript() {
  return new Promise((resolve) => {
    if (window.Razorpay) {
      resolve(true);
      return;
    }

    const script = document.createElement("script");

    script.src = "https://checkout.razorpay.com/v1/checkout.js";

    script.onload = () => resolve(true);

    script.onerror = () => resolve(false);

    document.body.appendChild(script);
  });
}

function Balances() {
  const { user } = useAuth();

  const [balances, setBalances] = useState([]);
  const [loading, setLoading] = useState(true);
  const [users, setUsers] = useState([]);
  const [groupNames, setGroupNames] = useState({});
  const [payingBalance, setPayingBalance] = useState(null);

  const loadBalances = async () => {
    if (!user?.id) return;

    try {
      setLoading(true);

      const [balanceResponse, usersResponse] = await Promise.all([
        getBalancesByUser(user.id),

        getUsers({
          active: true,
          page: 0,
          size: 100,
          sort: "name,asc",
        }),
      ]);

      const balanceItems = Array.isArray(balanceResponse)
        ? balanceResponse
        : [];

      const userItems = usersResponse?.content || [];

      setBalances(balanceItems);
      setUsers(userItems);

      const uniqueGroupIds = [
        ...new Set(
          balanceItems
            .map((balance) => balance.groupId)
            .filter((groupId) => groupId !== null && groupId !== undefined)
            .map((groupId) => String(groupId))
        ),
      ];

      if (uniqueGroupIds.length > 0) {
        const groupResponses = await Promise.all(
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

        groupResponses.forEach((group) => {
          names[group.id] = group.name;
        });

        setGroupNames(names);
      } else {
        setGroupNames({});
      }
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to load balances.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadBalances();
  }, [user?.id]);

  const getUserName = (userId) => {
    const matchedUser = users.find(
      (candidate) => Number(candidate.id) === Number(userId)
    );

    return matchedUser?.name || matchedUser?.username || "Unknown user";
  };

  const getGroupName = (groupId) => {
    if (groupId === null || groupId === undefined) {
      return "Unknown group";
    }

    return groupNames[String(groupId)] || "Unknown group";
  };

  const handleSettleUp = async (balance) => {
    if (!user?.id) {
      toast.error("Please log in again.");
      return;
    }

    const amount = Number(balance.amount || 0);

    if (amount <= 0) {
      toast.error("This balance cannot be settled.");
      return;
    }

    if (Number(balance.fromUserId) !== Number(user.id)) {
      toast.error("You can only settle balances that you owe.");
      return;
    }

    if (!balance.groupId || !balance.toUserId) {
      toast.error("Settlement information is incomplete.");
      return;
    }

    try {
      setPayingBalance(balance.id);

      const settlement = await createSettlement({
        groupId: Number(balance.groupId),

        fromUserId: Number(balance.fromUserId),

        toUserId: Number(balance.toUserId),

        amount,

        note: "Settlement via SplitBill",
      });

      if (!settlement?.id) {
        throw new Error("Settlement could not be created.");
      }

      const razorpayLoaded = await loadRazorpayScript();

      if (!razorpayLoaded) {
        throw new Error("Razorpay checkout could not be loaded.");
      }

      const order = await createPaymentOrder({
        userId: Number(user.id),

        groupId: Number(settlement.groupId),

        settlementId: Number(settlement.id),

        amount: Number(settlement.amount),

        description: "SplitBill settlement",
      });

      if (!order?.razorpayOrderId) {
        throw new Error("Payment order could not be created.");
      }

      const options = {
        key: order.razorpayKeyId,

        amount: Number(order.amount) * 100,

        currency: order.currency || "INR",

        name: "SplitBill",

        description: `Settlement with ${getUserName(balance.toUserId)}`,

        order_id: order.razorpayOrderId,

        prefill: {
          name: user.name || "",

          email: user.email || "",
        },

        theme: {
          color: "#475569",
        },

        handler: async (razorpayResponse) => {
          try {
            await verifyPayment({
              paymentId: order.paymentId,

              razorpayOrderId: razorpayResponse.razorpay_order_id,

              razorpayPaymentId: razorpayResponse.razorpay_payment_id,

              razorpaySignature: razorpayResponse.razorpay_signature,
            });

            toast.success("Settlement completed successfully.");

            await loadBalances();
          } catch (error) {
            toast.error(
              error.response?.data?.message || "Payment verification failed."
            );
          } finally {
            setPayingBalance(null);
          }
        },
      };

      const razorpay = new window.Razorpay(options);

      razorpay.on("payment.failed", () => {
        toast.error("Payment failed. Your balance was not settled.");

        setPayingBalance(null);
      });

      razorpay.open();
    } catch (error) {
      toast.error(
        error.response?.data?.message ||
          error.message ||
          "Unable to start settlement."
      );

      setPayingBalance(null);
    }
  };

  const youOwe = balances.filter(
    (balance) => Number(balance.fromUserId) === Number(user?.id)
  );

  const owedToYou = balances.filter(
    (balance) => Number(balance.toUserId) === Number(user?.id)
  );

  const oweTotal = youOwe.reduce(
    (sum, balance) => sum + Number(balance.amount || 0),
    0
  );

  const owedTotal = owedToYou.reduce(
    (sum, balance) => sum + Number(balance.amount || 0),
    0
  );

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-white">
          Balances
        </h1>

        <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
          See who owes whom and settle your outstanding balances.
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <div className="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900">
          <p className="text-sm text-slate-500 dark:text-slate-400">You owe</p>

          <p className="mt-2 text-3xl font-bold text-slate-900 dark:text-white">
            ₹{oweTotal.toFixed(2)}
          </p>
        </div>

        <div className="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900">
          <p className="text-sm text-slate-500 dark:text-slate-400">
            You are owed
          </p>

          <p className="mt-2 text-3xl font-bold text-slate-900 dark:text-white">
            ₹{owedTotal.toFixed(2)}
          </p>
        </div>
      </div>

      {loading ? (
        <div className="rounded-xl border border-slate-200 bg-white p-6 text-sm text-slate-500 dark:border-slate-800 dark:bg-slate-900">
          Loading balances...
        </div>
      ) : balances.length === 0 ? (
        <div className="rounded-xl border border-dashed border-slate-300 bg-white p-8 text-center dark:border-slate-700 dark:bg-slate-900">
          <p className="font-medium text-slate-900 dark:text-white">
            All settled
          </p>

          <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
            You currently have no outstanding balances.
          </p>
        </div>
      ) : (
        <div className="grid gap-3">
          {balances.map((balance) => {
            const isOwe = Number(balance.fromUserId) === Number(user?.id);

            const otherUserId = isOwe ? balance.toUserId : balance.fromUserId;

            const otherUserName = getUserName(otherUserId);

            const groupName = getGroupName(balance.groupId);

            const isPaying = payingBalance === balance.id;

            return (
              <div
                key={balance.id}
                className="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900"
              >
                <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                  <div className="min-w-0">
                    <p className="font-medium text-slate-900 dark:text-white">
                      {isOwe
                        ? `You owe ${otherUserName}`
                        : `${otherUserName} owes you`}
                    </p>

                    <p className="mt-1 text-xs font-medium text-slate-500 dark:text-slate-400">
                      {groupName}
                    </p>
                  </div>

                  <div className="flex items-center gap-4">
                    <p className="whitespace-nowrap text-lg font-bold text-slate-900 dark:text-white">
                      ₹{Number(balance.amount || 0).toFixed(2)}
                    </p>

                    {isOwe && (
                      <Button
                        type="button"
                        onClick={() => handleSettleUp(balance)}
                        loading={isPaying}
                        disabled={payingBalance !== null && !isPaying}
                      >
                        Settle Up
                      </Button>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}

export default Balances;
