import React from 'react';
import { Box, Stack, Typography, IconButton, useTheme } from '@mui/material';
import EditIcon from '@mui/icons-material/ModeEditOutlineOutlined';
import { FormattedMessage } from 'react-intl';

import Burger from 'components-burger';
import { cyan } from 'components-colors';
import { useAm } from 'descriptor-access-mgmt';
import { useActivePermission } from './PermissionsOverviewContext';
import { PermissionEditDialog } from 'components-access-mgmt/PermissionEdit';



const StyledStack: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const theme = useTheme();

  return (<Box sx={{
    height: '100%',
    position: 'fixed',
    //height: 'vh',
    overflowY: 'scroll',
    overflowX: 'hidden',
    boxShadow: 1,
    width: '23%',
    pt: theme.spacing(2),
    px: theme.spacing(2),
    backgroundColor: theme.palette.background.paper
  }}>
    <Stack direction='column' spacing={1}>
      {children}
    </Stack>
  </Box >);
}



const StyledTitle: React.FC<{ children: string }> = ({ children }) => {
  return (<Typography fontWeight='bold'><FormattedMessage id={children} /></Typography>)
}

const PermissionItemActive: React.FC = () => {
  const { permissions } = useAm();
  const { permissionId } = useActivePermission();
  const [editOpen, setEditOpen] = React.useState(false);

  function handleEdit() {
    setEditOpen(prev => !prev);
  }

  const activePerm = permissions.find(permission => permission.id === permissionId);

  if (activePerm) {
    return (<>
      <PermissionEditDialog open={editOpen} onClose={handleEdit} permission={activePerm} />

      <StyledStack>
        <Burger.Section>
          <StyledTitle children='permissions.activePermission.tools' />
          <Stack direction='row' spacing={1} justifyContent='center'>
            <Box display='flex' flexDirection='column' alignItems='center'>
              <IconButton onClick={handleEdit}><EditIcon sx={{ color: cyan }} /></IconButton>
              <Typography><FormattedMessage id='permissions.activePermission.edit' /></Typography>
            </Box>
          </Stack>
        </Burger.Section>

        {/* title section */}
        <Burger.Section>
          <StyledTitle children='permissions.permission.name' />
          <Typography>{activePerm.name}</Typography>
        </Burger.Section>

        {/* description section */}
        <Burger.Section>
          <StyledTitle children='permissions.permission.description' />
          <Typography>{activePerm.description}</Typography>
        </Burger.Section>

        {/* status section */}
        <Burger.Section>
          <StyledTitle children='permissions.permission.status' />
          <Typography>{activePerm.status}</Typography>
        </Burger.Section>
      </StyledStack >
    </>

    );
  }

  return null;
}

const PermissionItemActiveWithRefresh: React.FC<{}> = () => {
  const [dismount, setDismount] = React.useState(false);
  const { permissionId } = useActivePermission();

  React.useEffect(() => {
    if (dismount) {
      setDismount(false);
    }
  }, [dismount]);

  React.useEffect(() => {
    setDismount(true);
  }, [permissionId]);

  if (dismount) {
    return null;
  }

  return (<PermissionItemActive />)
}


export default PermissionItemActiveWithRefresh;