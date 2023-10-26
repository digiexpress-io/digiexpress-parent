import React from 'react';
import { Box, Stack, Grid, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import StyledFullScreenDialog from '../Dialogs';
import Fields from './CRMFields';
import Context from 'context';
import { TaskDescriptor } from 'taskdescriptor';
import { MenuProvider, useMenu } from './menu-ctx';
import Section from '../Section';
import Burger from '@the-wrench-io/react-burger';

const Left: React.FC<{}> = () => {

  return (
    <Stack spacing={1} direction='column'>
      <Section>
        <Typography fontWeight='bold'><FormattedMessage id='task.title' /></Typography>
        <Fields.Title />
      </Section>

      <Section>
        <Typography fontWeight='bold'><FormattedMessage id='task.description' /></Typography>
        <Fields.Description />
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

      <Grid item md={2} lg={2} sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', pb: 1 }}>
        <Stack spacing={1} direction='column'>
          <Fields.StartDate />
          <Fields.DueDate />
        </Stack>
      </Grid>

      <Grid item md={1} lg={1} sx={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', pb: 1 }}>
        <Fields.CloseDialogButton onClose={onClose} />
      </Grid>
    </Grid >
  )
}

const Footer: React.FC<{ onClick: () => void }> = ({ onClick }) => {
  return (
    <>
      <Burger.SecondaryButton label='core.taskWork.button.cancel' onClick={onClick} />
      <Burger.SecondaryButton label='core.taskWork.button.edit' onClick={onClick} />
      <Burger.SecondaryButton label='core.taskWork.button.reject' onClick={onClick} />
      <Burger.PrimaryButton label='core.taskWork.button.complete' onClick={onClick} />
    </>
  )
}

const CRMDialog: React.FC<{ open: boolean, onClose: () => void, task?: TaskDescriptor }> = (props) => {

  if (!props.open || !props.task) {
    return null;
  }

  return (
    <MenuProvider>
      <Context.EditProvider task={props.task}>
        <StyledFullScreenDialog
          header={<Header onClose={props.onClose} />}
          footer={<Footer onClick={props.onClose} />}
          left={<Left />}
          right={<Right />}
          onClose={props.onClose}
          open={props.open}
          shortHeader
        />
      </Context.EditProvider>
    </MenuProvider>
  );
}

export { CRMDialog };
