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
package org.mybatis.spring.config;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A {#code BeanDefinitionParser} that handles the element scan of the MyBatis. namespace
 *
 * @author Lishu Luo
 * @author Eduardo Macarron
 *
 * @since 1.2.0
 *
 * @see MapperFactoryBean
 * @see ClassPathMapperScanner
 * @see MapperScannerConfigurer
 */
public class MapperScannerBeanDefinitionParser extends AbstractBeanDefinitionParser {

  private static final String ATTRIBUTE_BASE_PACKAGE = "base-package";
  private static final String ATTRIBUTE_ANNOTATION = "annotation";
  private static final String ATTRIBUTE_MARKER_INTERFACE = "marker-interface";
  private static final String ATTRIBUTE_NAME_GENERATOR = "name-generator";
  private static final String ATTRIBUTE_TEMPLATE_REF = "template-ref";
  private static final String ATTRIBUTE_FACTORY_REF = "factory-ref";
  private static final String ATTRIBUTE_MAPPER_FACTORY_BEAN_CLASS = "mapper-factory-bean-class";
  private static final String ATTRIBUTE_LAZY_INITIALIZATION = "lazy-initialization";
  private static final String ATTRIBUTE_DEFAULT_SCOPE = "default-scope";
  private static final String ATTRIBUTE_PROCESS_PROPERTY_PLACEHOLDERS = "process-property-placeholders";
  private static final String ATTRIBUTE_EXCLUDE_FILTER = "exclude-filter";

  @Override
  protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);

    ClassLoader classLoader = ClassUtils.getDefaultClassLoader();

    String processPropertyPlaceHolders = element.getAttribute(ATTRIBUTE_PROCESS_PROPERTY_PLACEHOLDERS);
    builder.addPropertyValue("processPropertyPlaceHolders",
        !StringUtils.hasText(processPropertyPlaceHolders) || Boolean.parseBoolean(processPropertyPlaceHolders));
    try {
      String annotationClassName = element.getAttribute(ATTRIBUTE_ANNOTATION);
      if (StringUtils.hasText(annotationClassName)) {
        @SuppressWarnings("unchecked")
        Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) classLoader
            .loadClass(annotationClassName);
        builder.addPropertyValue("annotationClass", annotationClass);
      }
      String markerInterfaceClassName = element.getAttribute(ATTRIBUTE_MARKER_INTERFACE);
      if (StringUtils.hasText(markerInterfaceClassName)) {
        Class<?> markerInterface = classLoader.loadClass(markerInterfaceClassName);
        builder.addPropertyValue("markerInterface", markerInterface);
      }
      String nameGeneratorClassName = element.getAttribute(ATTRIBUTE_NAME_GENERATOR);
      if (StringUtils.hasText(nameGeneratorClassName)) {
        Class<?> nameGeneratorClass = classLoader.loadClass(nameGeneratorClassName);
        BeanNameGenerator nameGenerator = BeanUtils.instantiateClass(nameGeneratorClass, BeanNameGenerator.class);
        builder.addPropertyValue("nameGenerator", nameGenerator);
      }
      String mapperFactoryBeanClassName = element.getAttribute(ATTRIBUTE_MAPPER_FACTORY_BEAN_CLASS);
      if (StringUtils.hasText(mapperFactoryBeanClassName)) {
        @SuppressWarnings("unchecked")
        Class<? extends MapperFactoryBean> mapperFactoryBeanClass = (Class<? extends MapperFactoryBean>) classLoader
            .loadClass(mapperFactoryBeanClassName);
        builder.addPropertyValue("mapperFactoryBeanClass", mapperFactoryBeanClass);
      }

      // parse raw exclude-filter in <mybatis:scan>
      List<Map<String, String>> rawExcludeFilters = parseScanTypeFilters(element, parserContext);
      if (!rawExcludeFilters.isEmpty()) {
        builder.addPropertyValue("rawExcludeFilters", rawExcludeFilters);
      }

    } catch (Exception ex) {
      XmlReaderContext readerContext = parserContext.getReaderContext();
      readerContext.error(ex.getMessage(), readerContext.extractSource(element), ex.getCause());
    }

    builder.addPropertyValue("sqlSessionTemplateBeanName", element.getAttribute(ATTRIBUTE_TEMPLATE_REF));
    builder.addPropertyValue("sqlSessionFactoryBeanName", element.getAttribute(ATTRIBUTE_FACTORY_REF));
    builder.addPropertyValue("lazyInitialization", element.getAttribute(ATTRIBUTE_LAZY_INITIALIZATION));
    builder.addPropertyValue("defaultScope", element.getAttribute(ATTRIBUTE_DEFAULT_SCOPE));
    builder.addPropertyValue("basePackage", element.getAttribute(ATTRIBUTE_BASE_PACKAGE));

    // for spring-native
    builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

    return builder.getBeanDefinition();
  }

  private List<Map<String, String>> parseScanTypeFilters(Element element, ParserContext parserContext) {
    List<Map<String, String>> typeFilters = new ArrayList<>();
    NodeList nodeList = element.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      if (Node.ELEMENT_NODE == node.getNodeType()) {
        String localName = parserContext.getDelegate().getLocalName(node);
        if (ATTRIBUTE_EXCLUDE_FILTER.equals(localName)) {
          Map<String, String> filter = new HashMap<>(16);
          filter.put("type", ((Element) node).getAttribute("type"));
          filter.put("expression", ((Element) node).getAttribute("expression"));
          typeFilters.add(filter);
        }
      }
    }
    return typeFilters;
  }

  @Override
  protected boolean shouldGenerateIdAsFallback() {
    return true;
  }

}
