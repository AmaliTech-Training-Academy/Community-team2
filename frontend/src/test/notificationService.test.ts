import axiosInstance from "../api/axiosInstance";
import {
  syncSubscription,
  unsubscribeAll,
} from "../features/subscriptions/notificationService";
import { resolveCategoryId } from "../api/communityApi";

vi.mock("../api/axiosInstance", () => ({
  default: {
    post: vi.fn(),
  },
}));

vi.mock("../api/communityApi", () => ({
  resolveCategoryId: vi.fn(),
}));

describe("notificationService", () => {
  beforeEach(() => {
    vi.mocked(axiosInstance.post).mockReset();
    vi.mocked(resolveCategoryId).mockReset();
  });

  it("posts each selected category to the backend subscription endpoint", async () => {
    vi.mocked(resolveCategoryId)
      .mockResolvedValueOnce(3)
      .mockResolvedValueOnce(5);
    vi.mocked(axiosInstance.post).mockResolvedValue({ data: {} });

    await syncSubscription({
      email: "user@example.com",
      categories: ["Discussion", "News"],
      enabled: true,
    });

    expect(resolveCategoryId).toHaveBeenNthCalledWith(1, "Discussion");
    expect(resolveCategoryId).toHaveBeenNthCalledWith(2, "News");
    expect(axiosInstance.post).toHaveBeenNthCalledWith(
      1,
      "/subscriptions/categories/3",
    );
    expect(axiosInstance.post).toHaveBeenNthCalledWith(
      2,
      "/subscriptions/categories/5",
    );
  });

  it("does nothing when notifications are disabled or no categories are selected", async () => {
    await syncSubscription({
      email: "user@example.com",
      categories: [],
      enabled: false,
    });

    expect(resolveCategoryId).not.toHaveBeenCalled();
    expect(axiosInstance.post).not.toHaveBeenCalled();
  });

  it("keeps unsubscribeAll as a no-op until a backend unsubscribe route exists", async () => {
    await expect(unsubscribeAll("user@example.com")).resolves.toBeUndefined();
  });
});
