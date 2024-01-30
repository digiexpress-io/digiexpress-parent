import React from 'react';
import { Menu, MenuList, MenuItem, ListItemIcon, ListItemText, Typography } from '@mui/material';
import Check from '@mui/icons-material/Check';
import { FormattedMessage } from 'react-intl';

import { ButtonSearch } from 'components-generic';
import { GroupByOptions, GroupByTypes } from '../TableContext';



const GroupBySelect: React.FC<{
  onChange: (value: GroupByTypes) => void;
  value: GroupByTypes;
}> = ({ onChange, value }) => {
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleClose = () => {
    setAnchorEl(null);
  };


  return (<>
    <ButtonSearch onClick={handleClick} id='taskSearch.searchBar.groupBy' values={{ groupBy: value }} />

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
          <ListItemText><Typography fontWeight='bold'><FormattedMessage id='taskSearch.filter.groupBy' /></Typography></ListItemText>
        </MenuItem>
        {GroupByOptions.map(type => {

          if (value === type) {
            return <MenuItem key={type}><ListItemIcon><Check /></ListItemIcon><Typography fontWeight='bolder'>{type}</Typography></MenuItem>
          }
          return <MenuItem key={type} onClick={() => {
            handleClose();
            onChange(type);

          }}><ListItemText inset><Typography>{type}</Typography></ListItemText></MenuItem>;
        })}
      </MenuList>
    </Menu>
  </>
  );
}

export { GroupBySelect };