
import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

const FiltersRootClassName = 'EveliTableDrawerSavedFilters';

export const Root = styled('div', {
  name: FiltersRootClassName,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.optionButtons
    ];
  },

})(({ theme }) => {
  return {
    width: '100%',
    padding: theme.spacing(1),
    gap: theme.spacing(1),
    display: 'flex',
    alignItems: 'left',
    flexDirection: 'column',
    '& .EveliTableDrawerSavedFilters-optionButtons': {
      display: 'flex',
      justifyContent: 'center',
      gap: theme.spacing(1),
    }
  };
});


export const StyledFilterItem = styled('div', {
  name: FiltersRootClassName,
  slot: 'FilterItem',
})(({ theme }) => {

  return {
    paddingTop: theme.spacing(1),
    paddingRight: theme.spacing(1),
    display: 'flex',
    flexDirection: 'row',
    alignItems: 'center',

    '& .EveliTableDrawerSavedFilters-activeFilter.MuiSvgIcon-root': {
      color: theme.palette.success.main
    },

    '& .MuiSvgIcon-root': {
      fontSize: '15pt',
      color: theme.palette.primary.main,
      marginRight: theme.spacing(1),
      ':hover': {
        backgroundColor: theme.palette.secondary.dark,
        borderRadius: theme.spacing(0.5),
        cursor: 'pointer'
      },

    },
    '& .MuiListItem-root': {
      display: 'flex',
      alignItems: 'center'
    }
  };
});


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    optionButtons: ['optionButtons'],
    activeFilter: ['activeFilter']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(FiltersRootClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}
