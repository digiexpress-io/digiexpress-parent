import * as React from 'react';
import { Button, Menu, MenuItem, MenuList, ListItemIcon, ListItemText, Typography } from '@mui/material';
import Check from '@mui/icons-material/Check';
import { FormattedMessage } from 'react-intl';
import { TaskDescriptor } from 'taskdescriptor';

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
    <Button variant='outlined' sx={{ borderRadius: 10 }} onClick={handleClick}>
      <Typography variant='caption' sx={{ color: 'text.primary' }}>
        <FormattedMessage id='core.search.searchBar.filterColumns' />
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