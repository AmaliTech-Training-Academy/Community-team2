import React, { useCallback, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useToast } from "../components/atoms/Toast";
import { Text } from "../components/atoms/Text";
import Logo from "../assets/images/Logo.svg?react";
import Lock from "../assets/images/lock.svg?react";
import EyeOn from "../assets/images/eye-on.svg?react";
import EyeOff from "../assets/images/eye-off.svg?react";

type FormKey = "password" | "confirmPassword";
type FormState = Record<FormKey, string>;

const INITIAL_FORM: FormState = {
  password: "",
  confirmPassword: "",
};

export default function ResetPasswordPage() {
  const toast = useToast();
  const navigate = useNavigate();
  const [form, setForm] = useState<FormState>(INITIAL_FORM);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [errors, setErrors] = useState<Partial<FormState>>({});
  const [loading, setLoading] = useState(false);

  const formRef = useRef(form);
  formRef.current = form;

  const setField = useCallback((key: FormKey, value: string) => {
    setForm((current) => ({ ...current, [key]: value }));
  }, []);

  const validate = useCallback((): Partial<FormState> => {
    const nextErrors: Partial<FormState> = {};
    const { password, confirmPassword } = formRef.current;

    if (!password) {
      nextErrors.password = "Password is required";
    } else if (password.length < 6) {
      nextErrors.password =
        "Minimum of 6 characters including special characters";
    }

    if (!confirmPassword) {
      nextErrors.confirmPassword = "Please confirm your new password";
    } else if (confirmPassword !== password) {
      nextErrors.confirmPassword = "Passwords do not match";
    }

    return nextErrors;
  }, []);

  const handleSubmit = useCallback(async () => {
    const nextErrors = validate();
    if (Object.keys(nextErrors).length > 0) {
      setErrors(nextErrors);
      return;
    }

    setErrors({});
    setLoading(true);

    await new Promise((resolve) => window.setTimeout(resolve, 700));

    toast("Password updated successfully");
    navigate("/login");
  }, [navigate, toast, validate]);

  const handleKeyDown = useCallback(
    (event: React.KeyboardEvent<HTMLInputElement>) => {
      if (event.key === "Enter") {
        handleSubmit();
      }
    },
    [handleSubmit],
  );

  const inputClassName = (hasError: boolean, hasToggle = false) =>
    `w-full rounded-lg border py-2.5 pl-9 ${hasToggle ? "pr-9" : "pr-3"} text-body-lg text-blue-gray-light placeholder:text-[#5A6F7C] transition-colors focus:outline-none ${
      hasError
        ? "border-red-400 bg-red-50"
        : "border-gray-200 bg-gray-100 focus:border-navy focus:bg-white"
    }`;

  return (
    <div
      data-testid="reset-password-page"
      className="bg-background rounded-3xl border-0 md:border md:border-gray-200 pb-40 md:py-8 md:px-8 w-full max-w-md flex flex-col gap-2.5"
    >
      <div data-testid="reset-password-card" className="flex flex-col gap-12">
        <div className="flex flex-col items-center gap-8 ">
          <Logo />
          <div className="flex flex-col items-center gap-1 text-center">
            <h1 className="text-h-lg font-semibold text-blue-gray-dark">
              Reset your password
            </h1>
            <Text variant="body-sm" className="text-blue-gray">
              Create a strong new password for your account. Once updated, you
              can sign in again immediately.
            </Text>
          </div>
        </div>

        <div>
          <div className="mb-4">
            <label
              className="text-body-sm block text-blue-gray-light mb-1.5"
              htmlFor="reset-password"
            >
              New Password
            </label>
            <div className="relative">
              <span
                className={`absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none ${
                  errors.password ? "text-red-600" : "text-[#5A6F7C]"
                }`}
              >
                <Lock stroke="currentColor" />
              </span>
              <input
                id="reset-password"
                data-testid="reset-password-input"
                type={showPassword ? "text" : "password"}
                className={inputClassName(!!errors.password, true)}
                placeholder="Enter new password"
                value={form.password}
                onChange={(event) => setField("password", event.target.value)}
                onKeyDown={handleKeyDown}
              />
              <button
                data-testid="reset-toggle-password-btn"
                type="button"
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400"
                onClick={() => setShowPassword((current) => !current)}
              >
                {showPassword ? <EyeOn /> : <EyeOff />}
              </button>
            </div>
            <Text variant="body-sm" className="mt-1 text-blue-gray">
              Minimum of 6 characters including special characters
            </Text>
            {errors.password && (
              <Text
                variant="body-sm"
                data-testid="reset-password-error"
                className="mt-0.5 text-red-500"
              >
                {errors.password}
              </Text>
            )}
          </div>

          <div className="mb-6">
            <label
              className="text-body-sm block text-blue-gray-light mb-1.5"
              htmlFor="reset-confirm-password"
            >
              Confirm Password
            </label>
            <div className="relative">
              <span
                className={`absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none ${
                  errors.confirmPassword ? "text-red-600" : "text-[#5A6F7C]"
                }`}
              >
                <Lock stroke="currentColor" />
              </span>
              <input
                id="reset-confirm-password"
                data-testid="reset-confirm-password-input"
                type={showConfirmPassword ? "text" : "password"}
                className={inputClassName(!!errors.confirmPassword, true)}
                placeholder="Confirm new password"
                value={form.confirmPassword}
                onChange={(event) =>
                  setField("confirmPassword", event.target.value)
                }
                onKeyDown={handleKeyDown}
              />
              <button
                data-testid="reset-toggle-confirm-password-btn"
                type="button"
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400"
                onClick={() => setShowConfirmPassword((current) => !current)}
              >
                {showConfirmPassword ? <EyeOn /> : <EyeOff />}
              </button>
            </div>
            {errors.confirmPassword && (
              <Text
                variant="body-sm"
                data-testid="reset-confirm-password-error"
                className="mt-1 text-red-500"
              >
                {errors.confirmPassword}
              </Text>
            )}
          </div>

          <button
            data-testid="reset-password-submit-btn"
            type="button"
            disabled={loading}
            onClick={handleSubmit}
            className="w-full py-2.5 text-body-sm text-white bg-navy rounded-lg hover:bg-[#1a3347] transition-colors disabled:cursor-not-allowed disabled:opacity-60"
          >
            {loading ? "Updating password…" : "Update password"}
          </button>

          <p className="mt-7 flex items-center justify-center gap-2 text-center">
            <Text variant="body-sm" as="span" className="text-blue-gray">
              Need to use a different account?
            </Text>
            <Link
              data-testid="reset-password-goto-login-link"
              to="/login"
              className="text-body-sm font-semibold text-orange underline"
            >
              Back to login
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
