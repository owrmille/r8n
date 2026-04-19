import { createRoot } from "react-dom/client";
import "@/lib/auth/bootstrap";
import App from "./App.tsx";
import "./index.css";

createRoot(document.getElementById("root")!).render(<App />);
