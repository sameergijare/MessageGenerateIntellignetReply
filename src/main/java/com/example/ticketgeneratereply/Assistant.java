package com.example.ticketgeneratereply;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface Assistant {

    @SystemMessage("You are a helpful assistant that drafts clear, accurate replies using the provided context.")
    @UserMessage("""
            Use the following Phase 2 context to draft a reply to the incoming message.
            
            ### Context (Phase 2):
            {{aggregatedContent}}
            
            ### Incoming Message:
            {{incomingMessage}}
            
            ### Draft Reply:
            """)
    TokenStream draftReply(
            @V("aggregatedContent") String aggregatedContent,
            @V("incomingMessage") String incomingMessage
    );
}
