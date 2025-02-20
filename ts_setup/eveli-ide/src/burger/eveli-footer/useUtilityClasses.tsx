
import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

import { EveliFooterProps } from './EveliFooter';
import { useVariantOverride } from '../api-variants';


export const MUI_NAME = 'EveliFooter';
export interface EveliFooterClasses {
  root: string;
}

export type EveliFooterClassKey = keyof EveliFooterClasses;


export const useUtilityClasses = (ownerState: EveliFooterProps) => {
  const slots = {
    root: ['root']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const EveliFooterRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: EveliFooterProps }>(({ theme }) => {
  return {
    display: 'flex',
    [theme.breakpoints.down('md')]: {
      flexDirection: 'column'
    },
    marginTop: 'auto',
    paddingLeft: theme.spacing(3),
    paddingRight: theme.spacing(3),
    paddingTop: theme.spacing(3),
    gap: theme.spacing(3),
    color: theme.palette.primary.contrastText,
    backgroundColor: theme.palette.primary.main
  };
});