package io.leego.example.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

/**
 * @author Leego Yih
 */
@Configuration
@EnableMongoAuditing
public class SoftDeleteConfiguration {

    /** 持久化数据时，不包含<code>_class</code>字段。 */
    @EventListener(ContextRefreshedEvent.class)
    public void initMongoAfterStartup(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        MappingMongoConverter mappingMongoConverter = applicationContext.getBean(MappingMongoConverter.class);
        mappingMongoConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
    }

}