import * as React from 'react';
import { Menu, ListItemText, MenuList, MenuItem, ListItemIcon, Typography } from '@mui/material';
import Check from '@mui/icons-material/Check';
import { FormattedMessage } from 'react-intl';

import Context from 'context';
import { FilterByRoles, FilterBy } from 'descriptor-task';
import { ButtonSearch } from 'components-generic';

const FilterRoles: React.FC<{
  onChange: (value: string[]) => void;
  value: FilterBy[];
}> = (props) => {
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
    <ButtonSearch onClick={handleClick} id='taskSearch.searchBar.filterRoles' values={{ count: filterByRoles?.roles.length }} />

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
          <ListItemText><Typography fontWeight='bold'><FormattedMessage id='taskSearch.filter.roles' /></Typography></ListItemText>
        </MenuItem>
        {Object.keys(ctx.state.palette.roles).map(type => {
          const found = props.value.find(filter => filter.type === 'FilterByRoles');
          const selected = found ? found.type === 'FilterByRoles' && found.roles.includes(type) : false

          if (selected) {
            return (<MenuItem key={type} onClick={() => {
              handleClose();
              props.onChange([type]);
            }}><ListItemIcon><Check /></ListItemIcon><Typography fontWeight='bold'>{type}</Typography></MenuItem>);
          }
          return <MenuItem key={type} onClick={() => {
            handleClose();
            props.onChange([type]);
          }}>
            <ListItemText inset><Typography>{type}</Typography></ListItemText>
          </MenuItem>;
        })}

      </MenuList>
    </Menu>
  </>
  );
}

export { FilterRoles };