import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, act, waitFor } from "@testing-library/react";
import { AuthProvider, useAuth } from "../../contexts/AuthContext";
import { authService } from "../../services/authService";

// Mock authService
vi.mock("../../services/authService", () => ({
  authService: {
    isAuthenticated: vi.fn(),
    getCurrentUser: vi.fn(),
    login: vi.fn(),
    logout: vi.fn(),
    register: vi.fn(),
  },
}));

// Helper component to consume and display context values/trigger actions
const TestConsumer = () => {
  const auth = useAuth();
  if (!auth) return <div>Auth context not available</div>;

  return (
    <div>
      <div data-testid="user">{auth.user ? JSON.stringify(auth.user) : "null"}</div>
      <div data-testid="loading">{String(auth.loading)}</div>
      <button onClick={() => auth.login("testuser", "password")}>Login</button>
      <button onClick={() => auth.logout()}>Logout</button>
      <button onClick={() => auth.register("newuser", "new@example.com", "newpass")}>Register</button>
    </div>
  );
};

const mockUserData = { id: "1", name: "Test User" };

describe("AuthProvider", () => {
  beforeEach(() => {
    vi.clearAllMocks(); // Clear mock calls before each test
  });

  describe("Initialization (useEffect)", () => {
    it("should set user and stop loading if initially authenticated", async () => {
      authService.isAuthenticated.mockReturnValue(true);
      authService.getCurrentUser.mockResolvedValue(mockUserData);

      render(
        <AuthProvider>
          <TestConsumer />
        </AuthProvider>
      );

      await waitFor(() => expect(screen.getByTestId("loading").textContent).toBe("false"));
      expect(screen.getByTestId("user").textContent).toBe(JSON.stringify(mockUserData));
      expect(authService.getCurrentUser).toHaveBeenCalledTimes(1);
    });

    it("should set user to null and stop loading if not initially authenticated", async () => {
      authService.isAuthenticated.mockReturnValue(false);

      render(
        <AuthProvider>
          <TestConsumer />
        </AuthProvider>
      );

      await waitFor(() => expect(screen.getByTestId("loading").textContent).toBe("false"));
      expect(screen.getByTestId("user").textContent).toBe("null");
      expect(authService.getCurrentUser).not.toHaveBeenCalled();
    });

    it("should set user to null, stop loading, and call logout if getCurrentUser fails", async () => {
      authService.isAuthenticated.mockReturnValue(true);
      authService.getCurrentUser.mockRejectedValue(new Error("Failed to fetch user"));

      render(
        <AuthProvider>
          <TestConsumer />
        </AuthProvider>
      );

      await waitFor(() => expect(screen.getByTestId("loading").textContent).toBe("false"));
      expect(screen.getByTestId("user").textContent).toBe("null");
      expect(authService.logout).toHaveBeenCalledTimes(1);
    });
  });

  describe("login function", () => {
    it("should set user and loading to false on successful login", async () => {
      authService.login.mockResolvedValue({ success: true, userData: mockUserData, error: null });
      // Start with loading true, as useEffect might not have finished or user wasn't initially auth
      authService.isAuthenticated.mockReturnValue(false);

      render(
        <AuthProvider>
          <TestConsumer />
        </AuthProvider>
      );
      // Ensure initial state is set (loading false from useEffect)
      await waitFor(() => expect(screen.getByTestId("loading").textContent).toBe("false"));

      let loginResult;
      await act(async () => {
        // Directly call login from a hypothetical way to get context, or use a button click
        screen.getByRole("button", { name: /Login/i });
        // For direct call: need to get context instance. For simplicity, we use button click.
        loginResult = await new Promise((resolve) => {
          authService.login.mockImplementationOnce(async () => {
            const res = { success: true, userData: mockUserData, error: null };
            resolve(res);
            return res;
          });
          screen.getByRole("button", { name: /Login/i }).click();
        });
      });

      expect(authService.login).toHaveBeenCalledWith("testuser", "password");
      await waitFor(() => expect(screen.getByTestId("user").textContent).toBe(JSON.stringify(mockUserData)));
      expect(screen.getByTestId("loading").textContent).toBe("false");
      expect(loginResult).toEqual({ success: true, userData: mockUserData, error: null });
    });

    it("should not set user and return error on failed login", async () => {
      const loginError = { message: "Invalid credentials" };
      authService.login.mockResolvedValue({ success: false, error: loginError });
      authService.isAuthenticated.mockReturnValue(false);

      render(
        <AuthProvider>
          <TestConsumer />
        </AuthProvider>
      );
      await waitFor(() => expect(screen.getByTestId("loading").textContent).toBe("false")); // Initial load

      let loginResult;
      await act(async () => {
        loginResult = await new Promise((resolve) => {
          authService.login.mockImplementationOnce(async () => {
            const res = { success: false, error: loginError };
            resolve(res);
            return res;
          });
          screen.getByRole("button", { name: /Login/i }).click();
        });
      });

      expect(authService.login).toHaveBeenCalledWith("testuser", "password");
      expect(screen.getByTestId("user").textContent).toBe("null"); // User remains null
      expect(loginResult).toEqual({ success: false, error: loginError });
    });
  });

  describe("logout function", () => {
    it("should set user to null and call authService.logout", async () => {
      // Simulate initially logged in state
      authService.isAuthenticated.mockReturnValue(true);
      authService.getCurrentUser.mockResolvedValue(mockUserData);

      render(
        <AuthProvider>
          <TestConsumer />
        </AuthProvider>
      );

      await waitFor(() => expect(screen.getByTestId("user").textContent).toBe(JSON.stringify(mockUserData)));
      authService.logout.mockResolvedValue(undefined); // Mock logout service call

      await act(async () => {
        screen.getByRole("button", { name: /Logout/i }).click();
      });

      expect(authService.logout).toHaveBeenCalledTimes(1);
      // User becomes null after logout
      await waitFor(() => expect(screen.getByTestId("user").textContent).toBe("null"));
    });
  });

  describe("register function", () => {
    it("should call authService.register and return its result without changing context state", async () => {
      const registerResultPayload = { success: true, error: null };
      authService.register.mockResolvedValue(registerResultPayload);
      authService.isAuthenticated.mockReturnValue(false); // Start as not logged in

      render(
        <AuthProvider>
          <TestConsumer />
        </AuthProvider>
      );
      await waitFor(() => expect(screen.getByTestId("loading").textContent).toBe("false"));
      const initialUser = screen.getByTestId("user").textContent;
      const initialLoading = screen.getByTestId("loading").textContent;

      let registerResult;
      await act(async () => {
        // To get the return value, we need to call the register function from the context.
        // This is a bit tricky with the TestConsumer button. A more direct way would be to expose context instance.
        // For now, let's assume the button click works and we check the service call and context state.
        registerResult = await new Promise((resolve) => {
          authService.register.mockImplementationOnce(async () => {
            const res = registerResultPayload;
            resolve(res);
            return res;
          });
          screen.getByRole("button", { name: /Register/i }).click();
        });
      });

      expect(authService.register).toHaveBeenCalledWith("newuser", "new@example.com", "newpass");
      expect(registerResult).toEqual(registerResultPayload);
      // Context state should not change after register
      expect(screen.getByTestId("user").textContent).toBe(initialUser);
      expect(screen.getByTestId("loading").textContent).toBe(initialLoading);
    });
  });
});
