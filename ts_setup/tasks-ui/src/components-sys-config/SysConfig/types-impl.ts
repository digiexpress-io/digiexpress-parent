import React from 'react';
import { Stack, Grid, Typography, TablePagination, Alert } from '@mui/material';

import Context from 'context';
import { NavigationSticky } from 'components-generic';
import * as colors from 'components-colors'; 


const color_releases = colors.steelblue;
const color_deployments = colors.red;
const color_currentConfig = colors.orange;
const color_configs = colors.emerald;


function initTabs(): {}[] {
  return [
    {
      id: 0,
      label: 'sysconfig.currentConfig',
      color: color_currentConfig,
      type: 'CURRENT_SYS_CONFIG',
      selected: true,
    },
    {
      id: 1,
      label: 'sysconfig.deployments',
      color: color_deployments,
      type: 'DEPLOYMENTS',
      selected: false,
    },
    {
      id: 2,
      label: 'sysconfig.configs',
      color: color_configs,
      type: 'ALL_SYS_CONFIG',
      selected: false,
    },
    {
      id: 3,
      label: 'sysconfig.releases',
      color: color_releases,
      type: 'RELEASES',
      selected: false,
    },
  ]
}

export { initTabs } 
