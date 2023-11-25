/*
 * Copyright 2010-2023 the original author or authors.
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
package org.mybatis.spring;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mockrunner.mock.jdbc.MockDataSource;

import java.sql.SQLException;

import org.apache.ibatis.exceptions.PersistenceException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.support.SQLExceptionTranslator;

class MyBatisExceptionTranslatorTest {

  @Test
  void shouldNonPersistenceExceptionBeTranslatedToNull() {
    MockDataSource mockDataSource = new MockDataSource();
    MyBatisExceptionTranslator translator = new MyBatisExceptionTranslator(mockDataSource, false);
    DataAccessException e = translator.translateExceptionIfPossible(new RuntimeException());
    assertNull(e);
  }

  @Test
  void shouldSqlExceptionBeTranslatedToUncategorizedSqlException() {
    String msg = "Error!";
    SQLException sqlException = new SQLException(msg);
    SQLExceptionTranslator sqlExceptionTranslator = Mockito.mock(SQLExceptionTranslator.class);
    MyBatisExceptionTranslator translator = new MyBatisExceptionTranslator(() -> sqlExceptionTranslator, false);
    DataAccessException e = translator.translateExceptionIfPossible(new PersistenceException(sqlException));
    assertTrue(e instanceof UncategorizedSQLException);
    Mockito.verify(sqlExceptionTranslator, times(1)).translate(SQLException.class.getName() + ": " + msg + "\n", null,
        sqlException);
  }

  @Test
  void shouldPersistenceExceptionBeTranslatedToMyBatisSystemException() {
    String msg = "Error!";
    SQLExceptionTranslator sqlExceptionTranslator = Mockito.mock(SQLExceptionTranslator.class);
    MyBatisExceptionTranslator translator = new MyBatisExceptionTranslator(() -> sqlExceptionTranslator, false);
    DataAccessException e = translator.translateExceptionIfPossible(new PersistenceException(msg));
    assertTrue(e instanceof MyBatisSystemException);
    assertEquals(msg, e.getMessage());
  }

  @Test
  void shouldNestedPersistenceExceptionReportsMsgOfParentException() {
    String msg = "Error!";
    SQLExceptionTranslator sqlExceptionTranslator = Mockito.mock(SQLExceptionTranslator.class);
    MyBatisExceptionTranslator translator = new MyBatisExceptionTranslator(() -> sqlExceptionTranslator, false);
    DataAccessException e = translator
        .translateExceptionIfPossible(new PersistenceException(msg, new PersistenceException("Inner error!")));
    assertTrue(e instanceof MyBatisSystemException);
    assertEquals(msg, e.getMessage());
  }

  @Test
  void shouldNestedPersistenceExceptionReportsMsgOfChildExceptionIfParentsMsgIsNull() {
    String msg = "Error!";
    SQLExceptionTranslator sqlExceptionTranslator = Mockito.mock(SQLExceptionTranslator.class);
    MyBatisExceptionTranslator translator = new MyBatisExceptionTranslator(() -> sqlExceptionTranslator, false);
    DataAccessException e = translator
        .translateExceptionIfPossible(new PersistenceException(null, new PersistenceException(msg)));
    assertTrue(e instanceof MyBatisSystemException);
    assertEquals(msg, e.getMessage());
  }

}
