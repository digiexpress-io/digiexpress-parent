import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';

const MUI_NAME = 'FsPanelOverview';
const CONFIG_COL_WIDTH = '120px';
const UPDATED_COL_WIDTH = '140px'

export interface FsPanelOverviewClasses {
  root: string;
  header: string;
  headerName: string;
  headerDate: string;
  headerConfigOptions: string;
  container: string;
  row: string;
  childRow: string;
  childIcon: string;
  typeIcon: string;
  configOptionsCell: string;
  configIcon: string;
  name: string;
  parentName: string;
  date: string;
}

export type FsPanelOverviewClassKey = keyof FsPanelOverviewClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    header: ['header'],
    headerName: ['headerName'],
    headerDate: ['headerDate'],
    headerConfigOptions: ['headerConfigOptions'],
    container: ['container'],
    row: ['row'],
    childRow: ['childRow'],
    childIcon: ['childIcon'],
    typeIcon: ['typeIcon'],
    configOptionsCell: ['configOptionsCell'],
    configIcon: ['configIcon'],
    name: ['name'],
    parentName: ['parentName'],
    date: ['date'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsPanelOverviewRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => {
  const borderColor       = ownerState.isDarkMode ? FsColors.dark.border       : FsColors.light.border;
  const surfaceColor      = ownerState.isDarkMode ? FsColors.dark.surface      : FsColors.light.surface;
  const bgColor           = ownerState.isDarkMode ? FsColors.dark.background   : FsColors.light.background;
  const textColor         = ownerState.isDarkMode ? FsColors.dark.text         : FsColors.light.text;
  const textSecondaryColor = ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary;

  return {
    [`& .${MUI_NAME}-header`]: {
      display: 'flex',
      padding: theme.spacing(0.75, 1.5),
      borderBottom: `1px solid ${borderColor}`,
      backgroundColor: surfaceColor,
    },

    [`& .${MUI_NAME}-headerName`]: {
      ...theme.typography.caption,
      color: textSecondaryColor,
      flex: 1,
      fontWeight: 600,
      textTransform: 'uppercase',
    },

    [`& .${MUI_NAME}-headerConfigOptions`]: {
      ...theme.typography.caption,
      color: textSecondaryColor,
      fontWeight: 600,
      textTransform: 'uppercase',
      width: CONFIG_COL_WIDTH,
      flexShrink: 0,
    },

    [`& .${MUI_NAME}-headerDate`]: {
      ...theme.typography.caption,
      color: textSecondaryColor,
      fontWeight: 600,
      textTransform: 'uppercase',
      textAlign: 'right',
      width: UPDATED_COL_WIDTH,
      flexShrink: 0,
    },

    [`& .${MUI_NAME}-container`]: {
      display: 'flex',
      flexDirection: 'column',
      border: `1px solid ${borderColor}`,

      '& > div:nth-of-type(odd)': {
        backgroundColor: surfaceColor,
      },
    },

    [`& .${MUI_NAME}-row`]: {
      display: 'flex',
      overflow: 'hidden',
      padding: theme.spacing(1, 1.5),
      backgroundColor: bgColor,
      borderBottom: `1px solid ${borderColor}`,

      '&:last-child': {
        borderBottom: 'none',
      },
    },

    [`& .${MUI_NAME}-childRow`]: {
      display: 'flex',
      overflow: 'hidden',
      padding: theme.spacing(1, 1.5, 1, 2.5),
      backgroundColor: bgColor,
      borderBottom: `1px solid ${borderColor}`,

      '&:last-child': {
        borderBottom: 'none',
      },
    },

    [`& .${MUI_NAME}-childIcon`]: {
      color: textSecondaryColor,
      flexShrink: 0,
      alignSelf: 'center',
      marginRight: theme.spacing(0.5),
    },

    [`& .${MUI_NAME}-typeIcon`]: {
      color: textSecondaryColor,
      flexShrink: 0,
      alignSelf: 'center',
      marginRight: theme.spacing(0.5),
    },

    [`& .${MUI_NAME}-configOptionsCell`]: {
      display: 'flex',
      alignItems: 'center',
      gap: theme.spacing(0.5),
      width: CONFIG_COL_WIDTH,
      flexShrink: 0,
    },

    [`& .${MUI_NAME}-configIcon`]: {
      color: textSecondaryColor,
      flexShrink: 0,
    },

    [`& .${MUI_NAME}-name`]: {
      ...theme.typography.subtitle2,
      color: textColor,
      flex: 1,
      minWidth: 0,
      overflow: 'hidden',
      textOverflow: 'ellipsis',
      whiteSpace: 'nowrap',
    },

    [`& .${MUI_NAME}-parentName`]: {
      ...theme.typography.subtitle2,
      color: textColor,
      flex: 1,
      fontWeight: 600,
      minWidth: 0,
      overflow: 'hidden',
      textOverflow: 'ellipsis',
      whiteSpace: 'nowrap',
    },

    [`& .${MUI_NAME}-date`]: {
      ...theme.typography.subtitle2,
      color: textSecondaryColor,
      textAlign: 'right',
      width: UPDATED_COL_WIDTH,
      flexShrink: 0,
    },
  };
});
