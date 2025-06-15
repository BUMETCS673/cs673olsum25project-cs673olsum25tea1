// Home.test.jsx
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import Home from "../../pages/Home";
import { describe, expect, vi, beforeEach, afterEach, test } from "vitest";
import { MemoryRouter } from "react-router-dom";

vi.mock("../../contexts/AuthContext", () => {
  return {
    useAuth: () => ({
      loading: false,
      user: {
        username: "testuser",
        userEmail: "test@example.com",
      },
    }),
    AuthProvider: ({ children }) => <div data-testid="MockAuthProvider">{children}</div>,
  };
});

vi.mock("../../services/activityService", () => {
  return {
    activityService: {
      getRecentActivities: vi.fn(),
      getParticipantActivities: vi.fn(),
      joinActivity: vi.fn(),
      leaveActivity: vi.fn(),
      deleteActivity: vi.fn(),
    },
  };
});

import { activityService } from "../../services/activityService";
import { AuthProvider } from "../../contexts/AuthContext";

const mockRecentActivities = [
  {
    id: 1,
    name: "Recent 1",
    description: "desc1",
    location: "loc1",
    startDateTime: "2025-01-01T10:00:00",
    endDateTime: "2025-01-01T12:00:00",
  },
];
const mockParticipantActivities = [
  {
    activityId: 2,
    name: "Joined 1",
    description: "desc2",
    location: "loc2",
    startDateTime: "2025-01-02T10:00:00",
    endDateTime: "2025-01-02T12:00:00",
  },
  {
    activityId: 2,
    name: "Joined 2",
    description: "desc2",
    location: "loc2",
    startDateTime: "2025-01-02T10:00:00",
    endDateTime: "2025-01-02T12:00:00",
    role: "ADMIN",
  },
];

describe("Home Page Unit Test", () => {
  beforeEach(() => {
    activityService.getRecentActivities.mockResolvedValue(mockRecentActivities);
    activityService.getParticipantActivities.mockResolvedValue(mockParticipantActivities);
    activityService.joinActivity.mockResolvedValue({});
    activityService.leaveActivity.mockResolvedValue({});
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  test("test recent activity and participated activity", async () => {
    render(
      <AuthProvider>
        <MemoryRouter>
          <Home />
        </MemoryRouter>
      </AuthProvider>
    );
    await screen.findByText("Recent 1");
    fireEvent.click(screen.getByText("Participated Activities"));
    await screen.findByText("Joined 1");
  });

  test("test delete activity without participants", async () => {
    render(
      <AuthProvider>
        <MemoryRouter>
          <Home />
        </MemoryRouter>
      </AuthProvider>
    );
    await screen.findByText("Recent 1");
    fireEvent.click(screen.getByText("Participated Activities"));
    await screen.findByText("Joined 2");
    fireEvent.click(screen.getByText("Delete Activity"));
  });

  test("test delete activity with participants", async () => {
    activityService.deleteActivity.mockResolvedValue({
      success: false,
      error: {
        errorCode: "PARTICIPANTS_PRESENT",
      },
    });
    render(
      <AuthProvider>
        <MemoryRouter>
          <Home />
        </MemoryRouter>
      </AuthProvider>
    );
    await screen.findByText("Recent 1");
    fireEvent.click(screen.getByText("Participated Activities"));
    await screen.findByText("Joined 2");
    fireEvent.click(screen.getByText("Delete Activity"));

    await waitFor(() => {
      expect(activityService.deleteActivity).toHaveBeenCalledWith(2);
    });
    expect(screen.getByText("Activity has participants. Do you want to delete it?")).toBeInTheDocument();
    expect(screen.getByText("Yes")).toBeInTheDocument();
    fireEvent.click(screen.getByText("Yes"));

    await waitFor(() => {
      expect(activityService.deleteActivity).toHaveBeenCalledWith(2, true);
    });
  });

  test("test join activity", async () => {
    render(
      <AuthProvider>
        <MemoryRouter>
          <Home />
        </MemoryRouter>
      </AuthProvider>
    );
    await screen.findByText("Recent 1");
    fireEvent.click(screen.getByText("Join Activity"));
    await waitFor(() => {
      expect(activityService.joinActivity).toHaveBeenCalledWith(1);
    });
    activityService.getParticipantActivities.mockResolvedValue(mockRecentActivities);
    fireEvent.click(screen.getByText("Participated Activities"));
    await screen.findByText("Recent 1");
  });

  test("test leave activity", async () => {
    render(
      <AuthProvider>
        <MemoryRouter>
          <Home />
        </MemoryRouter>
      </AuthProvider>
    );
    fireEvent.click(screen.getByText("Participated Activities"));
    await screen.findByText("Joined 1");
    fireEvent.click(screen.getByText("Leave Activity"));
    activityService.getParticipantActivities.mockResolvedValue([]);
    await waitFor(() => {
      expect(activityService.leaveActivity).toHaveBeenCalledWith(2);
    });
    expect(screen.queryByText("Joined 1")).toBeNull();
  });
});
