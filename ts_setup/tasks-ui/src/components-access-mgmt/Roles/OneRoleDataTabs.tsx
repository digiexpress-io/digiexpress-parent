import React from 'react';
import { Tab, Tabs, Box } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { RoleDetails } from './RoleDetails';
import { RolePermissions } from './RolePermissions';
import { RoleMembers } from './RoleMembers';

import { Role } from 'descriptor-access-mgmt';


const TabContent: React.FC<{ selected: number, id: number, children: React.ReactNode }> = ({ selected, id, children }) => {

  if (selected !== id) {
    return null;
  }

  return (
    <Box sx={{ p: 2, width: '100%', height: '100%' }}>
      {children}
    </Box>);
}

export const OneRoleDataTabs: React.FC<{ role: Role }> = ({ role }) => {
  const [tabValue, setTabValue] = React.useState(0);

  function handleTabValue(event: React.SyntheticEvent, newValue: number) {
    setTabValue(newValue);
  }

  console.log(role)

  return (
    <Box sx={{ flexGrow: 1, bgcolor: 'background.paper', display: 'flex' }}>
      <Tabs
        orientation="vertical"
        variant="scrollable"
        value={tabValue}
        onChange={handleTabValue}
        sx={{ borderRight: 1, borderColor: 'divider' }}
      >
        <Tab label={<FormattedMessage id='permissions.roles.overview.tab.details' />} />
        <Tab label={<FormattedMessage id='permissions.roles.overview.tab.permissions' />} />
        <Tab label={<FormattedMessage id='permissions.roles.overview.tab.members' />} />
      </Tabs>

      <TabContent selected={tabValue} id={0}>
        <RoleDetails role={role} />
      </TabContent>

      <TabContent selected={tabValue} id={1}>
        <RolePermissions role={role} />
      </TabContent>

      <TabContent selected={tabValue} id={2}>
        <RoleMembers role={role} />
      </TabContent>
    </Box>
  );
}
