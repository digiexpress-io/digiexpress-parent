import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';

const MUI_NAME = 'FsDirentHistory';

export interface FsDirentHistoryClasses {
  root: string;
  title: string;
  tableContainer: string;
  tableRow: string;
  tableHeader: string;
  tableCell: string;
  divider: string;
}

export type FsDirentHistoryClassKey = keyof FsDirentHistoryClasses;

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

export const FsDirentHistoryRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => ({
  display: 'flex',
  flexDirection: 'column',

  [`& .${MUI_NAME}-title`]: {
    ...theme.typography.caption,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
  },

  [`& .${MUI_NAME}-tableContainer`]: {
    display: 'flex',
    flexDirection: 'column',
    border: `1px solid ${ownerState.isDarkMode ? '#555555' : '#cccccc'}`,

    // Every even row background (excluding header)
    '& > div:nth-of-type(even)': {
      [`& .${MUI_NAME}-tableCell`]: {
        backgroundColor: ownerState.isDarkMode ? '#292828' : '#f5f5f5',
      },
    },
  },

  [`& .${MUI_NAME}-tableRow`]: {
    display: 'flex',
    width: '100%',
  },

  [`& .${MUI_NAME}-tableHeader`]: {
    backgroundColor: ownerState.isDarkMode ? '#2d2d30' : '#e0e0e0',
    color: ownerState.isDarkMode ? '#cccccc' : '#333333',
    fontSize: '10px',
    fontWeight: 500,
    padding: '4px 6px',
    flex: 1,
  },

  [`& .${MUI_NAME}-tableCell`]: {
    backgroundColor: ownerState.isDarkMode ? '#3c3c3c' : '#ffffff',
    color: ownerState.isDarkMode ? '#cccccc' : '#333333',
    fontSize: '10px',
    padding: '2px 6px',
    flex: 1,
  },

  [`& .${MUI_NAME}-divider`]: {
    height: '1px',
    backgroundColor: ownerState.isDarkMode ? '#555555' : '#cccccc',
  },
}));