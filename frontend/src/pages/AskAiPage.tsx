import { FormEvent, useState } from "react";
import { Link } from "react-router";
import { askQuestion, AskResponse } from "../api/ai";
import { ApiError } from "../api/tickets";

export function AskAiPage() {
  const [question, setQuestion] = useState("");
  const [validationMessage, setValidationMessage] = useState<string | null>(null);
  const [errorDetail, setErrorDetail] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [result, setResult] = useState<AskResponse | null>(null);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    const trimmed = question.trim();
    if (!trimmed) {
      setValidationMessage("Enter a question.");
      setErrorDetail(null);
      setResult(null);
      return;
    }

    setValidationMessage(null);
    setErrorDetail(null);
    setIsSubmitting(true);
    try {
      const response = await askQuestion(trimmed);
      setResult(response);
    } catch (err) {
      setResult(null);
      if (err instanceof ApiError) {
        setErrorDetail(err.problem.detail || `Error: ${err.status}`);
      } else {
        setErrorDetail("Failed to ask the assistant.");
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div style={{ maxWidth: 720, margin: "2rem auto", padding: "0 1rem", fontFamily: "system-ui, -apple-system, sans-serif" }}>
      <h1 style={{ margin: "0 0 1.5rem", fontSize: "1.75rem" }}>Ask AI</h1>
      <form onSubmit={onSubmit}>
        <label htmlFor="question" style={{ display: "block", fontWeight: 600, marginBottom: 4 }}>
          Question
        </label>
        <textarea
          id="question"
          name="question"
          value={question}
          onChange={(event) => setQuestion(event.target.value)}
          rows={4}
          placeholder="Ask about past tickets"
          style={{ width: "100%", padding: "0.5rem", borderRadius: 4, border: "1px solid #d1d5db", font: "inherit" }}
        />
        {validationMessage && (
          <p role="alert" style={{ color: "#991b1b", margin: "0.5rem 0 0" }}>
            {validationMessage}
          </p>
        )}
        <button
          type="submit"
          disabled={isSubmitting}
          style={{
            marginTop: "0.75rem",
            backgroundColor: "#2563eb",
            color: "white",
            padding: "0.5rem 1rem",
            borderRadius: 4,
            border: "none",
            fontWeight: 600,
            cursor: isSubmitting ? "wait" : "pointer",
          }}
        >
          {isSubmitting ? "Asking..." : "Ask"}
        </button>
      </form>

      {errorDetail && (
        <div
          role="alert"
          style={{
            background: "#fee2e2",
            color: "#991b1b",
            padding: "0.75rem 1rem",
            borderRadius: 6,
            marginTop: "1rem",
          }}
        >
          {errorDetail}
        </div>
      )}

      {result && !result.grounded && (
        <p
          role="status"
          style={{
            marginTop: "1.5rem",
            padding: "0.75rem 1rem",
            background: "#f3f4f6",
            borderRadius: 6,
            fontWeight: 700,
          }}
        >
          {result.answer}
        </p>
      )}

      {result && result.grounded && (
        <section style={{ marginTop: "1.5rem" }}>
          <p style={{ whiteSpace: "pre-wrap", lineHeight: 1.5 }}>{result.answer}</p>
          <h2 style={{ fontSize: "1.1rem", marginBottom: "0.5rem" }}>Sources</h2>
          <ul style={{ listStyle: "none", padding: 0, margin: 0 }}>
            {result.sources.map((source) => (
              <li
                key={source.ticketId}
                style={{ borderTop: "1px solid #e5e7eb", padding: "0.75rem 0" }}
              >
                <Link
                  to={`/tickets/${source.ticketId}`}
                  style={{ color: "#2563eb", fontWeight: 600, textDecoration: "none" }}
                >
                  {source.title}
                </Link>
                <div style={{ color: "#4b5563", marginTop: 4 }}>{source.status}</div>
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  );
}
