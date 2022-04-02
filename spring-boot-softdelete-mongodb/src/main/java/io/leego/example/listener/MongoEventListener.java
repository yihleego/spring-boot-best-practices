package io.leego.example.listener;

import io.leego.example.entity.Garbage;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeDeleteEvent;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Leego Yih
 */
@Component
public class MongoEventListener extends AbstractMongoEventListener<Object> {
    private final MongoTemplate mongoTemplate;

    public MongoEventListener(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void onBeforeDelete(BeforeDeleteEvent<Object> event) {
        if (event.getType() == null || CollectionUtils.isEmpty(event.getDocument())) {
            return;
        }
        List<Object> objects = mongoTemplate.find(new BasicQuery(event.getDocument()), event.getType());
        if (!CollectionUtils.isEmpty(objects)) {
            String type = event.getCollectionName();
            LocalDateTime now = LocalDateTime.now();
            List<Garbage> garbages = objects.stream()
                    .map(o -> new Garbage(null, type, o, now))
                    .collect(Collectors.toList());
            mongoTemplate.insertAll(garbages);
        }
    }
}
