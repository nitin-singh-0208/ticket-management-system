import React, { useState } from "react";
import { useNavigate } from "react-router";
import {
  createTicket,
  ApiError,
  TicketPriority,
  CreateTicketRequest,
} from "../api/tickets";

const PRIORITY_OPTIONS: { value: TicketPriority; label: string }[] = [
  { value: "LOW", label: "Low" },
  { value: "MEDIUM", label: "Medium" },
  { value: "HIGH", label: "High" },
];

export function TicketCreatePage() {
  const navigate = useNavigate();

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [priority, setPriority] = useState<TicketPriority>("LOW");
  const [assignee, setAssignee] = useState("");
  const [category, setCategory] = useState("");
  const [resolutionNotes, setResolutionNotes] = useState("");

  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [generalError, setGeneralError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setFieldErrors({});
    setGeneralError(null);
    setIsSubmitting(true);

    const payload: CreateTicketRequest = {
      title,
      description,
      priority,
      assignee,
      category,
      ...(resolutionNotes ? { resolutionNotes } : {}),
    };

    try {
      const { ticket, location } = await createTicket(payload);
      let targetPath = `/tickets/${ticket.ticketId}`;
      if (location) {
        // If location is /api/tickets/TKT-1001 or full URL
        const match = location.match(/\/tickets\/(TKT-[^/?#]+)/);
        if (match) {
          targetPath = `/tickets/${match[1]}`;
        }
      }
      navigate(targetPath);
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.problem.errors && err.problem.errors.length > 0) {
          const errorsMap: Record<string, string> = {};
          for (const fe of err.problem.errors) {
            errorsMap[fe.field] = fe.message;
          }
          setFieldErrors(errorsMap);
        } else if (err.problem.detail) {
          setGeneralError(err.problem.detail);
        } else {
          setGeneralError("Validation failed. Please check the fields.");
        }
      } else {
        setGeneralError("An unexpected error occurred. Please try again.");
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div style={{ maxWidth: 640, margin: "2rem auto", padding: "0 1rem", fontFamily: "system-ui, -apple-system, sans-serif" }}>
      <h1>Create Support Ticket</h1>

      {generalError && (
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
          {generalError}
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate>
        <div style={{ marginBottom: "1rem" }}>
          <label htmlFor="title" style={{ display: "block", fontWeight: 600, marginBottom: 4 }}>
            Title <span style={{ color: "#dc2626" }}>*</span>
          </label>
          <input
            id="title"
            name="title"
            type="text"
            maxLength={200}
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            style={{ width: "100%", padding: "0.5rem", borderRadius: 4, border: fieldErrors.title ? "1px solid #dc2626" : "1px solid #d1d5db" }}
          />
          {fieldErrors.title && (
            <span style={{ color: "#dc2626", fontSize: "0.875rem", display: "block", marginTop: 4 }}>
              {fieldErrors.title}
            </span>
          )}
        </div>

        <div style={{ marginBottom: "1rem" }}>
          <label htmlFor="description" style={{ display: "block", fontWeight: 600, marginBottom: 4 }}>
            Description <span style={{ color: "#dc2626" }}>*</span>
          </label>
          <textarea
            id="description"
            name="description"
            rows={4}
            maxLength={5000}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            style={{ width: "100%", padding: "0.5rem", borderRadius: 4, border: fieldErrors.description ? "1px solid #dc2626" : "1px solid #d1d5db" }}
          />
          {fieldErrors.description && (
            <span style={{ color: "#dc2626", fontSize: "0.875rem", display: "block", marginTop: 4 }}>
              {fieldErrors.description}
            </span>
          )}
        </div>

        <div style={{ marginBottom: "1rem" }}>
          <label htmlFor="priority" style={{ display: "block", fontWeight: 600, marginBottom: 4 }}>
            Priority <span style={{ color: "#dc2626" }}>*</span>
          </label>
          <select
            id="priority"
            name="priority"
            value={priority}
            onChange={(e) => setPriority(e.target.value as TicketPriority)}
            style={{ width: "100%", padding: "0.5rem", borderRadius: 4, border: fieldErrors.priority ? "1px solid #dc2626" : "1px solid #d1d5db" }}
          >
            {PRIORITY_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
          {fieldErrors.priority && (
            <span style={{ color: "#dc2626", fontSize: "0.875rem", display: "block", marginTop: 4 }}>
              {fieldErrors.priority}
            </span>
          )}
        </div>

        <div style={{ marginBottom: "1rem" }}>
          <label htmlFor="assignee" style={{ display: "block", fontWeight: 600, marginBottom: 4 }}>
            Assignee <span style={{ color: "#dc2626" }}>*</span>
          </label>
          <input
            id="assignee"
            name="assignee"
            type="text"
            maxLength={200}
            value={assignee}
            onChange={(e) => setAssignee(e.target.value)}
            style={{ width: "100%", padding: "0.5rem", borderRadius: 4, border: fieldErrors.assignee ? "1px solid #dc2626" : "1px solid #d1d5db" }}
          />
          {fieldErrors.assignee && (
            <span style={{ color: "#dc2626", fontSize: "0.875rem", display: "block", marginTop: 4 }}>
              {fieldErrors.assignee}
            </span>
          )}
        </div>

        <div style={{ marginBottom: "1rem" }}>
          <label htmlFor="category" style={{ display: "block", fontWeight: 600, marginBottom: 4 }}>
            Category <span style={{ color: "#dc2626" }}>*</span>
          </label>
          <input
            id="category"
            name="category"
            type="text"
            maxLength={100}
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            style={{ width: "100%", padding: "0.5rem", borderRadius: 4, border: fieldErrors.category ? "1px solid #dc2626" : "1px solid #d1d5db" }}
          />
          {fieldErrors.category && (
            <span style={{ color: "#dc2626", fontSize: "0.875rem", display: "block", marginTop: 4 }}>
              {fieldErrors.category}
            </span>
          )}
        </div>

        <div style={{ marginBottom: "1.5rem" }}>
          <label htmlFor="resolutionNotes" style={{ display: "block", fontWeight: 600, marginBottom: 4 }}>
            Resolution Notes (Optional)
          </label>
          <textarea
            id="resolutionNotes"
            name="resolutionNotes"
            rows={3}
            maxLength={5000}
            value={resolutionNotes}
            onChange={(e) => setResolutionNotes(e.target.value)}
            style={{ width: "100%", padding: "0.5rem", borderRadius: 4, border: fieldErrors.resolutionNotes ? "1px solid #dc2626" : "1px solid #d1d5db" }}
          />
          {fieldErrors.resolutionNotes && (
            <span style={{ color: "#dc2626", fontSize: "0.875rem", display: "block", marginTop: 4 }}>
              {fieldErrors.resolutionNotes}
            </span>
          )}
        </div>

        <button
          type="submit"
          disabled={isSubmitting}
          style={{
            backgroundColor: "#2563eb",
            color: "white",
            padding: "0.6rem 1.25rem",
            border: "none",
            borderRadius: 4,
            fontWeight: 600,
            cursor: isSubmitting ? "not-allowed" : "pointer",
          }}
        >
          {isSubmitting ? "Creating..." : "Create Ticket"}
        </button>
      </form>
    </div>
  );
}
