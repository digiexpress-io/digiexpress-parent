import { generateUtilityClass, styled, darken } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';

const MUI_NAME = 'FsDirentCreatePhone';

export interface FsDirentCreatePhoneClasses {
  root: string;
  title: string;
  formContainer: string;
  expandToggle: string;
  expandToggleIcon: string;
  expandToggleIconOpen: string;
  optionalFields: string;
  label: string;
  localeLabel: string;
  localeRow: string;
  configRow: string;
  configLabel: string;
  switchRoot: string;
  buttonContainer: string;
}

export type FsDirentCreatePhoneClassKey = keyof FsDirentCreatePhoneClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    formContainer: ['formContainer'],
    label: ['label'],
    localeLabel: ['localeLabel'],
    localeRow: ['localeRow'],
    expandToggle: ['expandToggle'],
    expandToggleIcon: ['expandToggleIcon'],
    expandToggleIconOpen: ['expandToggleIconOpen'],
    optionalFields: ['optionalFields'],
    configRow: ['configRow'],
    configLabel: ['configLabel'],
    switchRoot: ['switchRoot'],
    buttonContainer: ['buttonContainer'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentCreatePhoneRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => ({
  display: 'flex',
  flexDirection: 'column',

  [`& .${MUI_NAME}-title`]: {
    ...theme.typography.body1,
    fontWeight: 500,
    marginBottom: theme.spacing(2),
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    '&.MuiTypography-root': {
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    },
  },

  [`& .${MUI_NAME}-formContainer`]: {
    display: 'flex',
    flexDirection: 'column',
    gap: theme.spacing(1.5),
    padding: '8px 0',
  },

  [`& .${MUI_NAME}-label`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    '&.MuiTypography-root': {
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    },
  },

  [`& .${MUI_NAME}-localeLabel`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    '&.MuiTypography-root': {
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    },
  },

  [`& .${MUI_NAME}-localeRow`]: {
    display: 'flex',
    flexDirection: 'column',
    gap: theme.spacing(0.5),
  },

  [`& .${MUI_NAME}-expandToggle`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.direntTypes.dark.form : (darken(FsColors.direntTypes.light.form, 0.1)),
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    gap: theme.spacing(0.5),
    fontWeight: 'bold',
    '&:hover': {
      opacity: 0.8,
    },
  },

  [`& .${MUI_NAME}-expandToggleIcon`]: {
    transition: 'transform 200ms ease',
    transform: 'rotate(0deg)',
  },

  [`& .${MUI_NAME}-expandToggleIconOpen`]: {
    transition: 'transform 200ms ease',
    transform: 'rotate(180deg)',
  },

  [`& .${MUI_NAME}-optionalFields`]: {
    display: 'flex',
    flexDirection: 'column',
    gap: theme.spacing(1.5),
  },

  [`& .${MUI_NAME}-configRow`]: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
  },

  [`& .${MUI_NAME}-configLabel.MuiTypography-root`]: {
    ...theme.typography.caption,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
  },

  [`& .${MUI_NAME}-switchRoot`]: {
    '& .MuiSwitch-track': {
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
    },
    '& .MuiSwitch-thumb': {
      color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
    },
    '& .MuiSwitch-colorPrimary.Mui-checked': {
      color: ownerState.isDarkMode ? FsColors.direntTypes.dark.form : FsColors.direntTypes.light.form,
    },
    '& .MuiSwitch-colorPrimary.Mui-checked + .MuiSwitch-track': {
      backgroundColor: ownerState.isDarkMode ? FsColors.direntTypes.dark.form : FsColors.direntTypes.light.form,
    },
  },

  [`& .${MUI_NAME}-buttonContainer`]: {
    display: 'flex',
    gap: '12px',
    marginTop: '16px',
    justifyContent: 'flex-end',
  },
}));
