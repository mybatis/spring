/**
 *    Copyright 2010-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.spring.batch;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;

import static org.junit.Assert.fail;

/**
 * Tests for {@link MyBatisCursorItemReader}.
 */
public class MyBatisCursorItemReaderTest {

  @Mock
  private SqlSessionFactory sqlSessionFactory;

  @Mock
  private SqlSession sqlSession;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testCloseOnFailing() throws Exception {

    Mockito.when(this.sqlSessionFactory.openSession(ExecutorType.SIMPLE))
        .thenReturn(this.sqlSession);
    Mockito.when(this.sqlSession.selectCursor(Mockito.eq("selectFoo"), Mockito.anyMap()))
        .thenThrow(new RuntimeException("error."));

    MyBatisCursorItemReader<Foo> itemReader = new MyBatisCursorItemReader<Foo>();
    itemReader.setSqlSessionFactory(this.sqlSessionFactory);
    itemReader.setQueryId("selectFoo");
    itemReader.afterPropertiesSet();

    ExecutionContext executionContext = new ExecutionContext();
    try {
      itemReader.open(executionContext);
      fail();
    } catch (ItemStreamException e) {
      Assert.assertThat(e.getMessage(), Is.is("Failed to initialize the reader"));
      Assert.assertThat(e.getCause(), IsInstanceOf.instanceOf(RuntimeException.class));
      Assert.assertThat(e.getCause().getMessage(), Is.is("error."));
    } finally {
      itemReader.close();
      Mockito.verify(this.sqlSession).close();
    }

  }

  @Test
  public void testCloseBeforeOpen() {
    MyBatisCursorItemReader<Foo> itemReader = new MyBatisCursorItemReader<Foo>();
    itemReader.close();
  }

  private static class Foo {
    private final String name;

    public Foo(String name) {
      this.name = name;
    }

    public String getName() {
      return this.name;
    }
  }

}
