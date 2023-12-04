import * as React from 'react';
import { Menu, MenuList, MenuItem, ListItemIcon, ListItemText, Typography } from '@mui/material';
import Check from '@mui/icons-material/Check';
import { FormattedMessage } from 'react-intl';

import { GroupBy } from 'descriptor-task';
import { NavigationButtonSearch } from '../NavigationSticky';

const types: GroupBy[] = ['none', 'owners', 'roles', 'status', 'priority'];


const GroupBySelect: React.FC<{
  onChange: (value: GroupBy) => void;
  value: GroupBy;
}> = ({ onChange, value }) => {
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleClose = () => {
    setAnchorEl(null);
  };


  return (<>
    <NavigationButtonSearch onClick={handleClick} id='core.search.searchBar.groupBy' values={{ groupBy: value }} />

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
          <ListItemText><Typography fontWeight='bold'><FormattedMessage id='taskSearch.filter.groupBy' /></Typography></ListItemText>
        </MenuItem>
        {types.map(type => {

          if (value === type) {
            return <MenuItem key={type}><ListItemIcon><Check /></ListItemIcon>{type}</MenuItem>
          }
          return <MenuItem key={type} onClick={() => {
            handleClose();
            onChange(type);

          }}><ListItemText inset>{type}</ListItemText></MenuItem>;
        })}
      </MenuList>
    </Menu>
  </>
  );
}

export { GroupBySelect };