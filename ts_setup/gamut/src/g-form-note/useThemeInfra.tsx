import { generateUtilityClass, styled, useThemeProps, Alert, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useVariantOverride } from '../api-variants';
import { GFormNoteProps } from './GFormNote';

const MUI_NAME = 'GFormNote';


export function useThemeInfra(initProps: GFormNoteProps) {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(props);
  const ownerState = { ...props };
  return { classes, ownerState, props };
}


// ------------------- MATERIAL INFRA, CSS CLASS NAMES FOR SELECTORS -------
const useUtilityClasses = (ownerState: GFormNoteProps) => {
  const slots = {
    root: ['root', ownerState.id],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GFormNoteRoot = styled(Alert, {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: GFormNoteProps }>(({ theme }) => {
  return {

  };
});