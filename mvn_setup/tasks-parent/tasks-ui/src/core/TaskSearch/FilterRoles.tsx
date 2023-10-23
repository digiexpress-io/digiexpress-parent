import * as React from 'react';
import { Button, Menu, Typography, ListItemText, MenuList, MenuItem, ListItemIcon } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import Check from '@mui/icons-material/Check';
import Context from 'context';
import { FilterByRoles, FilterBy } from 'taskdescriptor';


export default function DenseMenu(
  props: {
    onChange: (value: string[]) => void;
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
  }

  const filterByRoles = props.value.find(filter => filter.type === 'FilterByRoles') as FilterByRoles | undefined;

  return (<>
    <Button variant='outlined' sx={{ borderRadius: 10, borderColor: 'text.primary' }} onClick={handleClick}>
      <Typography variant='caption' sx={{ color: 'text.primary' }}>
        <FormattedMessage id='core.search.searchBar.filterRoles' values={{ count: filterByRoles?.roles.length }} />
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
          <ListItemText><b>Filter by roles</b></ListItemText>
        </MenuItem>
        {Object.keys(ctx.state.palette.roles).map(type => {
          const found = props.value.find(filter => filter.type === 'FilterByRoles');
          const selected = found ? found.type === 'FilterByRoles' && found.roles.includes(type) : false

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