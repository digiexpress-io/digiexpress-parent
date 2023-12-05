import * as React from 'react';
import { Box, Grid, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import RemoveIcon from '@mui/icons-material/Remove';
import { FormattedMessage } from 'react-intl';
import { SingleEvent, SingleEventDiff, AssignTaskEventBody } from 'descriptor-task';
import Burger from 'components-burger';


const DiffContainer: React.FC<{
  eventTitle: string,
  event: SingleEvent,
  children: React.ReactNode | string,
}> = ({ eventTitle, event, children }) => {
  if (!event.body.toCommand.targetDate) {
    return <></>;
  }

  const date = new Date(event.body.toCommand.targetDate);

  return (
    <Grid container>
      <Grid container>
        <Grid item md={9} lg={9}>
          <Typography fontWeight='bolder'><FormattedMessage id={eventTitle} /></Typography>
        </Grid>

        <Grid item md={3} lg={3}>
          <Typography textAlign='right'><Burger.DateTimeFormatter type='dateTime' value={date} /></Typography>
        </Grid>
      </Grid>

      <Grid item md={12} lg={12} mb={1}>
        {children}
      </Grid>
    </Grid>
  );
}


const DiffRemoved: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <Box display='flex' alignItems='center' sx={{ backgroundColor: 'rgb(254, 237, 240)' }}>
    <RemoveIcon sx={{ fontSize: '10pt', mr: 1, color: 'red' }} />
    <Typography>{children}</Typography>
  </Box>
)

const DiffAdded: React.FC<{ children: React.ReactNode | string }> = ({ children }) => (
  <Box display='flex' alignItems='center' sx={{ backgroundColor: 'rgb(230, 255, 237)' }}>
    <AddIcon sx={{ fontSize: '10pt', mr: 1, color: 'green' }} />
    <Typography>{children}</Typography>
  </Box>
)




const TaskEventSimpleDiff: React.FC<{
  eventTitle: string,
  event: SingleEvent,
  fromValue: string[] | string | undefined,
  toValue: string[] | string | undefined
}> = ({ eventTitle, event, fromValue, toValue }) => {
  if (!event.body.toCommand.targetDate) {
    return <></>;
  }
  const getChanges = (fromValues: string[] | string | undefined, toValues: string[] | string | undefined): { added: string[], removed: string[] } => {
    const fromArray = Array.isArray(fromValues) ? fromValues : (fromValues ? [fromValues] : []);
    const toArray = Array.isArray(toValues) ? toValues : (toValues ? [toValues] : []);

    const added = toArray.filter(value => !fromArray.includes(value));
    const removed = fromArray.filter(value => !toArray.includes(value));

    return { added, removed };
  };

  const { added, removed } = getChanges(fromValue, toValue);

  return (
    <DiffContainer event={event} eventTitle={eventTitle}>
      {added.map((value, index) => (<DiffAdded key={index}>{value}</DiffAdded>))}
      {removed.map((value, index) => (<DiffRemoved key={index}>{value}</DiffRemoved>))}
    </DiffContainer >
  );
}



const AssignTaskDiff: React.FC<{
  body: AssignTaskEventBody,
}> = ({ body }) => {
  const diff = body.diff;
  return (
    <>
      {diff.filter(({ operation }) => operation === 'ADDED').map((diff, index) => (<DiffAdded key={index}>{diff.value}</DiffAdded>))}
      {diff.filter(({ operation }) => operation === 'REMOVED').map((diff, index) => (<DiffRemoved key={index}>{diff.value}</DiffRemoved>))}
    </>
  );
}



const TaskEventDiff: React.FC<{
  eventTitle: string,
  event: SingleEvent,
  fromValue: string[] | string | undefined,
  toValue: string[] | string | undefined
}> = (props) => {
  if (!props.event.body.toCommand.targetDate) {
    return <></>;
  }

  if (props.event.body.diff.length === 0) {
    return <TaskEventSimpleDiff {...props} />
  }
  const { body } = props.event;

  let contents: React.ReactNode = <></>;
  if (body.commandType === 'AssignTask') {
    contents = <AssignTaskDiff body={body} />
  } else {
    const diff: SingleEventDiff<any>[] = props.event.body.diff;
    contents = (<>
      {diff.filter(({ operation }) => operation === 'ADDED').map((diff, index) => (<DiffAdded key={index}>{diff.value}</DiffAdded>))}
      {diff.filter(({ operation }) => operation === 'REMOVED').map((diff, index) => (<DiffRemoved key={index}>{diff.value}</DiffRemoved>))}
    </>)
  }

  return (<DiffContainer event={props.event} eventTitle={props.eventTitle}>{contents}</DiffContainer>);
}

export { TaskEventDiff }; 