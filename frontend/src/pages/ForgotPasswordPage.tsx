import { useCallback, useRef, useState, type KeyboardEvent } from "react";
import { Link } from "react-router-dom";
import { api } from "../api";
import { useToast } from "../components/atoms/Toast";
import { Text } from "../components/atoms/Text";
import Logo from "../assets/images/Logo.svg?react";
import EmailIcon from "../assets/images/mail.svg?react";
import { toErrorMessage } from "../utils";

const EMAIL_RE = /\S+@\S+\.\S+/;

export default function ForgotPasswordPage() {
  const toast = useToast();
  const [email, setEmail] = useState("");
  const [emailError, setEmailError] = useState("");
  const [submitError, setSubmitError] = useState("");
  const [submittedEmail, setSubmittedEmail] = useState("");
  const [loading, setLoading] = useState(false);

  const hasError = Boolean(emailError);

  const emailRef = useRef(email);
  emailRef.current = email;

  const handleSubmit = useCallback(async () => {
    const value = emailRef.current.trim();
    if (!value) {
      setEmailError("Email is required");
      setSubmitError("");
      return;
    }

    if (!EMAIL_RE.test(value)) {
      setEmailError("Your email is incorrect");
      setSubmitError("");
      return;
    }

    setLoading(true);
    setEmailError("");
    setSubmitError("");

    try {
      await api.auth.forgotPassword(value);
      setSubmittedEmail(value);
      toast("Password reset link sent");
    } catch (err: unknown) {
      setSubmitError(
        toErrorMessage(err, "Unable to send a reset link. Please try again."),
      );
    } finally {
      setLoading(false);
    }
  }, [toast]);

  const handleKeyDown = useCallback(
    (event: KeyboardEvent<HTMLInputElement>) => {
      if (event.key === "Enter") {
        handleSubmit();
      }
    },
    [handleSubmit],
  );

  if (submittedEmail) {
    return (
      <div
        data-testid="forgot-password-page"
        className="bg-background rounded-3xl border-0 md:border md:border-gray-200 pb-40 md:py-8 md:px-8 w-full max-w-md flex flex-col gap-2.5"
      >
        <div
          data-testid="forgot-password-card"
          className="flex flex-col gap-12"
        >
          <div className="flex flex-col items-center gap-8 text-center">
            <Logo />

            <div className="flex flex-col items-center gap-1">
              <div className="text-h-lg font-semibold text-blue-gray-dark text-center">
                Reset link sent
              </div>
              <div className="text-blue-gray text-center">
                Check your inbox to continue resetting your password
              </div>
            </div>
          </div>

          <div className="text-center">
            <Text variant="body-sm" className="mx-auto max-w-sm text-blue-gray">
              If an account exists for {submittedEmail}, a password reset link
              has been sent. Use it to continue the reset process.
            </Text>
          </div>

          <div className="grid w-full gap-3">
            <Link
              to="/login"
              className="flex w-full items-center justify-center py-2.5 text-center text-body-sm text-white bg-navy rounded-lg hover:bg-[#1a3347] transition-colors"
            >
              Back to login
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div
      data-testid="forgot-password-page"
      className="bg-background rounded-3xl border-0 md:border md:border-gray-200 pb-40 md:py-8 md:px-8 w-full max-w-md flex flex-col gap-2.5"
    >
      <div data-testid="forgot-password-card" className="flex flex-col gap-12">
        <div className="flex flex-col items-center gap-8 ">
          <Logo />
          <div className="flex flex-col items-center gap-1 text-center">
            <h1 className="text-h-lg font-semibold text-blue-gray-dark">
              Forgot your password?
            </h1>
            <Text variant="body-sm" className="text-blue-gray">
              Enter the email attached to your account and we will send a secure
              link to reset your password.
            </Text>
          </div>
        </div>

        <div>
          <div className="mb-6">
            <label
              className={`text-body-sm block mb-1.5 ${
                hasError ? "text-red-600" : "text-blue-gray-light"
              }`}
              htmlFor="forgot-password-email"
            >
              Email
            </label>
            <div className="relative">
              <span
                className={`absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none ${
                  hasError ? "text-red-600" : "text-muted-icon"
                }`}
              >
                <EmailIcon stroke="currentColor" />
              </span>
              <input
                id="forgot-password-email"
                data-testid="forgot-password-email-input"
                className={`w-full pl-9 pr-3 py-2.5 rounded-lg bg-primary text-body-lg text-blue-gray-light placeholder:text-muted-icon focus:outline-none transition-colors border ${
                  hasError
                    ? "border-red-400 bg-red-50 text-red-600"
                    : "border-gray-200 bg-gray-100 focus:border-navy focus:bg-white"
                }`}
                type="email"
                placeholder="your@example.com"
                value={email}
                onChange={(event) => {
                  setEmail(event.target.value);
                  setSubmitError("");
                  if (emailError) {
                    setEmailError("");
                  }
                }}
                onKeyDown={handleKeyDown}
              />
            </div>
            {emailError && (
              <Text
                variant="body-sm"
                data-testid="forgot-password-email-error"
                className="mt-1 text-red-500"
              >
                {emailError}
              </Text>
            )}
          </div>

          {submitError && (
            <Text
              variant="body-sm"
              data-testid="forgot-password-submit-error"
              className="mb-3 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-red-700"
            >
              {submitError}
            </Text>
          )}

          <button
            data-testid="forgot-password-submit-btn"
            type="button"
            onClick={handleSubmit}
            disabled={loading}
            className="w-full py-2.5 text-body-sm text-white bg-navy rounded-lg hover:bg-[#1a3347] transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {loading ? "Sending link…" : "Send reset link"}
          </button>

          <p className="mt-7 flex items-center justify-center gap-2 text-center">
            <Text variant="body-sm" as="span" className="text-blue-gray">
              Remembered your password?
            </Text>
            <Link
              data-testid="forgot-password-goto-login-link"
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
