package dev.gautam.matcher.config;

import dev.gautam.matcher.mcp.MatcherTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider matcherToolCallbacks(MatcherTools matcherTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(matcherTools)
                .build();
    }
}
