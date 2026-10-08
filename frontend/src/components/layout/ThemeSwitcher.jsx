import { useTheme } from "../../context/ThemeContext";
import { THEMES } from "../../utils/constants";

function ThemeSwitcher() {
  const { theme, changeTheme } = useTheme();

  return (
    <select
      value={theme}
      onChange={(event) => changeTheme(event.target.value)}
      className="theme-select"
      aria-label="Choose theme"
    >
      <option value={THEMES.LIGHT}>Light</option>

      <option value={THEMES.DARK}>Dark</option>

      <option value={THEMES.NIGHT}>Night</option>
    </select>
  );
}

export default ThemeSwitcher;
