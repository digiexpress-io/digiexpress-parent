import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';

const MUI_NAME = 'FsDirentCreateLanguage';

export interface FsDirentCreateLanguageClasses {
  root: string;
  title: string;
  formContainer: string;
  label: string;
  configOptionDescription: string;
  buttonContainer: string;
}

export type FsDirentCreateLanguageClassKey = keyof FsDirentCreateLanguageClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    formContainer: ['formContainer'],
    label: ['label'],
    configOptionDescription: ['configOptionDescription'],
    buttonContainer: ['buttonContainer'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentCreateLanguageRoot = styled('div', {
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

  [`& .${MUI_NAME}-configOptionDescription`]: {
    ...theme.typography.caption,
    color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
    '&.MuiTypography-root': {
      color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
    },
  },

  [`& .${MUI_NAME}-buttonContainer`]: {
    display: 'flex',
    gap: '12px',
    marginTop: '16px',
    justifyContent: 'flex-end',
  },

}));
