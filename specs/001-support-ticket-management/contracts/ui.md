# UI contract: Support Ticket Management

The screen talks to the API only through `frontend/src/api/tickets.ts`. That module exports typed functions for each operation in [tickets.openapi.yaml](tickets.openapi.yaml). No page controls are rendered.

## Routes

| Route | Purpose |
|-------|---------|
| `/tickets` | List, keyword box, status filter. |
| `/tickets/new` | Create form. |
| `/tickets/:ticketId` | Detail, field edit, status action, comments. |

Priority values `LOW`, `MEDIUM`, and `HIGH` are shown as Low, Medium, and High. Status values are shown as stored (`OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`).

## List

- Calls `GET /api/tickets` with `q` and `status` only. Omits `page` and `size`.
- Renders every item in `content`, in the order received: human id, title, status, priority, assignee.
- An empty `content` array shows "Nothing matched" and no ticket rows.
- A blank keyword sends no `q`. A blank status filter sends no `status`.

## Create

- Required: title, description, priority, assignee, category. Resolution notes are optional.
- On 201, follow `Location` or `ticketId` to `/tickets/:ticketId`.
- On 400, show each `errors[].message` next to `errors[].field`. Do not navigate away. Nothing was saved.

## Detail and edit

- Load `GET /api/tickets/{ticketId}`.
- Show ticket id, title, description, priority, assignee, category, status, resolution notes, created time, and comments oldest first.
- The edit form can change title, description, priority, assignee, and resolution notes. It does not send `status` or `category`. Save with `PATCH /api/tickets/{ticketId}`.
- The status control lists only `allowedNextStatuses`. `CLOSED` and `CANCELLED` show no next status. Submit with `PATCH /api/tickets/{ticketId}/status`.
- On 409, show `detail` and keep the status that was loaded. Do not offer a status that was refused as if it had succeeded.
- On 404, show `detail`.
- On 400, show each field error and keep the unsaved form values.

## Comments

- A non-empty comment posts to `POST /api/tickets/{ticketId}/comments`.
- A blank comment is stopped with a field message and is not posted.
- After 201, the new comment appears after the older ones. Status and list position stay as they were.
- The comment box stays available when the ticket is `CLOSED` or `CANCELLED`.
