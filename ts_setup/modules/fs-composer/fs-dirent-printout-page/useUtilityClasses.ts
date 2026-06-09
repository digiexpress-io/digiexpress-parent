import { generateUtilityClass, styled, darken, lighten } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

const MUI_NAME = 'FsDirentPrintoutPage';

export interface FsDirentPrintoutPageClasses {
  root: string;
  title: string;
  formContainer: string;
  label: string;
  expandToggle: string;
  expandToggleIcon: string;
  expandToggleIconOpen: string;
  optionalFields: string;
  resourceList: string;
}

export type FsDirentPrintoutPageClassKey = keyof FsDirentPrintoutPageClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    formContainer: ['formContainer'],
    label: ['label'],
    expandToggle: ['expandToggle'],
    expandToggleIcon: ['expandToggleIcon'],
    expandToggleIconOpen: ['expandToggleIconOpen'],
    optionalFields: ['optionalFields'],
    resourceList: ['resourceList'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentPrintoutPageRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: { isDarkMode: boolean } }>(({ theme, ownerState }) => ({
  display: 'flex',
  flexDirection: 'column',

  [`& .${MUI_NAME}-title`]: {
    ...theme.typography.body1,
    marginBottom: theme.spacing(2),
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
  },

  [`& .${MUI_NAME}-formContainer`]: {
    display: 'flex',
    flexDirection: 'column',
    gap: theme.spacing(1.5),
    padding: '8px 0',
  },

  [`& .${MUI_NAME}-label`]: {
    ...theme.typography.subtitle2,
    fontWeight: 500,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
  },

  [`& .${MUI_NAME}-expandToggle`]: {
    ...theme.typography.subtitle2,
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    gap: theme.spacing(0.5),
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
  },

  [`& .${MUI_NAME}-expandToggleIcon`]: {
    transition: 'transform 0.2s',
  },

  [`& .${MUI_NAME}-expandToggleIconOpen`]: {
    transition: 'transform 0.2s',
    transform: 'rotate(180deg)',
  },

  [`& .${MUI_NAME}-resourceList`]: {
    display: 'flex',
    flexDirection: 'column',
    gap: theme.spacing(0.5),
  },

  [`& .${MUI_NAME}-optionalFields`]: {
    display: 'flex',
    flexDirection: 'column',
    gap: theme.spacing(1.5),
    paddingTop: theme.spacing(1),
  },
}));
