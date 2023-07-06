package org.mybatis.spring.scan.filter.customfilter;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface AnnoTypeFilter {
}
