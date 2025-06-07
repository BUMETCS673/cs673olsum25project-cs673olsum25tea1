import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";

const ProtectedRoute = ({ children }) => {
  const { user, loading } = useAuth();
  const location = useLocation();
  if (loading) return <div>Loading...</div>;
  else if (!user) return <Navigate to="/login" state={{ from: location }} replace />;
  else return children;
};

export default ProtectedRoute;