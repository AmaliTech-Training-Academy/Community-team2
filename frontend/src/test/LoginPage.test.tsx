import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import LoginPage from "../pages/LoginPage";

const mocks = vi.hoisted(() => ({
  login: vi.fn(),
  toast: vi.fn(),
}));

vi.mock("../features/auth/authStore", () => ({
  useAuthStore: (selector: (state: { login: typeof mocks.login }) => unknown) =>
    selector({ login: mocks.login }),
}));

vi.mock("../components/atoms/Toast", () => ({
  useToast: () => mocks.toast,
}));

describe("LoginPage", () => {
  beforeEach(() => {
    mocks.login.mockReset();
    mocks.toast.mockReset();
  });

  it("shows backend login failures as a form submission error", async () => {
    mocks.login.mockRejectedValue(
      "We couldn't sign you in. Check your email and password, then try again.",
    );

    const user = userEvent.setup();

    render(
      <MemoryRouter
        future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
      >
        <LoginPage />
      </MemoryRouter>,
    );

    await user.type(
      screen.getByTestId("login-email-input"),
      "user@example.com",
    );
    await user.type(
      screen.getByTestId("login-password-input"),
      "wrong-password",
    );
    await user.click(screen.getByTestId("login-submit-btn"));

    expect(await screen.findByTestId("login-submit-error")).toHaveTextContent(
      "We couldn't sign you in. Check your email and password, then try again.",
    );
    expect(screen.queryByTestId("login-email-error")).not.toBeInTheDocument();
    expect(
      screen.queryByTestId("login-password-error"),
    ).not.toBeInTheDocument();
  });
});
