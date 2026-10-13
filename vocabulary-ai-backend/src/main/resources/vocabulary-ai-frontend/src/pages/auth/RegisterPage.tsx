import { useState, FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";
import { useAuth } from "@/hooks/useAuth";

export function RegisterPage() {
  const navigate = useNavigate();
  const { validateRegister, verifyOtpRegister, isLoading, error } = useAuth();
  
  // Step state: 1 = form, 2 = OTP verification
  const [step, setStep] = useState<1 | 2>(1);
  const [showPassword, setShowPassword] = useState(false);
  const [localError, setLocalError] = useState<string | null>(null);
  
  // Form data (Step 1)
  const [formData, setFormData] = useState({
    fullName: "",
    username: "",
    email: "",
    password: "",
  });
  
  // OTP data (Step 2)
  const [otp, setOtp] = useState("");

  const handleFormChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  // Step 1: Validate and send OTP
  const submitStep1 = async (event: FormEvent) => {
    event.preventDefault();
    setLocalError(null);

    try {
      // Call backend to validate register and send OTP
      await validateRegister(
        formData.email,
        formData.username,
        formData.password,
        formData.fullName
      );
      
      // Move to OTP verification step
      setStep(2);
    } catch (err) {
      setLocalError(err instanceof Error ? err.message : "Registration validation failed");
    }
  };

  // Step 2: Verify OTP and create account
  const submitStep2 = async (event: FormEvent) => {
    event.preventDefault();
    setLocalError(null);

    try {
      // Call backend to verify OTP
      await verifyOtpRegister(formData.email, otp);
      
      // Account created successfully
      // Redirect to login
      navigate("/login", {
        state: { 
          message: "Account created successfully! Please sign in with your credentials.",
          email: formData.email 
        }
      });
    } catch (err) {
      setLocalError(err instanceof Error ? err.message : "OTP verification failed");
    }
  };

  return (
    <form className="auth-form" onSubmit={step === 1 ? submitStep1 : submitStep2}>
      <div className="auth-heading">
        <p className="eyebrow">START YOUR JOURNEY</p>
        <h2>{step === 1 ? "Create your account" : "Verify your email"}</h2>
        <p>
          {step === 1
            ? "Join VocabularyAI and build a learning plan around your goals."
            : "Enter the verification code sent to your email"}
        </p>
      </div>

      {(error || localError) && (
        <div style={{ color: '#d32f2f', marginBottom: '1rem', fontSize: '14px' }}>
          {error || localError}
        </div>
      )}

      {step === 1 ? (
        <>
          {/* STEP 1: Registration Form */}
          <label className="auth-field">
            <span>Full name</span>
            <div>
              <Icon name="user" />
              <input
                required
                placeholder="Enter your full name"
                autoComplete="name"
                name="fullName"
                value={formData.fullName}
                onChange={handleFormChange}
                disabled={isLoading}
              />
            </div>
          </label>

          <label className="auth-field">
            <span>Username</span>
            <div>
              <Icon name="user" />
              <input
                required
                placeholder="Choose a username"
                autoComplete="username"
                name="username"
                value={formData.username}
                onChange={handleFormChange}
                disabled={isLoading}
              />
            </div>
          </label>

          <label className="auth-field">
            <span>Email address</span>
            <div>
              <Icon name="mail" />
              <input
                required
                type="email"
                placeholder="you@example.com"
                autoComplete="email"
                name="email"
                value={formData.email}
                onChange={handleFormChange}
                disabled={isLoading}
              />
            </div>
          </label>

          <label className="auth-field">
            <span>Password</span>
            <div>
              <Icon name="lock" />
              <input
                required
                type={showPassword ? "text" : "password"}
                placeholder="At least 8 characters"
                minLength={8}
                autoComplete="new-password"
                name="password"
                value={formData.password}
                onChange={handleFormChange}
                disabled={isLoading}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                aria-label={showPassword ? "Hide password" : "Show password"}
                disabled={isLoading}
              >
                <Icon name="eye" />
              </button>
            </div>
          </label>

          <Button type="submit" className="auth-submit" disabled={isLoading}>
            {isLoading ? "Creating account..." : "Create account"} <Icon name="arrow" />
          </Button>

          <div className="auth-divider">
            <span>or continue with</span>
          </div>

          <Button variant="secondary" className="google-button" disabled={isLoading}>
            <strong>G</strong> Google
          </Button>

          <p className="auth-switch">
            Already have an account? <button type="button" onClick={() => navigate("/login")} disabled={isLoading}>Sign in</button>
          </p>

          <p className="auth-terms">
            By creating an account, you agree to our{" "}
            <button type="button" disabled={isLoading}>Terms of Service</button> and{" "}
            <button type="button" disabled={isLoading}>Privacy Policy</button>.
          </p>
        </>
      ) : (
        <>
          {/* STEP 2: OTP Verification */}
          <div className="auth-field" style={{ marginBottom: '1rem' }}>
            <p style={{ fontSize: '14px', color: '#666', marginBottom: '0.5rem' }}>
              We've sent a 6-digit code to <strong>{formData.email}</strong>
            </p>
          </div>

          <label className="auth-field">
            <span>Verification code</span>
            <div>
              <Icon name="lock" />
              <input
                required
                type="text"
                placeholder="Enter 6-digit code"
                maxLength={6}
                pattern="[0-9]{6}"
                value={otp}
                onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                disabled={isLoading}
                autoComplete="off"
                inputMode="numeric"
              />
            </div>
          </label>

          <Button type="submit" className="auth-submit" disabled={isLoading || otp.length !== 6}>
            {isLoading ? "Verifying..." : "Verify email"} <Icon name="arrow" />
          </Button>

          <Button
            type="button"
            variant="secondary"
            onClick={() => {
              setStep(1);
              setOtp("");
              setLocalError(null);
            }}
            disabled={isLoading}
            style={{ marginTop: '0.5rem', width: '100%' }}
          >
            Back to register
          </Button>

          <p className="auth-switch" style={{ marginTop: '1rem' }}>
            Didn't receive the code?{" "}
            <button
              type="button"
              onClick={() => {
                setStep(1);
                setOtp("");
              }}
              disabled={isLoading}
            >
              Resend
            </button>
          </p>
        </>
      )}
    </form>
  );
}
