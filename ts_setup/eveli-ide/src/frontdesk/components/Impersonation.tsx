import { Button, Popover, Box, Grid,
  Typography,
  Divider} from '@mui/material';
import React from 'react';
import PersonIcon from '@mui/icons-material/Person';
import { useUserInfo } from '../context/UserContext';
import { FormattedMessage } from 'react-intl';
import { mapRole } from '../util/rolemapper';
import { useConfig } from '../context/ConfigContext';

export const Impersonation: React.FC = () => {
  const userInfo = useUserInfo();
  const config = useConfig();
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
        {userInfo.user.name || '-'}
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
          <Grid container>
            <Grid item xs={6}>
            <Typography variant="body2" gutterBottom><FormattedMessage id='profile.user.name' /></Typography>
            </Grid>
            <Grid item xs={6}>
              <strong>{userInfo.user.name}</strong>
            </Grid>
            <Grid item xs={6}>
            <Typography variant="body2" gutterBottom><FormattedMessage id='profile.user.email' /></Typography>
            </Grid>
            <Grid item xs={6}>
              <strong>{userInfo.user.email || '-'}</strong>
            </Grid>
            <Grid item xs={6}>
              <Typography variant="body2" gutterBottom><FormattedMessage id='profile.user.role' /></Typography>
            </Grid>
            <Grid item xs={6}>
              <strong>{userInfo.user.roles?.map(role=>mapRole(role)).join() || '-'}</strong>
            </Grid>
            <Grid item xs={12}>
              <Typography variant="body2" gutterBottom>
              <Divider />
              </Typography>
            </Grid>
            <Grid item xs={6}>
              <Typography variant="body2" gutterBottom><FormattedMessage id='profile.app.version' /></Typography>
            </Grid>
            <Grid item xs={6}>
              <strong>{config.appVersion || '-'}</strong>
            </Grid>
          </Grid>
        </Box>
      </Popover>
    </>

  );
}