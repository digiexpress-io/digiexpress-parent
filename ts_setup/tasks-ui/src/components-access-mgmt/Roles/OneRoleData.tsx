import React from 'react';
import { Paper, Box, IconButton } from '@mui/material';
import MoreHorizIcon from '@mui/icons-material/MoreHoriz';
import * as colors from 'components-colors';
import Burger from 'components-burger';
import { Role } from 'descriptor-access-mgmt';
import { OneRoleDataTabs } from './OneRoleDataTabs';

const sx = { borderRadius: '8px 8px 0px 0px', boxShadow: "unset", fontWeight: 'bolder' };

export const OneRoleData: React.FC<{ role: Role }> = ({ role }) => {
  console.log(role)
  return (
    <Box sx={{ paddingTop: 1, paddingLeft: 1 }}>
      <Box>
        <Box display="flex" alignItems='center'>
          <Burger.PrimaryButton sx={sx} label={<>{role.name}</>} onClick={() => { }} />
          <Box flexGrow={1} />
          <IconButton sx={{ color: colors.cyan }}><MoreHorizIcon /></IconButton>
        </Box>
        <Paper sx={{ borderTopLeftRadius: '0px', minHeight: '100px' }}>
          <OneRoleDataTabs role={role} />
        </Paper>
      </Box>
    </Box>
  );
}


