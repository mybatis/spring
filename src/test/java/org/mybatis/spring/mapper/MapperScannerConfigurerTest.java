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
package org.mybatis.spring.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.mockrunner.mock.jdbc.MockDataSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.child.MapperChildInterface;
import org.mybatis.spring.type.DummyMapperFactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.stereotype.Component;

class MapperScannerConfigurerTest {
  private GenericApplicationContext applicationContext;

  @BeforeEach
  void setupContext() {
    applicationContext = new GenericApplicationContext();

    // add the mapper scanner as a bean definition rather than explicitly setting a
    // postProcessor on the context so initialization follows the same code path as reading from
    // an XML config file
    var definition = new GenericBeanDefinition();
    definition.setBeanClass(MapperScannerConfigurer.class);
    definition.getPropertyValues().add("basePackage", "org.mybatis.spring.mapper");
    applicationContext.registerBeanDefinition("mapperScanner", definition);
    applicationContext.getBeanFactory().registerScope("thread", new SimpleThreadScope());

    setupSqlSessionFactory("sqlSessionFactory");

    // assume support for autowiring fields is added by MapperScannerConfigurer via
    // org.springframework.context.annotation.ClassPathBeanDefinitionScanner.includeAnnotationConfig
  }

  private void startContext() {
    applicationContext.refresh();
    applicationContext.start();

    // this will throw an exception if the beans cannot be found
    applicationContext.getBean("sqlSessionFactory");
  }

  @AfterEach
  void assertNoMapperClass() {
    try {
      // concrete classes should always be ignored by MapperScannerPostProcessor
      assertBeanNotLoaded("mapperClass");

      // no method interfaces should be ignored too
      assertBeanNotLoaded("package-info");
      // assertBeanNotLoaded("annotatedMapperZeroMethods"); // as of 1.1.0 mappers with no methods are loaded
    } finally {
      applicationContext.close();
    }
  }

  @Test
  void testInterfaceScan() {
    startContext();

    var sqlSessionFactory = applicationContext.getBean(SqlSessionFactory.class);

    assertEquals(5, sqlSessionFactory.getConfiguration().getMapperRegistry().getMappers().size());

    // all interfaces with methods should be loaded
    applicationContext.getBean("mapperInterface");
    applicationContext.getBean("mapperSubinterface");
    applicationContext.getBean("mapperChildInterface");
    applicationContext.getBean("annotatedMapper");
    applicationContext.getBean("scopedProxyMapper");
    applicationContext.getBean("scopedTarget.scopedProxyMapper");

    assertThat(Stream.of(applicationContext.getBeanDefinitionNames()).filter(x -> x.startsWith("scopedTarget")))
        .hasSize(1);
    assertThat(applicationContext.getBeanDefinition("mapperInterface").getPropertyValues().get("mapperInterface"))
        .isEqualTo(MapperInterface.class);
    assertThat(applicationContext.getBeanDefinition("mapperSubinterface").getPropertyValues().get("mapperInterface"))
        .isEqualTo(MapperSubinterface.class);
    assertThat(applicationContext.getBeanDefinition("mapperChildInterface").getPropertyValues().get("mapperInterface"))
        .isEqualTo(MapperChildInterface.class);
    assertThat(applicationContext.getBeanDefinition("annotatedMapper").getPropertyValues().get("mapperInterface"))
        .isEqualTo(AnnotatedMapper.class);
    assertThat(applicationContext.getBeanDefinition("scopedTarget.scopedProxyMapper").getPropertyValues()
        .get("mapperInterface")).isEqualTo(ScopedProxyMapper.class);
  }

  @Test
  void testNameGenerator() {
    var definition = new GenericBeanDefinition();
    definition.setBeanClass(BeanNameGenerator.class);
    applicationContext.registerBeanDefinition("beanNameGenerator", definition);

    applicationContext.getBeanDefinition("mapperScanner").getPropertyValues().add("nameGenerator",
        new RuntimeBeanReference("beanNameGenerator"));

    startContext();

    // only child inferfaces should be loaded and named with its class name
    applicationContext.getBean(MapperInterface.class.getName());
    applicationContext.getBean(MapperSubinterface.class.getName());
    applicationContext.getBean(MapperChildInterface.class.getName());
    applicationContext.getBean(AnnotatedMapper.class.getName());
  }

