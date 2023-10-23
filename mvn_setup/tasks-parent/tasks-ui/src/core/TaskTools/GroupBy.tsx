import * as React from 'react';
import { Button, Menu, Typography, MenuList, MenuItem, ListItemIcon, ListItemText } from '@mui/material';
import Check from '@mui/icons-material/Check';
import { FormattedMessage } from 'react-intl';
import Context from 'context';


const types: Context.GroupBy[] = ['none', 'owners', 'roles', 'status', 'priority'];


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


  return (<>
    <Button variant='outlined' sx={{ borderRadius: 10, borderColor: 'text.primary' }} onClick={handleClick}>
      <Typography variant='caption' sx={{ color: 'text.primary' }}>
        <FormattedMessage id='core.search.searchBar.groupBy' values={{ groupBy: ctx.state.groupBy }} />
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
          <ListItemText><b>Group by</b></ListItemText>
        </MenuItem>
        {types.map(type => {

          if (ctx.state.groupBy === type) {
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