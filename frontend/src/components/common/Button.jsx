function Button({
  children,
  type = "button",
  variant = "primary",
  loading = false,
  disabled = false,
  className = "",
  ...props
}) {
  const baseClasses =
    "inline-flex items-center justify-center rounded-lg px-4 py-2.5 text-sm font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-slate-400 disabled:cursor-not-allowed disabled:opacity-50";

  const variants = {
    primary:
      "bg-slate-800 text-white hover:bg-slate-700 dark:bg-slate-200 dark:text-slate-900 dark:hover:bg-white",

    secondary:
      "border border-slate-300 bg-white text-slate-700 hover:bg-slate-100 dark:border-slate-600 dark:bg-slate-800 dark:text-slate-200 dark:hover:bg-slate-700",

    danger: "bg-red-600 text-white hover:bg-red-700",

    ghost:
      "text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800",
  };

  return (
    <button
      type={type}
      disabled={disabled || loading}
      className={`${baseClasses} ${variants[variant]} ${className}`}
      {...props}
    >
      {loading ? "Please wait..." : children}
    </button>
  );
}

export default Button;
