export type TicketPriority = "LOW" | "MEDIUM" | "HIGH";

export type TicketStatus =
  | "OPEN"
  | "IN_PROGRESS"
  | "RESOLVED"
  | "CLOSED"
  | "CANCELLED";

export interface CommentResponse {
  id: number;
  text: string;
  createdAt: string;
}

export interface TicketDetail {
  ticketId: string;
  title: string;
  description: string;
  priority: TicketPriority;
  assignee: string;
  category: string;
  resolutionNotes: string | null;
  status: TicketStatus;
  createdAt: string;
  comments: CommentResponse[];
  allowedNextStatuses?: TicketStatus[];
}

export interface CreateTicketRequest {
  title: string;
  description: string;
  priority: TicketPriority;
  assignee: string;
  category: string;
  resolutionNotes?: string;
}

export interface UpdateTicketRequest {
  title?: string;
  description?: string;
  priority?: TicketPriority;
  assignee?: string;
  resolutionNotes?: string;
}

export interface CreateCommentRequest {
  text: string;
}

export interface FieldError {
  field: string;
  message: string;
}

export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  errors?: FieldError[];
}

export class ApiError extends Error {
  constructor(
    public status: number,
    public problem: ProblemDetail
  ) {
    super(problem.detail || problem.title || `API error ${status}`);
    this.name = "ApiError";
  }
}

const API_BASE = "http://localhost:8080";

export async function createTicket(
  request: CreateTicketRequest
): Promise<{ ticket: TicketDetail; location: string | null }> {
  const response = await fetch(`${API_BASE}/api/tickets`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(request),
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

  const location = response.headers.get("Location");
  const ticket: TicketDetail = await response.json();
  return { ticket, location };
}

export async function updateTicket(
  ticketId: string,
  request: UpdateTicketRequest
): Promise<TicketDetail> {
  const response = await fetch(
    `${API_BASE}/api/tickets/${encodeURIComponent(ticketId)}`,
    {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    }
  );

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

export async function changeStatus(
  ticketId: string,
  status: TicketStatus
): Promise<TicketDetail> {
  const response = await fetch(
    `${API_BASE}/api/tickets/${encodeURIComponent(ticketId)}/status`,
    {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ status }),
    }
  );

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

export async function addComment(
  ticketId: string,
  request: CreateCommentRequest
): Promise<CommentResponse> {
  const response = await fetch(
    `${API_BASE}/api/tickets/${encodeURIComponent(ticketId)}/comments`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    }
  );

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

export async function getTicket(ticketId: string): Promise<TicketDetail> {
  const response = await fetch(`${API_BASE}/api/tickets/${encodeURIComponent(ticketId)}`);

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
