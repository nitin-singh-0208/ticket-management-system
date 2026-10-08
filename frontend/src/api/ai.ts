import { ApiError, ProblemDetail } from "./tickets";

export interface AskRequest {
  question: string;
}

export interface AskSource {
  ticketId: string;
  title: string;
  status: string;
  score: number;
  snippet: string;
}

export interface AskResponse {
  answer: string;
  sources: AskSource[];
  grounded: boolean;
}

const API_BASE = "http://localhost:8080";

export async function askQuestion(question: string): Promise<AskResponse> {
  const body: AskRequest = { question };
  const response = await fetch(`${API_BASE}/api/ai/ask`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });

  if (!response.ok) {
    let problem: ProblemDetail;
    try {
      problem = await response.json();
    } catch {
      problem = {
        title: response.statusText,
        status: response.status,
        detail: "Failed to parse error response",
      };
    }
    throw new ApiError(response.status, problem);
  }

  return response.json();
}
