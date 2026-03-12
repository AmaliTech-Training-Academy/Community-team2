import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import RegisterPage from "../pages/RegisterPage";

const mocks = vi.hoisted(() => ({
  register: vi.fn(),
  toast: vi.fn(),
  checkUsernameAvailability: vi.fn(),
}));

vi.mock("../features/auth/authStore", () => ({
  useAuthStore: (
    selector: (state: { register: typeof mocks.register }) => unknown,
  ) => selector({ register: mocks.register }),
}));

vi.mock("../components/atoms/Toast", () => ({
  useToast: () => mocks.toast,
}));

vi.mock("../hooks/useDebounce", () => ({
  useDebounce: <T,>(value: T) => value,
}));

vi.mock("../api", () => ({
  api: {
    auth: {
      checkUsernameAvailability: mocks.checkUsernameAvailability,
    },
  },
}));

describe("RegisterPage", () => {
  beforeEach(() => {
    mocks.register.mockReset();
    mocks.toast.mockReset();
    mocks.checkUsernameAvailability.mockReset();
  });

  it("shows a taken username error after the debounced availability check", async () => {
    mocks.checkUsernameAvailability.mockResolvedValue(false);

    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <RegisterPage />
      </MemoryRouter>,
    );

    expect(
      screen.getByText(
        "3-20 characters, letters and numbers only. Dots or underscores can be used in the middle.",
      ),
    ).toBeInTheDocument();

    await user.type(screen.getByTestId("register-fullName-input"), "kofi_osei");

    await waitFor(() => {
      expect(mocks.checkUsernameAvailability).toHaveBeenCalledWith("kofi_osei");
    });

    expect(
      await screen.findByText(
        "This username is already on our team. Try another?",
      ),
    ).toBeInTheDocument();
  });
});
