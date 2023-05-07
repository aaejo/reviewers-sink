package io.github.aaejo.reviewerssink.messaging.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.adapter.ConsumerRecordMetadata;
import org.springframework.stereotype.Component;

import io.github.aaejo.messaging.records.Reviewer;
import io.github.aaejo.reviewerssink.ReviewerDatabaseAddition;

/**
 * @author Omri Harary
 * @author Jeffery Kung
 */
@Component
@KafkaListener(id = "reviewers-sink", topics = "reviewers-data")
public class ReviewersDataListener {
    private static final Logger log = LoggerFactory.getLogger(ReviewersDataListener.class);

    private final ReviewerDatabaseAddition reviewerDatabaseAddition;

    public ReviewersDataListener(ReviewerDatabaseAddition reviewerDatabaseAddition) {
        this.reviewerDatabaseAddition = reviewerDatabaseAddition;
    }

    @KafkaHandler
    public void handle(Reviewer reviewer, ConsumerRecordMetadata metadata) {
        log.info("Received reviewer #{} from {}", metadata.offset(), reviewer.institution().name());
        log.debug(reviewer.toString());
        reviewerDatabaseAddition.parseValues(reviewer);
    }
}
