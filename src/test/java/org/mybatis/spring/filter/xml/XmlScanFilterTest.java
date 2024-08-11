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
package org.mybatis.spring.filter.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.regex.PatternSyntaxException;

import org.junit.jupiter.api.Test;
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
  void testScanWithPlaceHolderFilter() {
    // exclude mappers which has @AnnoTypeFilter
    System.getProperties().put("annoFilter", "org.mybatis.spring.filter.customfilter.AnnoTypeFilter");
    startContext("org/mybatis/spring/filter/xml/appContextPlaceHolder.xml");
    assertThat(applicationContext.containsBean("mapperWithAnnoFilter")).isFalse();
    assertThat(applicationContext.containsBean("dataSource1Mapper")).isTrue();
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isTrue();
    closeContext();
  }

  @Test
  void testScanWithPlaceHolderFilter1() {
    // exclude datasource2 mappers by CustomTypeFilter
    startContext("org/mybatis/spring/filter/xml/appContextPlaceHolder1.xml");
    assertThat(applicationContext.containsBean("dataSource2Mapper")).isFalse();
    assertThat(applicationContext.containsBean("dataSource2Mapper1")).isFalse();
    assertThat(applicationContext.containsBean("mapperWithAnnoFilter")).isTrue();
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
    // exclude filters combined with Annotation Custom Assignable and aspectj expression
    startContext("org/mybatis/spring/filter/xml/appContextCombinedFilter.xml");
    assertThat(applicationContext.containsBean("mapperWithAnnoFilter")).isFalse();
    assertThat(applicationContext.containsBean("dataSource2Mapper")).isFalse();
    assertThat(applicationContext.containsBean("assignableMapper")).isFalse();
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isFalse();

    assertThat(applicationContext.containsBean("dataSource1Mapper")).isTrue();
    closeContext();
  }

  @Test
  void invalidPatternFilter() {
    assertThrows(IllegalArgumentException.class,
        () -> startContext("org/mybatis/spring/filter/xml/appContextInvalidFilter.xml"));
    closeContext();
  }

  @Test
  void invalidPropertyPattern() {
    assertThrows(IllegalArgumentException.class,
        () -> startContext("org/mybatis/spring/filter/xml/appContextInvalidFilter1.xml"));
    closeContext();
  }

  @Test
  void warpedClassNotFoundException() {
    assertThrows(RuntimeException.class,
        () -> startContext("org/mybatis/spring/filter/xml/appContextInvalidFilter2.xml"));
    closeContext();
  }

  @Test
  void processPropertyPlaceHoldersSwitchTest() {
    // if processPropertyPlaceHolders turn off regex compile will fail
    assertThrows(PatternSyntaxException.class,
        () -> startContext("org/mybatis/spring/filter/xml/appContextProcessPlaceHolderOff.xml"));
    closeContext();
  }

  private void startContext(String config) {
    applicationContext = new ClassPathXmlApplicationContext(config);
    applicationContext.refresh();
    applicationContext.start();
  }

  private void closeContext() {
    if (null != applicationContext) {
      applicationContext.close();
    }
  }
}
