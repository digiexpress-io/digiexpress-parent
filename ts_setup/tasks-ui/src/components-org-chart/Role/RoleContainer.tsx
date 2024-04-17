import React from "react";
import { Paper, List, ListSubheader, ListItemButton, ListItemIcon, ListItemText, Collapse, ListItemButtonProps, styled, Box, Popper, IconButton } from "@mui/material";
import PeopleIcon from '@mui/icons-material/People';
import PersonIcon from '@mui/icons-material/Person';
import { ExpandLess, ExpandMore, Security, Policy, HelpOutline } from "@mui/icons-material";

import { FormattedMessage } from "react-intl";

import { Role, useAm } from "descriptor-access-mgmt";
import { useAvatar } from "descriptor-avatar";



/** material ui styling failure, font goes bold, containter gets resized */
const MaterialUIResizeRoleChildrenFailure: React.FC<{ role: Role }> = ({ role }) => {
  const { getPrincipal, getPermission } = useAm();
  const principals = role.principals.map(id => getPrincipal(id).email);
  const permissions = role.permissions.map(id => getPermission(id).name);
  const allData = [...principals, ...permissions].sort((a, b) => b.length - a.length);

  if (allData.length === 0) {
    return null;
  }
  console.log(allData[0])
  return (<div style={{ visibility: 'hidden', display: 'flex', height: '1px' }}>
    <div style={{ width: "80px" }}></div>
    <div>
      <b>{allData[0]}</b>
    </div>
  </div>)
}

const MaterialUIResizeRoleFailure: React.FC<{ role: Role }> = ({ role }) => {
  ;

  return (<div style={{ visibility: 'hidden', height: '1px' }}>
    <div style={{ display: 'flex' }}>
      <div style={{ width: "60px" }}></div>
      <div><b><FormattedMessage id="am.chart.tree.node.permissions" values={{ permissions: role.permissions.length }} /></b></div>
    </div>
    <div style={{ display: 'flex' }}>
      <div style={{ width: "60px" }}></div>
      <div><b><FormattedMessage id="am.chart.tree.node.users" values={{ permissions: role.principals.length }} /></b></div>
    </div>
  </div>)
}


const RoleHeader: React.FC<{ role: Role }> = ({ role }) => {
  const { name, description } = role;
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);

  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(anchorEl ? null : event.currentTarget);
  }

  const open = Boolean(anchorEl);
  return (<ListSubheader component="div" sx={{ lineHeight: 2, whiteSpace: "normal" }}>
    {name}
    <IconButton onClick={handleClick}><HelpOutline /></IconButton>
    
    <Popper id={role.id} open={open} anchorEl={anchorEl}>
      <Box sx={{ border: 1, p: 1, bgcolor: 'background.paper' }}>
        {description}
      </Box>
    </Popper>
  </ListSubheader>)
}


export const RoleContainer: React.FC<{ role: Role }> = ({ role }) => {
  const { getPrincipal, getPermission } = useAm();
  const { name } = role;
  const avatar = useAvatar(name);
  const [principalsOpen, setPrincipalsOpen] = React.useState(false);
  const [permissionsOpen, setPermissionsOpen] = React.useState(false);

  function handleTogglePermissions() {
    setPrincipalsOpen(false);
    setPermissionsOpen(prev => !prev);
  }

  function handleTogglePrincipals() {
    setPermissionsOpen(false);
    setPrincipalsOpen(prev => !prev);
  }


  const principals = React.useMemo(() => role.principals.map(id => getPrincipal(id))
    .map(principal => (
      <ListItemButton sx={{ pl: 4 }}>
        <ListItemIcon><PersonIcon /></ListItemIcon>
        <ListItemText primary={principal.name} />
      </ListItemButton>
    )), []);

  const permissions = React.useMemo(() => role.permissions.map(id => getPermission(id))
    .map(perm => (
      <ListItemButton sx={{ pl: 4 }}>
        <ListItemIcon><Policy /></ListItemIcon>
        <ListItemText primary={perm.name} />
      </ListItemButton>
    )), []);

  return (
    <Paper elevation={2} sx={{
      mx: 1,
      minWidth: "200px",
      display: "inline-block",
      borderTop: "5px solid",
      whiteSpace: "nowrap",
      borderTopColor: avatar?.color,
    }}>
      <List subheader={<RoleHeader role={role} />}>
        <ListItemButton onClick={handleTogglePrincipals} disabled={!role.principals.length}>
          <ListItemIcon><PeopleIcon /></ListItemIcon>
          <ListItemText primary={
            <FormattedMessage id="am.chart.tree.node.users" values={{ members: role.principals.length }} />
          } />
          {principalsOpen ? <ExpandLess /> : <ExpandMore />}
        </ListItemButton>

        <ListItemButton onClick={handleTogglePermissions} disabled={!role.permissions.length}>
          <ListItemIcon><Security /></ListItemIcon>
          <ListItemText primary={
            <FormattedMessage id="am.chart.tree.node.permissions" values={{ permissions: role.permissions.length }} />
          } />
          {permissionsOpen ? <ExpandLess /> : <ExpandMore />}
        </ListItemButton>
      </List>
      <MaterialUIResizeRoleFailure role={role} />

      <Collapse in={principalsOpen || permissionsOpen} timeout="auto" unmountOnExit>
        <MaterialUIResizeRoleChildrenFailure role={role} />
        <List component="div" disablePadding>{
          (principalsOpen && principals) ||
          (permissionsOpen && permissions)
        }</List>
      </Collapse>
    </Paper>
  );
}
