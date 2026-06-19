import { generateUtilityClass, styled, darken, lighten } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

const MUI_NAME = 'FsDirentFlow';

export interface FsDirentFlowClasses {
  root: string;
  titleRow: string;
  title: string;
  formContainer: string;
  sectionTitle: string;
  sectionBox: string;
  sectionContent: string;
  editor: string;
}

export type FsDirentFlowClassKey = keyof FsDirentFlowClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    titleRow: ['titleRow'],
    title: ['title'],
    formContainer: ['formContainer'],
    sectionTitle: ['sectionTitle'],
    sectionBox: ['sectionBox'],
    sectionContent: ['sectionContent'],
    editor: ['editor'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentFlowRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: { isDarkMode: boolean } }>(({ theme, ownerState }) => ({
  display: 'flex',
  flexDirection: 'column',
  height: '100%',

  [`& .${MUI_NAME}-titleRow`]: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: theme.spacing(2),
  },

  [`& .${MUI_NAME}-title`]: {
    ...theme.typography.body1,
    fontWeight: 500,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    marginBottom: theme.spacing(2)
  },

  [`& .${MUI_NAME}-formContainer`]: {
    display: 'flex',
    flexDirection: 'column',
    flex: 1,
    minHeight: 0,
  },

}));
