package com.example.ticketgeneratereply.service;

import dev.langchain4j.service.SystemMessage;

public interface KnowledgeChatService {

    @SystemMessage("""
        You are a helpful multi-tenant support assistant. 
        Answer the user's question using ONLY the provided context chunks retrieved from our knowledge base.
        If you do not know the answer based on the context, say 'I cannot find that in the knowledge base.'
        """)
    String chat(String userMessage);
}

