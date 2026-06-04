import { generateUtilityClass, styled, darken, lighten } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

const MUI_NAME = 'FsDirentFlowTask';

export interface FsDirentFlowTaskClasses {
  root: string;
  title: string;
  formContainer: string;
  label: string;
  editor: string;
  buttonContainer: string;
  expandToggle: string;
  expandToggleIcon: string;
  expandToggleIconOpen: string;
  optionalFields: string;
}

export type FsDirentFlowTaskClassKey = keyof FsDirentFlowTaskClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    formContainer: ['formContainer'],
    label: ['label'],
    editor: ['editor'],
    buttonContainer: ['buttonContainer'],
    expandToggle: ['expandToggle'],
    expandToggleIcon: ['expandToggleIcon'],
    expandToggleIconOpen: ['expandToggleIconOpen'],
    optionalFields: ['optionalFields'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentFlowTaskRoot = styled('div', {
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

  [`& .${MUI_NAME}-label`]: {
    ...theme.typography.subtitle2,
    fontWeight: 500,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
  },

  [`& .${MUI_NAME}-editor`]: {
    height: '500px',
    border: `1px solid ${ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
    borderRadius: '4px',
  },

  [`& .${MUI_NAME}-buttonContainer`]: {
    display: 'flex',
    gap: '12px',
    marginTop: '16px',
    justifyContent: 'flex-end',
  },

  [`& .${MUI_NAME}-expandToggle`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.direntTypes.dark.form : darken(FsColors.direntTypes.light.form, 0.1),
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
    backgroundColor: ownerState.isDarkMode ? lighten(FsColors.dark.background, 0.05) : darken(FsColors.light.background, 0.06),
    border: ownerState.isDarkMode ? `1px solid ${FsColors.dark.border}` : `1px solid ${FsColors.light.border}`,
    padding: theme.spacing(1.5),
  },

}));
