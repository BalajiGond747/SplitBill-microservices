function LoadingSpinner({ size = "md" }) {
  const sizes = {
    sm: "h-4 w-4",
    md: "h-6 w-6",
    lg: "h-10 w-10",
  };

  return (
    <div
      className={`${sizes[size]} animate-spin rounded-full border-2 border-slate-300 border-t-slate-700 dark:border-slate-700 dark:border-t-slate-200`}
    />
  );
}

export default LoadingSpinner;
