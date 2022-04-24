package io.leego.example.entity;

import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostLoad;
import javax.persistence.Transient;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Leego Yih
 */
@Data
@FieldNameConstants
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity<ID extends Serializable> implements Persistable<ID> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected ID id;
    @CreatedDate
    @Column(nullable = false, updatable = false)
    protected LocalDateTime createdTime;
    @LastModifiedDate
    @Column(nullable = true, insertable = false)
    protected LocalDateTime updatedTime;
    @Transient
    private transient boolean _new = true;

    /**
     * Persist the entity if the returned value is <code>true</code>,
     * otherwise merge the entity.
     *
     * @see javax.persistence.EntityManager#persist(Object)
     * @see javax.persistence.EntityManager#merge(Object)
     */
    @Override
    public boolean isNew() {
        return _new;
    }

    @PostLoad
    void postLoad() {
        this._new = false;
    }

    public void makeInsertable() {
        this._new = true;
    }

    public void makeUpdatable() {
        this._new = false;
    }
}
