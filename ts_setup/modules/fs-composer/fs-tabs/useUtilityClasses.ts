import composeClasses from '@mui/utils/composeClasses';
import { generateUtilityClass, styled } from '@mui/material';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';
import { FsTabProps } from './FsTabProps';

export const MUI_NAME = 'FsTab';

export interface FsTabClasses {
  root: string;
  tabLight: string;
  tabDark: string;
}

export type FsTabClassKey = keyof FsTabClasses;

export const useUtilityClasses = (_props: FsTabProps) => {
  const slots = {
    root: ['root'],
    tab: ['tab'],
    active: ['tabActive'],
    inActive: ['tabInactive'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsTabRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => {

  const borderColor = ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border;
  
  return {
    height: 35,
    display: 'flex',
    borderBottom: `1px solid ${ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
    width: '100%',

    [`& .${MUI_NAME}-tabActive`]: {
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.surface : FsColors.light.background,
      borderBottom: `1px solid ${ownerState.isDarkMode ? FsColors.dark.surface : FsColors.light.background}`,
      marginBottom: '-1px'
    },

    [`& .${MUI_NAME}-tabInactive`]: {
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.background : FsColors.light.surface,
      borderBottom: 'none',
      marginBottom: 0
    },

    [`& .${MUI_NAME}-tab`]: {
      display: 'flex',
      alignItems: 'center',
      cursor: 'pointer',
      paddingLeft: theme.spacing(1),
      paddingRight: theme.spacing(1),
      minWidth: '10ch',
      maxWidth: '20ch',
      overflow: 'hidden',
      borderTop: `1px solid ${borderColor}`,
      borderRight: `1px solid ${borderColor}`,
    }
  };
});
