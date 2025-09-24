import { styled, useThemeProps } from "@mui/material";

import composeClasses from '@mui/utils/composeClasses';
import generateUtilityClass from '@mui/utils/generateUtilityClass';
import { GInputLabelProps } from "./GInputLabel";


const MUI_NAME = 'GInputLabel';


export function useThemeInfra(initProps: GInputLabelProps) {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  })
  
  const { braced = false } = props;

  const ownerState = {...props, braced }
  
  const classes = useUtilityClasses(ownerState);
  return { classes, ownerState, props };
}


// ------------------- MATERIAL INFRA, CSS CLASS NAMES FOR SELECTORS -------
const useUtilityClasses = (ownerState: GInputLabelProps) => {
  const slots = {
    root: ['root', ownerState.id],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GInputLabelRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
    ];
  },
  
})<{ ownerState: GInputLabelProps }>(({ theme, ownerState }) => {

  return {
    display: 'flex',
    flexDirection: 'row',
    height: '100%',
    width: '100%',

    [theme.breakpoints.up('md')]: {

      justifyContent: ownerState.labelPosition === 'label-left' ? 'flex-end' : 'flex-start',
      paddingRight: (ownerState.braced && ownerState.labelPosition === 'label-left') ? undefined : theme.spacing(2),
    },

    '& .MuiTypography-root': {
      paddingTop: (ownerState.braced && ownerState.labelPosition === 'label-left') ? theme.spacing(5.5) : undefined,
      alignSelf: (ownerState.braced && ownerState.labelPosition === 'label-left') ? undefined : 'center',
    }
  };
});