package io.digiexpress.eveli.client.api;

import java.util.List;

public interface OrgClient {
  GroupEmailQuery queryGroupEmails();
  
  interface GroupEmailQuery {
    List<String> findAllByGroupName(String groupName); 
  }
}
