import * as React from 'react';
import { Menu, ListItemText, MenuList, MenuItem, ListItemIcon } from '@mui/material';
import Check from '@mui/icons-material/Check';
import Context from 'context';
import { FilterByRoles, FilterBy } from 'taskdescriptor';
import { NavigationButton } from '../NavigationSticky';

export default function DenseMenu(
  props: {
    onChange: (value: string[]) => void;
    value: FilterBy[]
  }
) {
  const ctx = Context.useTasks();
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleClose = () => {
    setAnchorEl(null);
  }

  const filterByRoles = props.value.find(filter => filter.type === 'FilterByRoles') as FilterByRoles | undefined;

  return (<>
    <NavigationButton onClick={handleClick} id='core.search.searchBar.filterRoles' values={{ count: filterByRoles?.roles.length }} />

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
          <ListItemText><b>Filter by roles</b></ListItemText>
        </MenuItem>
        {Object.keys(ctx.state.palette.roles).map(type => {
          const found = props.value.find(filter => filter.type === 'FilterByRoles');
          const selected = found ? found.type === 'FilterByRoles' && found.roles.includes(type) : false

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