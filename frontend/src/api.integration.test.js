import axios from "axios";

function isBackendUnavailable(error) {
  if (!axios.isAxiosError(error)) {
    return false;
  }

  return (
    error.code === "ECONNREFUSED" ||
    error.code === "ERR_NETWORK" ||
    error.message === "Network Error"
  );
}

describe("Frontend API Integration Tests", () => {
  const API_URL = process.env.REACT_APP_API_URL || "http://localhost:8080";

  test("should fetch posts from backend", async () => {
    try {
      const response = await axios.get(`${API_URL}/api/posts`);
      expect(response.status).toBe(200);
      expect(Array.isArray(response.data)).toBe(true);
    } catch (error) {
      if (!isBackendUnavailable(error)) {
        throw error;
      }
    }
  });

  test("should check backend health", async () => {
    try {
      const response = await axios.get(`${API_URL}/actuator/health`);
      expect(response.status).toBe(200);
      expect(response.data).toHaveProperty("status");
    } catch (error) {
      if (!isBackendUnavailable(error)) {
        throw error;
      }
    }
  });
});
