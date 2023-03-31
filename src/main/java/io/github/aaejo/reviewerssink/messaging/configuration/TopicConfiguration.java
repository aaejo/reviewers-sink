package io.github.aaejo.reviewerssink.messaging.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * @author Omri Harary
 */
@Configuration
public class TopicConfiguration {

    @Bean
    public NewTopic reviewersDataTopic() {
        return TopicBuilder
                .name("reviewers-data")
                .build();
    }
}
