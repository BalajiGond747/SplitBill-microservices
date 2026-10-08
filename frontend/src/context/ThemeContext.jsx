import { createContext, useContext, useEffect, useState } from "react";

import { THEMES } from "../utils/constants";

const ThemeContext = createContext(null);

function getInitialTheme() {
  const savedTheme = localStorage.getItem("splitbill_theme");

  if (savedTheme && Object.values(THEMES).includes(savedTheme)) {
    return savedTheme;
  }

  return THEMES.LIGHT;
}

export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState(getInitialTheme);

  useEffect(() => {
    const root = document.documentElement;

    root.setAttribute("data-theme", theme);

    if (theme === THEMES.DARK || theme === THEMES.NIGHT) {
      root.classList.add("dark");
    } else {
      root.classList.remove("dark");
    }

    localStorage.setItem("splitbill_theme", theme);
  }, [theme]);

  const changeTheme = (nextTheme) => {
    if (Object.values(THEMES).includes(nextTheme)) {
      setTheme(nextTheme);
    }
  };

  return (
    <ThemeContext.Provider
      value={{
        theme,
        changeTheme,
      }}
    >
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  const context = useContext(ThemeContext);

  if (!context) {
    throw new Error("useTheme must be used inside ThemeProvider");
  }

  return context;
}
