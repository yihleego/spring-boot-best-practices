package io.leego.example.repository;

import io.leego.example.entity.DeletableEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Leego Yih
 */
@NoRepositoryBean
public interface DeletableRepository<T extends DeletableEntity<ID>, ID extends Serializable> extends CrudRepository<T, ID> {

    @Transactional
    @Modifying
    @Query("update #{#entityName} set deleted = id, deletedTime = :#{T(java.time.LocalDateTime).now()} where id = :id and deleted = 0")
    int softdeleteById(@Param("id") ID id);

    @Transactional
    @Modifying
    @Query("update #{#entityName} set deleted = id, deletedTime = :#{T(java.time.LocalDateTime).now()} where id in :ids and deleted = 0")
    int softdeleteAllById(@Param("ids") Collection<? extends ID> ids);

    @Override
    @Transactional
    @Modifying
    @Query("update #{#entityName} set deleted = id, deletedTime = :#{T(java.time.LocalDateTime).now()} where id = :id and deleted = 0")
    void deleteById(@Param("id") ID id);

    @Override
    @Transactional
    @Modifying
    @Query("update #{#entityName} set deleted = id, deletedTime = :#{T(java.time.LocalDateTime).now()} where id in :ids and deleted = 0")
    void deleteAllById(@Param("ids") Iterable<? extends ID> ids);

    @Override
    @Transactional
    @Modifying
    @Query("update #{#entityName} set deleted = id, deletedTime = :#{T(java.time.LocalDateTime).now()} where deleted = 0")
    void deleteAll();

    @Override
    @Transactional
    default void delete(@Param("entity") T entity) {
        if (entity.getId() != null) {
            this.deleteById(entity.getId());
        }
    }

    @Override
    @Transactional
    default void deleteAll(@Param("entities") Iterable<? extends T> entities) {
        List<ID> ids = new ArrayList<>();
        for (T entity : entities) {
            if (entity.getId() != null) {
                ids.add(entity.getId());
            }
        }
        this.deleteAllById(ids);
    }

}
