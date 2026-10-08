package com.ticketmanagement.rag.dto;

import java.util.List;

public record AskResponse(String answer, List<AskSource> sources, boolean grounded) {
}
