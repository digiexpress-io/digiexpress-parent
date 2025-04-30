import { generateUtilityClass, Menu, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';



const MUI_NAME = 'EveliTableToolHeaderSort';
export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    menu: ['menu'],
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
  '.menu-icon': {
    color: theme.palette.primary.main,
    fontSize: 'medium'
  }
}
));
