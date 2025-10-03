import { generateUtilityClass, styled, useThemeProps, Alert, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useVariantOverride } from '@dxs-ts/gamut-api';
import { GFormNoteValidationProps } from './GFormNoteValidation';

const MUI_NAME = 'GFormNoteValidation';


export function useThemeInfra(initProps: GFormNoteValidationProps) {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(props);
  const ownerState = { ...props };
  return { classes, ownerState, props };
}


// ------------------- MATERIAL INFRA, CSS CLASS NAMES FOR SELECTORS -------
const useUtilityClasses = (ownerState: GFormNoteValidationProps) => {
  const slots = {
    root: ['root', ownerState.id],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GFormNoteValidationRoot = styled(Alert, {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: GFormNoteValidationProps }>(({ theme }) => {
  return {

  };
});