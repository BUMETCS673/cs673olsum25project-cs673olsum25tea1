import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import validator from "validator";
import { Container, Paper, TextField, Button, Typography, Box, Alert, LinearProgress } from "@mui/material";

const Registration = () => {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState(undefined);
  const [registerSuccess, setRegisterSuccess] = useState(false);
  const [validationErrors, setValidationErrors] = useState(undefined);
  const [loading, setLoading] = useState(false);
  const [passwordStrength, setPasswordStrength] = useState(undefined);
  const navigate = useNavigate();
  const { register } = useAuth();

  /**
   * Calculates the strength of a password.
   * @param {*} password to evaluate
   * @returns {number} score from 0 to 4 based on password strength
   */
  const getPasswordStrength = (password) => {
    let score = 0;
    if (password.length >= 8) score += 1;
    if (/[A-Z]/.test(password)) score += 1;
    if (/[0-9]/.test(password)) score += 1;
    if (/[^A-Za-z0-9]/.test(password)) score += 1;
    return score;
  };

  /**
   * Defines password strength levels.
   */
  const PasswordStrength = {
    VERY_WEAK: { label: "Very Weak", color: "gray" },
    WEAK: { label: "Weak", color: "red" },
    MEDIUM: { label: "Medium", color: "orange" },
    STRONG: { label: "Strong", color: "green" },
    VERY_STRONG: { label: "Very Strong", color: "darkgreen" },
  };

  /**
   * Maps password strength score to a label.
   * @param {int} score from getPasswordStrength function
   * @returns {string} corresponding strength label
   */
  const getStrengthLabel = (score) => {
    switch (score) {
      case 1:
        return PasswordStrength.WEAK;
      case 2:
        return PasswordStrength.MEDIUM;
      case 3:
        return PasswordStrength.STRONG;
      case 4:
        return PasswordStrength.VERY_STRONG;
      default:
        return PasswordStrength.VERY_WEAK;
    }
  };

  const getNextLevelHint = (password) => {
    if (password.length < 8) return "Add more characters to make it at least 8.";
    if (!/[A-Z]/.test(password)) return "Add an uppercase letter.";
    if (!/[0-9]/.test(password)) return "Add a number.";
    if (!/[^A-Za-z0-9]/.test(password)) return "Add a special character.";
    return "";
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(undefined);
    setValidationErrors(undefined);
    setLoading(true);

    if (!validator.isEmail(email)) {
      setError("Invalid email address");
      setLoading(false);
      return;
    }

    if (password.length < 8 || !/[A-Z]/.test(password) || !/[0-9]/.test(password)) {
      setError("Password must be at least 8 characters long, contain an uppercase letter, and a number");
      setLoading(false);
      return;
    }

    if (password !== confirmPassword) {
      setError("Passwords do not match");
      setLoading(false);
      return;
    }

    try {
      const { success, error: registerError } = await register(username, email, password);
      if (success) {
        setRegisterSuccess(true);
        setTimeout(() => {
          navigate("/register/confirmation", { replace: true });
        }, 1000);
      } else {
        console.error("Registration failed:", registerError);
        if (registerError?.errorCode == "EMAIL_USERNAME_TAKEN") {
          setError("Username or email already taken");
        } else if (registerError?.validationErrors) {
          setValidationErrors(registerError.validationErrors);
        }
      }
    } catch (err) {
      console.error("Unexpected error during registration", err);
      setError("Registration failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container component="main" maxWidth="xs">
      <Box
        sx={{
          marginTop: 8,
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
        }}
      >
        <Paper elevation={3} sx={{ p: 4, width: "100%" }}>
          <Typography component="h1" variant="h5" align="center" gutterBottom>
            Register
          </Typography>
          {(error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )) ||
            (validationErrors && (
              <Alert severity="error" sx={{ mb: 2 }}>
                {typeof validationErrors === "string" ? (
                  validationErrors
                ) : (
                  <ul style={{ margin: 0, paddingLeft: "1.2em" }}>
                    {Object.values(validationErrors)
                      .flat()
                      .sort((a, b) => a.localeCompare(b))
                      .map((msg, idx) => (
                        <li key={idx}>{msg}</li>
                      ))}
                  </ul>
                )}
              </Alert>
            ))}
          {registerSuccess && (
            <Alert severity="success" sx={{ mb: 2 }}>
              Registration successful! Redirecting to confirmation...
            </Alert>
          )}

          <form onSubmit={handleSubmit}>
            <TextField
              margin="normal"
              required
              fullWidth
              label="Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={loading}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              label="Email"
              type="email"
              value={email}
              onChange={(e) => {
                setEmail(e.target.value);
                if (!validator.isEmail(e.target.value)) {
                  setError("Invalid email format");
                } else if (!e.target.value.endsWith("@bu.edu")) {
                  setError("Email must be a BU email (e.g., example@bu.edu)");
                } else {
                  setError("");
                }
              }}
              disabled={loading}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              label="Password"
              id="password"
              type="password"
              value={password}
              onChange={(e) => {
                setPassword(e.target.value);
                const score = getPasswordStrength(e.target.value);
                setPasswordStrength(getStrengthLabel(score));
              }}
              disabled={loading}
            />
            <LinearProgress
              variant="determinate"
              value={(getPasswordStrength(password) / 4) * 100}
              sx={{ height: 10, borderRadius: 5, backgroundColor: "lightgray", mt: 1 }}
            />
            <Typography variant="body2" align="left" sx={{ color: passwordStrength?.color || "", mt: 1 }}>
              {passwordStrength?.label}
            </Typography>
            <Typography variant="body2" align="left" sx={{ color: "gray", mt: 1 }}>
              {getNextLevelHint(password)}
            </Typography>
            <TextField
              margin="normal"
              required
              fullWidth
              label="Confirm Password"
              id="confirmpassword"
              type="password"
              value={confirmPassword}
              onChange={(e) => {
                setConfirmPassword(e.target.value);
                if (e.target.value !== password) {
                  setError("Passwords do not match");
                } else {
                  setError("");
                }
              }}
              disabled={loading}
            />
            <Button type="submit" fullWidth variant="contained" sx={{ mt: 3, mb: 2 }} disabled={loading}>
              {loading ? "Registering..." : "Register"}
            </Button>
          </form>
        </Paper>
      </Box>
    </Container>
  );
};

export default Registration;