  @Test
  void testMarkerInterfaceScan() {
    applicationContext.getBeanDefinition("mapperScanner").getPropertyValues().add("markerInterface",
        MapperInterface.class);

    startContext();

    // only child inferfaces should be loaded
    applicationContext.getBean("mapperSubinterface");
    applicationContext.getBean("mapperChildInterface");

    assertBeanNotLoaded("mapperInterface");
    assertBeanNotLoaded("annotatedMapper");
  }

  @Test
  void testAnnotationScan() {
    applicationContext.getBeanDefinition("mapperScanner").getPropertyValues().add("annotationClass", Component.class);

    startContext();

    // only annotated mappers should be loaded
    applicationContext.getBean("annotatedMapper");
    applicationContext.getBean("mapperChildInterface");

    assertBeanNotLoaded("mapperInterface");
    assertBeanNotLoaded("mapperSubinterface");
  }

  @Test
  void testMarkerInterfaceAndAnnotationScan() {
    applicationContext.getBeanDefinition("mapperScanner").getPropertyValues().add("markerInterface",
        MapperInterface.class);
    applicationContext.getBeanDefinition("mapperScanner").getPropertyValues().add("annotationClass", Component.class);

    startContext();

    // everything should be loaded but the marker interface
    applicationContext.getBean("annotatedMapper");
    applicationContext.getBean("mapperSubinterface");
    applicationContext.getBean("mapperChildInterface");

    assertBeanNotLoaded("mapperInterface");
  }

  @Test
  void testScopedProxyMapperScan() {
    applicationContext.getBeanDefinition("mapperScanner").getPropertyValues().add("annotationClass", Mapper.class);

    startContext();
    {
      var definition = applicationContext.getBeanDefinition("scopedProxyMapper");
      assertThat(definition.getBeanClassName()).isEqualTo("org.springframework.aop.scope.ScopedProxyFactoryBean");
      assertThat(definition.getScope()).isEqualTo("");
    }
    {
      var definition = applicationContext.getBeanDefinition("scopedTarget.scopedProxyMapper");
      assertThat(definition.getBeanClassName()).isEqualTo("org.mybatis.spring.mapper.MapperFactoryBean");
      assertThat(definition.getScope()).isEqualTo("thread");
    }
    {
      var mapper = applicationContext.getBean(ScopedProxyMapper.class);
      assertThat(mapper.test()).isEqualTo("test");
    }
    {
      var mapper = applicationContext.getBean("scopedTarget.scopedProxyMapper", ScopedProxyMapper.class);
      assertThat(mapper.test()).isEqualTo("test");
    }
    {
      var mapper = applicationContext.getBean("scopedProxyMapper", ScopedProxyMapper.class);
      assertThat(mapper.test()).isEqualTo("test");
    }

    var sqlSessionFactory = applicationContext.getBean(SqlSessionFactory.class);
    assertEquals(1, sqlSessionFactory.getConfiguration().getMapperRegistry().getMappers().size());
  }

  @Test
  void testScopedProxyMapperScanByDefault() {
    applicationContext.getBeanDefinition("mapperScanner").getPropertyValues().add("defaultScope", "thread");

    startContext();

    List<String> scopedProxyTargetBeans = Stream.of(applicationContext.getBeanDefinitionNames())
        .filter(x -> x.startsWith("scopedTarget")).collect(Collectors.toList());
    assertThat(scopedProxyTargetBeans).hasSize(6).contains("scopedTarget.scopedProxyMapper",
        "scopedTarget.annotatedMapper", "scopedTarget.annotatedMapperZeroMethods", "scopedTarget.mapperInterface",
        "scopedTarget.mapperSubinterface", "scopedTarget.mapperChildInterface");

    for (String scopedProxyTargetBean : scopedProxyTargetBeans) {
      {
        var definition = applicationContext.getBeanDefinition(scopedProxyTargetBean);
        assertThat(definition.getBeanClassName()).isEqualTo("org.mybatis.spring.mapper.MapperFactoryBean");
        assertThat(definition.getScope()).isEqualTo("thread");
      }
      {
        var definition = applicationContext.getBeanDefinition(scopedProxyTargetBean.substring(13));
        assertThat(definition.getBeanClassName()).isEqualTo("org.springframework.aop.scope.ScopedProxyFactoryBean");
        assertThat(definition.getScope()).isEqualTo("");
      }
    }
    {
      var mapper = applicationContext.getBean(ScopedProxyMapper.class);
      assertThat(mapper.test()).isEqualTo("test");
    }
    {
      var mapper = applicationContext.getBean(AnnotatedMapper.class);
      assertThat(mapper.test()).isEqualTo("main");
    }

    var sqlSessionFactory = applicationContext.getBean(SqlSessionFactory.class);
    assertEquals(2, sqlSessionFactory.getConfiguration().getMapperRegistry().getMappers().size());
  }

