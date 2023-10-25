import * as React from 'react';
import { Button, Menu, MenuList, MenuItem, ListItemIcon, ListItemText, Typography } from '@mui/material';
import Check from '@mui/icons-material/Check';
import { FormattedMessage } from 'react-intl';
import Context from 'context';
import { FilterByOwners, FilterBy } from 'taskdescriptor';

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
  };


  const filterByOwners = props.value.find(filter => filter.type === 'FilterByOwners') as FilterByOwners | undefined;

  return (<>
    <Button variant='outlined' sx={{ borderRadius: 10 }} onClick={handleClick}>
      <Typography variant='caption' sx={{ color: 'text.primary' }}>
        <FormattedMessage id='core.search.searchBar.filterOwners' values={{ count: filterByOwners?.owners.length }} />
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
          <ListItemText><b>Filter by owners</b></ListItemText>
        </MenuItem>
        {Object.keys(ctx.state.palette.owners).map(type => {
          const found = props.value.find(filter => filter.type === 'FilterByOwners');
          const selected = found ? found.type === 'FilterByOwners' && found.owners.includes(type) : false

          if (selected) {
            return (<MenuItem key={type} onClick={() => {
              handleClose();
              props.onChange([type]);
            }
            }> <ListItemIcon><Check /></ListItemIcon>{type}</MenuItem>);
          }
          return <MenuItem key={type} onClick={() => {
            handleClose();
            props.onChange([type]);
          }}>
            <ListItemText inset>{type}</ListItemText>
          </MenuItem>;
        })}

      </MenuList>
    </Menu >
  </>
  );
}