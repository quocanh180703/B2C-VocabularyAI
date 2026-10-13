import { useState, FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/common/Button";
import { Icon } from "@/components/common/Icon";
import { useAuth } from "@/hooks/useAuth";

export function LoginPage() {
  const navigate = useNavigate();
  const { login, isLoading, error } = useAuth();
  const [showPassword, setShowPassword] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [localError, setLocalError] = useState<string | null>(null);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setLocalError(null);

    try {
      await login(email, password);
      navigate("/dashboard");
    } catch (err) {
      setLocalError(err instanceof Error ? err.message : "Login failed");
    }
  };

  return (
    <form className="auth-form" onSubmit={submit}>
      <div className="auth-heading">
        <p className="eyebrow">WELCOME BACK</p>
        <h2>Continue learning</h2>
        <p>Sign in to pick up where you left off.</p>
      </div>

      {(error || localError) && (
        <div style={{ color: '#d32f2f', marginBottom: '1rem', fontSize: '14px' }}>
          {error || localError}
        </div>
      )}

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

      <label className="auth-field">
        <span>Password</span>
        <div>
          <Icon name="lock" />
          <input
            required
            type={showPassword ? "text" : "password"}
            placeholder="Enter your password"
            minLength={8}
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
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

      <div className="auth-options">
        <label>
          <input type="checkbox" disabled={isLoading} /> Remember me
        </label>
        <button type="button" onClick={() => navigate("/forgot-password")} disabled={isLoading}>
          Forgot password?
        </button>
      </div>

      <Button type="submit" className="auth-submit" disabled={isLoading}>
        {isLoading ? "Signing in..." : "Sign in"} <Icon name="arrow" />
      </Button>

      <div className="auth-divider">
        <span>or continue with</span>
      </div>

      <Button variant="secondary" className="google-button" disabled={isLoading}>
        <strong>G</strong> Google
      </Button>

      <p className="auth-switch">
        New to VocabularyAI?{" "}
        <button type="button" onClick={() => navigate("/register")} disabled={isLoading}>
          Create an account
        </button>
      </p>
    </form>
  );
}
