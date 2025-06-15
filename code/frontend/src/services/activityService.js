import api from "./api";

export const activityService = {
  getRecentActivities: async () => {
    const response = await api.get("/activities");
    return response.data.data.content;
  },
  getParticipantActivities: async () => {
    const response = await api.get("/activity/participants");
    return response.data.data.activities;
  },
  joinActivity: async (activityId) => {
    const response = await api.post("/activity/participants", {
      activityId: activityId,
    });
    return response.data;
  },
  leaveActivity: async (activityId) => {
    const response = await api.delete("/activity/participants", {
      data: {
        activityId: activityId,
      },
    });
    return response.data;
  },
  deleteActivity: async (activityId, force = false) => {
    try {
      await api.delete(`/activity/${activityId}`, {
        data: { force: force },
      });
      return {
        success: true,
        error: null,
      };
    } catch (error) {
      let errorMessage = "";
      if (error.response) {
        // Server responsed with an error
        errorMessage = error.response?.data?.errors || "Failed to delete activity";
      } else if (error.request) {
        // No response received from the server
        errorMessage = "Unable to delete activity at this time. Please try again later.";
        console.error("Server did not respond:", error.request);
      }
      return {
        success: false,
        error: errorMessage,
      };
    }
  },
  createActivity: async (activityData) => {
    const response = await api.post("/activity", activityData);
    return response.data;
  },
};
