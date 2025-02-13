import React from 'react';
import { useBackend } from 'descriptor-backend';
import { BackendEvent } from './event-types';
import { EventNotifications } from './EventNotifications';
import { EventConsumer } from './EventConsumer';


export interface EventsContextType {
  ws: WebSocket;
  event: BackendEvent | undefined;
  replay: () => void;
}

export const EventsContext = React.createContext<EventsContextType>({} as any);

export const EventsProvider: React.FC<{ children: React.ReactNode }> = (props) => {
  const { config } = useBackend();
  const [lastEvent, setLastEvent] = React.useState<BackendEvent>();

  const ws: WebSocket = React.useMemo(() => {
    const webSocket = new WebSocket(config.urls.EVENTS + "events");

    webSocket.addEventListener("open", (event) => {
      console.log('Connected to server');
      webSocket.send("Hello Server!");
    });
  
    webSocket.addEventListener("message", (event) => {
      try {
        const json: string = event.data;
        const backendEvent: BackendEvent = JSON.parse(json);
        setLastEvent(backendEvent);
      } catch(error) {
        console.error("Failed to parse UI event", error)
      }
    });

    return webSocket;
  }, [config, setLastEvent]);

  const contextValue: EventsContextType = React.useMemo(() => {

    return { 
      ws, 
      event: lastEvent, 
      replay: () => setLastEvent(undefined)
    };
  }, [ws, lastEvent, setLastEvent]);
  
  return (<EventsContext.Provider value={contextValue}>
    <>
      <EventNotifications />
      <EventConsumer />
      {props.children}
    </>
  </EventsContext.Provider>);
}

export const useEvents = () => {
  return React.useContext(EventsContext);
}