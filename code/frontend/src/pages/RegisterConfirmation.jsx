import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import validator from "validator";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Container,
  Paper,
  TextField,
  Button,
  Typography,
  Box,
  Alert,
  LinearProgress,
} from "@mui/material";

/**
 * A dialog component for resending confirmation emails.
 * @param {Object} props - Component properties
 * @param {boolean} props.open - Whether the dialog is open
 * @param {function} props.onClose - Function to call when dialog is closed
 * @param {function} props.onResend - Function to call when resend is triggered
 * @returns
 */
const ResendDialog = ({ open, onClose, onResend }) => {
  const [loading, setLoading] = useState(false);
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [usernameError, setUsernameError] = useState("");
  const [emailError, setEmailError] = useState("");

  useEffect(() => {
    if (open) {
      setUsername("");
      setEmail("");
      setUsernameError("");
      setEmailError("");
    }
  }, [open]);

  const handleSubmit = (e) => {
    e.preventDefault();

    let hasError = false;
    if (username.trim() === "") {
      hasError = true;
      setUsernameError("Username is required");
    }
    if (email.trim() === "") {
      hasError = true;
      setEmailError("Email is required");
    }
    if (hasError) {
      return;
    }

    setLoading(true);
    onResend({ username, email });
    setLoading(false);
  };

  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle data-testid="resendDialogId">Resend Confirmation Email</DialogTitle>
      <DialogContent>
        <DialogContentText>
          Please enter your username and email. A confirmation email will be sent if the information matches your
          account.
        </DialogContentText>
        <form noValidate autoComplete="off" onSubmit={handleSubmit}>
          <TextField
            margin="normal"
            label="Username"
            type="text"
            slotProps={{ htmlInput: { "data-testid": "usernameId" } }}
            value={username}
            required
            fullWidth
            error={!!usernameError}
            helperText={usernameError}
            onChange={(e) => {
              setUsername(e.target.value);
              setUsernameError("");
              if (e.target.value.trim() === "") {
                setUsernameError("Username is required");
              }
            }}
          />
          <TextField
            margin="normal"
            label="Email"
            type="email"
            slotProps={{ htmlInput: { "data-testid": "emailId" } }}
            value={email}
            fullWidth
            required
            error={!!emailError}
            helperText={emailError}
            onChange={(e) => {
              setEmail(e.target.value);
              setEmailError("");
              if (!validator.isEmail(e.target.value)) {
                setEmailError("Invalid email format");
              } else if (!e.target.value.endsWith("@bu.edu")) {
                setEmailError("Must be '@bu.edu' email");
              }
            }}
          />

          <DialogActions>
            <Button disabled={loading} onClick={onClose}>
              Cancel
            </Button>
            <Button type="submit" data-testid="resendButtonId" disabled={loading} variant="contained">
              Resend
            </Button>
          </DialogActions>
        </form>
      </DialogContent>
    </Dialog>
  );
};

/**
 * A component for confirming user registration via email.
 */
const RegisterConfirmation = () => {
  const [confirmToken, setConfirmToken] = useState("");
  const [confirmationSuccess, setConfirmationSuccess] = useState(false);
  const [error, setError] = useState(undefined);
  const [loading, setLoading] = useState(false);
  const [resendOpen, setResendOpen] = useState(false);
  const navigate = useNavigate();
  const { registerConfirmation, resendConfirmation } = useAuth();

  const handleResend = async ({ username, email }) => {
    try {
      const result = await resendConfirmation(username, email);
      if (!result.success) {
        console.error("Resend failed:", result.error);
      }
    } catch (err) {
      console.error("Resend error:", err);
    } finally {
      setResendOpen(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(undefined);
    setLoading(true);
    if (confirmToken.trim() === "") {
      setError("Field cannot be empty");
      setLoading(false);
      return;
    }

    try {
      const { success, error } = await registerConfirmation(confirmToken);
      if (success) {
        setConfirmationSuccess(true);
        setTimeout(() => {
          navigate("/login", { replace: true });
        }, 1000);
      } else {
        if (error?.errorCode === "TOKEN_INVALID") {
          setError("Invalid confirmation token");
        } else {
          setError("Registration confirmation failed. Please try again.");
        }
      }
    } catch (err) {
      console.error("Unexpected error during registration", err);
      setError("Registration failed");
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    e.preventDefault();
    if (error) {
      setError(undefined);
    }
    setConfirmToken(e.target.value);
  };

  return (
    <Container component="main" maxWidth="xs">
      <Box sx={{ marginTop: 8, display: "flex", flexDirection: "column", alignItems: "center" }}>
        <Paper elevation={3} sx={{ p: 4, width: "100%" }}>
          <Typography component="h1" variant="h5" align="center" gutterBottom>
            Confirm Your Email
          </Typography>
          <Typography variant="body1" align="center" sx={{ mb: 3 }}>
            We've sent a code to your email. Please enter it below to complete your registration.
          </Typography>

          {loading && <LinearProgress sx={{ mb: 2 }} />}

          {error && (
            <Alert data-testid="error-alert" severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}
          {confirmationSuccess && (
            <Alert data-testid="success-alert" severity="success" sx={{ mb: 2 }}>
              Confirmation successful! Redirecting to login...
            </Alert>
          )}

          <form onSubmit={handleSubmit}>
            <TextField
              label="Registration Code"
              variant="outlined"
              slotProps={{ htmlInput: { "data-testid": "confirmTokenId" } }}
              fullWidth
              required
              value={confirmToken}
              onChange={handleInputChange}
              margin="normal"
              autoFocus
            />

            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
              disabled={loading || error !== undefined || confirmToken.trim() === ""}
            >
              Confirm
            </Button>
          </form>

          <Typography variant="body2" align="center" color="textSecondary" sx={{ mt: 2 }}>
            Didn’t receive the code?{" "}
            <Button size="small" variant="text" onClick={() => setResendOpen(true)}>
              Resend
            </Button>
          </Typography>
        </Paper>
        <ResendDialog open={resendOpen} onClose={() => setResendOpen(false)} onResend={handleResend} />
      </Box>
    </Container>
  );
};

export default RegisterConfirmation;
