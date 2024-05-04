import React from 'react';
import { useSnackbar } from 'notistack';
import { useIntl } from 'react-intl';


import { useEvents } from './index';


export const EventNotifications: React.FC<{ }> = (props) => {
  const { enqueueSnackbar } = useSnackbar();
  const events = useEvents();
  const intl = useIntl();
  const event = events.event;


  React.useEffect(() => {
    if(!event) {
      return;
    }

    if(event.type === 'EVENT_AM_UPDATE') {
      const msg = intl.formatMessage({ id: 'events.am.update' });
      enqueueSnackbar(msg, { variant: 'info' });
    }
  }, [event]);  

  return (<></>);
}
