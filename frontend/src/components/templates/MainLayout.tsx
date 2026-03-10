import { Navbar } from "../organisms/Navbar";
import { Outlet } from "react-router-dom";

export function MainLayout() {
  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="mx-auto w-full max-w-300 px-6 py-6">
        <Outlet />
      </main>
    </div>
  );
}
