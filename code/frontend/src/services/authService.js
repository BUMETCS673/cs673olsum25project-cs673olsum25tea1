import api from "./api";
import { jwtUtils } from "../utils/jwt";

export const authService = {
  register: async (username, email, password) => {
    try {
      const response = await api.post("/register", {
        username,
        email,
        password,
      });
      console.log("Registration response:", response.data.data);
      return {
        success: true,
        token: response.data.data?.token,
        error: null,
      };
    } catch (error) {
      let errorMessage = "";
      if (error.response) {
        // Server responsed with an error
        errorMessage = error.response?.data?.errors || "Registration failed";
      } else if (error.request) {
        // No response received from the server
        errorMessage = "Registration is currently unavailable. Please try again later.";
        console.error("Server did not respond:", error.request);
      }

      return {
        success: false,
        error: errorMessage,
      };
    }
  },

  /**
   * Resends the registration code based on the provided username and email.
   * @param {string} username used to identify the user
   * @param {string} email used to identify the user
   * @returns {Promise<{success: boolean, error: (string|null)}>} The result of the request.
   */
  resendConfirmation: async (username, email) => {
    try {
      await api.post("/register/confirmation/resend", { username, email });
      return { success: true, error: null };
    } catch (error) {
      let errorMessage = "";
      if (error.response) {
        // Server responsed with an error
        console.error("Error response from resend confirmation:", error);
        errorMessage = error.response?.data?.errors || "Unable to resend confirmation";
      } else if (error.request) {
        // No response received from the server
        errorMessage = "Server is currently unavailable. Please try again later.";
        console.error("Server did not respond to resend confirmation request:", error.request);
      }
      return {
        success: false,
        error: errorMessage,
      };
    }
  },

  registerConfirmation: async (token) => {
    try {
      await api.post("/register/confirmation", { token });
      return { success: true, error: null };
    } catch (error) {
      let errorMessage = "";
      if (error.response) {
        // Server responsed with an error
        errorMessage = error.response?.data?.errors || "Registration confirmation failed";
      } else if (error.request) {
        // No response received from the server
        errorMessage = "Registration is currently unavailable. Please try again later.";
        console.error("Server did not respond:", error.request);
      }

      return {
        success: false,
        error: errorMessage,
      };
    }
  },

  login: async (username, password) => {
    try {
      const response = await api.post("/login", { username, password });
      const { data } = response.data;

      jwtUtils.setToken("auth_token", data.token);
      //jwtUtils.setToken('refresh_token', refreshToken);
      localStorage.setItem(
        "userData",
        JSON.stringify({ userId: data.userId, username: data.username, userEmail: data.email })
      );

      return {
        success: true,
        userData: {
          userId: data.userId,
          username: data.username,
          userEmail: data.email,
        },
        error: null,
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || "Login failed",
      };
    }
  },

  logout: async () => {
    try {
      await api.post("/auth/logout");
    } finally {
      jwtUtils.removeToken("auth_token");
      jwtUtils.removeToken("refresh_token");
      localStorage.removeItem("userData");
    }
  },

  getCurrentUser: async () => {
    const storedUserData = localStorage.getItem("userData");
    const token = jwtUtils.getToken("auth_token");

    if (storedUserData && token && !jwtUtils.isTokenExpired(token)) {
      try {
        return JSON.parse(storedUserData);
      } catch (error) {
        jwtUtils.removeToken("auth_token");
        localStorage.removeItem("userData");
        return null;
      }
    } else {
      jwtUtils.removeToken("auth_token");
      localStorage.removeItem("userData");
      return null;
    }
  },

  isAuthenticated: () => {
    const token = jwtUtils.getToken("auth_token");
    return Boolean(token && !jwtUtils.isTokenExpired(token));
  },
};
