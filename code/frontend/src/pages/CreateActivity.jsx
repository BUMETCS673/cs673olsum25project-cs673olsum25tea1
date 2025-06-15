import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { activityService } from "../services/activityService";

export default function CreateActivity() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    location: "",
    startDateTime: "",
    endDateTime: "",
  });

  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [apiError, setApiError] = useState("");

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    // Clear error when user starts typing
    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: "",
      }));
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.name.trim()) {
      newErrors.name = "Activity name is required";
    }

    if (!formData.description.trim()) {
      newErrors.description = "Description is required";
    }

    if (!formData.location.trim()) {
      newErrors.location = "Location is required";
    }

    if (!formData.startDateTime) {
      newErrors.startDateTime = "Start date and time is required";
    }

    if (!formData.endDateTime) {
      newErrors.endDateTime = "End date and time is required";
    }

    if (formData.startDateTime && formData.endDateTime) {
      const startDate = new Date(formData.startDateTime);
      const endDate = new Date(formData.endDateTime);

      if (endDate <= startDate) {
        newErrors.endDateTime = "End date and time must be after start date and time";
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setApiError("");

    if (validateForm()) {
      setIsLoading(true);
      try {
        // Convert datetime-local format to our required format (YYYY-MM-DD HH:MM)
        const formatDateTime = (dateTimeStr) => {
          // datetime-local gives us "YYYY-MM-DDTHH:MM" format
          // We need "YYYY-MM-DD HH:MM" format
          return dateTimeStr.replace("T", " ");
        };

        const activityData = {
          ...formData,
          startDateTime: formatDateTime(formData.startDateTime),
          endDateTime: formatDateTime(formData.endDateTime),
        };

        await activityService.createActivity(activityData);

        // Success - navigate back to home
        navigate("/home");
      } catch (error) {
        console.error("Error creating activity:", error);
        console.error("Error response:", error.response);
        console.error("Error response data:", error.response?.data);

        let errorMessage = "Failed to create activity. Please try again.";

        if (error.response?.data?.message) {
          errorMessage = error.response.data.message;
        } else if (error.response?.data?.errors?.message) {
          errorMessage = error.response.data.errors.message;
        } else if (error.response?.data?.error) {
          errorMessage = error.response.data.error;
        } else if (error.response?.status === 400) {
          errorMessage = "Please check your input data and try again.";
        } else if (error.response?.status === 401) {
          errorMessage = "You must be logged in to create an activity.";
        }

        setApiError(errorMessage);
      } finally {
        setIsLoading(false);
      }
    }
  };

  const handleCancel = () => {
    navigate("/home");
  };

  return (
    <div style={styles.container}>
      <div style={styles.formContainer}>
        <h1 style={styles.title}>Create New Activity</h1>

        {apiError && <div style={styles.errorBanner}>{apiError}</div>}

        <form onSubmit={handleSubmit} style={styles.form}>
          <div style={styles.formGroup}>
            <label style={styles.label}>Activity Name *</label>
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleInputChange}
              style={{
                ...styles.input,
                ...(errors.name ? styles.inputError : {}),
              }}
              placeholder="Enter activity name"
            />
            {errors.name && <span style={styles.errorText}>{errors.name}</span>}
          </div>

          <div style={styles.formGroup}>
            <label style={styles.label}>Description *</label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleInputChange}
              style={{
                ...styles.textarea,
                ...(errors.description ? styles.inputError : {}),
              }}
              placeholder="Describe your activity..."
              rows="4"
            />
            {errors.description && <span style={styles.errorText}>{errors.description}</span>}
          </div>

          <div style={styles.formGroup}>
            <label style={styles.label}>Location *</label>
            <input
              type="text"
              name="location"
              value={formData.location}
              onChange={handleInputChange}
              style={{
                ...styles.input,
                ...(errors.location ? styles.inputError : {}),
              }}
              placeholder="Enter location"
            />
            {errors.location && <span style={styles.errorText}>{errors.location}</span>}
          </div>

          <div style={styles.formRow}>
            <div style={styles.formGroup}>
              <label style={styles.label}>Start Date & Time *</label>
              <input
                type="datetime-local"
                name="startDateTime"
                value={formData.startDateTime}
                onChange={handleInputChange}
                style={{
                  ...styles.input,
                  ...(errors.startDateTime ? styles.inputError : {}),
                }}
              />
              {errors.startDateTime && <span style={styles.errorText}>{errors.startDateTime}</span>}
            </div>

            <div style={styles.formGroup}>
              <label style={styles.label}>End Date & Time *</label>
              <input
                type="datetime-local"
                name="endDateTime"
                value={formData.endDateTime}
                onChange={handleInputChange}
                style={{
                  ...styles.input,
                  ...(errors.endDateTime ? styles.inputError : {}),
                }}
              />
              {errors.endDateTime && <span style={styles.errorText}>{errors.endDateTime}</span>}
            </div>
          </div>

          <div style={styles.buttonGroup}>
            <button type="button" onClick={handleCancel} style={styles.cancelButton}>
              Cancel
            </button>
            <button
              type="submit"
              style={{
                ...styles.submitButton,
                ...(isLoading ? styles.submitButtonDisabled : {}),
              }}
              disabled={isLoading}
            >
              {isLoading ? "Creating..." : "Create Activity"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

const styles = {
  container: {
    minHeight: "100vh",
    backgroundColor: "#f8fafc",
    padding: "2rem",
    fontFamily: "'Segoe UI', Tahoma, Geneva, Verdana, sans-serif",
  },
  formContainer: {
    maxWidth: "600px",
    margin: "0 auto",
    backgroundColor: "white",
    borderRadius: "0.75rem",
    padding: "2rem",
    boxShadow: "0 4px 6px rgba(0,0,0,0.1)",
    border: "1px solid #e5e7eb",
  },
  title: {
    color: "#1f2937",
    fontSize: "2rem",
    fontWeight: "bold",
    marginBottom: "2rem",
    textAlign: "center",
  },
  form: {
    display: "flex",
    flexDirection: "column",
    gap: "1.5rem",
  },
  formGroup: {
    display: "flex",
    flexDirection: "column",
    gap: "0.5rem",
  },
  formRow: {
    display: "grid",
    gridTemplateColumns: "1fr 1fr",
    gap: "1rem",
  },
  label: {
    color: "#374151",
    fontSize: "0.9rem",
    fontWeight: "600",
  },
  input: {
    padding: "0.75rem",
    border: "1px solid #d1d5db",
    borderRadius: "0.5rem",
    fontSize: "1rem",
    outline: "none",
    transition: "border-color 0.2s",
    boxSizing: "border-box",
  },
  textarea: {
    padding: "0.75rem",
    border: "1px solid #d1d5db",
    borderRadius: "0.5rem",
    fontSize: "1rem",
    outline: "none",
    transition: "border-color 0.2s",
    boxSizing: "border-box",
    resize: "vertical",
    fontFamily: "inherit",
  },
  inputError: {
    borderColor: "#ef4444",
  },
  errorText: {
    color: "#ef4444",
    fontSize: "0.875rem",
    fontWeight: "500",
  },
  errorBanner: {
    backgroundColor: "#fee2e2",
    border: "1px solid #fecaca",
    color: "#dc2626",
    padding: "0.75rem",
    borderRadius: "0.5rem",
    fontSize: "0.9rem",
    fontWeight: "500",
    marginBottom: "1rem",
  },
  buttonGroup: {
    display: "flex",
    gap: "1rem",
    justifyContent: "flex-end",
    marginTop: "1rem",
  },
  cancelButton: {
    backgroundColor: "#6b7280",
    color: "white",
    border: "none",
    padding: "0.75rem 1.5rem",
    borderRadius: "0.5rem",
    fontSize: "1rem",
    fontWeight: "600",
    cursor: "pointer",
    transition: "background-color 0.2s",
  },
  submitButton: {
    backgroundColor: "#10b981",
    color: "white",
    border: "none",
    padding: "0.75rem 1.5rem",
    borderRadius: "0.5rem",
    fontSize: "1rem",
    fontWeight: "600",
    cursor: "pointer",
    transition: "background-color 0.2s",
  },
  submitButtonDisabled: {
    backgroundColor: "#9ca3af",
    cursor: "not-allowed",
  },
};
