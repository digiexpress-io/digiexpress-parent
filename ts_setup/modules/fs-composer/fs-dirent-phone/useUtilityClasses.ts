import { generateUtilityClass, styled, darken } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

const MUI_NAME = 'FsDirentPhone';

export interface FsDirentPhoneClasses {
  root: string;
  title: string;
  formContainer: string;
  expandToggle: string;
  expandToggleIcon: string;
  expandToggleIconOpen: string;
  optionalFields: string;
  label: string;
  localeRow: string;
  localeLabel: string;
  sectionTitle: string;
  sectionBox: string;
  sectionContent: string;
  buttonContainer: string;
}

export type FsDirentPhoneClassKey = keyof FsDirentPhoneClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    formContainer: ['formContainer'],
    expandToggle: ['expandToggle'],
    expandToggleIcon: ['expandToggleIcon'],
    expandToggleIconOpen: ['expandToggleIconOpen'],
    optionalFields: ['optionalFields'],
    label: ['label'],
    localeRow: ['localeRow'],
    localeLabel: ['localeLabel'],
    sectionTitle: ['sectionTitle'],
    sectionBox: ['sectionBox'],
    sectionContent: ['sectionContent'],
    buttonContainer: ['buttonContainer'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentPhoneRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: { isDarkMode: boolean } }>(({ theme, ownerState }) => ({
  display: 'flex',
  flexDirection: 'column',

  [`& .${MUI_NAME}-title`]: {
    ...theme.typography.body1,
    fontWeight: 500,
    marginBottom: theme.spacing(2),
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
  },

  [`& .${MUI_NAME}-formContainer`]: {
    display: 'flex',
    flexDirection: 'column',
    gap: theme.spacing(1.5),
    padding: '8px 0',
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

  [`& .${MUI_NAME}-label`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
  },

  [`& .${MUI_NAME}-localeRow`]: {
    display: 'flex',
    flexDirection: 'column',
    gap: theme.spacing(0.5),
  },

  [`& .${MUI_NAME}-localeLabel`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
  },

  [`& .${MUI_NAME}-sectionTitle`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    marginBottom: '8px',
    display: 'block',
  },

  [`& .${MUI_NAME}-sectionBox`]: {
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
    border: `1px solid ${ownerState.isDarkMode ? FsColors.light.textSecondary : FsColors.dark.textSecondary}`,
    borderRadius: '4px',
    padding: '12px',
    marginTop: '0px !important',
  },

  [`& .${MUI_NAME}-sectionContent`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
  },

  [`& .${MUI_NAME}-buttonContainer`]: {
    display: 'flex',
    gap: '12px',
    marginTop: '16px',
    justifyContent: 'flex-end',
  },

}));
