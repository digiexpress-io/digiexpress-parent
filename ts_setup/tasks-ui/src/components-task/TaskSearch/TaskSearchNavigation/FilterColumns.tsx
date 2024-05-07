import * as React from 'react';
import { Menu, MenuItem, MenuList, ListItemIcon, ListItemText, Typography } from '@mui/material';
import Check from '@mui/icons-material/Check';
import { FormattedMessage } from 'react-intl';
import { TaskDescriptor } from 'descriptor-task';
import { ButtonSearch } from 'components-generic';

const FilterColumns: React.FC<{
  onChange: (value: (keyof TaskDescriptor)[]) => void;
  value: (keyof TaskDescriptor)[];
  types: (keyof TaskDescriptor)[];
}> = (props) => {

  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleClose = () => {
    setAnchorEl(null);
  }

  return (<>
    <ButtonSearch onClick={handleClick} id='taskSearch.searchBar.filterColumns' values={undefined} />

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
          <ListItemText><Typography fontWeight='bold'><FormattedMessage id='taskSearch.filter.columns' /></Typography></ListItemText>
        </MenuItem>
        {props.types.map(type => {
          const selected = props.value.includes(type)

          if (selected) {
            return (<MenuItem key={type} onClick={() => {
              handleClose();
              props.onChange(props.value.filter(sel => sel !== type));
            }
            }> <ListItemIcon><Check /></ListItemIcon><Typography fontWeight='bolder'>{type}</Typography></MenuItem>);
          }
          return <MenuItem key={type} onClick={() => {
            handleClose();
            props.onChange([...props.value, type]);
          }}>
            <ListItemText inset><Typography>{type}</Typography></ListItemText>
          </MenuItem>;
        })}

      </MenuList>
    </Menu >
  </>
  );
}
export { FilterColumns };