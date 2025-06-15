import { describe, it, expect, vi, beforeEach } from "vitest";
import { activityService } from "../../services/activityService";
import api from "../../services/api";

vi.mock("../../services/api", () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    delete: vi.fn(),
  },
}));

describe("activityService", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("getRecentActivities", () => {
    it("should return activities on success", async () => {
      const mockData = [
        {
          id: 1,
          name: "Activity 1",
          activityId: 123,
          description: "Description 1",
          location: "Location 1",
          startDateTime: "2024-01-01 10:00:00",
          endDateTime: "2024-01-01 11:00:00",
        },
      ];
      api.get.mockResolvedValue({ data: { data: { content: mockData } } });
      const result = await activityService.getRecentActivities();
      expect(api.get).toHaveBeenCalledWith("/activities");
      expect(result).toEqual(mockData);
    });
    it("should throw on error", async () => {
      api.get.mockRejectedValue(new Error("fail to get recent activities"));
      await expect(activityService.getRecentActivities()).rejects.toThrow("fail to get recent activities");
    });
  });

  describe("getParticipantActivities", () => {
    it("should return participant activities on success", async () => {
      const mockActivities = [
        {
          id: 2,
          name: "Activity 2",
          activityId: 456,
          description: "Description 2",
          location: "Location 2",
          startDateTime: "2024-01-01 10:00:00",
          endDateTime: "2024-01-01 11:00:00",
          role: "participant",
        },
      ];
      api.get.mockResolvedValue({ data: { data: { activities: mockActivities } } });
      const result = await activityService.getParticipantActivities();
      expect(api.get).toHaveBeenCalledWith("/activity/participants");
      expect(result).toEqual(mockActivities);
    });
    it("should throw on error", async () => {
      api.get.mockRejectedValue(new Error("fail to get participant activities"));
      await expect(activityService.getParticipantActivities()).rejects.toThrow("fail to get participant activities");
    });
  });

  describe("joinActivity", () => {
    it("should return response data on success", async () => {
      const mockResp = { joined: true };
      api.post.mockResolvedValue({ data: mockResp });
      const result = await activityService.joinActivity(123);
      expect(api.post).toHaveBeenCalledWith("/activity/participants", { activityId: 123 });
      expect(result).toEqual(mockResp);
    });
    it("should throw on error", async () => {
      api.post.mockRejectedValue(new Error("fail to join activity"));
      await expect(activityService.joinActivity(123)).rejects.toThrow("fail to join activity");
    });
  });

  describe("leaveActivity", () => {
    it("should return response data on success", async () => {
      const mockResp = { success: true };
      api.delete.mockResolvedValue(mockResp);
      const result = await activityService.leaveActivity(456);
      expect(api.delete).toHaveBeenCalledWith("/activity/participants", { data: { activityId: 456 } });
      expect(result).toEqual(undefined);
    });
    it("should throw on error", async () => {
      api.delete.mockRejectedValue(new Error("fail to leave activity"));
      await expect(activityService.leaveActivity(456)).rejects.toThrow("fail to leave activity");
    });
  });
});
