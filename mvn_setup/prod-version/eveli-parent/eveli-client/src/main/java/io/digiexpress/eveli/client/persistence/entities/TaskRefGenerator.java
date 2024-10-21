package io.digiexpress.eveli.client.persistence.entities;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.text.SimpleDateFormat;
import java.util.Date;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;

import jakarta.persistence.FlushModeType;

public class TaskRefGenerator implements org.hibernate.tuple.ValueGenerator<String> {
  

 public static final String DATE_NUMBER_SEPARATOR_DEFAULT = "-";
  
 private static final String NEXTVAL_QUERY = "select nextval('TASKREF_SEQ')";
 private final SimpleDateFormat dataFormat = new SimpleDateFormat("yyyyMM");

 @Override
 public String generateValue(Session session, Object owner) {
     Date now = new Date();
     NativeQuery<Number> nextvalQuery = session.createNativeQuery(NEXTVAL_QUERY);
     Number nextvalValue = nextvalQuery.setFlushMode(FlushModeType.COMMIT).uniqueResult();
     return dataFormat.format(now) + DATE_NUMBER_SEPARATOR_DEFAULT + nextvalValue.longValue();
 }

}
