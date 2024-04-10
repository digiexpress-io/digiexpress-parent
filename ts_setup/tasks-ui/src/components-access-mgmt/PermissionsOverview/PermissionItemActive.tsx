import React from 'react';
import { Box, Stack, Typography, IconButton, Skeleton, useTheme } from '@mui/material';
import EditIcon from '@mui/icons-material/ModeEditOutlineOutlined';
import CrmIcon from '@mui/icons-material/AdminPanelSettingsOutlined';
import { FormattedMessage } from 'react-intl';

import Burger from 'components-burger';
import { cyan } from 'components-colors';
import { useAm } from 'descriptor-access-mgmt';
import { useActivePermission } from './PermissionsOverviewContext';



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
  const [taskEditOpen, setTaskEditOpen] = React.useState(false);
  const { permissions } = useAm();
  const { permissionId } = useActivePermission();

  const activePerm = permissions.find(permission => permission.id === permissionId);

  function handleTaskEdit() {
    setTaskEditOpen(prev => !prev);
  }


  if (activePerm) {
    return (<>
      {/*<TaskEditDialog open={taskEditOpen} onClose={handleTaskEdit} task={task} /> */}

      <StyledStack>
        <Burger.Section>
          <StyledTitle children='task.tools' />
          <Stack direction='row' spacing={1} justifyContent='center'>
            <Box display='flex' flexDirection='column' alignItems='center'>
              <IconButton onClick={handleTaskEdit}><EditIcon sx={{ color: cyan }} /></IconButton>
              <Typography><FormattedMessage id='task.edit' /></Typography>
            </Box>
            <Box display='flex' flexDirection='column' alignItems='center'>
              <IconButton onClick={() => { }}><CrmIcon sx={{ color: 'locale.dark' }} /></IconButton>
              <Typography><FormattedMessage id='customer.details.view' /></Typography>
            </Box>
          </Stack>
        </Burger.Section>

        {/* title section */}
        <Burger.Section>
          <StyledTitle children='task.title' />
          <Typography fontWeight='bold'>{activePerm.name}</Typography>
        </Burger.Section>

        {/* description section */}
        <Burger.Section>
          <StyledTitle children='task.description' />
          <Typography fontWeight='bold'>{activePerm.description}</Typography>
        </Burger.Section>

        {/* status section */}
        <Burger.Section>
          <StyledTitle children='task.status' />
          <Typography fontWeight='bold'>{activePerm.status}</Typography>
        </Burger.Section>

      </StyledStack >
    </>

    );
  }

  return (<StyledStack>
    <Skeleton animation={false} variant="rounded" width='100%' height={40} />
    <Skeleton animation={false} variant="rounded" width='100%' height={40} />
    <Skeleton animation={false} variant="rounded" width='100%' height={40} />

    <Skeleton animation={false} variant="text" width='100%' height='2rem' />
    <Skeleton animation={false} variant="rounded" width='100%' height={70} />

    <Skeleton animation={false} variant="text" width='100%' height='2rem' />
    <Skeleton animation={false} variant="text" width='85%' height='1rem' />
    <Skeleton animation={false} variant="text" width='35%' height='1rem' />
    <Skeleton animation={false} variant="text" width='60%' height='1rem' />

    <Skeleton animation={false} variant="text" width='100%' height='2rem' />
    <Skeleton animation={false} variant="rounded" width='25%' height={30} sx={{ borderRadius: '15px' }} />
  </StyledStack>);
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