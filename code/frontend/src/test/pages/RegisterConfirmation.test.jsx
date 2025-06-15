import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import RegisterConfirmation from "../../pages/RegisterConfirmation";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { BrowserRouter } from "react-router-dom";
import { AuthContext } from "../../contexts/AuthContext";
import { MemoryRouter } from "react-router-dom";

// Mock the AuthContext with a successful resendConfirmation
const mockResendConfirmation = vi.fn();
const mockRegisterConfirmation = vi.fn();

vi.mock("../../contexts/AuthContext", () => ({
  useAuth: () => ({
    registerConfirmation: mockRegisterConfirmation,
    resendConfirmation: mockResendConfirmation,
  }),
}));

const openResendDialog = async () => {
  // Open the resend dialog
  const resendButton = await screen.findByRole("button", { name: "Resend" });
  expect(resendButton).toBeInTheDocument();
  fireEvent.click(resendButton);

  // Wait for the dialog to appear
  await screen.findByText("Resend Confirmation Email");
};

describe("RegisterConfirmation Page", () => {
  beforeEach(async () => {
    render(
      <MemoryRouter>
        <RegisterConfirmation />
      </MemoryRouter>
    );
    mockResendConfirmation.mockReset();
    mockRegisterConfirmation.mockReset();

    const tokenElement = screen.getByTestId("confirmTokenId");
    expect(tokenElement).toBeInTheDocument();
  });

  it("Confirm button is disabled until token is entered", async () => {
    const confirmBttn = await screen.findByRole("button", { name: "Confirm" });
    expect(confirmBttn).toBeInTheDocument();
    expect(confirmBttn).toBeDisabled();

    // Verify the confirm button is disabled until non-empty data is entered
    const tokenElement = screen.getByTestId("confirmTokenId");
    fireEvent.change(tokenElement, { target: { value: "" } });
    expect(confirmBttn).toBeDisabled();
    fireEvent.change(tokenElement, { target: { value: "    " } });
    expect(confirmBttn).toBeDisabled();

    // Verify the confirm button is enabled when data is entered
    fireEvent.change(tokenElement, { target: { value: "test-token" } });
    expect(tokenElement.value).toBe("test-token");
    expect(confirmBttn).toBeEnabled();
  });

  it("Confirm button is disabled until valid token is entered", async () => {
    mockRegisterConfirmation.mockResolvedValue({
      success: false,
      error: { errorCode: "TOKEN_INVALID" },
    });

    const confirmBttn = await screen.findByRole("button", { name: "Confirm" });
    expect(confirmBttn).toBeInTheDocument();
    expect(confirmBttn).toBeDisabled();

    // Enter an invalid token and verify the button is enabled
    const tokenElement = screen.getByTestId("confirmTokenId");
    fireEvent.change(tokenElement, { target: { value: "invalid-token" } });
    expect(confirmBttn).toBeEnabled();
    fireEvent.click(confirmBttn);

    // Verify the error message appears
    const errorMessage = await screen.findByTestId("error-alert");
    expect(errorMessage).toBeInTheDocument();
    expect(confirmBttn).toBeDisabled();
  });

  it("Error alert is shown when invalid token is entered", async () => {
    const confirmBttn = await screen.findByRole("button", { name: "Confirm" });
    expect(confirmBttn).toBeInTheDocument();
    expect(confirmBttn).toBeDisabled();

    // Enter an invalid token and verify the button is enabled
    const tokenElement = screen.getByTestId("confirmTokenId");
    fireEvent.change(tokenElement, { target: { value: "invalid-token" } });
    expect(tokenElement.value).toBe("invalid-token");
    expect(confirmBttn).toBeEnabled();
    fireEvent.click(confirmBttn);

    // Verify the error message appears
    const errorMessage = await screen.findByTestId("error-alert");
    expect(errorMessage).toBeInTheDocument();
    expect(confirmBttn).toBeDisabled();
  });

  it("Given valid token, ", async () => {
    mockRegisterConfirmation.mockResolvedValue({
      success: true,
      error: null,
    });

    // Enter an invalid token and verify the button is enabled
    const confirmBttn = await screen.findByRole("button", { name: "Confirm" });
    const tokenElement = screen.getByTestId("confirmTokenId");
    fireEvent.change(tokenElement, {
      target: {
        value:
          "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30",
      },
    });
    expect(confirmBttn).toBeEnabled();
    fireEvent.click(confirmBttn);

    // Verify the success message appears
    const successMessage = await screen.findByTestId("success-alert");
    expect(successMessage).toBeInTheDocument();
    expect(successMessage).toHaveTextContent("Confirmation successful");
  });
});

