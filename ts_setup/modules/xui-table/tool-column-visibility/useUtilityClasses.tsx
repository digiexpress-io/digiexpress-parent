import { generateUtilityClass, styled } from '@mui/material';

import composeClasses from '@mui/utils/composeClasses';


const ToolColumnVisibilityRootClassName = 'ToolColumnVisibilityRoot';

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    columnSlot: ['columnSlot']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(ToolColumnVisibilityRootClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}




export const Root = styled('div', {
  name: ToolColumnVisibilityRootClassName,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})(({ theme }) => {

  return {
    width: '100%',
    padding: theme.spacing(1),
    display: 'flex',
    alignItems: 'left',
    justifySelf: 'flex-start',
    flexDirection: 'column'
  };
});


export const ColumnSlot = styled('div', {
  name: ToolColumnVisibilityRootClassName,
  slot: 'ColumnSlot',
})(({ theme }) => {

  return {
    display: 'flex',
    flexDirection: 'row',
    alignItems: 'center',
    margin: theme.spacing(0.5),
    '.MuiTypography-root': {
      fontSize: '10pt',
      fontWeight: 400,
    },
    '.cols-select-checkmark-icon': {
      marginLeft: theme.spacing(1),
      marginRight: theme.spacing(2),
      color: theme.palette.primary.main,
      fontSize: 'medium',
    },
  };
});

