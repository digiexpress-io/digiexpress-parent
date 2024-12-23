import React from 'react';
import { Menu, MenuItem, MenuList, ListItemIcon, ListItemText, Typography, CircularProgress, Box } from '@mui/material';
import Check from '@mui/icons-material/Check';
import { FormattedMessage } from 'react-intl';

import Burger from 'components-burger';
import { ButtonSearch } from 'components-generic';
import { Tenant, useDialobTenant } from 'descriptor-dialob';


export const FilterByTenant: React.FC = () => {
  const tenants = useDialobTenant();
  const tenantsList: Tenant[] = tenants.state.tenants.map(tenant => tenant.source);
  const [loading, setLoading] = React.useState(false);
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleClose = () => {
    setAnchorEl(null);
  };
  const handleSave = async () => {
    setLoading(true);
    tenants.reload().then(() => {
      setLoading(false)
      handleClose();
    });
  }

  if (tenantsList.length < 2) {
    return <></>;
  }

  return (<>
    <ButtonSearch onClick={handleClick} id='tenant.select.button' values={{ count: tenantsList.length }} />

    <Menu
      anchorEl={anchorEl}
      open={open}
      onClose={handleClose}
      anchorOrigin={{
        vertical: 'top',
        horizontal: 'left',
      }}
      transformOrigin={{
        vertical: 'top',
        horizontal: 'left',
      }}
    >
      <MenuList dense>
        <MenuItem disabled sx={{ '&.Mui-disabled': { opacity: '100%' } }}>
          <ListItemText><Typography variant='body2' fontWeight='bolder'><FormattedMessage id='tenant.select.menu.title' /></Typography></ListItemText>
        </MenuItem>
        {tenantsList.map(tenant => {
          const selected = tenants.state.activeTenant === tenant.id;

          if (selected) {
            return (<MenuItem key={tenant.id} onClick={async () => {
              tenants.withActiveTenant();
            }}><ListItemIcon><Check /></ListItemIcon>{tenant.name}</MenuItem>);
          }
          return <MenuItem key={tenant.id} onClick={async () => {
            tenants.withActiveTenant(tenant.id);
          }}>
            <ListItemText inset>{tenant.name}</ListItemText>
          </MenuItem>;
        })}
        <Box sx={{ m: 1, display: 'flex', justifyContent: 'flex-end' }}>
          {loading ? <CircularProgress size='14pt' sx={{ m: 1 }} /> : <Burger.SecondaryButton onClick={handleSave} label='buttons.apply' />}
        </Box>
      </MenuList>
    </Menu>
  </>
  );
}
