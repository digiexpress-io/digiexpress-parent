import React from 'react';

import { Box, Stack, Divider, Typography, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { MyWorkTasks } from './MyWorkTasks';

import client from '@taskclient';
import Styles from '@styles';



type ContentType = 'MyTasks' | 'MyAssignedComments' | 'MyAssignedChecklistItems';


const StyledMyWorkButton: React.FC<{ onClick: () => void, label: string, attentionItem?: React.ReactNode }> = ({ onClick, label, attentionItem }) => {

  return (
    <Box display='flex' flexDirection='column'>
      <Button variant='text' onClick={onClick}>
        <Typography fontWeight='bold'><FormattedMessage id={label} /></Typography>
      </Button>
      <Typography sx={{ ml: 1 }} variant='caption'>{attentionItem}</Typography>
    </Box>)
}

const MyAssignedComments: React.FC = () => {
  return (<>Assigned comments</>);
}

const MyAssignedChecklistItems: React.FC = () => {
  return (<>Assigned checklist items</>);
}

const MyWork: React.FC<{}> = () => {

  const [contentType, setContentType] = React.useState<ContentType>('MyTasks');

  let content;
  if (contentType === 'MyTasks') {
    content = <MyWorkTasks />
  } else if (contentType === 'MyAssignedComments') {
    content = <MyAssignedComments />
  } else {
    content = <MyAssignedChecklistItems />
  }

  return (
    <client.TableProvider>
      <Styles.Layout>
        <Box display='flex'>
          <Stack spacing={3} direction='row'>
            <StyledMyWorkButton onClick={() => setContentType('MyTasks')} label='core.myWork.todo.button' attentionItem={<>Overdue: 3</>} />
            <StyledMyWorkButton onClick={() => setContentType('MyAssignedComments')} label='core.myWork.assignedComments.button' attentionItem={<>New: 1</>} />
            <StyledMyWorkButton onClick={() => setContentType('MyAssignedChecklistItems')} label='core.myWork.assignedChecklistItems.button' />
          </Stack>
        </Box>
        <Divider sx={{ my: 1 }} />
        {content}
      </Styles.Layout>
    </client.TableProvider>);
}

export { MyWork };
