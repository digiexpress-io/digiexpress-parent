import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';

const MUI_NAME = 'FsDirentReferences';

export interface FsDirentReferencesClasses {
  root: string;
  title: string;
  tableContainer: string;
  tableRow: string;
  tableHeader: string;
  tableCell: string;
  divider: string;
}

export type FsDirentReferencesClassKey = keyof FsDirentReferencesClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    tableContainer: ['tableContainer'],
    tableRow: ['tableRow'],
    tableHeader: ['tableHeader'],
    tableCell: ['tableCell'],
    divider: ['divider'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentReferencesRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => ({
  display: 'flex',
  flexDirection: 'column',

  [`& .${MUI_NAME}-title`]: {
    ...theme.typography.caption,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    marginBottom: theme.spacing(1),
  },

  [`& .${MUI_NAME}-tableContainer`]: {
    display: 'flex',
    flexDirection: 'column',
    border: `1px solid ${ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border}`,

    '& > div:nth-of-type(even)': {
      '& > div': {
        backgroundColor: ownerState.isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
      },
    },
  },

  [`& .${MUI_NAME}-tableRow`]: {
    display: 'flex',
    width: '100%',
  },

  [`& .${MUI_NAME}-tableHeader`]: {
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    fontSize: '10px',
    fontWeight: 500,
    padding: theme.spacing(0.5, 0.75),
    flex: 1,
  },

  [`& .${MUI_NAME}-tableCell`]: {
    backgroundColor:ownerState.isDarkMode ? FsColors.dark.background : FsColors.light.background,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    fontSize: '10px',
    padding: theme.spacing(0.25, 0.75),
    flex: 1,
  },

  [`& .${MUI_NAME}-divider`]: {
    height: '1px',
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
  },
}));