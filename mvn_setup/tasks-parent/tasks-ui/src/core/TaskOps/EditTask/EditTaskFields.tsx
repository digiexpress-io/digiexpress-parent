import React from 'react';
import { TextField, Typography, Stack, Box, IconButton } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';
import DateRangeOutlinedIcon from '@mui/icons-material/DateRangeOutlined';
import Burger from '@the-wrench-io/react-burger';
import TaskClient from '@taskclient';



const SectionAddButton: React.FC<{}> = () => {
  return (<Burger.PrimaryButton label={'buttons.add'} onClick={() => { }} />)
}


const Section: React.FC<{ children: React.ReactNode, title: string, actions: React.ReactNode }> = ({ children, title, actions }) => {
  return (
    <>
      <Stack direction='row' spacing={1} alignItems='center'>
        <Box sx={{ minWidth: "50%" }}>
          <Typography><FormattedMessage id={title} /></Typography>
        </Box>
        {actions}
      </Stack>
      {children}
    </>);
}





const Title: React.FC<{}> = () => {
  const { state } = TaskClient.useTaskEdit();
  const intl = useIntl();

  return (<TextField
    placeholder={intl.formatMessage({ id: 'core.taskOps.editTask.taskTitle' })}
    InputProps={{ sx: { fontSize: '20pt' } }}
    fullWidth
    value={state.task.title}
  />);
}

const Description: React.FC<{}> = () => {
  const { state } = TaskClient.useTaskEdit();
  const intl = useIntl();

  return (<TextField placeholder={intl.formatMessage({ id: 'core.taskOps.editTask.taskDescription' })} multiline rows={4} maxRows={6} fullWidth
    value={state.task.description} />);
}

const Checklist: React.FC<{}> = () => {

  return (
    <Section title='core.taskOps.editTask.checklists' actions={<SectionAddButton />}>
      checklist content
    </Section>
  )
}

const Attachments: React.FC<{}> = () => {

  return (
    <Section title='core.taskOps.editTask.attachments' actions={<SectionAddButton />}>
      attachments content
    </Section>
  )
}

const Status: React.FC<{}> = () => {
  return (<Box>Status</Box>)
}

const Assignee: React.FC<{}> = () => {
  return (<Box>Assignee</Box>)
}

const Priority: React.FC<{}> = () => {
  return (<Box>Priority</Box>)
}

const Options: React.FC<{}> = () => {
  return (<Box>Options</Box>)
}


const StartDate: React.FC<{ onClick: () => void }> = ({ onClick }) => {

  return (
    <Box textAlign='center'>
      <Typography><FormattedMessage id='core.taskOps.editTask.startDate' /></Typography>
      <IconButton onClick={onClick} color='secondary'><DateRangeOutlinedIcon /></IconButton>
    </Box>);
}

const DueDate: React.FC<{ dueDate: string, onClick: () => void }> = ({ dueDate, onClick }) => {
  return (
    <Box onClick={onClick}>
      <Typography><FormattedMessage id='core.taskOps.editTask.dueDate' /></Typography>
      <Typography variant='caption'>{dueDate}</Typography>
    </Box>)
}



const Fields = { Title, Description, Checklist, Attachments, Status, Assignee, Priority, Options, StartDate, DueDate }
export default Fields;