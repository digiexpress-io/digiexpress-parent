import * as React from 'react';
import { generateUtilityClass, IconButton, ListItemIcon, Menu, MenuItem, styled } from '@mui/material';
import FilterListIcon from '@mui/icons-material/FilterList';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import composeClasses from '@mui/utils/composeClasses';

import { EveliTableSearchField } from './EveliTableSearchField';


export const EveliTableColumnFilter: React.FC = () => {
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);

  const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleClose = () => {
    setAnchorEl(null);
  };

  const classes = useUtilityClasses();
  return (
    <EveliTableColumnFilterRoot className={classes.root}>
      <IconButton onClick={handleClick} disableRipple disableFocusRipple>
        <FilterListIcon />
      </IconButton>

      <StyledMenu anchorEl={anchorEl} open={open} onClose={handleClose}>
        <EveliTableSearchField />

        <MenuItem onClick={handleClose}>
          <ListItemIcon><CheckBoxOutlineBlankIcon className='filters-icon' /></ListItemIcon>
          Select all
        </MenuItem>

        <MenuItem onClick={handleClose}>
          <ListItemIcon><CheckBoxOutlineBlankIcon className='filters-icon' /></ListItemIcon>
          Column 1
        </MenuItem>

        <MenuItem onClick={handleClose}>
          <ListItemIcon><CheckBoxOutlineBlankIcon className='filters-icon' /></ListItemIcon>
          Column 2
        </MenuItem>


      </StyledMenu>
    </EveliTableColumnFilterRoot>
  );
}


const MUI_NAME = 'EveliTableColumnFilter';
const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

const EveliTableColumnFilterRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },

})(({ theme }) => {

  return {
    '.MuiIconButton-root': {
      padding: 0,
      '&:hover': {
        backgroundColor: 'transparent',
      }
    },
    '.MuiSvgIcon-root': {
      ':hover': {
        backgroundColor: theme.palette.secondary.dark,
        borderRadius: theme.spacing(0.5)
      }
    }

  };
});


const StyledMenu = styled(Menu, {
  name: MUI_NAME,
  slot: 'MenuContainer',
  overridesResolver: (_props, styles) => styles.root,
})(({ theme }) => ({
  '& .MuiPaper-root': {
    backgroundColor: 'white',
    borderRadius: theme.spacing(1),
  },
  '& .MuiMenuItem-root': {
    fontSize: '10pt',
    fontWeight: 400
  },
  '.filters-icon': {
    color: theme.palette.primary.main,
    fontSize: 'medium'
  },
  '.filters-adornment-icon': {
    marginLeft: theme.spacing(1),
    color: theme.palette.primary.main,
    fontSize: 'medium',
  },

}));
