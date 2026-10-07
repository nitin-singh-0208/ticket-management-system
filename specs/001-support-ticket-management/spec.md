# Feature Specification: Support Ticket Management

**Feature Branch**: `001-support-ticket-management`

**Created**: 2026-10-07

**Status**: Draft

**Input**: User description: "Support Ticket Management covering the [Phase 1: App] items in specs/requirements.md. Users can create, list, and view tickets; update title, description, priority, and assignee; add comments; search by keyword (title + description); and filter by status. Tickets have a human ID like TKT-1001, a category, and resolution notes. Status changes follow OPEN→IN_PROGRESS→RESOLVED→CLOSED and OPEN/IN_PROGRESS→CANCELLED; invalid transitions are rejected. Data persists across restarts. The backend validates input; the UI shows meaningful field-level and server errors and offers only valid next statuses. Out of scope: auth and the AI assistant."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create and view a ticket (Priority: P1)

A support agent records a new problem and can open it again to see everything that was saved.

**Why this priority**: Without a saved ticket there is nothing to update, search, or move through the workflow.

**Independent Test**: Create one ticket with the required details and open it. The screen shows the same details and a human ID such as TKT-1001.

**Acceptance Scenarios**:

1. **Given** the agent is on the create screen, **When** they enter a title, description, priority, assignee, and category and save, **Then** the system assigns a unique human ID in the form TKT-1001, sets the status to OPEN, and shows the new ticket (FR-01, AC-01).
2. **Given** a ticket exists, **When** the agent opens it, **Then** they see its human ID, title, description, priority, assignee, category, status, resolution notes, and comments (FR-03, AC-03).
3. **Given** the agent is creating a ticket, **When** they leave resolution notes blank, **Then** the ticket is still created and the notes are empty (FR-01, AC-01).

---

### User Story 2 - Move a ticket through its allowed statuses (Priority: P1)

An agent advances a ticket only along the allowed path, and the screen never offers a status the workflow does not allow.

**Why this priority**: A ticket that can skip or rewind statuses no longer tells the team what state the work is in.

**Independent Test**: Create a ticket and follow each allowed next status. Then request a status that is not allowed and confirm the ticket does not change.

**Acceptance Scenarios**:

1. **Given** a ticket is OPEN, **When** the agent marks it IN_PROGRESS, **Then** the status becomes IN_PROGRESS (SM-01, AC-09).
2. **Given** a ticket is IN_PROGRESS, **When** the agent marks it RESOLVED, **Then** the status becomes RESOLVED (SM-01, AC-09).
3. **Given** a ticket is RESOLVED, **When** the agent marks it CLOSED, **Then** the status becomes CLOSED (SM-01, AC-09).
4. **Given** a ticket is OPEN, **When** the agent marks it CANCELLED, **Then** the status becomes CANCELLED (SM-02, AC-09).
5. **Given** a ticket is IN_PROGRESS, **When** the agent marks it CANCELLED, **Then** the status becomes CANCELLED (SM-03, AC-09).
6. **Given** a ticket is CLOSED, **When** a change to OPEN is requested, **Then** the system refuses the change and the status stays CLOSED (SM-04, AC-10).
7. **Given** a ticket is RESOLVED, **When** a change to OPEN is requested, **Then** the system refuses the change and the status stays RESOLVED (SM-04, AC-10).
8. **Given** a ticket is CANCELLED, **When** a change to OPEN is requested, **Then** the system refuses the change and the status stays CANCELLED (SM-04, AC-10).
9. **Given** a ticket is in any status, **When** a status is requested that is not an allowed next status, **Then** the system refuses that change, the current status is unchanged, and this outcome is defined for every such pair (SM-04, AC-10, AC-14).
10. **Given** a ticket is OPEN, **When** the agent looks at status choices, **Then** only IN_PROGRESS and CANCELLED are offered (SM-01, SM-02, AC-09).
11. **Given** a ticket is IN_PROGRESS, **When** the agent looks at status choices, **Then** only RESOLVED and CANCELLED are offered (SM-01, SM-03, AC-09).
12. **Given** a ticket is RESOLVED, **When** the agent looks at status choices, **Then** only CLOSED is offered (SM-01, AC-09).
13. **Given** a ticket is CLOSED or CANCELLED, **When** the agent looks at status choices, **Then** no next status is offered (SM-04, AC-10).

---

### User Story 3 - Find tickets (Priority: P2)

An agent can scan the queue, narrow it to one status, and find tickets whose title or description contains a word or phrase.

**Why this priority**: Once more than a few tickets exist, opening them one by one is not usable.

