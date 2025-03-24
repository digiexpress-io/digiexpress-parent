import { Button, Popover, Box, Grid2, Typography, Divider } from '@mui/material';
import React from 'react';
import PersonIcon from '@mui/icons-material/Person';
import { FormattedMessage } from 'react-intl';
import { mapIamRole, useIam } from '@/api-iam';


export const Impersonation: React.FC = () => {
  const { user } = useIam();
  
  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);
  const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const open = Boolean(anchorEl);
  const id = open ? 'simple-popover' : undefined;

  return (
    <>
      <Button aria-controls='impersonation-menu' variant='text' aria-haspopup='true' color='inherit'
        onClick={handleClick}>
        <PersonIcon />
        {user.name || '-'}
      </Button>
      <Popover
        id={id}
        open={open}
        anchorEl={anchorEl}
        onClose={handleClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'center',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'center',
        }}
      >
        <Box padding={1}>
          <Grid2 container>
            <Grid2 size={{ xs: 6 }}>
            <Typography variant="body2" gutterBottom><FormattedMessage id='profile.user.name' /></Typography>
            </Grid2>
            <Grid2 size={{ xs: 6 }}>
              <strong>{user.name}</strong>
            </Grid2>
            <Grid2 size={{ xs: 6 }}>
            <Typography variant="body2" gutterBottom><FormattedMessage id='profile.user.email' /></Typography>
            </Grid2>
            <Grid2 size={{ xs: 6 }}>
              <strong>{user.email || '-'}</strong>
            </Grid2>
            <Grid2 size={{ xs: 6 }}>
              <Typography variant="body2" gutterBottom><FormattedMessage id='profile.user.role' /></Typography>
            </Grid2>
            <Grid2 size={{ xs: 6 }}>
              <strong>{user.roles.map(role => mapIamRole(role)).join() || '-'}</strong>
            </Grid2>
            <Grid2 size={{ xs: 12 }}>
              <Typography variant="body2" gutterBottom>
              <Divider />
              </Typography>
            </Grid2>
          </Grid2>
        </Box>
      </Popover>
    </>

  );
}
