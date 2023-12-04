import * as React from 'react';
import { Menu, MenuList, MenuItem, ListItemIcon, ListItemText, Typography } from '@mui/material';
import Check from '@mui/icons-material/Check';
import { FormattedMessage } from 'react-intl';

import Context from 'context';
import { FilterByOwners, FilterBy } from 'descriptor-task';
import { NavigationButtonSearch } from '../NavigationSticky';


const FilterAssignees: React.FC<{
  onChange: (value: string[]) => void;
  value: FilterBy[];
}> = (props) => {

  const ctx = Context.useTasks();

  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleClose = () => {
    setAnchorEl(null);
  };


  const filterByOwners = props.value.find(filter => filter.type === 'FilterByOwners') as FilterByOwners | undefined;

  return (<>
    <NavigationButtonSearch onClick={handleClick} id='core.search.searchBar.filterAssignees' values={{ count: filterByOwners?.owners.length }} />

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
          <ListItemText><Typography fontWeight='bold'><FormattedMessage id='taskSearch.filter.assignees' /></Typography></ListItemText>
        </MenuItem>
        {Object.keys(ctx.state.palette.owners).map(type => {
          const found = props.value.find(filter => filter.type === 'FilterByOwners');
          const selected = found ? found.type === 'FilterByOwners' && found.owners.includes(type) : false

          if (selected) {
            return (<MenuItem key={type} onClick={() => {
              handleClose();
              props.onChange([type]);
            }
            }> <ListItemIcon><Check /></ListItemIcon>{type}</MenuItem>);
          }
          return <MenuItem key={type} onClick={() => {
            handleClose();
            props.onChange([type]);
          }}>
            <ListItemText inset>{type}</ListItemText>
          </MenuItem>;
        })}

      </MenuList>
    </Menu >
  </>
  );
}

export { FilterAssignees };