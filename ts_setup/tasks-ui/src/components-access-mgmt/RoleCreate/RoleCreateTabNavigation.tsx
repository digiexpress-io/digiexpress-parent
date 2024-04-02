import React from 'react';
import { Box, Tabs, Tab, Stack } from '@mui/material';
import { FilterByString } from 'components-generic';

import { PermissionsProvider } from '../PermissionsContext';
import Fields from './RoleCreateFields';
import { FormattedMessage } from 'react-intl';
import Context from 'context';


const TabContent: React.FC<{ selected: number, id: number, children: React.ReactNode }> = ({ selected, id, children }) => {
  if (selected !== id) {
    return null;
  }

  return (
    <Box sx={{ p: 2, width: '100%', height: '100%' }}>
      {children}
    </Box>);
}


const RoleCreateTabsNav: React.FC = () => {
  const [tabValue, setTabValue] = React.useState(0);

  function handleSearch(value: React.ChangeEvent<HTMLInputElement>) {

  }

  function handleTabValue(_event: React.SyntheticEvent, newValue: number) {
    setTabValue(newValue);
  }
  return (<Stack>
    <FilterByString defaultValue={''} onChange={handleSearch} />
    <Tabs value={tabValue} onChange={handleTabValue}>
      <Tab label={<FormattedMessage id='permissions.createRole.role_parent' />} />
      <Tab label={<FormattedMessage id='permissions.createRole.role_permissions' />} />
      <Tab label={<FormattedMessage id='permissions.createRole.role_members' />} />
    </Tabs>
    <TabContent id={0} selected={tabValue}><Fields.RoleParent /></TabContent>
    <TabContent id={1} selected={tabValue}><Fields.RolePermissions /></TabContent>
    <TabContent id={2} selected={tabValue}><Fields.RolePrincipals /></TabContent>
  </Stack>
  );
}



const RoleDialogRightLayout: React.FC = () => {
  const { roles } = Context.usePermissions();

  if (!roles) {
    return <>no roles here on the right side!</>;
  }

  return (<RoleCreateTabsNav />)
}


const RoleCreateTabsNavLoader: React.FC = () => {
  return (<PermissionsProvider><RoleDialogRightLayout /></PermissionsProvider>);
}

export default RoleCreateTabsNavLoader;
