package org.mybatis.spring.scan.filter.datasource.commonsource;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.scan.filter.customfilter.ExcludeMaker;

public interface AssignableMapper extends ExcludeMaker,Mapper {
}
