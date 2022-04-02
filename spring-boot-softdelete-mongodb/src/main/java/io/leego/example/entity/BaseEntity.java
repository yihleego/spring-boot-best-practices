package io.leego.example.entity;

import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

/**
 * @author Leego Yih
 */
@Data
@FieldNameConstants
public abstract class BaseEntity {
    @MongoId(FieldType.OBJECT_ID)
    protected String id;
    @CreatedDate
    protected LocalDateTime createdTime;
    @LastModifiedDate
    protected LocalDateTime updatedTime;
}
