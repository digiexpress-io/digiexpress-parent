import * as React from 'react';
import { Button, Menu, MenuList, MenuItem, ListItemIcon, ListItemText, Typography } from '@mui/material';
import Check from '@mui/icons-material/Check';
import { FormattedMessage } from 'react-intl';
import Client from 'taskclient';
import { FilterByPriority, FilterBy } from 'taskdescriptor';


const prioritytypes: Client.TaskPriority[] = ['HIGH', 'MEDIUM', 'LOW'];


export default function DenseMenu(

  props: {
    onChange: (value: Client.TaskPriority[]) => void;
    value: FilterBy[]
  }
) {
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleClose = () => {
    setAnchorEl(null);
  };
  const filterByPriority = props.value.find(filter => filter.type === 'FilterByPriority') as FilterByPriority | undefined;

  return (<>
    <Button variant='outlined' sx={{ borderRadius: 10, borderColor: 'text.primary' }} onClick={handleClick}>
      <Typography variant='caption' sx={{ color: 'text.primary' }}>
        <FormattedMessage id='core.search.searchBar.filterPriority' values={{ count: filterByPriority?.priority.length }} />
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
          <ListItemText><b>Filter by priority</b></ListItemText>
        </MenuItem>
        {prioritytypes.map(type => {
          const found = props.value.find(filter => filter.type === 'FilterByPriority');
          const selected = found ? found.type === 'FilterByPriority' && found.priority.includes(type) : false

          if (selected) {
            return <MenuItem key={type} onClick={() => {
              handleClose();
              props.onChange([type]);
            }}><ListItemIcon><Check /></ListItemIcon>{type}</MenuItem>
          }
          return <MenuItem key={type} onClick={() => {
            handleClose();
            props.onChange([type]);
          }}><ListItemText inset>{type}</ListItemText></MenuItem>;
        })}
      </MenuList>
    </Menu>
  </>
  );
}