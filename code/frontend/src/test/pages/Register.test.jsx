import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi, beforeEach } from "vitest";
import Registration from "../../pages/Registration";
import { AuthProvider } from "../../contexts/AuthContext";
import { MemoryRouter } from "react-router-dom";

const mockNavigate = vi.fn();
vi.mock("react-router-dom", async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

const mockRegister = vi.fn();
vi.mock("../../contexts/AuthContext", () => ({
  useAuth: () => ({
    register: mockRegister,
  }),
  AuthProvider: ({ children }) => <div data-testid="MockAuthProvider">{children}</div>,
}));

describe("Registration Component Tests", () => {
  beforeEach(() => {
    mockRegister.mockClear();
    mockNavigate.mockClear();
  });

  it("should allow a user to register successfully and navigate", async () => {
    mockRegister.mockResolvedValueOnce({ success: true, error: null });

    render(
      <AuthProvider>
        <MemoryRouter>
          <Registration />
        </MemoryRouter>
      </AuthProvider>
    );
    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/Username/i), "testuser");
    await user.type(screen.getByLabelText(/Email/i), "testuser@bu.edu");
    await user.type(screen.getAllByLabelText(/Password/i)[0], "Password123!");
    await user.type(screen.getAllByLabelText(/Confirm Password/i)[0], "Password123!");
    await user.click(screen.getByRole("button", { name: /REGISTER/i }));

    await waitFor(() => {
      expect(mockRegister).toHaveBeenCalledWith("testuser", "testuser@bu.edu", "Password123!");
      expect(screen.getByText(/registration successful/i)).toBeInTheDocument();
    });
    await waitFor(
      () => {
        expect(mockNavigate).toHaveBeenCalledWith("/register/confirmation", { replace: true });
      },
      { timeout: 2000 }
    );
  });

  it("should display an error message if registration fails (e.g., email exists)", async () => {
    mockRegister.mockResolvedValueOnce({ success: false, error: { errorCode: "EMAIL_USERNAME_TAKEN" } });

    render(
      <AuthProvider>
        <MemoryRouter>
          <Registration />
        </MemoryRouter>
      </AuthProvider>
    );
    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/Username/i), "testuser");
    await user.type(screen.getByLabelText(/Email/i), "testuser@bu.edu");
    await user.type(screen.getAllByLabelText(/Password/i)[0], "Password123!");
    await user.type(screen.getAllByLabelText(/Confirm Password/i)[0], "Password123!");
    await user.click(screen.getByRole("button", { name: /REGISTER/i }));

    await waitFor(() => {
      expect(mockRegister).toHaveBeenCalledWith("testuser", "testuser@bu.edu", "Password123!");
      expect(screen.getByText(/Username or email already taken/i)).toBeInTheDocument();
      expect(mockNavigate).not.toHaveBeenCalled();
    });
  });

  it("should display an error message if registration fails other than email or username exists", async () => {
    mockRegister.mockRejectedValueOnce(new Error("Registration failed"));

    render(
      <AuthProvider>
        <MemoryRouter>
          <Registration />
        </MemoryRouter>
      </AuthProvider>
    );
    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/Username/i), "testuser");
    await user.type(screen.getByLabelText(/Email/i), "testuser@bu.edu");
    await user.type(screen.getAllByLabelText(/Password/i)[0], "Password123!");
    await user.type(screen.getAllByLabelText(/Confirm Password/i)[0], "Password123!");
    await user.click(screen.getByRole("button", { name: /REGISTER/i }));

    await waitFor(() => {
      expect(mockRegister).toHaveBeenCalledWith("testuser", "testuser@bu.edu", "Password123!");
      expect(screen.getByText(/Registration failed/i)).toBeInTheDocument();
      expect(mockNavigate).not.toHaveBeenCalled();
    });
  });

  it("should display a client-side validation error if passwords do not match", async () => {
    mockRegister.mockResolvedValueOnce({ success: true, error: null });

    render(
      <AuthProvider>
        <MemoryRouter>
          <Registration />
        </MemoryRouter>
      </AuthProvider>
    );
    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/Username/i), "testuser");
    await user.type(screen.getByLabelText(/Email/i), "testuser@bu.edu");
    await user.type(screen.getAllByLabelText(/Password/i)[0], "Password123!");
    await user.type(screen.getAllByLabelText(/Confirm Password/i)[0], "DifferentPassword123!");

    await waitFor(() => {
      expect(screen.getByText(/Passwords do not match/i)).toBeInTheDocument();
    });

    expect(mockRegister).not.toHaveBeenCalled();
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it("should display a client-side validation error if email is not valid", async () => {
    mockRegister.mockResolvedValueOnce({ success: true, error: null });

    render(
      <AuthProvider>
        <MemoryRouter>
          <Registration />
        </MemoryRouter>
      </AuthProvider>
    );
    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/Username/i), "testuser");
    await user.type(screen.getByLabelText(/Email/i), "testuser");

    await waitFor(() => {
      expect(screen.getByText(/Invalid email format/i)).toBeInTheDocument();
    });

    expect(mockRegister).not.toHaveBeenCalled();
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it("should display a client-side validation error if email is not a BU email", async () => {
    mockRegister.mockResolvedValueOnce({ success: true, error: null });

    render(
      <AuthProvider>
        <MemoryRouter>
          <Registration />
        </MemoryRouter>
      </AuthProvider>
    );
    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/Username/i), "testuser");
    await user.type(screen.getByLabelText(/Email/i), "testuser@gmail.com");

    await waitFor(() => {
      expect(screen.getByText(/Email must be a BU email/i)).toBeInTheDocument();
    });

    expect(mockRegister).not.toHaveBeenCalled();
    expect(mockNavigate).not.toHaveBeenCalled();
  });
});