**Independent Test**: Create tickets with different titles, descriptions, and statuses. List them, filter by one status, and search for a word that appears only in a title or only in a description.

**Acceptance Scenarios**:

1. **Given** several tickets exist, **When** the agent opens the list, **Then** those tickets are shown (FR-02, AC-02).
2. **Given** tickets exist, **When** the agent searches for a keyword, **Then** the list includes a ticket whose title or description contains that keyword and excludes tickets where neither field contains it (FR-06, AC-07).
3. **Given** tickets in more than one status, **When** the agent filters by one status, **Then** only tickets in that status are shown (FR-07, AC-08).
4. **Given** a keyword and a status are both set, **When** the agent applies them together, **Then** a ticket appears only when it matches the status and the keyword is in its title or description (FR-06, FR-07, AC-07, AC-08).
5. **Given** the keyword appears only in a comment, resolution notes, category, or assignee, **When** the agent searches, **Then** that ticket is not returned on the strength of those fields (FR-06, AC-07).

---

### User Story 4 - Correct ticket details (Priority: P2)

An agent can fix the title, description, priority, assignee, and resolution notes after the ticket exists.

**Why this priority**: The first write is often incomplete. The team still needs the human ID, category, and status rules to stay stable.

**Independent Test**: Open an existing ticket, change each editable field, save, and reopen it. The human ID, category, and status are unchanged unless status was changed through the status action.

**Acceptance Scenarios**:

1. **Given** an existing ticket, **When** the agent changes the title, description, priority, or assignee and saves, **Then** the ticket shows the new values (FR-04, AC-04, AC-05).
2. **Given** an existing ticket, **When** the agent adds or replaces resolution notes and saves, **Then** the ticket shows the new notes and the status does not change (AC-04).
3. **Given** an existing ticket, **When** the agent saves other edits, **Then** the human ID and category stay as they were at creation (FR-03, FR-04).

---

### User Story 5 - Add a comment (Priority: P3)

An agent appends a note to the ticket so later readers can see what happened.

**Why this priority**: Comments add history, but the ticket is still useful before the first comment.

**Independent Test**: Open a ticket, add a comment, leave, and open the ticket again. The comment is still there, in order with any earlier comments.

**Acceptance Scenarios**:

1. **Given** an existing ticket, **When** the agent adds a comment with text, **Then** the comment appears on that ticket with the time it was added (FR-05, AC-06).
2. **Given** a ticket already has comments, **When** the agent adds another, **Then** both are shown in the order they were added (FR-05, AC-06).

---

### User Story 6 - See mistakes and keep saved work (Priority: P2)

An agent sees which field is wrong, sees a clear message when a status change is refused, and finds the same tickets after the application starts again.

**Why this priority**: Silent failures and lost tickets make the queue untrustworthy.

**Independent Test**: Submit a ticket with a required field blank, request a disallowed status, and restart the application. Field messages name the problem, the refused ticket is unchanged, and earlier tickets are still present.

**Acceptance Scenarios**:

1. **Given** the agent is creating or editing a ticket, **When** a required field is blank, **Then** the system does not save that submission and the screen names each invalid field (FR-09, FR-10, AC-12, AC-13).
2. **Given** a status change is not allowed, **When** it is submitted anyway, **Then** the system refuses it, the status stays the same, and the screen explains that the change was not allowed (FR-09, FR-10, SM-04, AC-10, AC-12, AC-13).
3. **Given** tickets, edits, comments, and status changes have been saved, **When** the application is stopped and started again, **Then** that same information is still present (FR-08, AC-11).

---

### Edge Cases

