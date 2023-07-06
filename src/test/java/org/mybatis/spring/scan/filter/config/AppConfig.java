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

