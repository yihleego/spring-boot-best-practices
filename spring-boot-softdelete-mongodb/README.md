# Spring Boot 数据库逻辑删除最佳实践（MongoDB篇）

## 前言

上一期我们介绍了关系型数据库`MySQL`逻辑删除的实现方案，这一期我们聊聊非关系型数据库`MongoDB`逻辑删除的实现方案。

[Spring Boot 数据库逻辑删除最佳实践（MySQL篇）](../spring-boot-softdelete-mysql/README.md)

![上号](images/start.jpg)

## 准备工作

本例在数据库中定义了两个名称分别为`user`和`garbage`的集合，结构如下：

`user`用户集合，用于测试。

| Field        | Type     | JavaType      | description |
|:-------------|:---------|:--------------|:------------|
| id           | ObjectId | String        | 主键          |
| username     | String   | String        | 用户名         |
| password     | String   | String        | 密码          |
| created_time | Date     | LocalDateTime | 创建时间        |
| updated_time | Date     | LocalDateTime | 更新时间        |

`garbage`垃圾集合，用于回收已删除的数据。

| Field          | Type     | JavaType      | description |
|:---------------|:---------|:--------------|:------------|
| id             | ObjectId | String        | 主键          |
| type           | String   | String        | 被删除记录集合的名称  |
| data           | Object   | Object        | 被删除记录的数据    |
| collected_time | Date     | LocalDateTime | 回收时间        |

示例环境如下：

> MongoDB版本：4.4.13 Community

## 使用Spring Data MongoDB实现逻辑删除

### 添加依赖

在项目添加`spring-boot-starter-data-mongodb`依赖。

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-mongodb</artifactId>
        <version>${spring-boot.version}</version>
    </dependency>
</dependencies>
```

### 前置准备

定义一个名为`BaseEntity`的基础实体类，包含所有实体的基本属性，如：主键、创建时间和更新时间等。

```java
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

public abstract class BaseEntity {
    @MongoId(FieldType.OBJECT_ID)
    protected String id;
    @CreatedDate
    protected LocalDateTime createdTime;
    @LastModifiedDate
    protected LocalDateTime updatedTime;
    /* Constructor, Getter and Setter */
}
```

定义一个名为`User`的用户实体类。

```java
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("user")
public class User extends BaseEntity {
    @Indexed(unique = true)
    private String username;
    private String password;
    /* Constructor, Getter and Setter */
}
```

定义一个名为`Garbage`的垃圾实体类。

```java
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

@Document("garbage")
public class Garbage {
    @MongoId(FieldType.OBJECT_ID)
    private String id;
    private String type;
    private Object data;
    @Indexed
    private LocalDateTime collectedTime;
    /* Constructor, Getter and Setter */
}
```

然后定义一个名为`UserRepository`的接口，包含一个名为`deleteByUsername`的方法。

```java
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

    /** 通过用户名删除用户数据 */
    int deleteByUsername(String username);

}
```

要在对象被删除前拦截它，可以注册`AbstractMappingEventListener`的子类并重写`onBeforeDelete`方法。
当事件被调度时，监听器将被调用并接收被删除的对象，更多回调事件请参考[Lifecycle Events]。

所以最后我们定义一个名为`MongoEventListener`的监听器，继承`AbstractMappingEventListener`并重写`onBeforeDelete`方法，在数据删除前，将待删除的数据迁移至`garbage`集合中。

```java
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeDeleteEvent;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
        // 通过事件中返回的`document`对象查询待删除的数据
        List<Object> objects = mongoTemplate.find(new BasicQuery(event.getDocument()), event.getType());
        if (!CollectionUtils.isEmpty(objects)) {
            // 在数据删除前，将所有数据迁移至`garbage`集合中
            String type = event.getCollectionName();
            LocalDateTime now = LocalDateTime.now();
            List<Garbage> garbages = objects.stream()
                    .map(o -> new Garbage(null, type, o, now))
                    .collect(Collectors.toList());
            mongoTemplate.insertAll(garbages);
        }
    }
}
```

特别需要注意，调用`org.springframework.data.repository.CrudRepository#deleteAll()`方法删除集合所有数据时，不会触发`onBeforeDelete`事件。

