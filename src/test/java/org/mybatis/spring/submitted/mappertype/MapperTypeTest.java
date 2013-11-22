/*
 * Copyright 2013 MyBatis.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mybatis.spring.submitted.mappertype;

import java.lang.reflect.Proxy;
import java.util.Collection;
import static org.hamcrest.CoreMatchers.*;
import org.hamcrest.Matcher;
import static org.junit.Assert.*;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MapperTypeTest {

  @Test
  public void shouldContextReturnMappersActualType() {
    final ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:org/mybatis/spring/submitted/mappertype/applicationContext.xml");
    try {
      final IFooMapper mapper = ctx.getBean(IFooMapper.class);
      assertNotNull("mapper must not be null", mapper);
      assertThat("mapper should be a proxy", mapper, instanceOf(Proxy.class));

      final Collection<Proxy> proxies = ctx.getBeansOfType(Proxy.class).values();
      final Matcher<Proxy> mapperMatcher = instanceOf(IFooMapper.class);
      assertThat("collection of all proxies should contain mapper", proxies, hasItem(mapperMatcher));
    } finally {
      ctx.close();
    }
  }

}
