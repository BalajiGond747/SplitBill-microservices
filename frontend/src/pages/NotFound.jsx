import { Link } from "react-router-dom";

function NotFound() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 px-4 dark:bg-slate-950">
      <div className="text-center">
        <p className="text-6xl font-bold text-slate-300 dark:text-slate-700">
          404
        </p>

        <h1 className="mt-4 text-2xl font-bold text-slate-900 dark:text-white">
          Page not found
        </h1>

        <p className="mt-2 text-slate-500 dark:text-slate-400">
          The page you're looking for doesn't exist.
        </p>

        <Link
          to="/"
          className="mt-6 inline-block rounded-lg bg-slate-800 px-4 py-2.5 text-sm font-medium text-white hover:bg-slate-700 dark:bg-slate-200 dark:text-slate-900 dark:hover:bg-white"
        >
          Go home
        </Link>
      </div>
    </div>
  );
}

export default NotFound;
