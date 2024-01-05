import * as React from 'react';
import { Menu, MenuList, MenuItem, ListItemIcon, ListItemText, Typography } from '@mui/material';
import Check from '@mui/icons-material/Check';
import { FormattedMessage } from 'react-intl';

import Client from 'client';
import { FilterByPriority, FilterBy } from 'descriptor-task';
import { ButtonSearch } from 'components-generic';

const prioritytypes: Client.TaskPriority[] = ['HIGH', 'MEDIUM', 'LOW'];


const FilterPriority: React.FC<{
  onChange: (value: Client.TaskPriority[]) => void;
  value: FilterBy[];
}> = (props) => {

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
    <ButtonSearch onClick={handleClick} id='taskSearch.searchBar.filterPriority' values={{ count: filterByPriority?.priority.length }} />

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
          <ListItemText><Typography fontWeight='bold'><FormattedMessage id='taskSearch.filter.priority' /></Typography></ListItemText>
        </MenuItem>
        {prioritytypes.map(type => {
          const found = props.value.find(filter => filter.type === 'FilterByPriority');
          const selected = found ? found.type === 'FilterByPriority' && found.priority.includes(type) : false

          if (selected) {
            return <MenuItem key={type} onClick={() => {
              handleClose();
              props.onChange([type]);
            }}><ListItemIcon><Check /></ListItemIcon><Typography fontWeight='bolder'>{type}</Typography></MenuItem>
          }
          return <MenuItem key={type} onClick={() => {
            handleClose();
            props.onChange([type]);
          }}><ListItemText inset><Typography>{type}</Typography></ListItemText></MenuItem>;
        })}
      </MenuList>
    </Menu>
  </>
  );
}
export { FilterPriority };