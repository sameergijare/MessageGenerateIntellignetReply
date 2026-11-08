package com.example.ticketgeneratereply.config;


import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;


@Configuration
public class LangChain4jConfig {
    @Bean
    public StreamingChatLanguageModel embeddingChatModel() {
        StreamingChatLanguageModel streamingModel = OllamaStreamingChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("qwen2.5:0.5b")
                .numCtx(2048)
                .numPredict(150)
                .timeout(Duration.ofMinutes(15))
                .build();
        //.numCtx(2048)       // Saves ~1 GB of RAM compared to 4096
        //.numPredict(512)
        return streamingModel;
    }

    /**
     * Instantiates an in-process local vector evaluation engine.
     * Required by AITicketingService to vectorise ticket data.
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2QuantizedEmbeddingModel(); // Runs entirely within your local JVM
    }

}
