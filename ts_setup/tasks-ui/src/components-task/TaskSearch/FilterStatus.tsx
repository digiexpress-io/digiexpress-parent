import * as React from 'react';
import { Menu, MenuItem, MenuList, ListItemIcon, ListItemText, Typography, Chip } from '@mui/material';
import Check from '@mui/icons-material/Check';
import { FormattedMessage } from 'react-intl';

import Client from 'client';
import { FilterByStatus, FilterBy } from 'descriptor-task';
import { NavigationButtonSearch } from '../NavigationSticky';


const statustypes: Client.TaskStatus[] = ['CREATED', 'IN_PROGRESS', 'COMPLETED', 'REJECTED'];

const FilterStatus: React.FC<{
  onChange: (value: Client.TaskStatus[]) => void;
  value: FilterBy[];
}> = (props) => {

  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleClose = () => {
    setAnchorEl(null);
  };
  const filterByStatus = props.value.find(filter => filter.type === 'FilterByStatus') as FilterByStatus | undefined;

  return (<>
    <NavigationButtonSearch onClick={handleClick} id='taskSearch.searchBar.filterStatus' values={{ count: filterByStatus?.status.length }} />

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
          <ListItemText><Typography fontWeight='bold'><FormattedMessage id='taskSearch.filter.status' /></Typography></ListItemText>
        </MenuItem>
        {statustypes.map(type => {
          const found = props.value.find(filter => filter.type === 'FilterByStatus');
          const selected = found ? found.type === 'FilterByStatus' && found.status.includes(type) : false

          if (selected) {
            return (<MenuItem key={type} onClick={() => {
              handleClose();
              props.onChange([type]);
            }}><ListItemIcon><Check /></ListItemIcon><Chip size='small' label={type} /></MenuItem>);
          }
          return <MenuItem key={type} onClick={() => {
            handleClose();
            props.onChange([type]);
          }}>
            <ListItemText inset><Chip label={type} size='small' /></ListItemText>
          </MenuItem>;
        })}

      </MenuList>
    </Menu>
  </>
  );
}

export { FilterStatus };