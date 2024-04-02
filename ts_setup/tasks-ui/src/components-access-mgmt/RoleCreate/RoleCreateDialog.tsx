import React from 'react';
import { Box, Typography, Tabs, Tab } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import Burger from 'components-burger';
import Fields from './RoleCreateFields';

import { RoleCreateProvider, useRoleCreateTabs, RoleCreateTabTypes } from './RoleCreateContext';


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

const HEADER_MAPPING: RoleCreateTabTypes[] = [
  'role_parent',
  'role_permissions',
  'role_members'
]


const Header: React.FC<{ onClose: () => void }> = ({ onClose }) => {

  const tabbing = useRoleCreateTabs();
  function handleTabValue(_event: React.SyntheticEvent, index: number) {
    const newValue: RoleCreateTabTypes = HEADER_MAPPING[index];
    tabbing.setActiveTab(newValue);
  }

  return (
    <Box display='flex' alignItems='center'>
      <Box width='50%' marginRight={5}>
        <Typography variant='h4'><FormattedMessage id='permissions.role.create' /></Typography>
      </Box>

      <Box width='50%'>
        <Tabs value={HEADER_MAPPING.indexOf(tabbing.activeTab.id)} onChange={handleTabValue}>
          <Tab label={<FormattedMessage id='permissions.createRole.role_parent' />} />
          <Tab label={<FormattedMessage id='permissions.createRole.role_permissions' />} />
          <Tab label={<FormattedMessage id='permissions.createRole.role_members' />} />
        </Tabs>
      </Box>
      <Box flexGrow={1} />
      <Fields.CloseDialogButton onClose={onClose} />
    </Box>
  )

}

const Right: React.FC<{}> = () => {
  const { roles } = Context.usePermissions();
  const tabbing = useRoleCreateTabs();

  if (!roles) {
    return <>no roles here on the right side!</>;
  }

  if (tabbing.activeTab.id === 'role_parent') {
    return <Fields.RoleParent />;
  } else if (tabbing.activeTab.id === 'role_permissions') {
    return <Fields.RolePermissions />
  } else if (tabbing.activeTab.id === 'role_members') {
    return <Fields.RolePrincipals />
  }

  return (<>unknown tab: {tabbing.activeTab.id}</>)
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
    <RoleCreateProvider>
      <StyledDialogLarge
        open={open}
        onClose={onClose}
        header={< Header onClose={onClose} />}
        footer={< Footer onClose={onClose} onCloseCreate={handleCloseCreate} />}
        left={< Left />}
        right={< Right />}
      />
    </RoleCreateProvider>
  )
}

export default RoleCreateDialog;