import { Navbar } from "../organisms/Navbar";
import { Outlet } from "react-router-dom";

export function MainLayout() {
  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="mx-auto w-full max-w-360 px-6 py-12 md:px-30 md:pt-12 md:pb-16">
        <Outlet />
      </main>
    </div>
  );
}
