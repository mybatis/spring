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
package org.mybatis.spring.filter.config;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.filter.customfilter.AnnoTypeFilter;
import org.mybatis.spring.filter.customfilter.CustomTypeFilter;
import org.mybatis.spring.filter.customfilter.ExcludeMaker;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

public class AppConfig {

  @MapperScan(basePackages = "org.mybatis.spring.filter.datasource", excludeFilters = {
      @ComponentScan.Filter(type = FilterType.CUSTOM, classes = CustomTypeFilter.class) })
  public static class CustomFilterConfig {

  }

  @MapperScan(basePackages = "org.mybatis.spring.filter.datasource", excludeFilters = {
      @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = AnnoTypeFilter.class) })
  public static class AnnoFilterConfig {

  }

  @MapperScan(basePackages = "org.mybatis.spring.filter.datasource", excludeFilters = {
      @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ExcludeMaker.class) })
  public static class AssignableFilterConfig {

  }

  @MapperScan(basePackages = "org.mybatis.spring.filter.datasource", excludeFilters = {
      @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org\\.mybatis\\.spring\\.filter\\.datasource\\.datasource1\\..*") })
  public static class RegexFilterConfig {

  }

  @MapperScan(basePackages = "org.mybatis.spring.filter.datasource", excludeFilters = {
      @ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = "*..DataSource1Mapper") })
  public static class AspectJFilterConfig {

  }

  @MapperScan(basePackages = "org.mybatis.spring.filter.datasource", excludeFilters = {
      @ComponentScan.Filter(type = FilterType.CUSTOM, classes = CustomTypeFilter.class),
      @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = AnnoTypeFilter.class) })
  public static class CombinedFilterConfig {

  }

  @MapperScan(basePackages = "org.mybatis.spring.filter.datasource", excludeFilters = {
      @ComponentScan.Filter(type = FilterType.REGEX, pattern = {
          "org\\.mybatis\\.spring\\.filter\\.datasource\\.datasource1\\..*",
          "org\\.mybatis\\.spring\\.filter\\.datasource\\.datasource2\\..*" }) })
  public static class MultiPatternRegexFilterConfig {

  }

  @MapperScan(basePackages = "org.mybatis.spring.filter.datasource", excludeFilters = {
      @ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = { "*..DataSource1Mapper", "*..DataSource2Mapper" }) })
  public static class MultiPatternAspectJFilterConfig {

  }

  @MapperScan(basePackages = "org.mybatis.spring.filter.datasource", excludeFilters = {
      @ComponentScan.Filter(type = FilterType.ANNOTATION, value = ExcludeMaker.class) })
  public static class InvalidFilterTypeConfig {

  }

  @MapperScan(basePackages = "org.mybatis.spring.filter.datasource", excludeFilters = {
      @ComponentScan.Filter(type = FilterType.ANNOTATION, pattern = {
          "org\\.mybatis\\.spring\\.filter\\.datasource\\.datasource1\\..*" }) })
  public static class AnnoTypeWithPatternPropertyConfig {

  }

  @MapperScan(basePackages = "org.mybatis.spring.filter.datasource", excludeFilters = {
      @ComponentScan.Filter(type = FilterType.REGEX, value = AnnoTypeFilter.class) })
  public static class RegexTypeWithClassesPropertyConfig {

  }
}
