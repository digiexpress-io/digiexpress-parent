import * as React from 'react';
import { Menu, MenuList, MenuItem, ListItemIcon, ListItemText, Typography } from '@mui/material';
import Check from '@mui/icons-material/Check';
import { FormattedMessage } from 'react-intl';

import Context from 'context';
import { FilterByOwners, FilterBy } from 'descriptor-task';
import { ButtonSearch } from 'components-generic';


const FilterAssignees: React.FC<{
  onChange: (value: string[]) => void;
  value: readonly FilterBy[];
}> = (props) => {

  const ctx = Context.useTasks();

  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const handleClick = (event: React.MouseEvent<HTMLElement>) => setAnchorEl(event.currentTarget);
  const handleClose = () => setAnchorEl(null);

  const filterByOwners = props.value.find(filter => filter.type === 'FilterByOwners') as FilterByOwners | undefined;

  return (<>
    <ButtonSearch onClick={handleClick} id='taskSearch.searchBar.filterAssignees' values={{ count: filterByOwners?.owners.length }} />

    <Menu sx={{ width: 320 }} anchorEl={anchorEl} open={open} onClose={handleClose}
      anchorOrigin=   {{ vertical: 'top', horizontal: 'left' }}
      transformOrigin={{ vertical: 'top', horizontal: 'left' }}
    >
      <MenuList dense>
        <MenuItem>
          <ListItemText><Typography fontWeight='bold'><FormattedMessage id='taskSearch.filter.assignees' /></Typography></ListItemText>
        </MenuItem>

        { ctx.owners.map(type => {
          
          const found = props.value.find(filter => filter.type === 'FilterByOwners');
          const selected = found ? found.type === 'FilterByOwners' && found.owners.includes(type) : false;

          if (selected) {
            return (
              <MenuItem key={type} onClick={() => { handleClose(); props.onChange([type]); } }>
                <ListItemIcon><Check /></ListItemIcon>
                <Typography fontWeight='bolder'>{type}</Typography>
              </MenuItem>
            );
          }
          return (<MenuItem key={type} onClick={() => {
            handleClose();
            props.onChange([type]);
          }}>
            <ListItemText inset><Typography>{type}</Typography></ListItemText>
          </MenuItem>)
        })}

      </MenuList>
    </Menu >
  </>
  );
}

export { FilterAssignees };