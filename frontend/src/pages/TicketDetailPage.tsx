import { useEffect, useState } from "react";
import { useParams, Link } from "react-router";
import { getTicket, changeStatus, TicketDetail, TicketStatus, ApiError } from "../api/tickets";

const PRIORITY_LABELS: Record<string, string> = {
  LOW: "Low",
  MEDIUM: "Medium",
  HIGH: "High",
};

export function TicketDetailPage() {
  const { ticketId } = useParams<{ ticketId: string }>();
  const [ticket, setTicket] = useState<TicketDetail | null>(null);
  const [errorDetail, setErrorDetail] = useState<string | null>(null);
  const [statusError, setStatusError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isChangingStatus, setIsChangingStatus] = useState(false);

  useEffect(() => {
    if (!ticketId) {
      setErrorDetail("Missing ticket ID");
      setIsLoading(false);
      return;
    }

    let isMounted = true;
    setIsLoading(true);
    setErrorDetail(null);

    getTicket(ticketId)
      .then((data) => {
        if (isMounted) {
          setTicket(data);
          setIsLoading(false);
        }
      })
      .catch((err) => {
        if (isMounted) {
          if (err instanceof ApiError) {
            setErrorDetail(err.problem.detail || `Error: ${err.status}`);
          } else {
            setErrorDetail("Failed to load ticket.");
          }
          setIsLoading(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, [ticketId]);

  async function handleStatusChange(targetStatus: TicketStatus) {
    if (!ticketId || !ticket) {
      return;
    }

    setIsChangingStatus(true);
    setStatusError(null);

    try {
      const updated = await changeStatus(ticketId, targetStatus);
      setTicket(updated);
    } catch (err) {
      if (err instanceof ApiError) {
        setStatusError(err.problem.detail || `Error: ${err.status}`);
      } else {
        setStatusError("Failed to change status.");
      }
    } finally {
      setIsChangingStatus(false);
    }
  }

  if (isLoading) {
    return (
      <div style={{ maxWidth: 720, margin: "2rem auto", padding: "0 1rem", fontFamily: "system-ui, -apple-system, sans-serif" }}>
        <p>Loading ticket...</p>
      </div>
    );
  }

  if (errorDetail) {
    return (
      <div style={{ maxWidth: 720, margin: "2rem auto", padding: "0 1rem", fontFamily: "system-ui, -apple-system, sans-serif" }}>
        <div
          role="alert"
          style={{
            background: "#fee2e2",
            color: "#991b1b",
            padding: "1rem",
            borderRadius: 6,
            marginBottom: "1rem",
          }}
        >
          {errorDetail}
        </div>
        <Link to="/tickets/new" style={{ color: "#2563eb" }}>
          &larr; Create another ticket
        </Link>
      </div>
    );
  }

  if (!ticket) {
    return null;
  }

  return (
    <div style={{ maxWidth: 720, margin: "2rem auto", padding: "0 1rem", fontFamily: "system-ui, -apple-system, sans-serif" }}>
      <header style={{ borderBottom: "1px solid #e5e7eb", paddingBottom: "1rem", marginBottom: "1.5rem" }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "baseline" }}>
          <h1 style={{ margin: 0, fontSize: "1.75rem" }}>
            <span style={{ color: "#6b7280", marginRight: "0.5rem" }}>{ticket.ticketId}:</span>
            {ticket.title}
          </h1>
          <span
            style={{
              padding: "0.25rem 0.75rem",
              borderRadius: 9999,
              fontWeight: 600,
              fontSize: "0.875rem",
              background: "#e5e7eb",
              color: "#374151",
            }}
          >
            {ticket.status}
          </span>
        </div>
      </header>

      <section style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "1rem", marginBottom: "1.5rem" }}>
        <div>
          <span style={{ color: "#6b7280", fontSize: "0.875rem", display: "block" }}>Priority</span>
          <strong>{PRIORITY_LABELS[ticket.priority] || ticket.priority}</strong>
        </div>
        <div>
          <span style={{ color: "#6b7280", fontSize: "0.875rem", display: "block" }}>Category</span>
          <strong>{ticket.category}</strong>
        </div>
        <div>
          <span style={{ color: "#6b7280", fontSize: "0.875rem", display: "block" }}>Assignee</span>
          <strong>{ticket.assignee}</strong>
        </div>
        <div>
          <span style={{ color: "#6b7280", fontSize: "0.875rem", display: "block" }}>Created At</span>
          <span>{ticket.createdAt}</span>
        </div>
      </section>

      {(ticket.allowedNextStatuses?.length ?? 0) > 0 && (
        <section style={{ marginBottom: "1.5rem" }}>
          <h2 style={{ fontSize: "1.1rem", marginBottom: "0.5rem" }}>Change Status</h2>
          {statusError && (
            <div
              role="alert"
              style={{
                background: "#fee2e2",
                color: "#991b1b",
                padding: "0.75rem 1rem",
                borderRadius: 6,
                marginBottom: "0.75rem",
              }}
            >
              {statusError}
            </div>
          )}
          <div style={{ display: "flex", gap: "0.5rem", flexWrap: "wrap" }}>
            {ticket.allowedNextStatuses!.map((nextStatus) => (
              <button
                key={nextStatus}
                type="button"
                disabled={isChangingStatus}
                onClick={() => handleStatusChange(nextStatus)}
                style={{
                  padding: "0.5rem 1rem",
                  borderRadius: 6,
                  border: "1px solid #d1d5db",
                  background: "#ffffff",
                  cursor: isChangingStatus ? "not-allowed" : "pointer",
                  fontWeight: 500,
                }}
              >
                Move to {nextStatus.replace("_", " ")}
              </button>
            ))}
          </div>
        </section>
      )}

      <section style={{ marginBottom: "1.5rem" }}>
        <h2 style={{ fontSize: "1.1rem", marginBottom: "0.5rem" }}>Description</h2>
        <div
          style={{
            whiteSpace: "pre-wrap",
            background: "#f9fafb",
            padding: "1rem",
            borderRadius: 6,
            border: "1px solid #e5e7eb",
          }}
        >
          {ticket.description}
        </div>
      </section>

      <section style={{ marginBottom: "1.5rem" }}>
        <h2 style={{ fontSize: "1.1rem", marginBottom: "0.5rem" }}>Resolution Notes</h2>
        <div
          style={{
            whiteSpace: "pre-wrap",
            background: "#f9fafb",
            padding: "1rem",
            borderRadius: 6,
            border: "1px solid #e5e7eb",
            color: ticket.resolutionNotes ? "#111827" : "#6b7280",
          }}
        >
          {ticket.resolutionNotes || "None"}
        </div>
      </section>

      <section style={{ marginBottom: "2rem" }}>
        <h2 style={{ fontSize: "1.1rem", marginBottom: "0.5rem" }}>Comments</h2>
        {ticket.comments.length === 0 ? (
          <p style={{ color: "#6b7280", fontStyle: "italic" }}>No comments yet.</p>
        ) : (
          <ul style={{ listStyle: "none", padding: 0, margin: 0 }}>
            {ticket.comments.map((comment) => (
              <li
                key={comment.id}
                style={{
                  border: "1px solid #e5e7eb",
                  borderRadius: 6,
                  padding: "0.75rem 1rem",
                  marginBottom: "0.75rem",
                  background: "#ffffff",
                }}
              >
                <div style={{ whiteSpace: "pre-wrap", marginBottom: "0.5rem" }}>{comment.text}</div>
                <div style={{ fontSize: "0.75rem", color: "#6b7280" }}>{comment.createdAt}</div>
              </li>
            ))}
          </ul>
        )}
      </section>

      <footer style={{ borderTop: "1px solid #e5e7eb", paddingTop: "1rem" }}>
        <Link to="/tickets/new" style={{ color: "#2563eb", textDecoration: "none" }}>
          &larr; Create another ticket
        </Link>
      </footer>
    </div>
  );
}
