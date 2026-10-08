import { useEffect, useState } from "react";
import toast from "react-hot-toast";

import Button from "../components/common/Button";
import Input from "../components/common/Input";

import {
  createPaymentOrder,
  getPaymentsByUser,
  verifyPayment,
} from "../api/paymentApi";

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

function Payments() {
  const { user } = useAuth();

  const [payments, setPayments] = useState([]);

  const [loading, setLoading] = useState(true);

  const [saving, setSaving] = useState(false);

  const [form, setForm] = useState({
    groupId: "",
    settlementId: "",
    amount: "",
    description: "",
  });

  const loadPayments = async () => {
    if (!user?.id) return;

    try {
      setLoading(true);

      const response = await getPaymentsByUser(user.id);

      setPayments(Array.isArray(response) ? response : []);
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to load payments.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPayments();
  }, [user?.id]);

  const handleChange = (event) => {
    setForm((previous) => ({
      ...previous,
      [event.target.name]: event.target.value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!form.amount || Number(form.amount) <= 0) {
      toast.error("Enter a valid amount.");
      return;
    }

    const razorpayLoaded = await loadRazorpayScript();

    if (!razorpayLoaded) {
      toast.error("Razorpay checkout could not be loaded.");
      return;
    }

    setSaving(true);

    try {
      const order = await createPaymentOrder({
        userId: Number(user.id),

        groupId: form.groupId ? Number(form.groupId) : null,

        settlementId: form.settlementId ? Number(form.settlementId) : null,

        amount: Number(form.amount),

        description: form.description.trim() || null,
      });

      const options = {
        key: order.razorpayKeyId,

        amount: Number(order.amount) * 100,

        currency: order.currency || "INR",

        name: "SplitBill",

        description: form.description || "SplitBill payment",

        order_id: order.razorpayOrderId,

        handler: async (razorpayResponse) => {
          try {
            await verifyPayment({
              paymentId: order.paymentId,

              razorpayOrderId: razorpayResponse.razorpay_order_id,

              razorpayPaymentId: razorpayResponse.razorpay_payment_id,

              razorpaySignature: razorpayResponse.razorpay_signature,
            });

            toast.success("Payment verified successfully.");

            setForm({
              groupId: "",
              settlementId: "",
              amount: "",
              description: "",
            });

            await loadPayments();
          } catch (error) {
            toast.error(
              error.response?.data?.message || "Payment verification failed."
            );
          }
        },

        prefill: {
          name: user.name || "",
          email: user.email || "",
        },

        theme: {
          color: "#475569",
        },
      };

      const razorpay = new window.Razorpay(options);

      razorpay.on("payment.failed", () => {
        toast.error("Payment failed.");
      });

      razorpay.open();
    } catch (error) {
      toast.error(
        error.response?.data?.message || "Unable to create payment order."
      );
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-white">
          Payments
        </h1>

        <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
          Make and review Razorpay test payments.
        </p>
      </div>

      <form
        onSubmit={handleSubmit}
        className="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900"
      >
        <h2 className="mb-5 text-lg font-semibold text-slate-900 dark:text-white">
          Make payment
        </h2>

        <div className="grid gap-4 md:grid-cols-2">
          <Input
            label="Amount"
            name="amount"
            type="number"
            min="0.01"
            step="0.01"
            value={form.amount}
            onChange={handleChange}
            placeholder="500"
            required
          />

          <Input
            label="Group ID (optional)"
            name="groupId"
            type="number"
            min="1"
            value={form.groupId}
            onChange={handleChange}
          />

          <Input
            label="Settlement ID (optional)"
            name="settlementId"
            type="number"
            min="1"
            value={form.settlementId}
            onChange={handleChange}
          />

          <Input
            label="Description"
            name="description"
            value={form.description}
            onChange={handleChange}
            maxLength={500}
            placeholder="Settlement payment"
          />
        </div>

        <div className="mt-4">
          <Button type="submit" loading={saving}>
            Pay with Razorpay
          </Button>
        </div>
      </form>

      <div className="space-y-3">
        {loading ? (
          <div className="rounded-xl border border-slate-200 bg-white p-6 text-sm text-slate-500 dark:border-slate-800 dark:bg-slate-900">
            Loading payments...
          </div>
        ) : payments.length === 0 ? (
          <div className="rounded-xl border border-dashed border-slate-300 bg-white p-8 text-center dark:border-slate-700 dark:bg-slate-900">
            <p className="font-medium text-slate-900 dark:text-white">
              No payments
            </p>
          </div>
        ) : (
          payments.map((payment) => (
            <div
              key={payment.id}
              className="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900"
            >
              <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                <div>
                  <p className="font-semibold text-slate-900 dark:text-white">
                    {payment.description || "Payment"}
                  </p>

                  <p className="mt-1 text-sm text-slate-500">Payment</p>

                  <p className="mt-1 text-xs text-slate-400">
                    Order: {payment.razorpayOrderId}
                  </p>
                </div>

                <div className="text-left md:text-right">
                  <p className="text-xl font-bold text-slate-900 dark:text-white">
                    {payment.currency} {Number(payment.amount || 0).toFixed(2)}
                  </p>

                  <span className="mt-1 inline-block rounded-full bg-slate-100 px-2.5 py-1 text-xs font-medium text-slate-700 dark:bg-slate-800 dark:text-slate-300">
                    {payment.status}
                  </span>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default Payments;
