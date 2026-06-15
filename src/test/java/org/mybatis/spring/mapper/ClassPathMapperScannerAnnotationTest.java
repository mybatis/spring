/*
 * Copyright 2010-2026 the original author or authors.
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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.scancandidate.ScanMapper;
import org.springframework.context.support.GenericApplicationContext;

class ClassPathMapperScannerAnnotationTest {

  /**
   * Annotation types are interfaces at the bytecode level, so they used to pass {@code isCandidateComponent} and were
   * wrongly registered as mapper beans. They must be excluded while regular mapper interfaces in the same package are
   * still scanned. See gh-1032.
   */
  @Test
  void annotationTypeIsNotRegisteredAsMapper() {
    try (var applicationContext = new GenericApplicationContext()) {
      var scanner = new ClassPathMapperScanner(applicationContext, applicationContext.getEnvironment());
      scanner.registerFilters();
      scanner.scan(ScanMapper.class.getPackageName());

      var beanDefinitionNames = List.of(applicationContext.getBeanDefinitionNames());

      // the regular mapper interface is still picked up
      assertThat(beanDefinitionNames).contains("scanMapper");
      // the annotation type living in the same package must not be registered as a mapper
      assertThat(applicationContext.containsBeanDefinition("scanMarkerAnnotation")).isFalse();
    }
  }
}
