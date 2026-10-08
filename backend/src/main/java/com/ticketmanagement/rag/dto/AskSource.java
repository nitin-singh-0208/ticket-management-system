package com.ticketmanagement.rag.dto;

public record AskSource(String ticketId, String title, String status, double score, String snippet) {
}
