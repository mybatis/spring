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
package org.mybatis.spring.sample;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.sample.domain.User;
import org.mybatis.spring.sample.service.BarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example of basic MyBatis-Spring integration usage with a manual DAO
 * implementation that subclasses SqlSessionDaoSupport.
 */
@DirtiesContext
@SpringJUnitConfig(locations = { "classpath:org/mybatis/spring/sample/config/applicationContext-sqlsession.xml" })
public class SampleSqlSessionTest {

  @Autowired
  private BarService barService;

  @Test
  final void testFooService() {
    User user = this.barService.doSomeBusinessStuff("u1");
    assertThat(user).isNotNull();
    assertThat(user.getName()).isEqualTo("Pocoyo");
  }

}
