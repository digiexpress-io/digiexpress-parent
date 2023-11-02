import * as React from 'react';
import { Menu, ListItemText, MenuList, MenuItem, ListItemIcon } from '@mui/material';
import Check from '@mui/icons-material/Check';
import Context from 'context';
import { FilterByUsers, FilterBy } from 'projectdescriptor';
import { NavigationButtonSearch } from '../NavigationSticky';

export default function DenseMenu(
  props: {
    onChange: (value: string[]) => void;
    value: FilterBy[]
  }
) {
  const ctx = Context.useProjects();
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleClose = () => {
    setAnchorEl(null);
  }

  const filterByRoles = props.value.find(filter => filter.type === 'FilterByUsers') as FilterByUsers | undefined;

  return (<>
    <NavigationButtonSearch onClick={handleClick} id='project.search.searchBar.filterUsers' values={{ count: filterByRoles?.users.length }} />

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
          <ListItemText><b>Filter by users</b></ListItemText>
        </MenuItem>
        {Object.keys(ctx.state.palette.users).map(type => {
          const found = props.value.find(filter => filter.type === 'FilterByUsers');
          const selected = found ? found.type === 'FilterByUsers' && found.users.includes(type) : false

          if (selected) {
            return (<MenuItem key={type} onClick={() => {
              handleClose();
              props.onChange([type]);
            }}><ListItemIcon><Check /></ListItemIcon>{type}</MenuItem>);
          }
          return <MenuItem key={type} onClick={() => {
            handleClose();
            props.onChange([type]);
          }}>
            <ListItemText inset>{type}</ListItemText>
          </MenuItem>;
        })}

      </MenuList>
    </Menu>
  </>
  );
}