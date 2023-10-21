import * as React from 'react';
import { Button, Menu } from '@mui/material';
import Divider from '@mui/material/Divider';
import MenuList from '@mui/material/MenuList';
import MenuItem from '@mui/material/MenuItem';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import Check from '@mui/icons-material/Check';
import FilterAltIcon from '@mui/icons-material/FilterAlt';
import client from '@taskclient';


const statustypes: client.TaskStatus[] = ['CREATED', 'IN_PROGRESS', 'COMPLETED', 'REJECTED'];
const prioritytypes: client.TaskPriority[] = ['HIGH', 'MEDIUM', 'LOW'];


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
      <FilterAltIcon />
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
          <ListItemText><b>Filter by status</b></ListItemText>
        </MenuItem>
        {statustypes.map(type => {
          const found = ctx.state.filterBy.find(filter => filter.type === 'FilterByStatus');
          const selected = found ? found.type === 'FilterByStatus' && found.status.includes(type) : false

          if (selected) {
            return (<MenuItem key={type} onClick={() => {
              handleClose();
              ctx.setState(prev => prev.withFilterByStatus([type]));
            }}><ListItemIcon><Check /></ListItemIcon>{type}</MenuItem>);
          }
          return <MenuItem key={type} onClick={() => {
            handleClose();
            ctx.setState(prev => prev.withFilterByStatus([type]));
          }}>
            <ListItemText inset>{type}</ListItemText>
          </MenuItem>;
        })}


        <Divider />

        <MenuItem>
          <ListItemText><b>Filter by priority</b></ListItemText>
        </MenuItem>
        {prioritytypes.map(type => {
          const found = ctx.state.filterBy.find(filter => filter.type === 'FilterByPriority');
          const selected = found ? found.type === 'FilterByPriority' && found.priority.includes(type) : false

          if (selected) {
            return <MenuItem key={type} onClick={() => {
              handleClose();
              ctx.setState(prev => prev.withFilterByPriority([type]));
            }}><ListItemIcon><Check /></ListItemIcon>{type}</MenuItem>
          }
          return <MenuItem key={type} onClick={() => {
            handleClose();
            ctx.setState(prev => prev.withFilterByPriority([type]));
          }}><ListItemText inset>{type}</ListItemText></MenuItem>;
        })}
      </MenuList>
    </Menu>
  </>
  );
}