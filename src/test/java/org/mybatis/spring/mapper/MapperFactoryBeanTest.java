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
package org.mybatis.spring.mapper;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.AbstractMyBatisSpringTest;
import org.mybatis.spring.MyBatisSystemException;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.TestMapper;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.mock.jdbc.MockDataSource;

public final class MapperFactoryBeanTest extends AbstractMyBatisSpringTest {

  private static SqlSessionTemplate sqlSessionTemplate;

  @BeforeAll
  static void setupSqlTemplate() {
    sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory);
  }

  // test normal MapperFactoryBean usage
  @Test
  void testBasicUsage() throws Exception {
    find();

    assertCommit(); // SqlSesssionTemplate autocommits
    assertSingleConnection();
    assertExecuteCount(1);
  }

  @Test
  void testAddToConfigTrue() throws Exception {
    // the default SqlSessionFactory in AbstractMyBatisSpringTest is created with an explicitly set
    // MapperLocations list, so create a new factory here that tests auto-loading the config
    SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
    factoryBean.setDatabaseIdProvider(null);
    // mapperLocations properties defaults to null
    factoryBean.setDataSource(dataSource);
    factoryBean.setPlugins(new Interceptor[] { executorInterceptor });

    SqlSessionFactory sqlSessionFactory = factoryBean.getObject();

    find(new SqlSessionTemplate(sqlSessionFactory), true);
    assertCommit(); // SqlSesssionTemplate autocommits
    assertSingleConnection();
    assertExecuteCount(1);
  }

  // will fail because TestDao's mapper config is never loaded
  @Test
  void testAddToConfigFalse() throws Throwable {
    try {
      // the default SqlSessionFactory in AbstractMyBatisSpringTest is created with an explicitly
      // set MapperLocations list, so create a new factory here that tests auto-loading the
      // config
      SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
      // mapperLocations properties defaults to null
      factoryBean.setDataSource(dataSource);

      SqlSessionFactory sqlSessionFactory = factoryBean.getObject();

      assertThrows(org.apache.ibatis.binding.BindingException.class, () ->
        find(new SqlSessionTemplate(sqlSessionFactory), false)
      );
//      fail("TestDao's mapper xml should not be loaded");
    } catch (MyBatisSystemException mbse) {
      // unwrap exception so the exact MyBatis exception can be tested
      throw mbse.getCause();
    } finally {
      // connection not used; force close to avoid failing in validateConnectionClosed()
      connection.close();
    }
  }

  @Test
  void testWithTx() throws Exception {
    TransactionStatus status = txManager.getTransaction(new DefaultTransactionDefinition());

    find();

    txManager.commit(status);

    assertCommit();
    assertSingleConnection();
    assertExecuteCount(1);
  }

  // MapperFactoryBeans should be usable outside of Spring TX, as long as a there is no active
  // transaction
  @Test
  void testWithNonSpringTransactionFactory() throws Exception {
    Environment original = sqlSessionFactory.getConfiguration().getEnvironment();
    Environment nonSpring = new Environment("non-spring", new JdbcTransactionFactory(), dataSource);
    sqlSessionFactory.getConfiguration().setEnvironment(nonSpring);

    try {
      find(new SqlSessionTemplate(sqlSessionFactory));

      assertCommit(); // SqlSessionTemplate autocommits
      assertCommitSession();
      assertSingleConnection();
      assertExecuteCount(1);
    } finally {
      sqlSessionFactory.getConfiguration().setEnvironment(original);
    }
  }

  // active transaction using the DataSource, but without a SpringTransactionFactory
  // this should error
  @Test
  void testNonSpringTxMgrWithTx() throws Exception {
    Environment original = sqlSessionFactory.getConfiguration().getEnvironment();
    Environment nonSpring = new Environment("non-spring", new JdbcTransactionFactory(), dataSource);
    sqlSessionFactory.getConfiguration().setEnvironment(nonSpring);

    TransactionStatus status = null;

    try {
      status = txManager.getTransaction(new DefaultTransactionDefinition());

      find();

      fail("should not be able to get an SqlSession using non-Spring tx manager when there is an active Spring tx");
    } catch (TransientDataAccessResourceException e) {
      assertThat(e.getMessage()).isEqualTo("SqlSessionFactory must be using a SpringManagedTransactionFactory in order to use" +
          " Spring transaction synchronization");
    } finally {
      // rollback required to close connection
      txManager.rollback(status);

      sqlSessionFactory.getConfiguration().setEnvironment(original);
    }
  }

  // similar to testNonSpringTxFactoryNonSpringDSWithTx() in MyBatisSpringTest
  @Test
  void testNonSpringWithTx() throws Exception {
    Environment original = sqlSessionFactory.getConfiguration().getEnvironment();

    MockDataSource mockDataSource = new MockDataSource();
    mockDataSource.setupConnection(createMockConnection());

    Environment nonSpring = new Environment("non-spring", new JdbcTransactionFactory(), mockDataSource);
    sqlSessionFactory.getConfiguration().setEnvironment(nonSpring);

    SqlSessionTemplate sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory);

    TransactionStatus status;

    try {
      status = txManager.getTransaction(new DefaultTransactionDefinition());

      find(sqlSessionTemplate);

      txManager.commit(status);

      // txManager still uses original connection
      assertCommit();
      assertSingleConnection();

      // SqlSessionTemplate uses its own connection
      MockConnection mockConnection = (MockConnection) mockDataSource.getConnection();
      assertThat(mockConnection.getNumberCommits()).as("should call commit on Connection").isEqualTo(1);
      assertThat(mockConnection.getNumberRollbacks()).as("should not call rollback on Connection").isEqualTo(0);
      assertCommitSession();
    } finally {

      sqlSessionFactory.getConfiguration().setEnvironment(original);
    }
  }

  private void find() throws Exception {
    find(MapperFactoryBeanTest.sqlSessionTemplate, true);
  }

  private void find(SqlSessionTemplate sqlSessionTemplate) throws Exception {
    find(sqlSessionTemplate, true);
  }

  private void find(SqlSessionTemplate sqlSessionTemplate, boolean addToConfig) throws Exception {
    // recreate the mapper for each test since sqlSessionTemplate or the underlying
    // SqlSessionFactory could change for each test
    MapperFactoryBean<TestMapper> mapper = new MapperFactoryBean<>();
    mapper.setMapperInterface(TestMapper.class);
    mapper.setSqlSessionTemplate(sqlSessionTemplate);
    mapper.setAddToConfig(addToConfig);
    mapper.afterPropertiesSet();

    mapper.getObject().findTest();
  }
}
