import React from 'react';
import { Box, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import Burger from 'components-burger';
import Fields from './RoleCreateFields';
import RoleCreateTabNavLoader from './RoleCreateTabNavigation';
import Context from 'context';
import { StyledDialogLarge } from '../Dialogs';


const Left: React.FC<{}> = () => {
  return (
    <>
      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='permissions.role.name' /></Typography>
        <Fields.RoleName />
      </Burger.Section>

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='permissions.role.description' /></Typography>
        <Fields.RoleDescription />
      </Burger.Section>

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='permissions.role.roleParentalOverview' /></Typography>
        <Fields.RoleParentOverview />
      </Burger.Section>

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='permissions.role.rolePermissionsOverview' /></Typography>
        <Fields.RolePermissionsOverview />
      </Burger.Section>

      <Burger.Section>
        <Typography fontWeight='bold'><FormattedMessage id='permissions.role.roleMembersOverview' /></Typography>
        <Fields.RoleMembersOverview />
      </Burger.Section>


      <Box sx={{ px: .5 }}>
      </Box>
    </>)
}

const Right: React.FC<{}> = () => {
  return (<RoleCreateTabNavLoader />)
}

const Header: React.FC<{ onClose: () => void }> = ({ onClose }) => {

  return (
    <Box display='flex' alignItems='center'>
      <Typography variant='h4'><FormattedMessage id='permissions.role.create' /></Typography>
      <Box flexGrow={1} />
      <Fields.CloseDialogButton onClose={onClose} />
    </Box>
  )
}

const Footer: React.FC<{ onClose: () => void, onCloseCreate: () => void }> = ({ onClose, onCloseCreate }) => {

  return (
    <>
      <Burger.SecondaryButton label='buttons.cancel' onClick={onClose} />
      <Burger.PrimaryButton label='buttons.accept' onClick={onCloseCreate} />
    </>
  )
}

const RoleCreateDialog: React.FC<{ open: boolean, onClose: () => void }> = ({ open, onClose }) => {
  const permissions = Context.usePermissions();

  function handleCloseCreate() { //TODO
    permissions.reload().then(() => {
      onClose();
    });
  }

  return (
    <StyledDialogLarge
      open={open}
      onClose={onClose}
      header={<Header onClose={onClose} />}
      footer={<Footer onClose={onClose} onCloseCreate={handleCloseCreate} />}
      left={<Left />}
      right={<Right />}
    />
  )
}

export default RoleCreateDialog;