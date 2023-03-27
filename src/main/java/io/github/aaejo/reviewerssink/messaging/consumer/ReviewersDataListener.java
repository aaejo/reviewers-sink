package io.github.aaejo.reviewerssink.messaging.consumer;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import io.github.aaejo.messaging.records.Reviewer;
import io.github.aaejo.reviewerssink.ReviewerDatabaseAddition;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@KafkaListener(id = "reviewers-sink", topics = "reviewers-data")
public class ReviewersDataListener {

    private final ReviewerDatabaseAddition reviewerDatabaseAddition;

    public ReviewersDataListener(ReviewerDatabaseAddition reviewerDatabaseAddition) {
        this.reviewerDatabaseAddition = reviewerDatabaseAddition;
    }

    @KafkaHandler
    public void handle(Reviewer reviewer) {
        reviewerDatabaseAddition.parseValues(reviewer);
        log.info(reviewer.toString());
        
    }
}
