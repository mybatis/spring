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
package org.mybatis.spring.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mockrunner.mock.jdbc.MockDataSource;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.filter.config.AppConfig;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * test the function of excludeFilters in @MapperScan
 */
public class ScanFilterTest {

  private AnnotationConfigApplicationContext applicationContext;

  @Test
  void testCustomScanFilter() {
    startContext(AppConfig.CustomFilterConfig.class);
    // use org.mybatis.spring.scan.filter.datasource as basePackages and exclude package datasource2 by
    // MapperScan.excludeFilters
    // mapper in package datasource2 will not be registered to beanFactory
    assertThat(applicationContext.containsBean("dataSource2Mapper")).isFalse();

    // mapper in package datasource except datasource2 will be registered to beanFactory correctly.
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isTrue();
    assertThat(applicationContext.containsBean("dataSource1Mapper")).isTrue();
  }

  @Test
  void testAnnoScanFilter() {
    startContext(AppConfig.AnnoFilterConfig.class);

    // use @AnnoTypeFilter to exclude mapper
    assertThat(applicationContext.containsBean("annoExcludeMapper")).isFalse();

    // mapper in package datasource except datasource2 will be registered to beanFactory correctly.
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isTrue();
    assertThat(applicationContext.containsBean("dataSource1Mapper")).isTrue();
    assertThat(applicationContext.containsBean("dataSource2Mapper")).isTrue();
  }

  @Test
  void testAssignableScanFilter() {
    startContext(AppConfig.AssignableFilterConfig.class);

    // exclude AssignableMapper by AssignableFilter
    assertThat(applicationContext.containsBean("assignableMapper")).isFalse();

    // mapper in package datasource except datasource2 will be registered to beanFactory correctly.
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isTrue();
    assertThat(applicationContext.containsBean("dataSource1Mapper")).isTrue();
    assertThat(applicationContext.containsBean("dataSource2Mapper")).isTrue();
  }

  @Test
  void testRegexScanFilter() {
    startContext(AppConfig.RegexFilterConfig.class);

    // exclude package datasource1 by Regex
    assertThat(applicationContext.containsBean("dataSource1Mapper")).isFalse();

    // mapper in package datasource except datasource1 will be registered to beanFactory correctly.
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isTrue();
    assertThat(applicationContext.containsBean("dataSource2Mapper")).isTrue();
  }

  @Test
  void testAspectJScanFilter() {

    startContext(AppConfig.AspectJFilterConfig.class);

    // exclude dataSource1Mapper by AspectJ
    assertThat(applicationContext.containsBean("dataSource1Mapper")).isFalse();

    // mapper in package datasource except datasource1 will be registered to beanFactory correctly.
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isTrue();
    assertThat(applicationContext.containsBean("dataSource2Mapper")).isTrue();
  }

  @Test
  void combinedScanFilter() {
    // combined filter with Custom and Annotation
    startContext(AppConfig.CombinedFilterConfig.class);

    // exclude datasource2.DataSource2Mapper by CustomTypeFilter
    assertThat(applicationContext.containsBean("dataSource2Mapper")).isFalse();
    // exclude datasource1.MapperWithAnnoFilter by AnnoTypeFilter
    assertThat(applicationContext.containsBean("mapperWithAnnoFilter")).isFalse();

    // other mapper could be registered to beanFactory correctly.
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isTrue();
    assertThat(applicationContext.containsBean("dataSource1Mapper")).isTrue();
  }

  @Test
  void multiPatternRegexScanFilter() {
    // multi pattern regex filter
    startContext(AppConfig.MultiPatternRegexFilterConfig.class);

    // exclude datasource1 by pattern[0]
    assertThat(applicationContext.containsBean("dataSource1Mapper")).isFalse();
    // exclude datasource2 by pattern[1]
    assertThat(applicationContext.containsBean("dataSource2Mapper")).isFalse();

    // other mapper could be registered to beanFactory correctly.
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isTrue();
  }

  @Test
  void multiPatternAspectJScanFilter() {
    // multi pattern regex filter
    startContext(AppConfig.MultiPatternAspectJFilterConfig.class);

    // exclude datasource1 by pattern[0]
    assertThat(applicationContext.containsBean("dataSource1Mapper")).isFalse();
    // exclude datasource2 by pattern[1]
    assertThat(applicationContext.containsBean("dataSource2Mapper")).isFalse();

    // other mapper could be registered to beanFactory correctly.
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isTrue();
    assertThat(applicationContext.containsBean("dataSource2Mapper1")).isTrue();
  }

  @Test
  void invalidTypeFilter() {
    // invalid value using Annotation type filter
    assertThrows(IllegalArgumentException.class, () -> startContext(AppConfig.InvalidFilterTypeConfig.class));
  }

  @Test
  void invalidPropertyPattern() {
    assertThrows(IllegalArgumentException.class, () -> startContext(AppConfig.AnnoTypeWithPatternPropertyConfig.class));
  }

  @Test
  void invalidPropertyClasses() {
    assertThrows(IllegalArgumentException.class,
        () -> startContext(AppConfig.RegexTypeWithClassesPropertyConfig.class));
  }

  private void startContext(Class<?> config) {
    applicationContext = new AnnotationConfigApplicationContext();
    // use @MapperScan with excludeFilters in AppConfig.class
    applicationContext.register(config);
    setupSqlSessionFactory("sqlSessionFactory");
    applicationContext.refresh();
    applicationContext.start();
  }

  private void setupSqlSessionFactory(String name) {
    GenericBeanDefinition definition = new GenericBeanDefinition();
    definition.setBeanClass(SqlSessionFactoryBean.class);
    definition.getPropertyValues().add("dataSource", new MockDataSource());
    applicationContext.registerBeanDefinition(name, definition);
  }
}
