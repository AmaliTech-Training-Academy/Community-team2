import { Navbar } from "../organisms/Navbar";
import { Outlet } from "react-router-dom";

export function MainLayout() {
  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="max-w-5xl mx-auto px-6 py-6">
        <Outlet />
      </main>
    </div>
  );
}
