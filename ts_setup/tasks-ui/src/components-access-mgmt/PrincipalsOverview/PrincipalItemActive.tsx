import React from 'react';
import { Box, Stack, Typography, IconButton, useTheme } from '@mui/material';
import EditIcon from '@mui/icons-material/ModeEditOutlineOutlined';
import { FormattedMessage } from 'react-intl';

import Burger from 'components-burger';
import { cyan } from 'components-colors';
import { useAm } from 'descriptor-access-mgmt';
import { useActivePrincipal } from './PrincipalsOverviewContext';
import { PrincipalEditDialog } from 'components-access-mgmt/PrincipalEdit/';



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

const PrincipalItemActive: React.FC = () => {
  const { principals } = useAm();
  const { principalId } = useActivePrincipal();
  const [editOpen, setEditOpen] = React.useState(false);

  const activePrincipal = principals.find(principal => principal.id === principalId);

  function handleEdit() {
    setEditOpen(prev => !prev);
  }

  if (activePrincipal) {
    return (<>
      <PrincipalEditDialog open={editOpen} onClose={handleEdit} principal={activePrincipal} />

      <StyledStack>
        <Burger.Section>
          <StyledTitle children='permissions.activePrincipal.tools' />
          <Stack direction='row' spacing={1} justifyContent='center'>
            <Box display='flex' flexDirection='column' alignItems='center'>
              <IconButton onClick={handleEdit}><EditIcon sx={{ color: cyan }} /></IconButton>
              <Typography><FormattedMessage id='permissions.activePrincipal.edit' /></Typography>
            </Box>
          </Stack>
        </Burger.Section>

        {/* name section */}
        <Burger.Section>
          <StyledTitle children='permissions.principal.name' />
          <Typography>{activePrincipal.name}</Typography>
        </Burger.Section>

        {/* email section */}
        <Burger.Section>
          <StyledTitle children='permissions.principal.email' />
          <Typography>{activePrincipal.email}</Typography>
        </Burger.Section>

        {/* status section */}
        <Burger.Section>
          <StyledTitle children='permissions.principal.status' />
          <Typography>{activePrincipal.status}</Typography>
        </Burger.Section>

        {/* direct roles section */}
        <Burger.Section>
          <StyledTitle children='permissions.principal.directRoles' />
          {activePrincipal.directRoles.length ? activePrincipal.directRoles
            .map(role => (<Typography key={role}>{role}</Typography>)) : <FormattedMessage id='permissions.principal.directRoles.none' />
          }
        </Burger.Section>

        {/* direct permissions section */}
        <Burger.Section>
          <StyledTitle children='permissions.principal.directPermissions' />
          {activePrincipal.directPermissions.length ? activePrincipal.directPermissions
            .map(permission => (<Typography key={permission}>{permission}</Typography>)) : <FormattedMessage id='permissions.principal.directPermissions.none' />
          }
        </Burger.Section>
      </StyledStack >
    </>
    );
  }

  return null;
}

const PrincipalItemActiveWithRefresh: React.FC<{}> = () => {
  const [dismount, setDismount] = React.useState(false);
  const { principalId } = useActivePrincipal();

  React.useEffect(() => {
    if (dismount) {
      setDismount(false);
    }
  }, [dismount]);

  React.useEffect(() => {
    setDismount(true);
  }, [principalId]);

  if (dismount) {
    return null;
  }

  return (<PrincipalItemActive />)
}


export default PrincipalItemActiveWithRefresh;