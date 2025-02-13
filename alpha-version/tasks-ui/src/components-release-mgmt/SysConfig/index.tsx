import React from 'react';
import { Box } from '@mui/material';



import * as colors from 'components-colors';
import { LayoutList, NavigationButton, FilterByString } from 'components-generic';

import { ImmutableCollection } from 'descriptor-grouping';
import { SysConfigService } from 'descriptor-sys-config';


import { SysConfigFlowGroup } from './SysConfigFlowGroup';
import { SysConfigProvider, TabTypes, useSysConfig } from '../SysConfigContext';



const color_current_config = colors.orange;
const color_all_configs = colors.emerald;


const SysConfigNavigation: React.FC = () => {
  const { setActiveTab, activeTab } = useSysConfig();
  const { id } = activeTab;


  function getLocale(id: TabTypes) {
    return { count: 0 };
  }

  function handleSearch(value: React.ChangeEvent<HTMLInputElement>) {

  }

  return (<>
    <FilterByString defaultValue={''} onChange={handleSearch} />
    <NavigationButton id='core.sysconfig.current_config'
      values={getLocale('current_config')}
      color={color_current_config}
      active={id === 'current_config'}
      onClick={() => setActiveTab("current_config")} />
    <NavigationButton id='core.sysconfig.all_config'
      values={getLocale('all_config')}
      color={color_all_configs}
      active={id === 'all_config'}
      onClick={() => setActiveTab("all_config")} />
  </>);
}



const SysConfigLayout: React.FC = () => {
  const { sysConfig } = useSysConfig();
  if (!sysConfig) {
    return null;
  }

  const grouping = new ImmutableCollection<SysConfigService>({
    groupValues: [],
    classifierName: 'flowName',
    definition: (entry) => entry.flowName,
    origin: sysConfig.services
  });

  const navigation = <SysConfigNavigation />;
  const pagination = <></>;
  const items = grouping.groups.map(group => (<SysConfigFlowGroup group={group} key={group.id} />));
  return (<LayoutList slots={{ navigation, items, pagination }} />)
}


const SysConfigLoader: React.FC = () => {
  return (<SysConfigProvider><SysConfigLayout /></SysConfigProvider>);
}

export default SysConfigLoader;
