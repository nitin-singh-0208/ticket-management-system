# Data model

Phase 1 stores tickets and comments. Field tables, limits, the seed rows, and the 25-pair status matrix are in [001-support-ticket-management/data-model.md](001-support-ticket-management/data-model.md). Allocation and search rules are in [001-support-ticket-management/research.md](001-support-ticket-management/research.md).

## Ticket

One support item. The API addresses it by `ticketId` (`TKT-1001`). The surrogate key is not in any request or response.

- Public id is `TKT-` plus a number from `ticket_number_seq`, which starts at 1001. Numbers are not reused and continue after a restart.
- Required: title, description, priority (`LOW`, `MEDIUM`, `HIGH`), assignee, category.
- Optional: resolution notes. They are not required to resolve or close.
- Category and `ticketId` are fixed at creation. Title, description, priority, assignee, and resolution notes stay editable in every status, including `CLOSED` and `CANCELLED`.
- Status starts at `OPEN` and changes only through the status action. See [state-machine.md](state-machine.md).
- `createdAt` is set once. The list is newest first. A tie breaks on the higher human-id number. Edits, comments, and status changes do not move the row.

Tickets are not deleted. One ticket has many comments.

## Comment

Text appended to one ticket, with `createdAt`. Comments are not edited or removed. They are shown oldest first, with `id` breaking ties. A comment may be added in every ticket status and does not change status or list position.

## Search and list

`q` is a case-insensitive contiguous substring of title or description only. Comments, resolution notes, category, assignee, and the human id are not search fields. When `page` and `size` are both omitted, every match is returned. `page` without `size` is 400.

The running app seeds fifteen tickets (TKT-1001 through TKT-1015). The next created id is TKT-1016. Themes: payment failures, shipment tracking, and login issues.
