import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import RegisterPage from "../pages/RegisterPage";

const mocks = vi.hoisted(() => ({
  register: vi.fn(),
  toast: vi.fn(),
}));

vi.mock("../features/auth/authStore", () => ({
  useAuthStore: (
    selector: (state: { register: typeof mocks.register }) => unknown,
  ) => selector({ register: mocks.register }),
}));

vi.mock("../components/atoms/Toast", () => ({
  useToast: () => mocks.toast,
}));

describe("RegisterPage", () => {
  beforeEach(() => {
    mocks.register.mockReset();
    mocks.toast.mockReset();
  });

  it("uses Full Name instead of Username and submits without live availability checks", async () => {
    const user = userEvent.setup();
    mocks.register.mockResolvedValue(undefined);

    render(
      <MemoryRouter>
        <RegisterPage />
      </MemoryRouter>,
    );

    expect(screen.getByLabelText("Full Name")).toBeInTheDocument();
    expect(screen.queryByText("Username")).not.toBeInTheDocument();

    await user.type(screen.getByTestId("register-fullName-input"), "Kofi Osei");
    await user.type(
      screen.getByTestId("register-email-input"),
      "kofi@example.com",
    );
    await user.type(screen.getByTestId("register-password-input"), "Abcd!1");
    await user.type(
      screen.getByTestId("register-confirmPassword-input"),
      "Abcd!1",
    );
    await user.click(screen.getByTestId("register-submit-btn"));

    expect(mocks.register).toHaveBeenCalledWith(
      "Kofi Osei",
      "kofi@example.com",
      "Abcd!1",
    );
  });
});
