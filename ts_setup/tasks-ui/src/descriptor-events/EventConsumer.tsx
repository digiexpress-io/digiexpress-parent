import React from 'react';

import { useAm } from 'descriptor-access-mgmt';
import { useEvents } from './index';
import { useTasks } from 'descriptor-task';
import { useProfileReload } from 'descriptor-backend';


export const EventConsumer: React.FC<{ }> = (props) => {
  const profileReload = useProfileReload();
  const events = useEvents();
  const am = useAm();
  const tasks = useTasks();
  const event = events.event;


  React.useEffect(() => {
    if(!event) {
      return;
    }

    if(event.type === 'EVENT_AM_UPDATE') {
      am.reload();
      profileReload();
      tasks.reload();
      events.replay();
    }
  }, [event]);  

  return (<></>);
}

