import * as React from 'react';
import { Divider, generateUtilityClass, IconButton, ListItemIcon, Menu, MenuItem, styled } from '@mui/material';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';
import TableChartOutlinedIcon from '@mui/icons-material/TableChartOutlined';
import RestartAltIcon from '@mui/icons-material/RestartAlt';
import composeClasses from '@mui/utils/composeClasses';

interface EveliTableColumnProps {
  onChooseCols: () => void,
  onSortAsc: () => void,
  onSortDesc: () => void,
}

export const EveliTableColumnOptions: React.FC<EveliTableColumnProps> = ({ onChooseCols, onSortAsc, onSortDesc }) => {
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);

  function handleClick(event: React.MouseEvent<HTMLButtonElement>) {
    setAnchorEl(event.currentTarget);
  };
  function handleClose() {
    setAnchorEl(null);
  };

  function handleChooseCols() {
    onChooseCols();
    handleClose();
  }

  function handleSortAsc() {
    onSortAsc();
    handleClose();
  }

  function handleSortDesc() {
    onSortDesc();
    handleClose();
  }

  const classes = useUtilityClasses();
  return (
    <EveliTableColumnOptionsRoot className={classes.root}>
      <IconButton onClick={handleClick} disableRipple disableFocusRipple>
        <MoreVertIcon />
      </IconButton>

      <StyledMenu anchorEl={anchorEl} open={open} onClose={handleClose}>

        <MenuItem onClick={handleSortAsc}>
          <ListItemIcon><ArrowUpwardIcon className='menu-icon' /></ListItemIcon>
          Sort ascending
        </MenuItem>

        <MenuItem onClick={handleSortDesc}>
          <ListItemIcon><ArrowDownwardIcon className='menu-icon' /></ListItemIcon>
          Sort descending
        </MenuItem>

        <Divider />

        <MenuItem onClick={handleChooseCols}>
          <ListItemIcon><TableChartOutlinedIcon className='menu-icon' /></ListItemIcon>
          Choose columns
        </MenuItem>
        <MenuItem onClick={handleClose}>
          <ListItemIcon><RestartAltIcon className='menu-icon' /></ListItemIcon>
          Reset columns
        </MenuItem>
      </StyledMenu>
    </EveliTableColumnOptionsRoot>
  );
}


const MUI_NAME = 'EveliTableColumnOptions';
const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

const EveliTableColumnOptionsRoot = styled('div', {
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
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },
})(({ theme }) => ({
  '& .MuiPaper-root': {
    backgroundColor: 'white',
    borderRadius: theme.spacing(1),
  },
  '& .MuiMenuItem-root': {
    fontSize: '10pt',
    fontWeight: 400
  },
  '.menu-icon': {
    color: theme.palette.primary.main,
    fontSize: 'medium'
  }
}
));
