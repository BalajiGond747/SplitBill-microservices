function Input({ label, error, className = "", ...props }) {
  const hasError = Boolean(error);

  return (
    <div className="space-y-1.5">
      {label && (
        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
          {label}
        </label>
      )}

      <input
        className={`
          w-full rounded-lg border bg-white px-3 py-2.5 text-sm
          text-slate-900 outline-none transition
          placeholder:text-slate-400

          dark:bg-slate-900 dark:text-slate-100

          ${
            hasError
              ? "border-red-500 focus:border-red-500 focus:ring-2 focus:ring-red-200 dark:border-red-500 dark:focus:border-red-500 dark:focus:ring-red-900"
              : "border-slate-300 focus:border-slate-500 focus:ring-2 focus:ring-slate-200 dark:border-slate-600 dark:focus:border-slate-400 dark:focus:ring-slate-800"
          }

          ${className}
        `}
        aria-invalid={hasError}
        {...props}
      />

      {hasError && (
        <p className="text-sm text-red-600 dark:text-red-400">{error}</p>
      )}
    </div>
  );
}
export default Input;
