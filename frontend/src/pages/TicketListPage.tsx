import { useEffect, useState } from "react";
import { Link } from "react-router";
import {
  listTickets,
  TicketSummary,
  TicketStatus,
  ApiError,
} from "../api/tickets";

const PRIORITY_LABELS: Record<string, string> = {
  LOW: "Low",
  MEDIUM: "Medium",
  HIGH: "High",
};

const STATUS_OPTIONS: { value: "" | TicketStatus; label: string }[] = [
  { value: "", label: "All statuses" },
  { value: "OPEN", label: "OPEN" },
  { value: "IN_PROGRESS", label: "IN_PROGRESS" },
  { value: "RESOLVED", label: "RESOLVED" },
  { value: "CLOSED", label: "CLOSED" },
  { value: "CANCELLED", label: "CANCELLED" },
];

export function TicketListPage() {
  const [keyword, setKeyword] = useState("");
  const [statusFilter, setStatusFilter] = useState<"" | TicketStatus>("");
  const [tickets, setTickets] = useState<TicketSummary[]>([]);
  const [errorDetail, setErrorDetail] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;
    setIsLoading(true);
    setErrorDetail(null);

    listTickets({
      q: keyword.trim() || undefined,
      status: statusFilter || undefined,
    })
      .then((page) => {
        if (isMounted) {
          setTickets(page.content);
          setIsLoading(false);
        }
      })
      .catch((err) => {
        if (isMounted) {
          if (err instanceof ApiError) {
            setErrorDetail(err.problem.detail || `Error: ${err.status}`);
          } else {
            setErrorDetail("Failed to load tickets.");
          }
          setIsLoading(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, [keyword, statusFilter]);

  return (
    <div style={{ maxWidth: 960, margin: "2rem auto", padding: "0 1rem", fontFamily: "system-ui, -apple-system, sans-serif" }}>
      <header style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1.5rem" }}>
        <h1 style={{ margin: 0, fontSize: "1.75rem" }}>Support Tickets</h1>
        <Link
          to="/tickets/new"
          style={{
            backgroundColor: "#2563eb",
            color: "white",
            padding: "0.5rem 1rem",
            borderRadius: 4,
            textDecoration: "none",
            fontWeight: 600,
          }}
        >
          Create Ticket
        </Link>
      </header>

      <section style={{ display: "grid", gridTemplateColumns: "2fr 1fr", gap: "1rem", marginBottom: "1.5rem" }}>
        <div>
          <label htmlFor="search" style={{ display: "block", fontWeight: 600, marginBottom: 4 }}>
            Search
          </label>
          <input
            id="search"
            name="q"
            type="search"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="Search title or description"
            style={{ width: "100%", padding: "0.5rem", borderRadius: 4, border: "1px solid #d1d5db" }}
          />
        </div>
        <div>
          <label htmlFor="status-filter" style={{ display: "block", fontWeight: 600, marginBottom: 4 }}>
            Status
          </label>
          <select
            id="status-filter"
            name="status"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as "" | TicketStatus)}
            style={{ width: "100%", padding: "0.5rem", borderRadius: 4, border: "1px solid #d1d5db" }}
          >
            {STATUS_OPTIONS.map((opt) => (
              <option key={opt.label} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>
      </section>

      {errorDetail && (
        <div
          role="alert"
          style={{
            background: "#fee2e2",
            color: "#991b1b",
            padding: "0.75rem 1rem",
            borderRadius: 6,
            marginBottom: "1rem",
          }}
        >
          {errorDetail}
        </div>
      )}

      {isLoading ? (
        <p>Loading tickets...</p>
      ) : tickets.length === 0 ? (
        <p style={{ color: "#6b7280", fontStyle: "italic" }}>Nothing matched</p>
      ) : (
        <table style={{ width: "100%", borderCollapse: "collapse" }}>
          <thead>
            <tr style={{ borderBottom: "2px solid #e5e7eb", textAlign: "left" }}>
              <th style={{ padding: "0.75rem 0.5rem" }}>ID</th>
              <th style={{ padding: "0.75rem 0.5rem" }}>Title</th>
              <th style={{ padding: "0.75rem 0.5rem" }}>Status</th>
              <th style={{ padding: "0.75rem 0.5rem" }}>Priority</th>
              <th style={{ padding: "0.75rem 0.5rem" }}>Assignee</th>
            </tr>
          </thead>
          <tbody>
            {tickets.map((ticket) => (
              <tr key={ticket.ticketId} style={{ borderBottom: "1px solid #e5e7eb" }}>
                <td style={{ padding: "0.75rem 0.5rem" }}>
                  <Link to={`/tickets/${ticket.ticketId}`} style={{ color: "#2563eb", textDecoration: "none" }}>
                    {ticket.ticketId}
                  </Link>
                </td>
                <td style={{ padding: "0.75rem 0.5rem" }}>{ticket.title}</td>
                <td style={{ padding: "0.75rem 0.5rem" }}>{ticket.status}</td>
                <td style={{ padding: "0.75rem 0.5rem" }}>{PRIORITY_LABELS[ticket.priority] || ticket.priority}</td>
                <td style={{ padding: "0.75rem 0.5rem" }}>{ticket.assignee}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
