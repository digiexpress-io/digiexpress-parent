import React from 'react';
import { Box, Typography, IconButton, Tabs, Tab } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';

import { FormattedMessage } from 'react-intl';
import { TabTypes, useTabs } from './PermissionEditContext';

const HEADER_MAPPING: TabTypes[] = [
  'permission_roles',
  'permission_members'
];


const CloseDialogButton: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  return (
    <IconButton onClick={onClose}>
      <CloseIcon />
    </IconButton>
  )
}


const Header: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const tabbing = useTabs();
  function handleTabValue(_event: React.SyntheticEvent, index: number) {
    const newValue: TabTypes = HEADER_MAPPING[index];
    tabbing.setActiveTab(newValue);
  }

  return (
    <Box display='flex' alignItems='center'>
      <Box width='50%' marginRight={5}>
        <Typography variant='h4'><FormattedMessage id='permissions.permission.edit' /></Typography>
      </Box>

      <Box width='50%'>
        <Tabs value={HEADER_MAPPING.indexOf(tabbing.activeTab.id)} onChange={handleTabValue}>
          <Tab label={<FormattedMessage id='permissions.editPermission.roles' />} />
          <Tab label={<FormattedMessage id='permissions.editPermission.principals' />} />
        </Tabs>
      </Box>
      <CloseDialogButton onClose={onClose} />
    </Box>
  )
}

export { Header }