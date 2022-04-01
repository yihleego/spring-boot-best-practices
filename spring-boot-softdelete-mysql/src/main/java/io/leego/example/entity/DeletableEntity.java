package io.leego.example.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Leego Yih
 */
@Data
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
public abstract class DeletableEntity<ID extends Serializable> extends BaseEntity<ID> {
    @Column(nullable = false, insertable = false)
    protected boolean deleted;
    @Column(nullable = true, insertable = false)
    protected LocalDateTime deletedTime;
}
