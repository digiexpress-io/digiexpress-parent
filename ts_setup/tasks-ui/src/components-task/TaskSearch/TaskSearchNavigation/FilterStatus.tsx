import * as React from 'react';
import { Menu, MenuItem, MenuList, ListItemIcon, ListItemText, Typography, Chip } from '@mui/material';
import Check from '@mui/icons-material/Check';
import { FormattedMessage } from 'react-intl';


import { FilterByStatus, FilterBy, TaskStatus } from 'descriptor-task';
import { ButtonSearch } from 'components-generic';


const statustypes: TaskStatus[] = ['CREATED', 'IN_PROGRESS', 'COMPLETED', 'REJECTED'];

const FilterStatus: React.FC<{
  onChange: (value: TaskStatus[]) => void;
  value: readonly FilterBy[];
}> = (props) => {

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
    <ButtonSearch onClick={handleClick} id='taskSearch.searchBar.filterStatus' values={{ count: filterByStatus?.status.length }} />

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
          <ListItemText><Typography fontWeight='bold'><FormattedMessage id='taskSearch.filter.status' /></Typography></ListItemText>
        </MenuItem>
        {statustypes.map(type => {
          const found = props.value.find(filter => filter.type === 'FilterByStatus');
          const selected = found ? found.type === 'FilterByStatus' && found.status.includes(type) : false

          if (selected) {
            return (<MenuItem key={type} onClick={() => {
              handleClose();
              props.onChange([type]);
            }}><ListItemIcon><Check /></ListItemIcon><Chip size='small' label={type} /></MenuItem>);
          }
          return <MenuItem key={type} onClick={() => {
            handleClose();
            props.onChange([type]);
          }}>
            <ListItemText inset><Chip label={type} size='small' /></ListItemText>
          </MenuItem>;
        })}

      </MenuList>
    </Menu>
  </>
  );
}

export { FilterStatus };