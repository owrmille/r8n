import { useState, type FormEvent } from "react";
import { motion } from "framer-motion";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import { useLoginMutation } from "@/lib/server-state";
import { toast } from "@/hooks/use-toast";
import logo from "@/assets/logo.png";

const Login = () => {
  const [isSignUp, setIsSignUp] = useState(false);
  const [login, setLogin] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [gdprAccepted, setGdprAccepted] = useState(false);
  const navigate = useNavigate();
  const loginMutation = useLoginMutation({
    onSuccess: () => {
      navigate("/", { replace: true });
    },
  });

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (isSignUp) {
      toast({
        title: "Sign up is not connected yet",
        description: "Use the sign in flow while backend registration is still missing.",
      });
      return;
    }

    if (login.trim() === "" || password.trim() === "") {
      toast({
        title: "Missing credentials",
        description: "Enter both login and password.",
      });
      return;
    }

    loginMutation.mutate({
      login: login.trim(),
      password,
    });
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-background px-4">
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="w-full max-w-sm"
      >
        <div className="mb-8 text-center">
          <img src={logo} alt="r8n" className="mx-auto mb-4 h-10 w-auto" />
          <h1 className="text-2xl font-semibold tracking-tight text-foreground">
            {isSignUp ? "Create your account" : "Welcome back"}
          </h1>
          <p className="mt-1 text-sm text-muted-foreground">
            {isSignUp ? "Start building your trusted network." : "Sign in to continue."}
          </p>
        </div>

        <div className="rounded-2xl border border-border bg-card p-6">
          <form
            onSubmit={handleSubmit}
            className="space-y-4"
          >
            <div className="space-y-2">
              <Label htmlFor="login" className="text-xs">Login</Label>
              <Input
                id="login"
                type="text"
                placeholder="test@test.test"
                value={login}
                onChange={(e) => setLogin(e.target.value)}
                className="rounded-xl"
                autoComplete="username"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="password" className="text-xs">Password</Label>
              <Input
                id="password"
                type="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="rounded-xl"
                autoComplete={isSignUp ? "new-password" : "current-password"}
              />
            </div>

            {!isSignUp && (
              <p className="text-xs text-muted-foreground">
                Stub credentials for local development: <span className="font-mono">test@test.test / 1234</span>
              </p>
            )}

            {isSignUp && (
              <div className="space-y-2">
                <Label htmlFor="confirmPassword" className="text-xs">Confirm password</Label>
                <Input
                  id="confirmPassword"
                  type="password"
                  placeholder="••••••••"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="rounded-xl"
                />
              </div>
            )}

            {isSignUp && (
              <label className="flex items-start gap-2.5 cursor-pointer">
                <input
                  type="checkbox"
                  checked={gdprAccepted}
                    onChange={(e) => setGdprAccepted(e.target.checked)}
                    className="mt-0.5 h-4 w-4 rounded border-border accent-primary"
                  />
                <span className="text-xs text-muted-foreground leading-relaxed">
                  I agree to the{" "}
                  <button type="button" className="text-primary hover:underline">Privacy Policy</button>{" "}
                  and{" "}
                  <button type="button" className="text-primary hover:underline">Terms of Service</button>.
                  Your data is processed in accordance with GDPR.
                </span>
              </label>
            )}

            {!isSignUp && (
              <div className="text-right">
                <button type="button" className="text-xs text-muted-foreground hover:text-foreground transition-colors">
                  Forgot password?
                </button>
              </div>
            )}

            <Button type="submit" className="w-full rounded-xl">
              {loginMutation.isPending
                ? "Signing in..."
                : isSignUp
                  ? "Create account"
                  : "Sign in"}
            </Button>
          </form>

          <Separator className="my-5" />

          <Button variant="outline" className="w-full rounded-xl gap-2" onClick={() => {}}>
            <svg className="h-4 w-4" viewBox="0 0 24 24">
              <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" fill="#4285F4"/>
              <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
              <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
              <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
            </svg>
            Continue with Google
          </Button>
        </div>

        <p className="mt-6 text-center text-xs text-muted-foreground">
          {isSignUp ? "Already have an account?" : "Don't have an account?"}{" "}
          <button
            onClick={() => setIsSignUp(!isSignUp)}
            className="font-medium text-primary hover:underline"
          >
            {isSignUp ? "Sign in" : "Create one"}
          </button>
        </p>
      </motion.div>
    </div>
  );
};

export default Login;
