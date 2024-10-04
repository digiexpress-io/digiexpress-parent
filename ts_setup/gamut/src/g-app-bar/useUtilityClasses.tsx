import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { GAppBarProps } from './GAppBar';


export const MUI_NAME = 'GAppBar';

export interface GAppBarClasses {
  root: string;
  buttons: string;
}

export type GAppBarClassKey = keyof GAppBarClasses;

export const useUtilityClasses = (ownerState: GAppBarProps) => {
  const slots = {
    root: ['root'],
    buttons: ['buttons']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}



export const GAppBarRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      styles.buttons
    ];
  },
})<{ ownerState: GAppBarProps }>(({ theme }) => {
  return {
    display: 'flex',

    '& .GAppBar-buttons': {
      display: 'flex',
      [theme.breakpoints.down('md')]: {
        display: 'flex',
        width: '100%',
        justifyContent: 'space-between'
      },
    }
  };
});
