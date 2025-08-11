
import { styled, generateUtilityClass, alpha } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { GTooltipProps } from './GTooltip';


export const MUI_NAME = 'GTooltip';

export interface GTooltipClasses {
  root: string;
  icon: string;
  title: string;
}
export type GTooltipClassKey = keyof GTooltipClasses;



export const GTooltipRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',

  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.icon,
      styles.title
    ];
  },
})<{ ownerState: GTooltipProps }>(({ theme }) => {
  return {
    display: 'flex',
    alignItems: 'center',
    '& .GTooltip-icon': {
      fontSize: 'large',
      color: alpha(theme.palette.text.primary, 0.9),
      marginLeft: theme.spacing(1)
    }
  }
})




export const useUtilityClasses = (ownerState: GTooltipProps) => {
  const slots = {
    root: ['root'],
    icon: ['icon'],
    title: ['title']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}