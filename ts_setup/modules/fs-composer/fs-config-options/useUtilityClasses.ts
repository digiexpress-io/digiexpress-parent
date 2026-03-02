import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';


const MUI_NAME = 'FsConfigOptions';

export interface FsConfigOptionsClasses {
  root: string;
  optionItem: string;
  optionHeader: string;
  optionTitle: string;
  optionDescription: string;
  divider: string;
}

export type FsConfigOptionsClassKey = keyof FsConfigOptionsClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    optionItem: ['optionItem'],
    optionHeader: ['optionHeader'],
    optionTitle: ['optionTitle'],
    optionDescription: ['optionDescription'],
    divider: ['divider'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsConfigOptionsRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'isDarkMode',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => ({
  display: 'flex',
  flexDirection: 'column',

  [`& .${MUI_NAME}-optionItem`]: {
    marginBottom: theme.spacing(1),
  },

  [`& .${MUI_NAME}-optionHeader`]: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
  },

  [`& .${MUI_NAME}-optionTitle`]: {
    fontWeight: 'bold',
  },

  [`& .${MUI_NAME}-optionDescription`]: {
    color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
  },

  [`& .${MUI_NAME}-divider`]: {
    marginTop: theme.spacing(1),
  },
}));