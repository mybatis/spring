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
package org.mybatis.spring.scan.filter.config;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.scan.filter.customfilter.AnnoTypeFilter;
import org.mybatis.spring.scan.filter.customfilter.ExcludeMaker;
import org.mybatis.spring.scan.filter.customfilter.CustomTypeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;


public class AppConfig {


  @MapperScan(basePackages = "org.mybatis.spring.scan.filter.datasource",
    excludeFilters = {@ComponentScan.Filter(type = FilterType.CUSTOM, classes = CustomTypeFilter.class)})
  public static class CustomFilterConfig {

  }

  @MapperScan(basePackages = "org.mybatis.spring.scan.filter.datasource",
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, classes = AnnoTypeFilter.class)})
  public static class AnnoFilterConfig {

  }

  @MapperScan(basePackages = "org.mybatis.spring.scan.filter.datasource",
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ExcludeMaker.class)})
  public static class AssignableFilterConfig {

  }

  @MapperScan(basePackages = "org.mybatis.spring.scan.filter.datasource",
    excludeFilters = {@ComponentScan.Filter(type = FilterType.REGEX,
      pattern = "org\\.mybatis\\.spring\\.scan\\.filter\\.datasource\\.datasource1\\..*")})
  public static class RegexFilterConfig {

  }

  @MapperScan(basePackages = "org.mybatis.spring.scan.filter.datasource",
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASPECTJ,
      pattern = "*..DataSource1Mapper")})
  public static class AspectJFilterConfig {

  }
}

