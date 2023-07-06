package org.mybatis.spring.scan.filter.customfilter;

import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;

public class CustomTypeFilter implements TypeFilter {
  @Override
  public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
    return metadataReader.getClassMetadata().getClassName().contains("datasource2");
  }
}
