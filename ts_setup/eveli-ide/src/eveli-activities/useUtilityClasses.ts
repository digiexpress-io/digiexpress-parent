import { alpha, generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useVariantOverride } from '../api-variants';


export const MUI_NAME = 'EveliActivities';
export interface EveliActivitiesClasses {
  root: string;
}

export type EveliActivitiesClassKey = keyof EveliActivitiesClasses;


export const useUtilityClasses = () => {
  const slots = {
    root: ['root']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const EveliActivitiesRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})(({ theme }) => {
  return {

    margin: 1,
    display: 'flex',
    flexWrap: 'wrap',
    justifyContent: 'center',

    '.MuiCard-root': {
      margin: 30,
      width: '20vw',
      display: 'flex',
      flexDirection: 'column',
    },

    '.MuiCardHeader-root .MuiCardHeader-title': {
      ...theme.typography.h2,
      display: 'flex',
      justifyContent: 'center',
      fontWeight: 'bold', 
      padding: theme.spacing(1),
    },


    '.MuiCardContent-root': {
      ...theme.typography.body2,
      flexGrow: 1, 
      padding: theme.spacing(2), 
      height: 'fit-content'
    },

    '.MuiCardActions-root': {
      alignSelf: "flex-end"
    }
  };
});