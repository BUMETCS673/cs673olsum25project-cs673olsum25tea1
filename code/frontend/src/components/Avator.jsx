import { useState, useRef } from "react";
import { styled } from "@mui/material/styles";
import imageCompression from "browser-image-compression";
import { Snackbar, Alert, Tooltip } from "@mui/material";
import { useAuth } from "../contexts/AuthContext";

const AvatarContainer = styled("div")({
  position: "relative",
  cursor: "pointer",
});

const AvatarImage = styled("div")({
  width: "40px",
  height: "40px",
  borderRadius: "50%",
  backgroundColor: "#3b82f6",
  color: "white",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  fontWeight: "bold",
  fontSize: "1.1rem",
  overflow: "hidden",
  "& img": {
    width: "100%",
    height: "100%",
    objectFit: "cover",
  },
});

const HiddenInput = styled("input")({
  display: "none",
});

export default function AvatarUpload({ user }) {
  const { updateAvatar } = useAuth();
  const [notification, setNotification] = useState({
    open: false,
    message: "",
    severity: "success",
  });
  const fileInputRef = useRef(null);

  const handleFileSelect = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    if (!file.type.match(/^image\/(jpeg|png)$/)) {
      setNotification({
        open: true,
        message: "only jpeg and png are supported",
        severity: "error",
      });
      return;
    }

    try {
      const options = {
        maxSizeMB: 3,
        maxWidthOrHeight: 1024,
        useWebWorker: true,
      };
      const compressedFile = await imageCompression(file, options);

      const reader = new FileReader();
      reader.onload = async (e) => {
        const base64Data = e.target.result;
        try {
          await updateAvatar(base64Data);
          setNotification({
            open: true,
            message: "success to update avatar",
            severity: "success",
          });
        } catch (error) {
          setNotification({
            open: true,
            message: error.message || "failed to update avatar",
            severity: "error",
          });
        }
      };
      reader.readAsDataURL(compressedFile);
    } catch (error) {
      setNotification({
        open: true,
        message: "failed to process image",
        severity: "error",
      });
    }
  };

  const handleClose = (event, reason) => {
    if (reason === "clickaway") return;
    setNotification({ ...notification, open: false });
  };

  const handleClick = () => {
    fileInputRef.current?.click();
  };

  return (
    <>
      <Tooltip title="Click to update avatar" placement="top">
        <AvatarContainer onClick={handleClick}>
          <AvatarImage>{user.avatar ? <img src={user.avatar} /> : user.username.charAt(0).toUpperCase()}</AvatarImage>
          <HiddenInput type="file" ref={fileInputRef} accept="image/jpeg,image/png" onChange={handleFileSelect} />
        </AvatarContainer>
      </Tooltip>
      <Snackbar
        open={notification.open}
        autoHideDuration={5000}
        onClose={handleClose}
        anchorOrigin={{ vertical: "top", horizontal: "center" }}
      >
        <Alert onClose={handleClose} severity={notification.severity} variant="filled">
          {notification.message}
        </Alert>
      </Snackbar>
    </>
  );
}
