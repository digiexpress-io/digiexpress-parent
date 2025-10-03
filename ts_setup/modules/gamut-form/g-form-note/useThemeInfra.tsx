import { generateUtilityClass, styled, useThemeProps, Alert } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useVariantOverride } from '@dxs-ts/gamut-api';
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


const useUtilityClasses = (ownerState: GFormNoteProps) => {
  const slots = {
    root: ['root', ownerState.id],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GFormNoteRoot = styled(Alert, {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: GFormNoteProps }>(({ theme, ownerState }) => {
  const severity = (ownerState.style ?? 'info') as 'error' | 'success' | 'warning' | 'info';
  const severityColor = theme.palette[severity]?.main ?? theme.palette.text.primary;

  return {
    display: 'flex',
    alignItems: 'center',

    '& .GMarkdown-root .MuiTypography-root': {
      marginBottom: '0 !important',
      color: severity === 'warning' ? theme.palette.text.primary : severityColor
    },
    '& .MuiAlert-icon .MuiIconButton-root .MuiSvgIcon-root': {
      color: severityColor,
    },
    '& .MuiPaper-root.MuiPopover-paper': {
      backgroundColor: 'pink',
      padding: theme.spacing(2)
    }
  };
});