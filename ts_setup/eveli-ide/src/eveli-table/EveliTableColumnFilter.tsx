import * as React from 'react';
import { generateUtilityClass, IconButton, InputAdornment, ListItemIcon, Menu, MenuItem, styled, TextField } from '@mui/material';
import FilterListIcon from '@mui/icons-material/FilterList';
import SearchIcon from '@mui/icons-material/Search';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import composeClasses from '@mui/utils/composeClasses';


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
      <IconButton
        onClick={handleClick}
        disableRipple
        disableFocusRipple
        sx={{
          padding: 0,
          '&:hover': {
            backgroundColor: 'transparent',
          },
        }}
      >
        <FilterListIcon />
      </IconButton>

      <StyledMenu anchorEl={anchorEl} open={open} onClose={handleClose}>
        <TextField placeholder='Search' slotProps={{
          input: {
            startAdornment: <InputAdornment position="start">
              <SearchIcon className='filters-adornment-icon' />
            </InputAdornment>
          }
        }}
        >
        </TextField>

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
  '.MuiFormControl-root': {
    margin: theme.spacing(1),
  },
  '.MuiInputBase-root': {
    paddingLeft: 0,
    minHeight: '1rem',
    fontSize: '10pt',
  },
  '.MuiInputBase-input': {
    padding: theme.spacing(1),
    height: 'auto',
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
