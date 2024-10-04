import { generateUtilityClass, styled, useThemeProps, lighten } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useVariantOverride } from '../api-variants';
import { GFormStepperProps } from './GFormStepper';

const MUI_NAME = 'GFormStepper';


export function useThemeInfra(initProps: GFormStepperProps) {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const pages = props.totalPages + 1;
  const activeStep = props.pageNumber;
  const progress = -1 * 100 / pages * activeStep;

  const classes = useUtilityClasses(props);
  const ownerState = { 
    ...props, 
    pages, activeStep, progress
   };
  return { classes, ownerState, props };
}


// ------------------- MATERIAL INFRA, CSS CLASS NAMES FOR SELECTORS -------
const useUtilityClasses = (ownerState: GFormStepperProps) => {
  const slots = {
    root: ['root'],
    progress: ['progress'],
    label: ['label'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GFormStepperRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: GFormStepperProps }>(({ theme }) => {
  return {
    position: 'relative', 
    display: 'inline-flex',
    '& .GFormStepper-progress': {
      borderRadius: '50%',
      boxShadow: `inset 0 0 0 7px ${lighten(theme.palette.warning.main, 0.7)}`,
    },
    '& .GFormStepper-label': {
      top: 0,
      left: 0,
      bottom: 0,
      right: 0,
      cursor: 'pointer',
      position: 'absolute',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
    }
  };
});