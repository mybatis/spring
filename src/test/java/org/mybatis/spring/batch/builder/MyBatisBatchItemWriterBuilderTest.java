/*
 * Copyright 2010-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mybatis.spring.batch.builder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.batch.item.Chunk;

/**
 * Tests for {@link MyBatisBatchItemWriterBuilder}.
 *
 * @since 2.0.0
 *
 * @author Kazuki Shimizu
 */
class MyBatisBatchItemWriterBuilderTest {

  @Mock
  private DataSource dataSource;

  @Mock
  private SqlSessionFactory sqlSessionFactory;

  @Mock
  private SqlSession sqlSession;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    {
      var configuration = new Configuration();
      var environment = new Environment("unittest", new JdbcTransactionFactory(), dataSource);
      configuration.setEnvironment(environment);
      Mockito.when(this.sqlSessionFactory.getConfiguration()).thenReturn(configuration);
      Mockito.when(this.sqlSessionFactory.openSession(ExecutorType.BATCH)).thenReturn(this.sqlSession);
    }
    {
      var result = new BatchResult(null, null);
      result.setUpdateCounts(new int[] { 1 });
      Mockito.when(this.sqlSession.flushStatements()).thenReturn(Collections.singletonList(result));
    }
  }

  @Test
  void testConfigurationUsingSqlSessionFactory() {

    // @formatter:off
        var itemWriter = new MyBatisBatchItemWriterBuilder<Foo>()
                .sqlSessionFactory(this.sqlSessionFactory)
                .statementId("updateFoo")
                .build();
        // @formatter:on
    itemWriter.afterPropertiesSet();

    var foos = getFoos();

    itemWriter.write(foos);

    Mockito.verify(this.sqlSession).update("updateFoo", foos.getItems().get(0));
    Mockito.verify(this.sqlSession).update("updateFoo", foos.getItems().get(1));
    Mockito.verify(this.sqlSession).update("updateFoo", foos.getItems().get(2));

  }

  @Test
  void testConfigurationUsingSqlSessionTemplate() {

    // @formatter:off
        var itemWriter = new MyBatisBatchItemWriterBuilder<Foo>()
                .sqlSessionTemplate(new SqlSessionTemplate(this.sqlSessionFactory, ExecutorType.BATCH))
                .statementId("updateFoo")
                .build();
        // @formatter:on
    itemWriter.afterPropertiesSet();

    var foos = getFoos();

    itemWriter.write(foos);

    Mockito.verify(this.sqlSession).update("updateFoo", foos.getItems().get(0));
    Mockito.verify(this.sqlSession).update("updateFoo", foos.getItems().get(1));
    Mockito.verify(this.sqlSession).update("updateFoo", foos.getItems().get(2));

  }

  @Test
  void testConfigurationAssertUpdatesIsFalse() {

    Mockito.when(this.sqlSession.flushStatements()).thenReturn(Collections.emptyList());

    // @formatter:off
        var itemWriter = new MyBatisBatchItemWriterBuilder<Foo>()
                .sqlSessionTemplate(new SqlSessionTemplate(this.sqlSessionFactory, ExecutorType.BATCH))
                .statementId("updateFoo")
                .assertUpdates(false)
                .build();
        // @formatter:on
    itemWriter.afterPropertiesSet();

    var foos = getFoos();

    itemWriter.write(foos);

    Mockito.verify(this.sqlSession).update("updateFoo", foos.getItems().get(0));
    Mockito.verify(this.sqlSession).update("updateFoo", foos.getItems().get(1));
    Mockito.verify(this.sqlSession).update("updateFoo", foos.getItems().get(2));

  }

  @Test
  void testConfigurationSetItemToParameterConverter() {

    // @formatter:off
        var itemWriter = new MyBatisBatchItemWriterBuilder<Foo>()
                .sqlSessionFactory(this.sqlSessionFactory)
                .statementId("updateFoo")
                .itemToParameterConverter(item -> {
                    Map<String, Object> parameter = new HashMap<>();
                    parameter.put("item", item);
                    parameter.put("now", LocalDateTime.now(Clock.fixed(Instant.ofEpochMilli(0), ZoneId.systemDefault())));
                    return parameter;
                })
                .build();
        // @formatter:on
    itemWriter.afterPropertiesSet();

    var foos = getFoos();

    itemWriter.write(foos);

    Map<String, Object> parameter = new HashMap<>();
    parameter.put("now", LocalDateTime.now(Clock.fixed(Instant.ofEpochMilli(0), ZoneId.systemDefault())));
    parameter.put("item", foos.getItems().get(0));
    Mockito.verify(this.sqlSession).update("updateFoo", parameter);
    parameter.put("item", foos.getItems().get(1));
    Mockito.verify(this.sqlSession).update("updateFoo", parameter);
    parameter.put("item", foos.getItems().get(2));
    Mockito.verify(this.sqlSession).update("updateFoo", parameter);
  }

  private Chunk<Foo> getFoos() {
    return Chunk.of(new Foo("foo1"), new Foo("foo2"), new Foo("foo3"));
  }

  // Note: Do not cleanup this 'foo' class
  private static class Foo {
    private final String name;

    Foo(String name) {
      this.name = name;
    }

    public String getName() {
      return this.name;
    }
  }

}
