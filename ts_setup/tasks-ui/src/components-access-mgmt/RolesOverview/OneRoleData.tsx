import React from 'react';
import { Paper, Box, IconButton } from '@mui/material';
import MoreHorizIcon from '@mui/icons-material/MoreHoriz';
import * as colors from 'components-colors';
import Burger from 'components-burger';
import { Role } from 'descriptor-access-mgmt';
import { OneRoleDataTabs } from './OneRoleDataTabs';
import { RoleEditDialog } from 'components-access-mgmt/RoleEdit/RoleEditDialog';
import { useAvatar } from 'descriptor-avatar';

const sx = { borderRadius: '8px 8px 0px 0px', boxShadow: "unset", fontWeight: 'bolder'};

export const OneRoleData: React.FC<{ role: Role }> = ({ role }) => {
  const [editOpen, setEditOpen] = React.useState(false);
  const avatar = useAvatar(role.id);


  return (<>
    <RoleEditDialog open={editOpen} role={role} onClose={() => setEditOpen(false)} />

    <Box sx={{ paddingTop: 1, paddingLeft: 1 }}>
      <Box>
        <Box display="flex" alignItems='center'>
          <Burger.PrimaryButton sx={{...sx, backgroundColor: avatar?.colorCode}} label={<>{avatar?.letterCode}</>} onClick={() => { }} />
          <Box flexGrow={1} />
          <IconButton sx={{ color: colors.cyan }} onClick={() => setEditOpen(true)}><MoreHorizIcon /></IconButton>
        </Box>
        <Paper sx={{ borderTopLeftRadius: '0px', borderBottomLeftRadius: '8px', borderBottomRightRadius: '0px', minHeight: '100px' }}>
          <OneRoleDataTabs role={role} />
        </Paper>
      </Box>
    </Box>
  </>
  );
}



