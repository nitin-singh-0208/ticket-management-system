# Review RAG output

The request must include:

- The user question
- The retrieved chunks, each with its ticket ID
- The assistant answer

Check the answer against the retrieved context:

- Every factual claim is supported by at least one retrieved chunk
- Every cited ticket ID appears in the retrieved chunks
- No ticket IDs are invented or cited that were not retrieved
- No general-knowledge or outside content fills gaps the chunks do not cover
- When retrieval is empty, the answer is exactly an explicit "No relevant tickets found" with no LLM-generated content

Decompose the answer into individual claims. For each claim, identify the supporting ticket ID and chunk, or mark it unsupported.

Report as a table:

| Claim | Supporting ticket/chunk | Verdict |
|-------|-------------------------|---------|
| ...   | ...                     | Supported / Unsupported |

Only report findings you can point to in the answer and chunks. Do not speculate.

Do not modify any files. Report only.

End with an overall verdict and reasons:

- **GROUNDED** — every claim supported; cited IDs match retrieved IDs; no outside content
- **PARTIALLY_GROUNDED** — some claims supported but others lack chunk support, cite missing IDs, or add outside content
- **HALLUCINATED** — majority unsupported, invented IDs, or general-knowledge filler despite retrieved context
