package org.mybatis.spring.scan.filter.datasource.commonsource;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.scan.filter.customfilter.AnnoTypeFilter;

@AnnoTypeFilter
public interface AnnoExcludeMapper extends Mapper {
}