通过源码可知，原因是调用底层通用删除方法`doRemove`时未指定实体类型（`entityClass`），然而回调事件时需要实体类型不能为空，所以使用`deleteAll()`方法不会触发事件，我们应该避免使用该方法。

1. `org.springframework.data.repository.CrudRepository`

    ```java
    /** Deletes all entities managed by the repository. */
    void deleteAll();
    ```

2. `org.springframework.data.mongodb.repository.support.SimpleMongoRepository`

    ```java
    @Override
    public void deleteAll() {
        mongoOperations.remove(new Query(), entityInformation.getCollectionName());
    }
    ```

3. `org.springframework.data.mongodb.core.MongoTemplate`

    ```java
    @Override
    public DeleteResult remove(Query query, String collectionName) {
        // 未指定`entityClass`
        return doRemove(collectionName, query, null, true);
    }
    
    protected <T> DeleteResult doRemove(String collectionName, Query query, @Nullable Class<T> entityClass, boolean multi) {
        // 忽略前置无关代码
        maybeEmitEvent(new BeforeDeleteEvent<>(queryObject, entityClass, collectionName));
        // 忽略后置无关代码
    }
    ```

4. `org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener`

    ```java
    @Override
    public void onApplicationEvent(MongoMappingEvent<?> event) {
        if (event instanceof AbstractDeleteEvent) {
            // `eventDomainType`为空时，则不会触发`onBeforeDelete`事件
            Class<?> eventDomainType = ((AbstractDeleteEvent) event).getType();
            if (eventDomainType != null && domainClass.isAssignableFrom(eventDomainType)) {
                if (event instanceof BeforeDeleteEvent) {
                    onBeforeDelete((BeforeDeleteEvent<E>) event);
                }
                if (event instanceof AfterDeleteEvent) {
                    onAfterDelete((AfterDeleteEvent<E>) event);
                }
            }
            return;
        }
    }
    ```

### 使用逻辑删除

通过调用`UserRepository`的方法删除数据。

```java
// 调用内置删除方法
userRepository.deleteById(id);
// 调用自定义删除方法
userRepository.deleteByUsername(username);
```

通过调用`MongoTemplate`的方法删除数据，请注意必须指定`entityClass`，否则不会触发删除事件。

```java
// 通过主键删除数据，特别注意主键字段默认为`_id`
mongoTemplate.remove(Query.query(new Criteria("_id").is(id)), User.class);
// 通过其他字段删除数据
mongoTemplate.remove(Query.query(new Criteria("username").is(username)), User.class);
```

被删除的数据均可以在`garbage`中被找到。

本例中设计将所有业务数据迁移至统一的`garbage`集合中，也可以为每个业务数据创建各自的回收集合，并保持数据结构一致，例如：为`user`集合新建`user_garbage`集合，其实现方案与本文大同小异，遂不再赘述。

## 总结

我们对`MongoDB`采用的逻辑删除的方案，与`MySQL`完全不同。
得益于`MongoDB`擅长储存非结构化数据的优点，即使业务数据结构发生，也不会影响原来的数据，还能保证业务表查询效率。
若`MySQL`采用此方案，则有业务数据库表结构变动导致数据迁移失败的风险，甚至影响正常业务流程。
综合考虑，关系型数据库适合通过“删除标记”实现逻辑删除，非关系型数据库更适合将“已删除”的数据迁移至回收表中。

## 示例源码

通过Git下载源码或[直接浏览][spring-boot-softdelete-mongodb]。

```bash
git clone https://github.com/yihleego/spring-boot-best-practices.git
cd spring-boot-best-practices/spring-boot-softdelete-mongodb
```

## 参考资料

- [Spring Boot]
- [Spring Data MongoDB]

[Spring Boot]:https://spring.io/projects/spring-boot

[Spring Data MongoDB]:https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/

[Lifecycle Events]:https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mongodb.mapping-usage.events

[spring-boot-softdelete-mongodb]:https://github.com/yihleego/spring-boot-best-practices/tree/main/spring-boot-softdelete-mongodb

