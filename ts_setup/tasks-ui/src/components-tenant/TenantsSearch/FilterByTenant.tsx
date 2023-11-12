import * as React from 'react';
import { Menu, MenuItem, MenuList, ListItemIcon, ListItemText, Typography } from '@mui/material';
import Check from '@mui/icons-material/Check';
import { FormattedMessage } from 'react-intl';
import Client from 'client';
import { FilterByStatus, FilterBy } from 'descriptor-task';
import { } from 'descriptor-tenant';
import { NavigationButtonSearch } from '../NavigationSticky';


const tenants: Client.Tenant[] = [
  { id: '1', name: 'tenant1' },
  { id: '2', name: 'tenant2' },
  { id: '3', name: 'tenant3' }
];

const FilterByTenant: React.FC<{
  onChange: (value: Client.Tenant[]) => void
}> = ({ onChange }) => {

  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleClose = () => {
    setAnchorEl(null);
  };
  //const filterByStatus = value.find(filter => filter.type === 'FilterByStatus') as FilterByStatus | undefined;

  return (<>
    <NavigationButtonSearch onClick={handleClick} id='tenant.select.button' values={{ count: tenants.length }} />

    <Menu sx={{ width: 320 }}
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
        <MenuItem>
          <ListItemText><Typography variant='body2' fontWeight='bolder'><FormattedMessage id='tenant.select.menu.title' /></Typography></ListItemText>
        </MenuItem>
        {tenants.map(tenant => {
          // const found = value.find(filter => filter.type === 'FilterByStatus');
          const selected = '';

          if (selected) {
            return (<MenuItem key={tenant.id} onClick={() => {
              handleClose();
              onChange([tenant]);
            }}><ListItemIcon><Check /></ListItemIcon>{tenant.name}</MenuItem>);
          }
          return <MenuItem key={tenant.id} onClick={() => {
            handleClose();
            onChange([tenant]);
          }}>
            <ListItemText inset>{tenant.name}</ListItemText>
          </MenuItem>;
        })}

      </MenuList>
    </Menu>
  </>
  );
}

export default FilterByTenant;