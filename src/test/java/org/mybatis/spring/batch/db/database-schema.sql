--    Copyright 2010-2012 The myBatis Team

--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at

--       http://www.apache.org/licenses/LICENSE-2.0

--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.

--    version: $Id: db.sql 2398 2010-08-29 15:16:24Z simone.tripodi $

create table employees (
    id integer not null,
    name varchar(80) not null,
    salary integer not null,
    skill varchar(80) not null
);
