import { useAuth } from "../contexts/AuthContext";
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Tabs, Tab } from "@mui/material";
import { styled } from "@mui/material/styles";
import { activityService } from "../services/activityService";

const StyledTab = styled(Tab)({
  textTransform: "none",
  color: "#1f2937",
  fontSize: "1.5rem",
  fontWeight: "600",
});

function TabPanel(props) {
  const { children, value, index, ...other } = props;
  return (
    <div hidden={value !== index} {...other}>
      {value === index && <div>{children}</div>}
    </div>
  );
}

export default function Home() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState("");
  const [sortOrder, setSortOrder] = useState("newest");
  const [value, setValue] = useState(0);
  const [participantActivities, setParticipantActivities] = useState([]);
  const [recentActivities, setRecentActivities] = useState([]);

  const handleChange = async (event, newValue) => {
    setValue(newValue);
    if (newValue === 0) {
      const activities = await activityService.getRecentActivities();
      setRecentActivities(activities);
    }
    if (newValue === 1) {
      const activities = await activityService.getParticipantActivities();
      setParticipantActivities(activities);
    }
  };

  useEffect(() => {
    activityService.getRecentActivities().then(setRecentActivities);
    activityService.getParticipantActivities().then(setParticipantActivities);
  }, []);

  const handleJoinActivity = async (activityId) => {
    await activityService.joinActivity(activityId);
    const activities = await activityService.getParticipantActivities();
    setParticipantActivities(activities);
  };

  const handleLeaveActivity = async (activityId) => {
    await activityService.leaveActivity(activityId);
    const activities = await activityService.getParticipantActivities();
    setParticipantActivities(activities);
  };

  // Example activity data - replace with actual data from API later
  // eslint-disable-next-line no-unused-vars
  const [activities] = useState([
    {
      name: "Morning Yoga Class",
      description:
        "Start your day with a relaxing yoga session in the heart of the city. Perfect for all skill levels.",
      location: "Central Park, NYC",
      startDateTime: "2025-01-15 08:00",
      endDateTime: "2025-01-15 10:30",
    },
    {
      name: "Book Club Meeting",
      description:
        "Monthly discussion of our current read: 'The Seven Husbands of Evelyn Hugo'. Coffee and snacks provided!",
      location: "Downtown Library",
      startDateTime: "2025-01-18 19:00",
      endDateTime: "2025-01-18 21:00",
    },
    {
      name: "Hiking Adventure",
      description:
        "Scenic mountain trail hike with breathtaking views. Bring water, snacks, and comfortable hiking boots.",
      location: "Blue Ridge Mountains",
      startDateTime: "2025-01-20 06:30",
      endDateTime: "2025-01-20 14:00",
    },
    {
      name: "Cooking Workshop",
      description: "Learn to prepare authentic Italian pasta from scratch. All ingredients and equipment provided.",
      location: "Community Center",
      startDateTime: "2025-01-12 14:00",
      endDateTime: "2025-01-12 17:00",
    },
  ]);

  // Filter and sort activities

  // eslint-disable-next-line no-unused-vars
  const filteredActivities = activities
    .filter(
      (activity) =>
        activity.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
        activity.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
        activity.location.toLowerCase().includes(searchQuery.toLowerCase())
    )
    .sort((a, b) => {
      const dateA = new Date(a.startDateTime);
      const dateB = new Date(b.startDateTime);
      return sortOrder === "newest" ? dateB - dateA : dateA - dateB;
    });

  const handleCreateActivity = () => {
    navigate("/create-activity");
  };

  return (
    <div style={styles.container}>
      {/* Header */}
      <header style={styles.header}>
        <div style={styles.headerLeft}>
          <div style={styles.logo}>
            <span style={styles.logoIcon}>🎯</span>
            <h1 style={styles.logoText}>ActivityHub</h1>
          </div>
        </div>
        <div style={styles.headerRight}>
          {user ? (
            <div style={styles.accountInfo}>
              <div style={styles.avatar}>{user.username.charAt(0).toUpperCase()}</div>
              <div style={styles.userDetails}>
                <p style={styles.username}>{user.username}</p>
                <p style={styles.userEmail}>{user.userEmail}</p>
              </div>
            </div>
          ) : (
            <div style={styles.accountInfo}>
              <div style={styles.avatar}>?</div>
              <div style={styles.userDetails}>
                <p style={styles.username}>Loading...</p>
                <p style={styles.userEmail}>Please wait</p>
              </div>
            </div>
          )}
        </div>
      </header>

      {/* Main Content */}
      <main style={styles.main}>
        {/* Search and Filter Section */}
        <div style={styles.controlsSection}>
          <div style={styles.searchContainer}>
            <input
              type="text"
              placeholder="Search activities..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={styles.searchInput}
            />
            <span style={styles.searchIcon}>🔍</span>
          </div>

          <div style={styles.filterContainer}>
            <label style={styles.filterLabel}>Sort by date:</label>
            <select value={sortOrder} onChange={(e) => setSortOrder(e.target.value)} style={styles.filterSelect}>
              <option value="newest">Newest First</option>
              <option value="oldest">Oldest First</option>
            </select>
          </div>

          <button onClick={handleCreateActivity} style={styles.createButton}>
            + Create Activity
          </button>
        </div>

        {/* Activities Section */}
        <div style={styles.activitiesSection}>
          <Tabs value={value} onChange={handleChange}>
            <StyledTab label="Recent Activities" />
            <StyledTab label="Participated Activities" />
          </Tabs>
          <TabPanel value={value} index={1}>
            <div style={styles.activitiesGrid}>
              {participantActivities.map((activity, index) => {
                const startDate = new Date(activity.startDateTime);
                const endDate = new Date(activity.endDateTime);
                const formatDateTime = (date) =>
                  `${date.toLocaleDateString()} ${date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}`;

                return (
                  <div key={index} style={styles.activityCard}>
                    <h3 style={styles.activityTitle}>{activity.name}</h3>
                    <p style={styles.activityDescription}>{activity.description}</p>
                    <div style={styles.activityDetails}>
                      <div style={styles.activityMeta}>
                        <span style={styles.metaIcon}>🕐</span>
                        <span>
                          <strong>Start:</strong> {formatDateTime(startDate)}
                        </span>
                      </div>
                      <div style={styles.activityMeta}>
                        <span style={styles.metaIcon}>🕕</span>
                        <span>
                          <strong>End:</strong> {formatDateTime(endDate)}
                        </span>
                      </div>
                      <div style={styles.activityMeta}>
                        <span style={styles.metaIcon}>📍</span>
                        <span>{activity.location}</span>
                      </div>
                    </div>
                    <button style={styles.joinButton} onClick={() => handleLeaveActivity(activity.activityId)}>
                      Leave Activity
                    </button>
                  </div>
                );
              })}
            </div>
          </TabPanel>
          <TabPanel value={value} index={0}>
            <div style={styles.activitiesGrid}>
              {recentActivities.map((activity, index) => {
                const startDate = new Date(activity.startDateTime);
                const endDate = new Date(activity.endDateTime);
                const formatDateTime = (date) =>
                  `${date.toLocaleDateString()} ${date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}`;

                return (
                  <div key={index} style={styles.activityCard}>
                    <h3 style={styles.activityTitle}>{activity.name}</h3>
                    <p style={styles.activityDescription}>{activity.description}</p>
                    <div style={styles.activityDetails}>
                      <div style={styles.activityMeta}>
                        <span style={styles.metaIcon}>🕐</span>
                        <span>
                          <strong>Start:</strong> {formatDateTime(startDate)}
                        </span>
                      </div>
                      <div style={styles.activityMeta}>
                        <span style={styles.metaIcon}>🕕</span>
                        <span>
                          <strong>End:</strong> {formatDateTime(endDate)}
                        </span>
                      </div>
                      <div style={styles.activityMeta}>
                        <span style={styles.metaIcon}>📍</span>
                        <span>{activity.location}</span>
                      </div>
                    </div>
                    <button style={styles.joinButton} onClick={() => handleJoinActivity(activity.id)}>
                      Join Activity
                    </button>
                  </div>
                );
              })}
            </div>
          </TabPanel>
        </div>
      </main>
    </div>
  );
}

