import * as React from 'react';
import { Menu, MenuItem, MenuList, ListItemIcon, ListItemText } from '@mui/material';
import Check from '@mui/icons-material/Check';
import { TaskDescriptor } from 'taskdescriptor';
import { NavigationButtonSearch } from '../NavigationSticky';

export default function DenseMenu(
  props: {
    onChange: (value: (keyof TaskDescriptor)[]) => void;
    value: (keyof TaskDescriptor)[];
    types: (keyof TaskDescriptor)[];
  }
) {
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleClose = () => {
    setAnchorEl(null);
  }

  return (<>
    <NavigationButtonSearch onClick={handleClick} id='core.search.searchBar.filterColumns' values={undefined} />

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
          <ListItemText><b>Show/Hide columns</b></ListItemText>
        </MenuItem>
        {props.types.map(type => {
          const selected = props.value.includes(type)

          if (selected) {
            return (<MenuItem key={type} onClick={() => {
              handleClose();
              props.onChange(props.value.filter(sel => sel !== type));
            }
            }> <ListItemIcon><Check /></ListItemIcon>{type}</MenuItem>);
          }
          return <MenuItem key={type} onClick={() => {
            handleClose();
            props.onChange([...props.value, type]);
          }}>
            <ListItemText inset>{type}</ListItemText>
          </MenuItem>;
        })}

      </MenuList>
    </Menu >
  </>
  );
}