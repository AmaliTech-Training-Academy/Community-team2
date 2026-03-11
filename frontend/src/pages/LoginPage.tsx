import React, { useState, useCallback, useRef } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuthStore } from "../features/auth/authStore";
import { useToast } from "../components/atoms/Toast";
import { Text } from "../components/atoms/Text";
import Logo from "../assets/images/Logo.svg?react";
import Lock from "../assets/images/lock.svg?react";
import EyeOn from "../assets/images/eye-on.svg?react";
import EyeOff from "../assets/images/eye-off.svg?react";
import EmailIcon from "../assets/images/mail.svg?react";

const EMAIL_RE = /\S+@\S+\.\S+/;

export default function LoginPage() {
  const login = useAuthStore((s) => s.login);
  const toast = useToast();
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPass, setShowPass] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);

  const fieldsRef = useRef({ email, password });
  fieldsRef.current = { email, password };

  const validate = useCallback((): Record<string, string> => {
    const { email: e, password: p } = fieldsRef.current;
    const errs: Record<string, string> = {};
    if (!e) errs.email = "Email is required";
    else if (!EMAIL_RE.test(e)) errs.email = "Your email is incorrect";
    if (!p) errs.password = "Password is required";
    return errs;
  }, []);

  const handleSubmit = useCallback(async () => {
    const errs = validate();
    if (Object.keys(errs).length) {
      setErrors(errs);
      return;
    }
    setLoading(true);
    setErrors({});
    try {
      const { email: e, password: p } = fieldsRef.current;
      await login(e, p);
      toast("Authenticated successfully");
      navigate("/");
    } catch (err: unknown) {
      setErrors({ email: String(err) });
    } finally {
      setLoading(false);
    }
  }, [validate, login, toast, navigate]);

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (e.key === "Enter") handleSubmit();
    },
    [handleSubmit],
  );

  const toggleShowPass = useCallback(() => setShowPass((s) => !s), []);

  return (
    <div
      data-testid="login-page"
      className=" bg-background rounded-3xl  border-0 md:border md:border-gray-200  pb-40 md:py-8 md:px-8  w-full max-w-md flex flex-col gap-2.5"
    >
      <div className="flex flex-col gap-12">
        <div className="flex flex-col items-center gap-8 ">
          <Logo />
          <div className="flex flex-col items-center gap-1">
            <div className="text-h-lg font-semibold text-blue-gray-dark text-center">
              Welcome back
            </div>
            <div className="  text-blue-gray text-center">
              Sign in to your neighborhood community
            </div>
          </div>
        </div>

        <div>
          <div className="mb-4">
            <label
              className=" text-body-sm block text-blue-gray-light mb-1.5"
              htmlFor="login-email"
            >
              Email
            </label>
            <div className="relative">
              <span
                className={`absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none ${
                  errors.email ? "text-red-600" : "text-[#5A6F7C]"
                }`}
              >
                <EmailIcon stroke="currentColor" />
              </span>
              <input
                id="login-email"
                data-testid="login-email-input"
                className={`w-full pl-9 pr-3 py-2.5 rounded-lg bg-primary text-body-lg text-blue-gray-light placeholder:text-[#5A6F7C] focus:outline-none transition-colors border ${
                  errors.email
                    ? "border-red-400 bg-red-50 text-red-600"
                    : "border-gray-200 bg-gray-100 focus:border-navy focus:bg-white"
                }`}
                type="email"
                placeholder="your@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                onKeyDown={handleKeyDown}
              />
            </div>
            {errors.email && (
              <Text
                variant="body-sm"
                data-testid="login-email-error"
                className="text-red-500 mt-1"
              >
                {errors.email}
              </Text>
            )}
          </div>

          <div className="mb-6">
            <label
              className="text-body-sm block text-blue-gray-light mb-1.5"
              htmlFor="login-password"
            >
              Password
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
                id="login-password"
                data-testid="login-password-input"
                className={`w-full pl-9 pr-9 py-2.5 rounded-lg text-body-lg bg-primary text-blue-gray-light placeholder:text-[#5A6F7C] focus:outline-none transition-colors border ${
                  errors.password
                    ? "border-red-400 bg-red-50 text-red-600"
                    : "border-gray-200 bg-gray-100 focus:border-navy focus:bg-white"
                }`}
                type={showPass ? "text" : "password"}
                placeholder="Enter password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                onKeyDown={handleKeyDown}
              />
              <button
                data-testid="login-toggle-password-btn"
                type="button"
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400"
                onClick={toggleShowPass}
              >
                {showPass ? <EyeOff /> : <EyeOn />}
              </button>
            </div>
            {errors.password && (
              <Text
                variant="body-sm"
                data-testid="login-password-error"
                className="text-red-500 mt-1"
              >
                {errors.password}
              </Text>
            )}
          </div>

          <div>
            <button
              data-testid="login-submit-btn"
              onClick={handleSubmit}
              disabled={loading}
              className="w-full py-2.5 text-body-sm text-white bg-navy rounded-lg hover:bg-[#1a3347] transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {loading ? "Signing in…" : "Log In"}
            </button>

            <p className="flex justify-center items-center gap-3 text-center mt-7">
              <Text variant="body-sm" as="span" className="text-blue-gray">
                Don't have an account?
              </Text>
              <Link
                data-testid="login-goto-register-link"
                to="/register"
                className="text-body-sm  text-orange underline"
              >
                Create one now
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
