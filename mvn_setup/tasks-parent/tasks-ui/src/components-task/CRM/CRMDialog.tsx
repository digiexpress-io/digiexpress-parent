import React from 'react';
import { Box, Stack, Grid, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import StyledFullScreenDialog from '../Dialogs';
import Fields from './CRMFields';
import Context from 'context';
import { TaskDescriptor } from 'taskdescriptor';
import { MenuProvider, useMenu } from './menu-ctx';
import Section from 'section';
import Burger from 'components-burger';

const Left: React.FC<{}> = () => {

  return (
    <Stack spacing={1} direction='column' pt={1}>
      <Section>
        <Typography fontWeight='bold'><FormattedMessage id='task.title' /></Typography>
        <Fields.Title />
      </Section>

      <Section>
        <Typography fontWeight='bold'><FormattedMessage id='task.description' /></Typography>
        <Fields.Description />
      </Section>

      <Section>
        <Typography fontWeight='bold'><FormattedMessage id='task.startDate' /></Typography>
        <Fields.StartDate />
      </Section>

      <Section>
        <Typography fontWeight='bold'><FormattedMessage id='task.dueDate' /></Typography>
        <Fields.DueDate />
      </Section>

      <Fields.Form />
    </Stack>
  )
}

const Right: React.FC<{}> = () => {
  const { activeTab } = useMenu();

  return (
    <Box>
      {activeTab === 'messages' && <Fields.Messages />}
      {activeTab === 'attachments' && <Fields.Attachments />}
      {activeTab === 'checklists' && <Fields.Checklist />}
    </Box>
  );

}

const Header: React.FC<{ onClose: () => void }> = ({ onClose }) => {

  return (
    <Grid container>
      <Grid item md={6} lg={6}>
        <FormattedMessage id='crmDialog.title' />
      </Grid>
      <Grid item md={3} lg={3}>
        <Fields.Menu />
      </Grid>
      <Box flexGrow={1} />
      <Grid item md={1} lg={1} sx={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', pb: 1 }}>
        <Fields.CloseDialogButton onClose={onClose} />
      </Grid>
    </Grid>
  )
}

const Footer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  return (
    <>
      <Burger.SecondaryButton label='core.taskWork.button.cancel' onClick={() => { }} />
      <Burger.SecondaryButton label='core.taskWork.button.edit' onClick={() => { }} />
      <Burger.SecondaryButton label='core.taskWork.button.reject' onClick={() => { }} />
      <Burger.PrimaryButton label='core.taskWork.button.complete' onClick={onClose} />
    </>
  )
}

const CRMDialog: React.FC<{ open: boolean, onClose: () => void, task?: TaskDescriptor }> = (props) => {
  const tasks = Context.useTasks();

  if (!props.open || !props.task) {
    return null;
  }
  function handleClose() {
    tasks.reload().then(() => props.onClose());
  }

  return (
    <MenuProvider>
      <Context.EditProvider task={props.task}>
        <StyledFullScreenDialog
          header={<Header onClose={props.onClose} />}
          footer={<Footer onClose={handleClose} />}
          left={<Left />}
          right={<Right />}
          onClose={props.onClose}
          open={props.open}
        />
      </Context.EditProvider>
    </MenuProvider>
  );
}

export { CRMDialog };
