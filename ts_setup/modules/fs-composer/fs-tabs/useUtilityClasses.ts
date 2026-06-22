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
  tabTypography: string;
  tabError: string;
}

export type FsTabClassKey = keyof FsTabClasses;

export const useUtilityClasses = (_props: FsTabProps) => {
  const slots = {
    root: ['root'],
    tab: ['tab'],
    active: ['tabActive'],
    inActive: ['tabInactive'],
    tabTypography: ['tabTypography'],
    tabError: ['tabError']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsTabRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => {

  const borderColor = FsColors.light.border;
  
  return {
    height: 35,
    display: 'flex',
    borderBottom: `1px solid ${FsColors.light.border}`,
    width: '100%',
    overflowX: 'auto',

    [`& .${MUI_NAME}-tabActive`]: {
      backgroundColor: FsColors.light.background,
      borderBottom: `1px solid ${FsColors.light.background}`,
      marginBottom: '-1px'
    },

    [`& .${MUI_NAME}-tabInactive`]: {
      backgroundColor: FsColors.light.surface,
      borderBottom: 'none',
      marginBottom: 0
    },

    [`& .${MUI_NAME}-tab`]: {
      display: 'flex',
      alignItems: 'center',
      cursor: 'pointer',
      paddingLeft: theme.spacing(1),
      paddingRight: theme.spacing(0.5),
      minWidth: '10ch',
      maxWidth: '20ch',
      overflow: 'hidden',
      borderTop: `1px solid ${borderColor}`,
      borderRight: `1px solid ${borderColor}`,
    },

    [`& .${MUI_NAME}-tabTypography`]: {
      flex: 1,
      minWidth: 0,
      display: 'flex',
      alignItems: 'center',
      color: FsColors.light.text,
    },

    [`& .${MUI_NAME}-tabTypography .MuiTypography-root`]: {
      overflow: 'hidden',
      textOverflow: 'ellipsis',
      whiteSpace: 'nowrap',
      fontWeight: 500,
    },

    [`& .${MUI_NAME}-tabError .${MUI_NAME}-tabTypography`]: {
      fontWeight: 500,
      color: FsColors.semantic.dangerLight,
    },

    [`& .MuiIconButton-root`]: {
      marginLeft: theme.spacing(0.5),
      padding: theme.spacing(0.25),
      '& .MuiSvgIcon-root': {
        fontSize: 'medium'
      }
    },
  };
});
