package io.digiexpress.eveli.client.api;

import java.util.List;

public interface HdesCommands {
  void execute(String dialobSessionId);
  HdesQuery query();
  
  interface HdesQuery {
    List<String> findFlowNames();
  }
}
