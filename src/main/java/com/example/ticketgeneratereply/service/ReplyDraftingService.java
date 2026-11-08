package com.example.ticketgeneratereply.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ReplyDraftingService {
    @SystemMessage("""
        You are an elite customer support agent. Draft a professional response based *only* on the provided context. 
        If the context does not contain the answer, draft a polite message stating a human agent is reviewing the case.
        Context: {{context}}
        """)
    @UserMessage("Customer Message: {{customerMessage}}")
    String draftReply(@V("context") String context, @V("customerMessage") String customerMessage);
}
