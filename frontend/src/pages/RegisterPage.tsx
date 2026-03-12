import React, { useState, useCallback, useRef } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuthStore } from "../features/auth/authStore";
import { useToast } from "../components/atoms/Toast";
import { Text } from "../components/atoms/Text";
import Logo from "../assets/images/Logo.svg?react";
import EmailIcon from "../assets/images/mail.svg?react";
import Lock from "../assets/images/lock.svg?react";
import EyeOn from "../assets/images/eye-on.svg?react";
import EyeOff from "../assets/images/eye-off.svg?react";
import UserIcon from "../assets/images/user.svg?react";
import { toErrorMessage } from "../utils";

const EMAIL_RE = /\S+@\S+\.\S+/;

type FormKey = "username" | "email" | "password" | "confirmPassword";
type FormState = Record<FormKey, string>;

const INITIAL_FORM: FormState = {
  username: "",
  email: "",
  password: "",
  confirmPassword: "",
};

function validateFullName(fullName: string): string | null {
  const value = fullName.trim();

  if (!value) {
    return "Full name is required";
  }

  if (value.length < 2) {
    return "Full name must be at least 2 characters.";
  }

  return null;
}

export default function RegisterPage() {
  const register = useAuthStore((s) => s.register);
  const toast = useToast();
  const navigate = useNavigate();

  const [form, setForm] = useState<FormState>(INITIAL_FORM);
  const [showPass, setShowPass] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [errors, setErrors] = useState<Partial<FormState>>({});
  const [submitError, setSubmitError] = useState("");
  const [loading, setLoading] = useState(false);

  const formRef = useRef(form);
  formRef.current = form;

  const setField = useCallback((k: FormKey, v: string) => {
    setForm((f) => ({ ...f, [k]: v }));
    setSubmitError("");
    setErrors((current) => ({
      ...current,
      [k]: "",
    }));
  }, []);

  const validate = useCallback((): Partial<FormState> => {
    const { username, email, password, confirmPassword } = formRef.current;
    const errs: Partial<FormState> = {};
    const fullNameError = validateFullName(username);
    if (fullNameError) errs.username = fullNameError;
    if (!email) errs.email = "Email is required";
    else if (!EMAIL_RE.test(email)) errs.email = "Invalid email address";
    if (!password) errs.password = "Password is required";
    else if (password.length < 6)
      errs.password = "Minimum of 6 characters including special characters";
    if (!confirmPassword) errs.confirmPassword = "Please confirm your password";
    else if (password !== confirmPassword)
      errs.confirmPassword = "Passwords do not match";
    return errs;
  }, []);

  const handleSubmit = useCallback(async () => {
    const errs = validate();
    if (Object.keys(errs).length) {
      setErrors(errs);
      setSubmitError("");
      return;
    }

    setLoading(true);
    setErrors({});
    setSubmitError("");
    try {
      const { username, email, password } = formRef.current;
      await register(username.trim(), email, password);
      toast("Account created successfully!");
      navigate("/");
    } catch (err: unknown) {
      setSubmitError(
        toErrorMessage(err, "Unable to create your account. Please try again."),
      );
    } finally {
      setLoading(false);
    }
  }, [validate, register, toast, navigate]);

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (e.key === "Enter") handleSubmit();
    },
    [handleSubmit],
  );

  const inputBase = (hasError: boolean) =>
    `w-full pl-9 pr-3 py-2.5 rounded-xl text-body-lg text-blue-gray-light placeholder:text-[#5A6F7C] focus:outline-none transition-colors border ${
      hasError
        ? "border-red-400 bg-red-50"
        : "border-borderstroke bg-primary focus:border-navy focus:bg-white"
    }`;

  const inputWithEye = (hasError: boolean) =>
    `w-full pl-9 pr-9 py-2.5 rounded-xl text-body-lg text-blue-gray-light placeholder:text-[#5A6F7C] focus:outline-none transition-colors border ${
      hasError
        ? "border-red-400 bg-red-50"
        : "border-borderstroke bg-primary focus:border-navy focus:bg-white"
    }`;

  const fullNameHasError = Boolean(errors.username);
  const passwordHasError = Boolean(errors.password);

  return (
    <div
      data-testid="register-page"
      className="fade-in bg-background rounded-3xl border-0 md:border md:border-borderstroke pb-40 md:py-8 md:px-8 w-full max-w-md flex flex-col gap-2.5"
    >
      <div className="flex flex-col gap-12">
        <div className="flex flex-col items-center gap-8">
          <Logo />
          <div className="flex flex-col items-center gap-1">
            <div className="text-h-lg font-semibold text-blue-gray-dark text-center">
              Join the <span className="block">Community</span>
            </div>
            <div className=" text-blue-gray text-center">
              Create an account to get started
            </div>
          </div>
        </div>

        <div>
          <div className="mb-4">
            <label
              className={`text-body-sm block mb-1.5 ${
                fullNameHasError ? "text-red-600" : "text-blue-gray-light"
              }`}
              htmlFor="register-username"
            >
              Full Name
            </label>
            <div className="relative">
              <span
                className={`absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none ${
                  fullNameHasError ? "text-red-600" : "text-muted-icon"
                }`}
              >
                <UserIcon />
              </span>
              <input
                id="register-username"
                data-testid="register-fullName-input"
                className={inputBase(!!errors.username)}
                type="text"
                placeholder="e.g., John Doe"
                value={form.username}
                onChange={(e) => setField("username", e.target.value)}
                onKeyDown={handleKeyDown}
              />
            </div>
            {errors.username && (
              <Text
                variant="body-sm"
                data-testid="register-fullName-error"
                className="text-red-500 mt-1"
              >
                {errors.username}
              </Text>
            )}
          </div>

          <div className="mb-4">
            <label
              className="text-body-sm block text-blue-gray-light mb-1.5"
              htmlFor="register-email"
            >
              Email
            </label>
            <div className="relative">
              <span
                className={`absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none ${
                  errors.email ? "text-red-600" : "text-muted-icon"
                }`}
              >
                <EmailIcon stroke="currentColor" />
              </span>
              <input
                id="register-email"
                data-testid="register-email-input"
                className={inputBase(!!errors.email)}
                type="email"
                placeholder="your@example.com"
                value={form.email}
                onChange={(e) => setField("email", e.target.value)}
                onKeyDown={handleKeyDown}
              />
            </div>
            {errors.email && (
              <Text
                variant="body-sm"
                data-testid="register-email-error"
                className="text-red-500 mt-1"
              >
                {errors.email}
              </Text>
            )}
          </div>

          <div className="mb-4">
            <label
              className={`text-body-sm block mb-1.5 ${
                passwordHasError ? "text-red-600" : "text-blue-gray-light"
              }`}
              htmlFor="register-password"
            >
              Password
            </label>
            <div className="relative">
              <span
                className={`absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none ${
                  errors.password ? "text-red-600" : "text-muted-icon"
                }`}
              >
                <Lock stroke="currentColor" />
              </span>
              <input
                id="register-password"
                data-testid="register-password-input"
                className={inputWithEye(!!errors.password)}
                type={showPass ? "text" : "password"}
                placeholder="Enter password"
                value={form.password}
                onChange={(e) => setField("password", e.target.value)}
                onKeyDown={handleKeyDown}
              />
              <button
                data-testid="register-toggle-password-btn"
                type="button"
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400"
                onClick={() => setShowPass((s) => !s)}
              >
                {showPass ? <EyeOn /> : <EyeOff />}
              </button>
            </div>
            <Text
              variant="body-sm"
              className={`mt-1 ${passwordHasError ? "text-red-600" : "text-blue-gray"}`}
            >
              Minimum of 6 characters including special characters
            </Text>
          </div>

          <div className="mb-6">
            <label
              className="text-body-sm block text-blue-gray-light mb-1.5"
              htmlFor="register-confirmPassword"
            >
              Confirm Password
            </label>
            <div className="relative">
              <span
                className={`absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none ${
                  errors.confirmPassword ? "text-red-600" : "text-muted-icon"
                }`}
              >
                <Lock stroke="currentColor" />
              </span>
              <input
                id="register-confirmPassword"
                data-testid="register-confirmPassword-input"
                className={inputWithEye(!!errors.confirmPassword)}
                type={showConfirm ? "text" : "password"}
                placeholder="Enter password"
                value={form.confirmPassword}
                onChange={(e) => setField("confirmPassword", e.target.value)}
                onKeyDown={handleKeyDown}
              />
              <button
                data-testid="register-toggle-confirm-password-btn"
                type="button"
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400"
                onClick={() => setShowConfirm((s) => !s)}
              >
                {showConfirm ? <EyeOn /> : <EyeOff />}
              </button>
            </div>
            {errors.confirmPassword && (
              <Text
                variant="body-sm"
                data-testid="register-confirmPassword-error"
                className="text-red-500 mt-1"
              >
                {errors.confirmPassword}
              </Text>
            )}
          </div>

          {submitError && (
            <Text
              variant="body-sm"
              data-testid="register-submit-error"
              className="mb-3 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-red-700"
            >
              {submitError}
            </Text>
          )}

          <button
            data-testid="register-submit-btn"
            onClick={handleSubmit}
            disabled={loading}
            className="w-full py-2.5 text-body-lg font-semibold text-white bg-navy  rounded-xl hover:opacity-90 transition-opacity disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {loading ? "Creating account…" : "Register"}
          </button>

          <p className="flex justify-center items-center gap-1.5 text-center mt-7">
            <Text variant="body-sm" as="span" className="text-blue-gray">
              Already have an account?
            </Text>
            <Link
              data-testid="register-goto-login-link"
              to="/login"
              className="text-body-sm font-semibold text-orange underline"
            >
              Log in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