- A keyword matches the description and not the title. The ticket is included (FR-06, AC-07).
- A keyword matches the title and not the description. The ticket is included (FR-06, AC-07).
- No ticket matches the keyword or the status filter. The list is empty and the screen says that nothing matched, rather than showing an unrelated ticket.
- The keyword differs only by letter case, or has leading or trailing spaces. Matching ignores case and surrounding spaces.
- Two tickets share a title. Each receives its own human ID (FR-01, AC-01).
- OPEN cannot move directly to RESOLVED or CLOSED. IN_PROGRESS cannot move directly to CLOSED or back to OPEN. RESOLVED cannot move to CANCELLED. Those requests are refused (SM-04, AC-10, AC-14).
- A blank comment is not added, and the screen explains that the comment needs text (FR-09, FR-10, AC-12, AC-13).
- A search does not use comments or resolution notes as match fields (FR-06, AC-07).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST let an agent create a ticket with title, description, priority, assignee, and category, and MAY include resolution notes (source FR-01, AC-01).
- **FR-002**: The system MUST assign each ticket a unique human ID in the form shown by TKT-1001 and MUST set the initial status to OPEN (source FR-01, AC-01).
- **FR-003**: The system MUST let an agent open one ticket and see its human ID, title, description, priority, assignee, category, status, resolution notes, and comments (source FR-03, AC-03).
- **FR-004**: The system MUST let an agent list existing tickets (source FR-02, AC-02).
- **FR-005**: The system MUST let an agent search by a keyword matched against title and description only (source FR-06, AC-07).
- **FR-006**: The system MUST let an agent filter the list to one status, including together with a keyword (source FR-07, AC-08).
- **FR-007**: The system MUST let an agent update title, description, priority, and assignee (source FR-04, AC-04, AC-05).
- **FR-008**: The system MUST let an agent add or replace resolution notes without changing status, human ID, or category (source AC-04).
- **FR-009**: The system MUST let an agent add a non-empty comment to a ticket and MUST keep comments in the order they were added (source FR-05, AC-06).
- **FR-010**: The system MUST allow only these status changes: OPEN to IN_PROGRESS, IN_PROGRESS to RESOLVED, RESOLVED to CLOSED, OPEN to CANCELLED, and IN_PROGRESS to CANCELLED (source SM-01, SM-02, SM-03, AC-09).
- **FR-011**: The system MUST refuse every other status change, leave the current status unchanged, and define that result for each disallowed pair (source SM-04, AC-10, AC-14).
- **FR-012**: The screen MUST offer only the allowed next statuses for the ticket's current status, and MUST offer none when the status is CLOSED or CANCELLED (source SM-01, SM-02, SM-03, SM-04, AC-09, AC-10).
- **FR-013**: The system MUST check required input before saving a new ticket, an edit, or a comment (source FR-09, AC-12).
- **FR-014**: The screen MUST name each invalid field and MUST show a clear explanation when a status change is refused (source FR-10, AC-13).
- **FR-015**: The system MUST keep tickets, edits, comments, and statuses after the application stops and starts again (source FR-08, AC-11).

### Key Entities *(include if feature involves data)*

- **Ticket**: One support item. It has a system-assigned human ID, title, description, priority, assignee, category, resolution notes, status, and the time it was created. Category and human ID stay as assigned at creation.
- **Comment**: Text added to one ticket, kept with the time it was added. Comments are not edited or removed in this feature.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: An agent can create a ticket and see its human ID on the ticket details in under 2 minutes.
- **SC-002**: All 5 allowed status changes succeed and leave the ticket in the requested status.
- **SC-003**: Every status change outside those 5 is refused, and in each case the ticket keeps its previous status.
- **SC-004**: For any ticket, the status choices on screen are exactly the allowed next statuses, or none when no next status exists.
- **SC-005**: A keyword search returns a ticket only when the keyword appears in the title or the description, including when it appears in just one of those fields.
- **SC-006**: After a restart, 100% of previously saved tickets, comments, field edits, and statuses are still available.
- **SC-007**: When required information is missing, the agent sees every invalid field named on screen and the submission is not saved.
- **SC-008**: On a first attempt with complete, valid details, an agent can create a ticket, find it by keyword, and open it without help.

## Assumptions

- Anyone who can open the application may create and update tickets. Sign-in, roles, and the assistant that answers questions from ticket history are out of scope.
- Priority is one of Low, Medium, or High. The agent must choose one. There is no separate urgent level.
- Category is required text chosen at creation, not a fixed list, and it cannot be changed later.
- Resolution notes are optional text. The agent may fill them in at creation or later. They are not required to resolve or close a ticket.
- Assignee is a name entered as text. It is not linked to a user account.
- The agent does not choose the human ID. Identifiers look like TKT-1001: the prefix TKT- and a number. They are unique and do not change.
- A new ticket always starts as OPEN. Status changes only through the status action.
- Keyword search is a case-insensitive phrase match on title or description. Comments, resolution notes, category, assignee, and the human ID are not search fields.
- A blank search shows the list for the selected status, or every ticket when no status is selected.
- Title, description, priority, assignee, and category are required. A value that is only spaces counts as blank. Title and assignee may be up to 200 characters. Category may be up to 100 characters. Description, resolution notes, and a comment may be up to 5,000 characters.
- Comments are append-only.
- The list is ordered with the newest ticket first and shows human ID, title, status, priority, and assignee.
- No secrets are stored in the project (source AC-22).
- The natural-language assistant, embeddings, and citation of tickets in generated answers are out of scope for this feature (source FR-11, FR-12, FR-13 are Phase 2).
