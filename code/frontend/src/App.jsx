import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./contexts/AuthContext";
import { Navigate } from "react-router-dom";
import ProtectedRoute from "./components/ProtectedRoute";
import Login from "./pages/Login";
import Home from "./pages/Home";
import Registration from "./pages/Registration";
import RegisterConfirmation from "./pages/RegisterConfirmation";
import CreateActivity from "./pages/CreateActivity";

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register/confirmation" element={<RegisterConfirmation />} />
          <Route path="/register" element={<Registration />} />
          <Route
            path="/create-activity"
            element={
              <ProtectedRoute>
                <CreateActivity />
              </ProtectedRoute>
            }
          />
          <Route
            path="/home"
            element={
              <ProtectedRoute>
                <Home />
              </ProtectedRoute>
            }
          />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
