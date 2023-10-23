import * as React from 'react';
import { Button, Menu, MenuItem, MenuList, ListItemIcon, ListItemText, Typography } from '@mui/material';
import Check from '@mui/icons-material/Check';
import { FormattedMessage } from 'react-intl';
import Client from '@taskclient';
import Context from 'context';


const statustypes: Client.TaskStatus[] = ['CREATED', 'IN_PROGRESS', 'COMPLETED', 'REJECTED'];

export default function DenseMenu() {
  const ctx = Context.useTasks();

  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleClose = () => {
    setAnchorEl(null);
  };
  const filterByStatus = ctx.state.filterBy.find(filter => filter.type === 'FilterByStatus') as Context.FilterByStatus | undefined;

  return (<>
    <Button variant='outlined' sx={{ borderRadius: 10, borderColor: 'text.primary' }} onClick={handleClick}>
      <Typography variant='caption' sx={{ color: 'text.primary' }}>
        <FormattedMessage id='core.search.searchBar.filterColumns' values={{ count: 0 }} />
      </Typography>
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
          <ListItemText><b>Show/Hide columns</b></ListItemText>
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

      </MenuList>
    </Menu>
  </>
  );
}