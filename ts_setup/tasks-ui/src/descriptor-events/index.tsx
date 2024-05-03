import React from 'react';
import { useBackend } from 'descriptor-backend';


export interface EventsContextType {
  ws: WebSocket;
}

/*
this._webSocket = webSocket ? webSocket : ;
if(webSocket) {

  webSocket.addEventListener("open", (event) => {
    console.log('Connected to server');
    webSocket.send("Hello Server!");
  });

  webSocket.addEventListener("message", (event) => {
    console.log("Message from server ", event.data);
  });
}
*/

export const EventsContext = React.createContext<EventsContextType>({} as any);

export const EventsProvider: React.FC<{ children: React.ReactNode }> = (props) => {
  const { config } = useBackend();
  const ws: WebSocket = React.useMemo(() => {
    const webSocket = new WebSocket(config.urls.EVENTS + "events");

    webSocket.addEventListener("open", (event) => {
      console.log('Connected to server');
      webSocket.send("Hello Server!");
    });
  
    webSocket.addEventListener("message", (event) => {
      console.log("Message from server ", event.data);
    });

    return webSocket;
  }, [config]);

  const contextValue: EventsContextType = React.useMemo(() => ({ws}), [ws]);
  
  return (<EventsContext.Provider value={contextValue}>
    {props.children}
  </EventsContext.Provider>);
}

export const useEvents = () => {
  return React.useContext(EventsContext);
}