const styles = {
  container: {
    minHeight: "100vh",
    backgroundColor: "#f8fafc",
    fontFamily: "'Segoe UI', Tahoma, Geneva, Verdana, sans-serif",
  },
  header: {
    backgroundColor: "white",
    padding: "1rem 2rem",
    borderBottom: "1px solid #e2e8f0",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
  },
  headerLeft: {
    display: "flex",
    alignItems: "center",
  },
  logo: {
    display: "flex",
    alignItems: "center",
    gap: "0.5rem",
  },
  logoIcon: {
    fontSize: "2rem",
  },
  logoText: {
    margin: 0,
    color: "#1e40af",
    fontSize: "1.8rem",
    fontWeight: "bold",
  },
  headerRight: {
    display: "flex",
    alignItems: "center",
  },
  accountInfo: {
    display: "flex",
    alignItems: "center",
    gap: "0.75rem",
  },
  avatar: {
    width: "40px",
    height: "40px",
    backgroundColor: "#3b82f6",
    color: "white",
    borderRadius: "50%",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontWeight: "bold",
    fontSize: "1.1rem",
  },
  userDetails: {
    display: "flex",
    flexDirection: "column",
    alignItems: "flex-start",
  },
  username: {
    margin: 0,
    fontWeight: "600",
    color: "#1f2937",
    fontSize: "0.95rem",
  },
  userEmail: {
    margin: 0,
    color: "#6b7280",
    fontSize: "0.85rem",
  },
  main: {
    padding: "2rem",
    maxWidth: "1200px",
    margin: "0 auto",
  },
  controlsSection: {
    display: "flex",
    gap: "1rem",
    alignItems: "center",
    marginBottom: "2rem",
    flexWrap: "wrap",
  },
  searchContainer: {
    position: "relative",
    flex: "1",
    minWidth: "300px",
  },
  searchInput: {
    width: "100%",
    padding: "0.75rem 1rem",
    paddingRight: "2.5rem",
    border: "1px solid #d1d5db",
    borderRadius: "0.5rem",
    fontSize: "1rem",
    outline: "none",
    transition: "border-color 0.2s",
    boxSizing: "border-box",
  },
  searchIcon: {
    position: "absolute",
    right: "0.75rem",
    top: "50%",
    transform: "translateY(-50%)",
    color: "#9ca3af",
  },
  filterContainer: {
    display: "flex",
    alignItems: "center",
    gap: "0.5rem",
  },
  filterLabel: {
    color: "#374151",
    fontSize: "0.9rem",
    fontWeight: "500",
  },
  filterSelect: {
    padding: "0.75rem",
    border: "1px solid #d1d5db",
    borderRadius: "0.5rem",
    fontSize: "0.9rem",
    outline: "none",
    backgroundColor: "white",
  },
  createButton: {
    backgroundColor: "#10b981",
    color: "white",
    border: "none",
    padding: "0.75rem 1.5rem",
    borderRadius: "0.5rem",
    fontSize: "1rem",
    fontWeight: "600",
    cursor: "pointer",
    transition: "background-color 0.2s",
    whiteSpace: "nowrap",
  },
  activitiesSection: {
    marginTop: "1rem",
  },
  sectionTitle: {
    color: "#1f2937",
    fontSize: "1.5rem",
    fontWeight: "600",
    marginBottom: "1.5rem",
  },
  activitiesGrid: {
    marginTop: "1rem",
    display: "grid",
    gridTemplateColumns: "repeat(auto-fill, minmax(320px, 1fr))",
    gap: "1.5rem",
    rowGap: "4.5rem",
  },
  activityCard: {
    backgroundColor: "white",
    borderRadius: "0.75rem",
    padding: "1.5rem",
    boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
    border: "1px solid #e5e7eb",
    transition: "transform 0.2s, box-shadow 0.2s",
    display: "flex",
    flexDirection: "column",
    height: "100%",
  },
  activityTitle: {
    margin: "0 0 0.5rem 0",
    color: "#1f2937",
    fontSize: "1.25rem",
    fontWeight: "600",
  },
  activityDescription: {
    margin: "0 0 1rem 0",
    color: "#6b7280",
    fontSize: "0.9rem",
    lineHeight: "1.4",
  },
  activityDetails: {
    display: "flex",
    flexDirection: "column",
    gap: "0.5rem",
    marginBottom: "1.5rem",
    flex: "1",
  },
  activityMeta: {
    display: "flex",
    alignItems: "center",
    gap: "0.5rem",
    color: "#6b7280",
    fontSize: "0.9rem",
  },
  metaIcon: {
    fontSize: "1rem",
  },
  joinButton: {
    backgroundColor: "#3b82f6",
    color: "white",
    border: "none",
    padding: "0.75rem 1.5rem",
    borderRadius: "0.5rem",
    fontSize: "0.9rem",
    fontWeight: "600",
    cursor: "pointer",
    transition: "background-color 0.2s",
    width: "100%",
    alignSelf: "center",
    marginTop: "auto",
  },
};
