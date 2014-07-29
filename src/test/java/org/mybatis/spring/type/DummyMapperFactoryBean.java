package org.mybatis.spring.type;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.mybatis.spring.mapper.MapperFactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

public class DummyMapperFactoryBean<T> extends MapperFactoryBean<T> {

  private static final Logger LOGGER = Logger.getLogger(DummyMapperFactoryBean.class);

  private static final AtomicInteger mapperInstanceCount = new AtomicInteger(0);

  @Override
  protected void checkDaoConfig() {
    super.checkDaoConfig();
    // make something more
    if (isAddToConfig()) {
      LOGGER.debug("register mapper for interface : " + getMapperInterface());

    }
  }

  @Override
  public T getObject() throws Exception {
    MapperFactoryBean<T> mapperFactoryBean = new MapperFactoryBean<T>();
    mapperFactoryBean.setMapperInterface(getMapperInterface());
    mapperFactoryBean.setAddToConfig(isAddToConfig());
    mapperFactoryBean.setSqlSessionFactory(getCustomSessionFactoryForClass(getMapperInterface()));
    T object = mapperFactoryBean.getObject();
    mapperInstanceCount.incrementAndGet();
    return object;
  }



  private SqlSessionFactory getCustomSessionFactoryForClass(Class mapperClass) {
    // can for example read a custom annotation to set a custom sqlSessionFactory

    // just a dummy implementation example
    return (SqlSessionFactory) Proxy.newProxyInstance(
        SqlSessionFactory.class.getClassLoader(),
        new Class[]{SqlSessionFactory.class},
        new InvocationHandler() {
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("getConfiguration".equals(method.getName())) {
              return getSqlSession().getConfiguration();
            }
            // dummy
            return null;
          }
        });
  }

  public static final int getMapperCount(){
    return mapperInstanceCount.get();
  }
}
