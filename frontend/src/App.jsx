import { BrowserRouter } from "react-router-dom";
import { Toaster } from "react-hot-toast";

import AppRoutes from "./routes/AppRoutes";
import { AuthProvider } from "./context/AuthContext";
import { ThemeProvider } from "./context/ThemeContext";

function App() {
  return (
    <BrowserRouter>
      <ThemeProvider>
        <AuthProvider>
          <AppRoutes />

          <Toaster
            position="top-right"
            toastOptions={{
              duration: 3000,
              style: {
                background: "#1e293b",
                color: "#f8fafc",
                border: "1px solid #475569",
              },
            }}
          />
        </AuthProvider>
      </ThemeProvider>
    </BrowserRouter>
  );
}

export default App;
