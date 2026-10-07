insert into tickets (ticket_number, ticket_id, title, description, priority, assignee, category, resolution_notes, status, created_at)
values
    (1001, 'TKT-1001', 'Payment declined at checkout', 'Card network returned a payment failure during checkout.', 'HIGH', 'Alex Kim', 'Payment', null, 'OPEN', '2026-01-15T12:00:00Z'),
    (1002, 'TKT-1002', 'Customer charged twice for one order', 'A second capture was taken for the same order.', 'HIGH', 'Alex Kim', 'Payment', null, 'IN_PROGRESS', '2026-01-15T12:01:00Z'),
    (1003, 'TKT-1003', 'Refund still missing after five days', 'The refund has not appeared after five days.', 'MEDIUM', 'Alex Kim', 'Payment', 'The refund posted.', 'RESOLVED', '2026-01-15T12:02:00Z'),
    (1004, 'TKT-1004', 'Invoice shows the wrong currency', 'The invoice used USD instead of EUR.', 'LOW', 'Alex Kim', 'Payment', 'The invoice was reissued.', 'CLOSED', '2026-01-15T12:03:00Z'),
    (1005, 'TKT-1005', 'Duplicate payment report, already handled', 'Caller confirmed this report is a duplicate.', 'MEDIUM', 'Alex Kim', 'Payment', null, 'CANCELLED', '2026-01-15T12:04:00Z'),
    (1006, 'TKT-1006', 'Shipment has not moved in four days', 'No carrier scan since handover.', 'HIGH', 'Alex Kim', 'Shipment', null, 'OPEN', '2026-01-15T12:05:00Z'),
    (1007, 'TKT-1007', 'Tracking number does not update', 'The tracking page has shown the same scan for two days.', 'MEDIUM', 'Alex Kim', 'Shipment', null, 'IN_PROGRESS', '2026-01-15T12:06:00Z'),
    (1008, 'TKT-1008', 'Parcel delivered to the wrong address', 'The parcel was left at a neighboring building.', 'HIGH', 'Alex Kim', 'Shipment', 'A replacement was sent.', 'RESOLVED', '2026-01-15T12:07:00Z'),
    (1009, 'TKT-1009', 'Delivery scan missing after handoff', 'The handoff scan was never recorded.', 'LOW', 'Alex Kim', 'Shipment', 'The scan was corrected.', 'CLOSED', '2026-01-15T12:08:00Z'),
    (1010, 'TKT-1010', 'Shipment cancelled by the customer', 'The customer asked to stop this shipment.', 'LOW', 'Alex Kim', 'Shipment', null, 'CANCELLED', '2026-01-15T12:09:00Z'),
    (1011, 'TKT-1011', 'Cannot sign in after a password reset', 'The reset mail arrived but sign-in still fails.', 'HIGH', 'Alex Kim', 'Login', null, 'OPEN', '2026-01-15T12:10:00Z'),
    (1012, 'TKT-1012', 'MFA code is rejected', 'The one-time code is rejected as expired.', 'MEDIUM', 'Alex Kim', 'Login', null, 'IN_PROGRESS', '2026-01-15T12:11:00Z'),
    (1013, 'TKT-1013', 'Account locked after failed attempts', 'Too many failed sign-in attempts locked the account.', 'HIGH', 'Alex Kim', 'Login', 'The lock was cleared.', 'RESOLVED', '2026-01-15T12:12:00Z'),
    (1014, 'TKT-1014', 'SSO redirect loop on the login page', 'The login page repeats the same redirect.', 'MEDIUM', 'Alex Kim', 'Login', 'The redirect URL was corrected.', 'CLOSED', '2026-01-15T12:13:00Z'),
    (1015, 'TKT-1015', 'Second report of the password-reset failure', 'Same reset problem already tracked on TKT-1011.', 'LOW', 'Alex Kim', 'Login', null, 'CANCELLED', '2026-01-15T12:14:00Z');

insert into comments (ticket_id, text, created_at)
values
    ((select id from tickets where ticket_id = 'TKT-1001'), 'Noted.', '2026-01-15T12:00:00Z'),
    ((select id from tickets where ticket_id = 'TKT-1002'), 'The duplicate capture is being traced.', '2026-01-15T12:01:00Z'),
    ((select id from tickets where ticket_id = 'TKT-1003'), 'Noted.', '2026-01-15T12:02:00Z'),
    ((select id from tickets where ticket_id = 'TKT-1006'), 'The carrier scan gap is still open.', '2026-01-15T12:05:00Z'),
    ((select id from tickets where ticket_id = 'TKT-1007'), 'The carrier was contacted.', '2026-01-15T12:06:00Z'),
    ((select id from tickets where ticket_id = 'TKT-1011'), 'The reset mail arrived.', '2026-01-15T12:10:00Z'),
    ((select id from tickets where ticket_id = 'TKT-1012'), 'A new code was requested.', '2026-01-15T12:11:00Z'),
    ((select id from tickets where ticket_id = 'TKT-1015'), 'Same reset problem already tracked on TKT-1011.', '2026-01-15T12:14:00Z');

select setval('ticket_number_seq', 1015);