  @Test
  void testScanWithExplicitSqlSessionFactory() {
    setupSqlSessionFactory("sqlSessionFactory2");

    applicationContext.getBeanDefinition("mapperScanner").getPropertyValues().add("sqlSessionFactoryBeanName",
        "sqlSessionFactory2");

    startContext();

    // all interfaces with methods should be loaded
    applicationContext.getBean("mapperInterface");
    applicationContext.getBean("mapperSubinterface");
    applicationContext.getBean("mapperChildInterface");
    applicationContext.getBean("annotatedMapper");
  }

  @Test
  void testScanWithExplicitSqlSessionTemplate() {
    var definition = new GenericBeanDefinition();
    definition.setBeanClass(SqlSessionTemplate.class);
    var constructorArgs = new ConstructorArgumentValues();
    constructorArgs.addGenericArgumentValue(new RuntimeBeanReference("sqlSessionFactory"));
    definition.setConstructorArgumentValues(constructorArgs);
    applicationContext.registerBeanDefinition("sqlSessionTemplate", definition);

    applicationContext.getBeanDefinition("mapperScanner").getPropertyValues().add("sqlSessionTemplateBeanName",
        "sqlSessionTemplate");

    startContext();

    // all interfaces with methods should be loaded
    applicationContext.getBean("mapperInterface");
    applicationContext.getBean("mapperSubinterface");
    applicationContext.getBean("mapperChildInterface");
    applicationContext.getBean("annotatedMapper");
  }

  @Test
  void testScanWithExplicitSqlSessionFactoryViaPlaceholder() {
    setupSqlSessionFactory("sqlSessionFactory2");

    // use a property placeholder for the session factory name
    applicationContext.getBeanDefinition("mapperScanner").getPropertyValues().add("sqlSessionFactoryBeanName",
        "${sqlSessionFactoryBeanNameProperty}");

    var props = new java.util.Properties();
    props.put("sqlSessionFactoryBeanNameProperty", "sqlSessionFactory2");

    var propertyDefinition = new GenericBeanDefinition();
    propertyDefinition.setBeanClass(PropertySourcesPlaceholderConfigurer.class);
    propertyDefinition.getPropertyValues().add("properties", props);

    applicationContext.registerBeanDefinition("propertiesPlaceholder", propertyDefinition);

    startContext();

    // all interfaces with methods should be loaded
    applicationContext.getBean("mapperInterface");
    applicationContext.getBean("mapperSubinterface");
    applicationContext.getBean("mapperChildInterface");
    applicationContext.getBean("annotatedMapper");
  }

  @Test
  void testScanWithNameConflict() {
    var definition = new GenericBeanDefinition();
    definition.setBeanClass(Object.class);
    applicationContext.registerBeanDefinition("mapperInterface", definition);

    startContext();

    assertThat(applicationContext.getBean("mapperInterface").getClass())
        .as("scanner should not overwrite existing bean definition").isSameAs(Object.class);
  }