describe("Resend Dialog", () => {
  beforeEach(async () => {
    render(
      <MemoryRouter>
        <RegisterConfirmation />
      </MemoryRouter>
    );

    await openResendDialog();
  });

  it("Opens with expected input fields", async () => {
    const usernameInput = screen.getByTestId("usernameId");
    expect(usernameInput).toBeInTheDocument();
    const emailInput = screen.getByTestId("emailId");
    expect(emailInput).toBeInTheDocument();
  });

  it("Nonempty username field is required", async () => {
    // Click the resend button without entering a username
    const resendConfirmButton = screen.getByRole("button", { name: "Resend" });
    fireEvent.click(resendConfirmButton);

    // Verify the error message for username
    const usernameError = screen.getByText("Username is required");
    expect(usernameError).toBeInTheDocument();

    // Verify the error message for username
    const usernameInput = screen.getByTestId("usernameId");
    fireEvent.change(usernameInput, { target: { value: "           " } });

    // Verify the error message for username
    const usernameErrorAfterChange = screen.getByText("Username is required");
    expect(usernameErrorAfterChange).toBeInTheDocument();
  });

  it("Valid email field is required", async () => {
    // Click the resend button without entering an email
    const resendConfirmButton = screen.getByRole("button", { name: "Resend" });
    fireEvent.click(resendConfirmButton);

    // Verify the error message when email is not entered
    let emailError = screen.getByText("Email is required");
    expect(emailError).toBeInTheDocument();

    // Verify the error message for email domain
    const emailInput = screen.getByTestId("emailId");
    fireEvent.change(emailInput, { target: { value: "testuser@bu.not.edu" } });
    emailError = screen.getByText("Must be '@bu.edu' email");
    expect(emailError).toBeInTheDocument();

    // Verify the error message for invalid email format
    fireEvent.change(emailInput, { target: { value: "invalid-email" } });
    emailError = screen.getByText("Invalid email format");
    expect(emailError).toBeInTheDocument();
  });

  it("Closes when username and valid BU email is entered", async () => {
    mockResendConfirmation.mockResolvedValue({ success: true });

    // Enter a username and email
    const usernameInput = screen.getByTestId("usernameId");
    fireEvent.change(usernameInput, { target: { value: "testuser" } });

    const emailInput = screen.getByTestId("emailId");
    fireEvent.change(emailInput, { target: { value: "testuser@bu.edu" } });

    // Click the resend button without entering an email
    const resendConfirmButton = screen.getByRole("button", { name: "Resend" });
    fireEvent.click(resendConfirmButton);

    // Verify the dialog closes
    await waitFor(() => {
      expect(screen.queryByTestId("resendDialogId")).not.toBeInTheDocument();
    });

    // Verify the mocked resendConfirmation was called
    expect(mockResendConfirmation).toHaveBeenCalledWith("testuser", "testuser@bu.edu");
  });

  it("Closes when username and valid BU email is entered and server is unreachable", async () => {
    mockResendConfirmation.mockResolvedValue({ success: false });

    // Enter a username and email
    const usernameInput = screen.getByTestId("usernameId");
    fireEvent.change(usernameInput, { target: { value: "testuser" } });

    const emailInput = screen.getByTestId("emailId");
    fireEvent.change(emailInput, { target: { value: "testuser@bu.edu" } });

    // Click the resend button without entering an email
    const resendConfirmButton = screen.getByRole("button", { name: "Resend" });
    fireEvent.click(resendConfirmButton);

    // Verify the dialog closes
    await waitFor(() => {
      expect(screen.queryByTestId("resendDialogId")).not.toBeInTheDocument();
    });

    // Verify the mocked resendConfirmation was called
    expect(mockResendConfirmation).toHaveBeenCalledWith("testuser", "testuser@bu.edu");
  });

  it("Closes when username and valid BU email is entered and server is unreachable", async () => {
    mockResendConfirmation.mockRejectedValue(new Error("Network error"));

    // Enter a username and email
    const usernameInput = screen.getByTestId("usernameId");
    fireEvent.change(usernameInput, { target: { value: "testuser" } });

    const emailInput = screen.getByTestId("emailId");
    fireEvent.change(emailInput, { target: { value: "testuser@bu.edu" } });

    // Click the resend button without entering an email
    const resendConfirmButton = screen.getByRole("button", { name: "Resend" });
    fireEvent.click(resendConfirmButton);

    // Verify the dialog closes
    await waitFor(() => {
      expect(screen.queryByTestId("resendDialogId")).not.toBeInTheDocument();
    });
  });
});
