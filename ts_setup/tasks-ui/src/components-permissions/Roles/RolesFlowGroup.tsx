import React from 'react';
import { Paper, Box, IconButton } from '@mui/material';
import MoreHorizIcon from '@mui/icons-material/MoreHoriz';

import * as colors from 'components-colors';
import Burger from 'components-burger';
import { Group } from 'descriptor-grouping';

import { RolesFlow } from './RolesFlow';

const sx = { borderRadius: '8px 8px 0px 0px', boxShadow: "unset", fontWeight: 'bolder' };
export const RolesFlowGroup: React.FC<{ group: Group }> = ({ group }) => {

  console.log("group ", group);

  return (
    <Box sx={{ paddingTop: 1, paddingLeft: 1 }}>
      <Box>
        <Box display="flex">
          <Burger.PrimaryButton sx={sx} label={<>{group.id}</>} onClick={() => { }} />
          <Box flexGrow={1} />
          <IconButton sx={{ color: colors.cyan }}><MoreHorizIcon /></IconButton>
        </Box>
        <Paper sx={{ borderTopLeftRadius: '0px', minHeight: '100px' }}>
          <RolesFlow />
        </Paper>
      </Box>
    </Box >
  );
}


