import React from 'react';
import { Button, CircularProgress, List, ListItem, ListItemAvatar, Avatar, ListItemText, Box, Fade } from '@mui/material';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';

import FavoriteBorderIcon from '@mui/icons-material/FavoriteBorder';
import PersonIcon from '@mui/icons-material/Person';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import { UserProfileAndOrg } from 'descriptor-access-mgmt';
import { Health } from 'descriptor-backend';



function GradientCircularProgress() {
  return (
    <React.Fragment>
      <svg width={0} height={0}>
        <defs>
          <linearGradient id="my_gradient" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" stopColor="#e01cd5" />
            <stop offset="100%" stopColor="#1CB5E0" />
          </linearGradient>
        </defs>
      </svg>
      <CircularProgress sx={{ 'svg circle': { stroke: 'url(#my_gradient)' } }} />
    </React.Fragment>
  );
}

const Divider: React.FC<{}> = ({ }) => {
  return (<Box sx={{ pt: '20px' }} />)
}

const Version: React.FC<{}> = ({ }) => {
  return (
    <Button>build version 1.α</Button>)
}


export const Loader: React.FC<{ health: Health | undefined, profile: UserProfileAndOrg | undefined }> = ({ profile, health }) => {
  const [open, setOpen] = React.useState(true);
  React.useEffect(() => {
    if (!open) {
      return;
    }

    if (!profile || !health) {
      return;
    }

    new Promise(resolve => setTimeout(resolve, 3000)).then(() => {
      setOpen(false);
    })

  }, [profile, health])


  function handleClose() {

  }

  return (

    <Dialog onClose={handleClose} open={open} transitionDuration={1000}>
      <DialogTitle sx={{ m: 0, p: 2 }}>
        Loading DigiExpress application
      </DialogTitle>

      <DialogContent dividers>
        <List  sx={{ width: "50vh" }}>
          <ListItem>
            <ListItemAvatar><Avatar><FavoriteBorderIcon /></Avatar></ListItemAvatar>
            <ListItemText primary={health?.contentType ?? "Loading..."} secondary="System health" />
          </ListItem>
          <Divider />
          <ListItem>
            <ListItemAvatar><Avatar><PersonIcon /></Avatar></ListItemAvatar>
            <ListItemText primary={profile?.am.principal.email ?? "Loading..."} secondary="User profile" />
          </ListItem>
          <Divider />
          <ListItem>
            <ListItemAvatar><Avatar><AdminPanelSettingsIcon /></Avatar></ListItemAvatar>
            <ListItemText primary={profile?.tenant.id ?? "Loading..."} secondary="Tenant configuration" />
          </ListItem>
        </List>
      </DialogContent>
      <DialogActions><Version /></DialogActions>
    </Dialog>
  );
}
