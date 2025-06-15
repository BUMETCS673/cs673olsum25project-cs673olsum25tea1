import axios from "axios";

const API_BASE_URL = "/api/users";

export const userService = {
  // Fetch all users
  getUsers: async () => {
    try {
      const response = await axios.get(API_BASE_URL);
      return response.data; // Return the list of users
    } catch (error) {
      console.error("Error fetching users:", error);
      throw error;
    }
  },

  // Create a new user
  createUser: async (userData) => {
    try {
      const response = await axios.post(API_BASE_URL, userData);
      return response.data; // Return the created user
    } catch (error) {
      console.error("Error creating user:", error);
      throw error;
    }
  },

  // Update a user
  updateUser: async (userId, userData) => {
    try {
      const response = await axios.put(`${API_BASE_URL}/${userId}`, userData);
      return response.data; // Return the updated user
    } catch (error) {
      console.error("Error updating user:", error);
      throw error;
    }
  },

  // Delete a user
  deleteUser: async (userId) => {
    try {
      const response = await axios.delete(`${API_BASE_URL}/${userId}`);
      return response.data; // Return the result of the deletion
    } catch (error) {
      console.error("Error deleting user:", error);
      throw error;
    }
  },
};
