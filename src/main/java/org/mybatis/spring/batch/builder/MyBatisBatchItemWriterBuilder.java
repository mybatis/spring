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
package org.mybatis.spring.batch.builder;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;

/**
 * A builder for the {@link MyBatisBatchItemWriter}.
 *
 * @author Kazuki Shimizu
 * @since 2.0.0
 * @see MyBatisBatchItemWriter
 */
public class MyBatisBatchItemWriterBuilder<T> {

  private SqlSessionTemplate sqlSessionTemplate;
  private SqlSessionFactory sqlSessionFactory;
  private String statementId;
  private Boolean assertUpdates;

  /**
   * Set the {@link SqlSessionTemplate} to be used by writer for database access.
   *
   * @param sqlSessionTemplate the {@link SqlSessionTemplate} to be used by writer for database access
   * @return this instance for method chaining
   * @see MyBatisBatchItemWriter#setSqlSessionTemplate(SqlSessionTemplate)
   */
  public MyBatisBatchItemWriterBuilder<T> sqlSessionTemplate(
      SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSessionTemplate = sqlSessionTemplate;
    return this;
  }

  /**
   * Set the {@link SqlSessionFactory} to be used by writer for database access.
   *
   * @param sqlSessionFactory the {@link SqlSessionFactory} to be used by writer for database access
   * @return this instance for method chaining
   * @see MyBatisBatchItemWriter#setSqlSessionFactory(SqlSessionFactory)
   */
  public MyBatisBatchItemWriterBuilder<T> sqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
    this.sqlSessionFactory = sqlSessionFactory;
    return this;
  }

  /**
   * Set the statement id identifying the statement in the SqlMap configuration file.
   *
   * @param statementId the id for the statement
   * @return this instance for method chaining
   * @see MyBatisBatchItemWriter#setStatementId(String)
   */
  public MyBatisBatchItemWriterBuilder<T> statementId(String statementId) {
    this.statementId = statementId;
    return this;
  }

  /**
   * The flag that determines whether an assertion is made that all items cause at least one row to
   * be updated.
   *
   * @param assertUpdates the flag to set. Defaults to true
   * @return this instance for method chaining
   * @see MyBatisBatchItemWriter#setAssertUpdates(boolean)
   */
  public MyBatisBatchItemWriterBuilder<T> assertUpdates(boolean assertUpdates) {
    this.assertUpdates = assertUpdates;
    return this;
  }

  /**
   * Returns a fully built {@link MyBatisBatchItemWriter}.
   *
   * @return the writer
   */
  public MyBatisBatchItemWriter<T> build() {
    MyBatisBatchItemWriter<T> writer = new MyBatisBatchItemWriter<>();
    writer.setSqlSessionTemplate(this.sqlSessionTemplate);
    writer.setSqlSessionFactory(this.sqlSessionFactory);
    writer.setStatementId(this.statementId);
    if (this.assertUpdates != null) {
      writer.setAssertUpdates(this.assertUpdates);
    }
    return writer;
  }

}
