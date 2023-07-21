import * as React from 'react';
import { Button, Menu } from '@mui/material';
import MenuList from '@mui/material/MenuList';
import MenuItem from '@mui/material/MenuItem';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import Check from '@mui/icons-material/Check';
import PersonIcon from '@mui/icons-material/Person';

import client from '@taskclient';


export default function DenseMenu() {
  const ctx = client.useTasks();

  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleClose = () => {
    setAnchorEl(null);
  };


  return (<>
    <Button variant="outlined" color="secondary" sx={{ ml: 2 }} onClick={handleClick}>
      <PersonIcon />
    </Button>


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
          <ListItemText><b>Filter by owners</b></ListItemText>
        </MenuItem>
        {Object.keys(ctx.state.pallette.owners).map(type => {
          const found = ctx.state.filterBy.find(filter => filter.type === 'FilterByOwners');
          const selected = found ? found.type === 'FilterByOwners' && found.owners.includes(type) : false

          if (selected) {
            return (<MenuItem key={type} onClick={() => {
              handleClose();
              ctx.setState(prev => prev.withFilterByOwner([type]));
            }}><ListItemIcon><Check /></ListItemIcon>{type}</MenuItem>);
          }
          return <MenuItem key={type} onClick={() => {
            handleClose();
            ctx.setState(prev => prev.withFilterByOwner([type]));
          }}>
            <ListItemText inset>{type}</ListItemText>
          </MenuItem>;
        })}

      </MenuList>
    </Menu>
  </>
  );
}