  @Test
  void testScanWithPropertyPlaceholders() {
    var definition = (GenericBeanDefinition) applicationContext.getBeanDefinition("mapperScanner");

    // use a property placeholder for basePackage
    definition.getPropertyValues().removePropertyValue("basePackage");
    definition.getPropertyValues().add("basePackage", "${basePackageProperty}");
    definition.getPropertyValues().add("processPropertyPlaceHolders", true);
    // for lazy initialization
    definition.getPropertyValues().add("lazyInitialization", "${mybatis.lazy-initialization:false}");

    // also use a property placeholder for an SqlSessionFactory property
    // to make sure the configLocation was setup correctly and MapperScanner did not change
    // regular property placeholder substitution
    definition = (GenericBeanDefinition) applicationContext.getBeanDefinition("sqlSessionFactory");
    definition.getPropertyValues().removePropertyValue("configLocation");
    definition.getPropertyValues().add("configLocation", "${configLocationProperty}");

    var props = new java.util.Properties();
    props.put("basePackageProperty", "org.mybatis.spring.mapper");
    props.put("configLocationProperty", "classpath:org/mybatis/spring/mybatis-config.xml");
    props.put("mybatis.lazy-initialization", "true");

    var propertyDefinition = new GenericBeanDefinition();
    propertyDefinition.setBeanClass(PropertySourcesPlaceholderConfigurer.class);
    propertyDefinition.getPropertyValues().add("properties", props);

    applicationContext.registerBeanDefinition("propertiesPlaceholder", propertyDefinition);

    startContext();

    var sqlSessionFactory = applicationContext.getBean(SqlSessionFactory.class);
    System.out.println(sqlSessionFactory.getConfiguration().getMapperRegistry().getMappers());
    assertEquals(1, sqlSessionFactory.getConfiguration().getMapperRegistry().getMappers().size());

    // all interfaces with methods should be loaded
    applicationContext.getBean("mapperInterface");
    applicationContext.getBean("mapperSubinterface");
    applicationContext.getBean("mapperChildInterface");
    applicationContext.getBean("annotatedMapper");

    assertEquals(5, sqlSessionFactory.getConfiguration().getMapperRegistry().getMappers().size());

    // make sure the configLocation was setup correctly
    // mybatis-config.xml changes the executor from the default SIMPLE type
    var sessionFactory = (SqlSessionFactory) applicationContext.getBean("sqlSessionFactory");
    assertThat(sessionFactory.getConfiguration().getDefaultExecutorType()).isSameAs(ExecutorType.REUSE);
  }

  @Test
  void testScanWithMapperFactoryBeanClass() {
    DummyMapperFactoryBean.clear();
    applicationContext.getBeanDefinition("mapperScanner").getPropertyValues().add("mapperFactoryBeanClass",
        DummyMapperFactoryBean.class);

    startContext();

    applicationContext.getBean("mapperInterface");
    applicationContext.getBean("mapperSubinterface");
    applicationContext.getBean("mapperChildInterface");
    applicationContext.getBean("annotatedMapper");

    assertTrue(DummyMapperFactoryBean.getMapperCount() > 0);
  }

  @Test
  void testMapperBeanAttribute() {
    startContext();

    assertThat(applicationContext.getBeanDefinition("annotatedMapper")
        .getAttribute(ClassPathMapperScanner.FACTORY_BEAN_OBJECT_TYPE)).isEqualTo(AnnotatedMapper.class);
  }

  @Test
  void testMapperBeanOnConditionalProperties() {
    var propertySources = applicationContext.getEnvironment().getPropertySources();
    propertySources.addLast(new MockPropertySource().withProperty("mapper.condition", "true"));

    startContext();

    assertThat(applicationContext.getBeanDefinition("annotatedMapperOnPropertyCondition")
        .getAttribute(ClassPathMapperScanner.FACTORY_BEAN_OBJECT_TYPE))
            .isEqualTo(AnnotatedMapperOnPropertyCondition.class);
  }

  private void setupSqlSessionFactory(String name) {
    var definition = new GenericBeanDefinition();
    definition.setBeanClass(SqlSessionFactoryBean.class);
    definition.getPropertyValues().add("dataSource", new MockDataSource());
    applicationContext.registerBeanDefinition(name, definition);
  }

  private void assertBeanNotLoaded(String name) {
    try {
      applicationContext.getBean(name);
      fail("Spring bean should not be defined for class " + name);
    } catch (NoSuchBeanDefinitionException nsbde) {
      // success
    }
  }

  public static class BeanNameGenerator implements org.springframework.beans.factory.support.BeanNameGenerator {

    @Override
    public String generateBeanName(BeanDefinition beanDefinition, BeanDefinitionRegistry definitionRegistry) {
      return beanDefinition.getBeanClassName();
    }

  }

}
