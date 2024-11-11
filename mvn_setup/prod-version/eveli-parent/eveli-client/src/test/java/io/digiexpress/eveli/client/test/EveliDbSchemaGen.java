package io.digiexpress.eveli.client.test;

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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.digiexpress.eveli.client.persistence.entities.TaskEntity;



public class EveliDbSchemaGen {

  private final static StandardServiceRegistry PROPS = new StandardServiceRegistryBuilder()
    .applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
    .applySetting("org.hibernate.envers.audit_table_suffix", "_aud")
    .applySetting("org.hibernate.envers.revision_field_name", "rev")
    .applySetting("org.hibernate.envers.revision_type_field_name", "revtype")
    .applySetting("org.hibernate.envers.do_not_audit_optimistic_locking_field", "false")
    .build();
  
  @Test
  @Disabled
  public void test() throws Exception {
    final var metadata = new MetadataSources(PROPS);
    
    for (Class<?> clazz : getClasses(TaskEntity.class.getPackageName())) {
      metadata.addAnnotatedClass(clazz);
    }
    
    new SchemaExport()
        .setDelimiter(";")
        .setFormat(true)
        .execute(
            EnumSet.of(TargetType.STDOUT), 
            SchemaExport.Action.CREATE, 
            metadata.buildMetadata());
  }
  
  
  @SuppressWarnings("rawtypes")
  private List<Class> getClasses(String packageName) throws Exception {
      File directory = null;
      try {
          ClassLoader cld = getClassLoader();
          URL resource = getResource(packageName, cld);
          directory = new File(resource.getFile());
      } catch (NullPointerException ex) {
          throw new ClassNotFoundException(packageName + " (" + directory + ") does not appear to be a valid package");
      }
      return collectClasses(packageName, directory);
  }
  private ClassLoader getClassLoader() throws ClassNotFoundException {
    ClassLoader cld = Thread.currentThread().getContextClassLoader();
    if (cld == null) {
        throw new ClassNotFoundException("Can't get class loader.");
    }
    return cld;
  }
  
  private URL getResource(String packageName, ClassLoader cld) throws ClassNotFoundException {
      String path = packageName.replace('.', '/');
      URL resource = cld.getResource(path);
      if (resource == null) {
          throw new ClassNotFoundException("No resource for " + path);
      }
      return resource;
  }
  
  @SuppressWarnings("rawtypes")
  private List<Class> collectClasses(String packageName, File directory) throws ClassNotFoundException {
    final var classes = new ArrayList<Class>();
    if (directory.exists()) {
      final var files = directory.list();
      for (final var file : files) {
        if (file.endsWith(".class")) {
            // removes the .class extension
            classes.add(Class.forName(packageName + '.' + file.substring(0, file.length() - 6)));
        }
      }
    } else {
      throw new ClassNotFoundException(packageName + " is not a valid package");
    }
    return classes;
  }
}
