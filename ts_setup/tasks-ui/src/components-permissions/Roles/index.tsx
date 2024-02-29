import React from 'react';
import { Box } from '@mui/material';

import * as colors from 'components-colors';
import { LayoutList, NavigationButton, FilterByString } from 'components-generic';

import { ImmutableCollection } from 'descriptor-grouping';
import { SysConfigService } from 'descriptor-sys-config';

import { RolesFlowGroup } from './RolesFlowGroup';
import { PermissionsProvider, TabTypes, usePermissions } from '../PermissionsContext';


const color_create_role = colors.cyan;
const color_create_permission = colors.steelblue;


const SysConfigNavigation: React.FC = () => {
  const { setActiveTab, activeTab } = usePermissions();
  const { id } = activeTab;


  function getLocale(id: TabTypes) {
    return { count: 0 };
  }

  function handleSearch(value: React.ChangeEvent<HTMLInputElement>) {

  }

  return (<>
    <FilterByString defaultValue={''} onChange={handleSearch} />
    <NavigationButton id='permissions.navButton.role.create'
      values={getLocale('role_create')}
      color={color_create_role}
      active={id === 'role_create'}
      onClick={() => setActiveTab("role_create")} />
    <NavigationButton id='permissions.navButton.permission.create'
      values={getLocale('permission_create')}
      color={color_create_permission}
      active={id === 'permission_create'}
      onClick={() => setActiveTab("permission_create")} />
  </>);
}



const PermissionsLayout: React.FC = () => {
  const { permissions } = usePermissions();
  if (!permissions) {
    return null;
  }

  const grouping = new ImmutableCollection<SysConfigService>({
    groupValues: [],
    classifierName: 'flowName',
    definition: (entry) => entry.flowName,
    origin: permissions.services
  });

  const navigation = <SysConfigNavigation />;
  const pagination = <></>;
  const items = grouping.groups.map(group => (<RolesFlowGroup group={group} key={group.id} />));
  return (<LayoutList slots={{ navigation, items, pagination }} />)
}


const RolesOverviewLoader: React.FC = () => {
  return (<PermissionsProvider><PermissionsLayout /></PermissionsProvider>);
}

export default RolesOverviewLoader;
