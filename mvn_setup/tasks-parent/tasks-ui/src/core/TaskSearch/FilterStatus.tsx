import * as React from 'react';
import { Button, Menu, MenuItem, MenuList, ListItemIcon, ListItemText, Typography } from '@mui/material';
import Check from '@mui/icons-material/Check';
import { FormattedMessage } from 'react-intl';
import Client from 'taskclient';
import Context from 'context';
import { FilterByStatus, FilterBy } from 'taskdescriptor';


const statustypes: Client.TaskStatus[] = ['CREATED', 'IN_PROGRESS', 'COMPLETED', 'REJECTED'];

export default function DenseMenu(
  props: {
    onChange: (value: Client.TaskStatus[]) => void;
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
  };
  const filterByStatus = props.value.find(filter => filter.type === 'FilterByStatus') as FilterByStatus | undefined;

  return (<>
    <Button variant='outlined' sx={{ borderRadius: 10 }} onClick={handleClick}>
      <Typography variant='caption' sx={{ color: 'text.primary' }}>
        <FormattedMessage id='core.search.searchBar.filterStatus' values={{ count: filterByStatus?.status.length }} />
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
          <ListItemText><b>Filter by status</b></ListItemText>
        </MenuItem>
        {statustypes.map(type => {
          const found = props.value.find(filter => filter.type === 'FilterByStatus');
          const selected = found ? found.type === 'FilterByStatus' && found.status.includes(type) : false

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