import { generateUtilityClass, Menu, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';



const MUI_NAME = 'EveliTableToolHeaderSearch';

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    menu: ['menu'],
    filterByString: ['filterByString']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

export const Root = styled('div', {
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
    },
    '& .MuiBadge-badge': {
      transform: 'translate(5px, 7px)',
      backgroundColor: theme.palette.error.main
    }
  };
});


export const MenuSlot = styled(Menu, {
  name: MUI_NAME,
  slot: 'Menu',
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




export const FilterByStringSlot = styled('div', {
  name: MUI_NAME,
  slot: 'FilterByString',
})(({ theme }) => {
  return {
    '.MuiFormControl-root': {
      margin: theme.spacing(1),
    },
    '.MuiInputBase-root': {
      paddingLeft: 0,
      fontSize: '10pt',
      height: '2rem',
      minWidth: '100%'
    },
    '.MuiInputBase-input': {
      padding: theme.spacing(0.5),
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
    '.MuiTypography-root': {
      paddingLeft: theme.spacing(1),
      paddingRight: theme.spacing(1),
      ...theme.typography.subtitle2,
    }
  }
});