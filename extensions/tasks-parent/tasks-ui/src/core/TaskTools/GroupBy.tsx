import * as React from 'react';
import { Paper, Button, Menu } from '@mui/material';
import Divider from '@mui/material/Divider';
import MenuList from '@mui/material/MenuList';
import MenuItem from '@mui/material/MenuItem';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import Check from '@mui/icons-material/Check';

import GroupWorkIcon from '@mui/icons-material/GroupWork';
import client from '@taskclient';


const types: client.GroupBy[] = ['none', 'owners', 'roles', 'status', 'priority'];


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
      <GroupWorkIcon />
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
          <ListItemText><b>Group by</b></ListItemText>
        </MenuItem>
        {types.map(type => {
          
          if(ctx.state.groupBy === type) {
            return <MenuItem key={type}><ListItemIcon><Check /></ListItemIcon>{type}</MenuItem>  
          }
          return <MenuItem key={type} onClick={() => {
            handleClose();
            ctx.setState(prev => prev.withGroupBy(type));
            
          }}><ListItemText inset>{type}</ListItemText></MenuItem>;
        })}
      </MenuList>
    </Menu>
  </>
  );
}