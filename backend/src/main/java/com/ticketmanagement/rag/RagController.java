package com.ticketmanagement.rag;

import com.ticketmanagement.rag.dto.AskRequest;
import com.ticketmanagement.rag.dto.AskResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class RagController {

    private final RagAskService ragAskService;

    public RagController(RagAskService ragAskService) {
        this.ragAskService = ragAskService;
    }

    @PostMapping("/ask")
    public AskResponse ask(@Valid @RequestBody AskRequest request) {
        return ragAskService.ask(request.question());
    }
}
