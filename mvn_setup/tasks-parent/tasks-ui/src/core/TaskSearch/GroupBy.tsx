import * as React from 'react';
import { Button, Menu, Typography, MenuList, MenuItem, ListItemIcon, ListItemText } from '@mui/material';
import Check from '@mui/icons-material/Check';
import { FormattedMessage } from 'react-intl';
import Context from 'context';
import { GroupBy } from 'taskdescriptor';


const types: GroupBy[] = ['none', 'owners', 'roles', 'status', 'priority'];


const DenseMenu: React.FC<{
  onChange: (value: GroupBy) => void;
  value: GroupBy
}> = ({ onChange, value }) => {
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
    <Button variant='outlined' sx={{ borderRadius: 10 }} onClick={handleClick}>
      <Typography variant='caption' sx={{ color: 'text.primary' }}>
        <FormattedMessage id='core.search.searchBar.groupBy' values={{ groupBy: value }} />
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

export default DenseMenu;