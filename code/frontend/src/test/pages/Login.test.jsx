import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import Login from "../../pages/Login";
import { AuthProvider } from "../../contexts/AuthContext";
import userEvent from "@testing-library/user-event";

const mockNavigate = vi.fn();
vi.mock("react-router-dom", async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

const mockLoginInUseAuth = vi.fn();
vi.mock("../../contexts/AuthContext", () => {
  return {
    useAuth: () => ({
      login: mockLoginInUseAuth,
      user: { userId: "1", username: "testuser", userEmail: "test@test.com" },
      logout: vi.fn(),
    }),
    AuthProvider: ({ children }) => <div data-testid="MockAuthProvider">{children}</div>,
  };
});

describe("LoginPage Unit Test", () => {
  beforeEach(() => {
    mockNavigate.mockClear();
    mockLoginInUseAuth.mockClear();
  });

  it("should navigate to /home on successful login", async () => {
    mockLoginInUseAuth.mockResolvedValueOnce({
      success: true,
      userData: { userId: "1", username: "testuser", userEmail: "test@test.com" },
      error: null,
    });

    render(
      <AuthProvider>
        <MemoryRouter>
          <Login />
        </MemoryRouter>
      </AuthProvider>
    );
    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/username/i), "testuser");
    await user.type(screen.getByLabelText(/password/i), "correctpassword");
    await user.click(screen.getByRole("button", { name: /Login/i }));

    expect(mockLoginInUseAuth).toHaveBeenCalledWith("testuser", "correctpassword");

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledTimes(1);
      expect(mockNavigate).toHaveBeenCalledWith("/home", { replace: true });
    });
  });

  it("should display an error message on failed login", async () => {
    mockLoginInUseAuth.mockRejectedValueOnce({ success: false, error: "User or password is invalid" });

    render(
      <AuthProvider>
        <MemoryRouter>
          <Login />
        </MemoryRouter>
      </AuthProvider>
    );
    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/username/i), "testuser");
    await user.type(screen.getByLabelText(/password/i), "wrongpassword");
    await user.click(screen.getByRole("button", { name: /Login/i }));

    expect(mockLoginInUseAuth).toHaveBeenCalledWith("testuser", "wrongpassword");

    await waitFor(() => {
      expect(screen.getByText(/Login failed/i)).toBeInTheDocument();
    });
  });
});
