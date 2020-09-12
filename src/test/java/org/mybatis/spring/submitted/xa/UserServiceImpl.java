/**
 * Copyright 2010-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mybatis.spring.submitted.xa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

  @Autowired
  private UserMapper userMapperPrimary;
  @Autowired
  private UserMapper userMapperReplica;

  @Override
  @Transactional
  public void saveWithNoFailure(User user) {
    userMapperPrimary.save(user);
    userMapperReplica.save(user);
  }

  @Override
  @Transactional
  public void saveWithFailure(User user) {
    userMapperPrimary.save(user);
    userMapperReplica.save(user);
    throw new RuntimeException("failed!");
  }

  @Override
  public boolean checkUserExists(int id) {
    if (userMapperPrimary.select(id) != null)
      return true;
    if (userMapperReplica.select(id) != null)
      return true;
    return false;
  }
}
