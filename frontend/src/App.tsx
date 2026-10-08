import { BrowserRouter, NavLink, Routes, Route, Navigate } from "react-router";
import { AskAiPage } from "./pages/AskAiPage";
import { TicketCreatePage } from "./pages/TicketCreatePage";
import { TicketDetailPage } from "./pages/TicketDetailPage";
import { TicketListPage } from "./pages/TicketListPage";

const navLinkStyle = ({ isActive }: { isActive: boolean }) => ({
  color: isActive ? "#1d4ed8" : "#374151",
  fontWeight: isActive ? 700 : 600,
  textDecoration: "none",
});

export function App() {
  return (
    <BrowserRouter>
      <nav
        style={{
          display: "flex",
          gap: "1.25rem",
          padding: "0.75rem 1rem",
          borderBottom: "1px solid #e5e7eb",
          fontFamily: "system-ui, -apple-system, sans-serif",
        }}
      >
        <NavLink to="/tickets" style={navLinkStyle}>
          Tickets
        </NavLink>
        <NavLink to="/ask" style={navLinkStyle}>
          Ask AI
        </NavLink>
      </nav>
      <Routes>
        <Route path="/tickets" element={<TicketListPage />} />
        <Route path="/tickets/new" element={<TicketCreatePage />} />
        <Route path="/tickets/:ticketId" element={<TicketDetailPage />} />
        <Route path="/ask" element={<AskAiPage />} />
        <Route path="*" element={<Navigate to="/tickets" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
