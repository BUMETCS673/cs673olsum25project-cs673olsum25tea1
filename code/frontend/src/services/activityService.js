import api from "./api";

export const activityService = {
  getRecentActivities: async () => {
    const response = await api.get("/activities");
    return response.data.data;
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
};
