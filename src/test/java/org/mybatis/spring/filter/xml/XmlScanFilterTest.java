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
package org.mybatis.spring.filter.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * test the function of excludeFilters in <mybatis:scan/>
 */
public class XmlScanFilterTest {

  private ClassPathXmlApplicationContext applicationContext;

  @Test
  void testCustomScanFilter() {
    // exclude datasource2 by CustomTypeFilter
    startContext("org/mybatis/spring/filter/xml/appContextCustFilter.xml");
    assertThat(applicationContext.containsBean("dataSource2Mapper")).isFalse();
    assertThat(applicationContext.containsBean("mapperWithAnnoFilter")).isTrue();
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isTrue();
    closeContext();
  }

  @Test
  void testAnnoScanFilter() {
    // exclude mappers which has @AnnoTypeFilter
    startContext("org/mybatis/spring/filter/xml/appContextAnnoFilter.xml");
    assertThat(applicationContext.containsBean("mapperWithAnnoFilter")).isFalse();
    assertThat(applicationContext.containsBean("dataSource1Mapper")).isTrue();
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isTrue();
    closeContext();
  }

  @Test
  void testAssignScanFilter() {
    // exclude mappers which can assignable to ExcludeMaker
    startContext("org/mybatis/spring/filter/xml/appContextAssignFilter.xml");
    assertThat(applicationContext.containsBean("assignableMapper")).isFalse();
    assertThat(applicationContext.containsBean("dataSource1Mapper")).isTrue();
    assertThat(applicationContext.containsBean("dataSource2Mapper")).isTrue();
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isTrue();
    closeContext();
  }

  @Test
  void testRegexScanFilter() {
    // exclude datasource1 by regex
    startContext("org/mybatis/spring/filter/xml/appContextRegexFilter.xml");
    assertThat(applicationContext.containsBean("dataSource1Mapper")).isFalse();
    assertThat(applicationContext.containsBean("mapperWithAnnoFilter")).isFalse();
    assertThat(applicationContext.containsBean("dataSource2Mapper")).isTrue();
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isTrue();
    closeContext();
  }

  @Test
  void testAspectJScanFilter() {
    // exclude mappers which class name start with DataSource
    startContext("org/mybatis/spring/filter/xml/appContextAspectJFilter.xml");
    assertThat(applicationContext.containsBean("dataSource1Mapper")).isFalse();
    assertThat(applicationContext.containsBean("dataSource2Mapper")).isFalse();
    assertThat(applicationContext.containsBean("mapperWithAnnoFilter")).isTrue();
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isTrue();
    closeContext();
  }

  @Test
  void testCombinedScanFilter() {
    // exclude filters combined with Annotation Custom and Assignable
    startContext("org/mybatis/spring/filter/xml/appContextCombinedFilter.xml");
    assertThat(applicationContext.containsBean("mapperWithAnnoFilter")).isFalse();
    assertThat(applicationContext.containsBean("dataSource2Mapper")).isFalse();
    assertThat(applicationContext.containsBean("assignableMapper")).isFalse();
    assertThat(applicationContext.containsBean("dataSource1Mapper")).isTrue();
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isTrue();
    closeContext();
  }

  @Test
  void invalidPatternFilter() {
    try {
      startContext("org/mybatis/spring/filter/xml/appContextInvalidFilter.xml");
    } catch (BeanDefinitionParsingException ex) {
      assertThat(ex.getMessage()).contains("Class is not assignable to [java.lang.annotation.Annotation]");
    } finally {
      closeContext();
    }
  }

  @Test
  void invalidPropertyPattern() {
    assertThrows(BeanDefinitionParsingException.class,
      () -> startContext("org/mybatis/spring/filter/xml/appContextInvalidFilter1.xml"));
    closeContext();
  }


  private void startContext(String config) {
    applicationContext = new ClassPathXmlApplicationContext(config);
    applicationContext.refresh();
    applicationContext.start();
  }

  private void closeContext() {
    if (null != applicationContext)
      applicationContext.close();
  }
}
