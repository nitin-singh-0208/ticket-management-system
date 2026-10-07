import { BrowserRouter, Routes, Route, Navigate } from "react-router";
import { TicketCreatePage } from "./pages/TicketCreatePage";
import { TicketDetailPage } from "./pages/TicketDetailPage";

export function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/tickets/new" element={<TicketCreatePage />} />
        <Route path="/tickets/:ticketId" element={<TicketDetailPage />} />
        <Route path="*" element={<Navigate to="/tickets/new" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
