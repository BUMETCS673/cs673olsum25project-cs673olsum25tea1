import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import { useAuth } from "../../contexts/AuthContext";
import ProtectedRoute from "../../components/ProtectedRoute";

const mockNavigate = vi.fn();
const mockUseLocation = vi.fn();
vi.mock("react-router-dom", async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    Navigate: (props) => {
      mockNavigate(props);
      return null;
    },
    useLocation: () => mockUseLocation(),
  };
});

vi.mock("../../contexts/AuthContext", () => ({
  useAuth: vi.fn(),
}));

describe("<ProtectedRoute />", () => {
  const mockLocation = { pathname: "/protected-page", search: "", hash: "", state: null };

  beforeEach(() => {
    mockNavigate.mockClear();
    mockUseLocation.mockReturnValue(mockLocation);
  });

  it("should render loading state when loading is true", () => {
    useAuth.mockReturnValue({ user: null, loading: true });
    render(
      <MemoryRouter>
        <ProtectedRoute>
          <div>Protected Component</div>
        </ProtectedRoute>
      </MemoryRouter>
    );
    expect(screen.getByText("Loading...")).toBeInTheDocument();
    expect(screen.queryByText("Protected Component")).not.toBeInTheDocument();
  });

  it("should navigate to /login if not authenticated and not loading", () => {
    useAuth.mockReturnValue({ user: null, loading: false });
    render(
      <MemoryRouter initialEntries={["/protected-page"]}>
        <Routes>
          <Route
            path="/protected-page"
            element={
              <ProtectedRoute>
                <div>Protected Component</div>
              </ProtectedRoute>
            }
          />
          <Route path="/login" element={<div>Login Page</div>} />
        </Routes>
      </MemoryRouter>
    );

    expect(mockNavigate).toHaveBeenCalledTimes(1);
    expect(mockNavigate).toHaveBeenCalledWith({
      to: "/login",
      state: { from: mockLocation },
      replace: true,
    });
    expect(screen.queryByText("Protected Component")).not.toBeInTheDocument();
  });

  it("should render children if authenticated and not loading", () => {
    useAuth.mockReturnValue({ user: { id: "1", name: "Test User" }, loading: false });
    render(
      <MemoryRouter>
        <ProtectedRoute>
          <div data-testid="child">Protected Component Content</div>
        </ProtectedRoute>
      </MemoryRouter>
    );
    expect(screen.getByTestId("child")).toBeInTheDocument();
    expect(screen.getByText("Protected Component Content")).toBeInTheDocument();
    expect(screen.queryByText("Loading...")).not.toBeInTheDocument();
    expect(mockNavigate).not.toHaveBeenCalled();
  });
});
