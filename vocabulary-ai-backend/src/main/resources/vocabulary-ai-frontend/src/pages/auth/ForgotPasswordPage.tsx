import { useState, FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";
import { useAuth } from "@/hooks/useAuth";

export function ForgotPasswordPage() {
  const navigate = useNavigate();
  const { 
    sendOtpForgotPassword, 
    verifyOtpForgotPassword, 
    resetPassword, 
    isLoading, 
    error 
  } = useAuth();
  
  // Step: 1 = email, 2 = OTP, 3 = new password, 4 = success
  const [step, setStep] = useState<1 | 2 | 3 | 4>(1);
  const [email, setEmail] = useState("");
  const [otp, setOtp] = useState("");
  const [resetToken, setResetToken] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [localError, setLocalError] = useState<string | null>(null);

  // Step 1: Send OTP
  const submitStep1 = async (event: FormEvent) => {
    event.preventDefault();
    setLocalError(null);

    try {
      await sendOtpForgotPassword(email);
      setStep(2);
    } catch (err) {
      setLocalError(err instanceof Error ? err.message : "Failed to send OTP");
    }
  };

  // Step 2: Verify OTP
  const submitStep2 = async (event: FormEvent) => {
    event.preventDefault();
    setLocalError(null);

    try {
      const token = await verifyOtpForgotPassword(email, otp);
      setResetToken(token);
      setStep(3);
    } catch (err) {
      setLocalError(err instanceof Error ? err.message : "OTP verification failed");
    }
  };

  // Step 3: Reset password (AUTO-LOGIN)
  const submitStep3 = async (event: FormEvent) => {
    event.preventDefault();
    setLocalError(null);

    if (newPassword !== confirmPassword) {
      setLocalError("Passwords do not match");
      return;
    }

    try {
      await resetPassword(email, newPassword, resetToken);
      // Auto-logged in, redirect to dashboard
      setStep(4);
      setTimeout(() => navigate("/dashboard"), 1000);
    } catch (err) {
      setLocalError(err instanceof Error ? err.message : "Failed to reset password");
    }
  };

  if (step === 4) {
    return (
      <div className="auth-form">
        <div className="auth-heading">
          <span className="complete-icon" style={{ fontSize: '3rem', marginBottom: '1rem' }}>
            <Icon name="check" />
          </span>
          <h2>Password reset successful</h2>
          <p>Your password has been updated. You're now signed in and can start learning!</p>
        </div>

        <Button type="button" onClick={() => navigate("/dashboard")} className="auth-submit">
          Go to dashboard <Icon name="arrow" />
        </Button>
      </div>
    );
  }

  return (
    <form
      className="auth-form"
      onSubmit={step === 1 ? submitStep1 : step === 2 ? submitStep2 : submitStep3}
    >
      <div className="auth-heading">
        <p className="eyebrow">ACCOUNT RECOVERY</p>
        <h2>
          {step === 1
            ? "Reset your password"
            : step === 2
              ? "Verify your email"
              : "Create new password"}
        </h2>
        <p>
          {step === 1
            ? "Enter your email address and we'll send you a verification code."
            : step === 2
              ? "Enter the 6-digit code we sent to your email"
              : "Enter a new password for your account."}
        </p>
      </div>

      {(error || localError) && (
        <div style={{ color: '#d32f2f', marginBottom: '1rem', fontSize: '14px' }}>
          {error || localError}
        </div>
      )}

      {step === 1 && (
        <label className="auth-field">
          <span>Email address</span>
          <div>
            <Icon name="mail" />
            <input
              required
              type="email"
              placeholder="you@example.com"
              autoComplete="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disabled={isLoading}
            />
          </div>
        </label>
      )}

      {step === 2 && (
        <>
          <div className="auth-field" style={{ marginBottom: '1rem' }}>
            <p style={{ fontSize: '14px', color: '#666', marginBottom: '0.5rem' }}>
              Verification code sent to <strong>{email}</strong>
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
          <p className="form-hint" style={{ fontSize: '14px', marginTop: '0.5rem' }}>
            Didn't receive the code?{" "}
            <button
              type="button"
              onClick={(e) => {
                e.preventDefault();
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

      {step === 3 && (
        <>
          <label className="auth-field">
            <span>New password</span>
            <div>
              <Icon name="lock" />
              <input
                required
                type="password"
                placeholder="At least 8 characters"
                minLength={8}
                autoComplete="new-password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                disabled={isLoading}
              />
            </div>
          </label>
          <label className="auth-field">
            <span>Confirm password</span>
            <div>
              <Icon name="lock" />
              <input
                required
                type="password"
                placeholder="Confirm your password"
                minLength={8}
                autoComplete="new-password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                disabled={isLoading}
              />
            </div>
          </label>
        </>
      )}

      <Button 
        type="submit" 
        className="auth-submit" 
        disabled={isLoading || (step === 2 && otp.length !== 6)}
      >
        {step === 1 
          ? "Send verification code" 
          : step === 2 
            ? "Verify code" 
            : "Reset password"}
        <Icon name="arrow" />
      </Button>

      <p className="auth-switch">
        <button type="button" onClick={() => navigate("/login")} disabled={isLoading}>
          ← Back to sign in
        </button>
      </p>
    </form>
  );
}
