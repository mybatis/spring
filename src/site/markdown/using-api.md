<a name="Using_the_MyBatis_API"></a>
# Using the MyBatis API

With MyBatis-Spring, you can continue to directly use the MyBatis API. Simply create an `SqlSessionFactory` in Spring using `SqlSessionFactoryBean` and use the factory in your code.

```java
public class UserDaoImpl implements UserDao {
  // SqlSessionFactory would normally be set by SqlSessionDaoSupport
  private final SqlSessionFactory sqlSessionFactory;

  public UserDaoImpl(SqlSessionFactory sqlSessionFactory) {
    this.sqlSessionFactory = sqlSessionFactory;
  }

  public User getUser(String userId) {
    // note standard MyBatis API usage - opening and closing the session manually
    try (SqlSession session = sqlSessionFactory.openSession()) {
      return session.selectOne("org.mybatis.spring.sample.mapper.UserMapper.getUser", userId);
    }
  }
}
```

Use this option **with care** because wrong usage may produce runtime errors or worse, data integrity problems. Be aware of the following caveats with direct API usage:

* Unless **explicitly managed by Spring's `@Transactional` annotation** or configured for **auto-commit** at the database connection level, the raw `SqlSession` does **not** participate in any transaction management. Reliance on the raw `SqlSession` is highly discouraged as it bypasses Spring's automatic resource management mechanism.
* MyBatis' `DefaultSqlSession` is not thread safe. If you inject it in your beans you **will** get errors.
* Mappers created using `DefaultSqlSession` are not thread safe either. If you inject them it in your beans you **will** get errors.
* You must make sure that your `SqlSession`s are **always** closed in a finally block.