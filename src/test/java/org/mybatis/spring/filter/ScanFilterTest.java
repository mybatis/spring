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

import com.mockrunner.mock.jdbc.MockDataSource;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.filter.config.AppConfig;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * test the function of excludeFilters in @MapperScan
 */
public class ScanFilterTest {

  private AnnotationConfigApplicationContext applicationContext;


  @Test
  void testCustomScanFilter() {
    startContext(AppConfig.CustomFilterConfig.class);
    // use org.mybatis.spring.scan.filter.datasource as basePackages and exclude package datasource2 by MapperScan.excludeFilters
    // mapper in package datasource2 will not be registered to beanFactory
    assertThat(applicationContext.containsBean("dataSource2Mapper")).isEqualTo(false);

    // mapper in package datasource except datasource2 will be registered to beanFactory correctly.
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isEqualTo(true);
    assertThat(applicationContext.containsBean("dataSource1Mapper")).isEqualTo(true);
  }

  @Test
  void testAnnoScanFilter() {
    startContext(AppConfig.AnnoFilterConfig.class);

    // use @AnnoTypeFilter to exclude mapper
    assertThat(applicationContext.containsBean("annoExcludeMapper")).isEqualTo(false);

    // mapper in package datasource except datasource2 will be registered to beanFactory correctly.
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isEqualTo(true);
    assertThat(applicationContext.containsBean("dataSource1Mapper")).isEqualTo(true);
    assertThat(applicationContext.containsBean("dataSource2Mapper")).isEqualTo(true);
  }


  @Test
  void testAssignableScanFilter() {
    startContext(AppConfig.AssignableFilterConfig.class);

    // exclude AssignableMapper by AssignableFilter
    assertThat(applicationContext.containsBean("assignableMapper")).isEqualTo(false);

    // mapper in package datasource except datasource2 will be registered to beanFactory correctly.
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isEqualTo(true);
    assertThat(applicationContext.containsBean("dataSource1Mapper")).isEqualTo(true);
    assertThat(applicationContext.containsBean("dataSource2Mapper")).isEqualTo(true);
  }

  @Test
  void testRegexScanFilter() {
    startContext(AppConfig.RegexFilterConfig.class);

    // exclude package datasource1 by Regex
    assertThat(applicationContext.containsBean("dataSource1Mapper")).isEqualTo(false);

    // mapper in package datasource except datasource1 will be registered to beanFactory correctly.
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isEqualTo(true);
    assertThat(applicationContext.containsBean("dataSource2Mapper")).isEqualTo(true);
  }

  @Test
  void testAspectJScanFilter() {

    startContext(AppConfig.AspectJFilterConfig.class);

    // exclude dataSource1Mapper by AspectJ
    assertThat(applicationContext.containsBean("dataSource1Mapper")).isEqualTo(false);

    // mapper in package datasource except datasource1 will be registered to beanFactory correctly.
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isEqualTo(true);
    assertThat(applicationContext.containsBean("dataSource2Mapper")).isEqualTo(true);
  }


  @Test
  void combinedScanFilter() {
    // combined filter with Custom and Annotation
    startContext(AppConfig.CombinedFilterConfig.class);

    // exclude datasource2.DataSource2Mapper by CustomTypeFilter
    assertThat(applicationContext.containsBean("dataSource2Mapper")).isEqualTo(false);
    // exclude datasource1.MapperWithAnnoFilter by AnnoTypeFilter
    assertThat(applicationContext.containsBean("mapperWithAnnoFilter")).isEqualTo(false);

    // other mapper could be registered to beanFactory correctly.
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isEqualTo(true);
    assertThat(applicationContext.containsBean("dataSource1Mapper")).isEqualTo(true);
  }


  @Test
  void multiPatternRegexScanFilter() {
    // multi pattern regex filter
    startContext(AppConfig.MultiPatternRegexFilterConfig.class);

    // exclude datasource1 by pattern[0]
    assertThat(applicationContext.containsBean("dataSource1Mapper")).isEqualTo(false);
    // exclude datasource2 by pattern[1]
    assertThat(applicationContext.containsBean("dataSource2Mapper")).isEqualTo(false);

    // other mapper could be registered to beanFactory correctly.
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isEqualTo(true);
  }

  @Test
  void multiPatternAspectJScanFilter() {
    // multi pattern regex filter
    startContext(AppConfig.MultiPatternAspectJFilterConfig.class);

    // exclude datasource1 by pattern[0]
    assertThat(applicationContext.containsBean("dataSource1Mapper")).isEqualTo(false);
    // exclude datasource2 by pattern[1]
    assertThat(applicationContext.containsBean("dataSource2Mapper")).isEqualTo(false);

    // other mapper could be registered to beanFactory correctly.
    assertThat(applicationContext.containsBean("commonDataSourceMapper")).isEqualTo(true);
    assertThat(applicationContext.containsBean("dataSource2Mapper1")).isEqualTo(true);
  }


  @Test
  void invalidTypeFilter() {
    // invalid value using Annotation type filter
    assertThrows(IllegalArgumentException.class,
      () -> startContext(AppConfig.InvalidFilterTypeConfig.class));
  }

  @Test
  void invalidPropertyPattern() {
    assertThrows(IllegalArgumentException.class,
      () -> startContext(AppConfig.AnnoTypeWithPatternPropertyConfig.class));
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
