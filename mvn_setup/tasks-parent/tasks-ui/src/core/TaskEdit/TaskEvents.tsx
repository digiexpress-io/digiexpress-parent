import React from 'react';
import { ListItem, ListItemText} from '@mui/material';
import Client from '@taskclient';


const CollapsedGroup: React.FC<{event: Client.CollapsedEvent }> = () => {
  return (<>19 items collapsed</>)
}

const SingleGroup: React.FC<{event: Client.SingleEvent }> = ({event}) => {
  
  if(event.body.commandType === "CreateTask") {
    return (<ListItemText primary={event.body.commandType} secondary={event.body.toCommand.targetDate} />)  
  }
  
  return (<ListItemText primary={event.body.commandType} secondary={event.body.toCommand.targetDate} />)
}


const Event: React.FC<{ event: Client.TaskEditEvent }> = ({ event }) => {

  if (event.type === 'SINGLE') {
    return <ListItem><SingleGroup event={event} /></ListItem>
  }
  return <ListItem><CollapsedGroup event={event} /></ListItem>;
}


export default Event;