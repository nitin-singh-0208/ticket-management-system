import { useEffect, useState } from "react";
import { useParams, Link } from "react-router";
import {
  getTicket,
  updateTicket,
  changeStatus,
  TicketDetail,
  TicketStatus,
  TicketPriority,
  ApiError,
} from "../api/tickets";

const PRIORITY_LABELS: Record<string, string> = {
  LOW: "Low",
  MEDIUM: "Medium",
  HIGH: "High",
};

const PRIORITY_OPTIONS: { value: TicketPriority; label: string }[] = [
  { value: "LOW", label: "Low" },
  { value: "MEDIUM", label: "Medium" },
  { value: "HIGH", label: "High" },
];

export function TicketDetailPage() {
  const { ticketId } = useParams<{ ticketId: string }>();
  const [ticket, setTicket] = useState<TicketDetail | null>(null);
  const [errorDetail, setErrorDetail] = useState<string | null>(null);
  const [statusError, setStatusError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isChangingStatus, setIsChangingStatus] = useState(false);

  const [editTitle, setEditTitle] = useState("");
  const [editDescription, setEditDescription] = useState("");
  const [editPriority, setEditPriority] = useState<TicketPriority>("LOW");
  const [editAssignee, setEditAssignee] = useState("");
  const [editResolutionNotes, setEditResolutionNotes] = useState("");
  const [editFieldErrors, setEditFieldErrors] = useState<Record<string, string>>({});
  const [editGeneralError, setEditGeneralError] = useState<string | null>(null);
  const [isSaving, setIsSaving] = useState(false);

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
          setEditTitle(data.title);
          setEditDescription(data.description);
          setEditPriority(data.priority);
          setEditAssignee(data.assignee);
          setEditResolutionNotes(data.resolutionNotes ?? "");
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

  async function handleEditSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!ticketId || !ticket) {
      return;
    }

    setEditFieldErrors({});
    setEditGeneralError(null);
    setIsSaving(true);

    const payload: Record<string, string> = {};
    if (editTitle !== ticket.title) payload.title = editTitle;
    if (editDescription !== ticket.description) payload.description = editDescription;
    if (editPriority !== ticket.priority) payload.priority = editPriority;
    if (editAssignee !== ticket.assignee) payload.assignee = editAssignee;
    const currentNotes = ticket.resolutionNotes ?? "";
    if (editResolutionNotes !== currentNotes) payload.resolutionNotes = editResolutionNotes;

    try {
      const updated = await updateTicket(ticketId, payload);
      setTicket(updated);
      setEditTitle(updated.title);
      setEditDescription(updated.description);
      setEditPriority(updated.priority);
      setEditAssignee(updated.assignee);
      setEditResolutionNotes(updated.resolutionNotes ?? "");
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.problem.errors && err.problem.errors.length > 0) {
          const errorsMap: Record<string, string> = {};
          for (const fe of err.problem.errors) {
            errorsMap[fe.field] = fe.message;
          }
          setEditFieldErrors(errorsMap);
        } else if (err.problem.detail) {
          setEditGeneralError(err.problem.detail);
        } else {
          setEditGeneralError("Validation failed. Please check the fields.");
        }
      } else {
        setEditGeneralError("Failed to save changes.");
      }
    } finally {
      setIsSaving(false);
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
        <h2 style={{ fontSize: "1.1rem", marginBottom: "0.5rem" }}>Edit Ticket</h2>
        {editGeneralError && (
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
            {editGeneralError}
          </div>
        )}
        <form onSubmit={handleEditSubmit} noValidate>
          <div style={{ marginBottom: "1rem" }}>
            <label htmlFor="edit-title" style={{ display: "block", fontWeight: 600, marginBottom: 4 }}>
              Title
            </label>
            <input
              id="edit-title"
              name="title"
              type="text"
              maxLength={200}
              value={editTitle}
              onChange={(e) => setEditTitle(e.target.value)}
              style={{
                width: "100%",
                padding: "0.5rem",
                borderRadius: 4,
                border: editFieldErrors.title ? "1px solid #dc2626" : "1px solid #d1d5db",
              }}
            />
            {editFieldErrors.title && (
              <span style={{ color: "#dc2626", fontSize: "0.875rem", display: "block", marginTop: 4 }}>
                {editFieldErrors.title}
              </span>
            )}
          </div>

          <div style={{ marginBottom: "1rem" }}>
            <label htmlFor="edit-description" style={{ display: "block", fontWeight: 600, marginBottom: 4 }}>
              Description
            </label>
            <textarea
              id="edit-description"
              name="description"
              rows={4}
              maxLength={5000}
              value={editDescription}
              onChange={(e) => setEditDescription(e.target.value)}
              style={{
                width: "100%",
                padding: "0.5rem",
                borderRadius: 4,
                border: editFieldErrors.description ? "1px solid #dc2626" : "1px solid #d1d5db",
              }}
            />
            {editFieldErrors.description && (
              <span style={{ color: "#dc2626", fontSize: "0.875rem", display: "block", marginTop: 4 }}>
                {editFieldErrors.description}
              </span>
            )}
          </div>

          <div style={{ marginBottom: "1rem" }}>
            <label htmlFor="edit-priority" style={{ display: "block", fontWeight: 600, marginBottom: 4 }}>
              Priority
            </label>
            <select
              id="edit-priority"
              name="priority"
              value={editPriority}
              onChange={(e) => setEditPriority(e.target.value as TicketPriority)}
              style={{
                width: "100%",
                padding: "0.5rem",
                borderRadius: 4,
                border: editFieldErrors.priority ? "1px solid #dc2626" : "1px solid #d1d5db",
              }}
            >
              {PRIORITY_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
            {editFieldErrors.priority && (
              <span style={{ color: "#dc2626", fontSize: "0.875rem", display: "block", marginTop: 4 }}>
                {editFieldErrors.priority}
              </span>
            )}
          </div>

          <div style={{ marginBottom: "1rem" }}>
            <label htmlFor="edit-assignee" style={{ display: "block", fontWeight: 600, marginBottom: 4 }}>
              Assignee
            </label>
            <input
              id="edit-assignee"
              name="assignee"
              type="text"
              maxLength={200}
              value={editAssignee}
              onChange={(e) => setEditAssignee(e.target.value)}
              style={{
                width: "100%",
                padding: "0.5rem",
                borderRadius: 4,
                border: editFieldErrors.assignee ? "1px solid #dc2626" : "1px solid #d1d5db",
              }}
            />
            {editFieldErrors.assignee && (
              <span style={{ color: "#dc2626", fontSize: "0.875rem", display: "block", marginTop: 4 }}>
                {editFieldErrors.assignee}
              </span>
            )}
          </div>

          <div style={{ marginBottom: "1rem" }}>
            <label htmlFor="edit-resolutionNotes" style={{ display: "block", fontWeight: 600, marginBottom: 4 }}>
              Resolution Notes
            </label>
            <textarea
              id="edit-resolutionNotes"
              name="resolutionNotes"
              rows={3}
              maxLength={5000}
              value={editResolutionNotes}
              onChange={(e) => setEditResolutionNotes(e.target.value)}
              style={{
                width: "100%",
                padding: "0.5rem",
                borderRadius: 4,
                border: editFieldErrors.resolutionNotes ? "1px solid #dc2626" : "1px solid #d1d5db",
              }}
            />
            {editFieldErrors.resolutionNotes && (
              <span style={{ color: "#dc2626", fontSize: "0.875rem", display: "block", marginTop: 4 }}>
                {editFieldErrors.resolutionNotes}
              </span>
            )}
          </div>

          <button
            type="submit"
            disabled={isSaving}
            style={{
              backgroundColor: "#2563eb",
              color: "white",
              padding: "0.5rem 1rem",
              border: "none",
              borderRadius: 4,
              fontWeight: 600,
              cursor: isSaving ? "not-allowed" : "pointer",
            }}
          >
            {isSaving ? "Saving..." : "Save Changes"}
          </button>
        </form>
      </section>

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
        <Link to="/tickets" style={{ color: "#2563eb", textDecoration: "none", marginRight: "1rem" }}>
          &larr; All tickets
        </Link>
        <Link to="/tickets/new" style={{ color: "#2563eb", textDecoration: "none" }}>
          Create another ticket
        </Link>
      </footer>
    </div>
  );
}
