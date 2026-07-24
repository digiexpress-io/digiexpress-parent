import { generateUtilityClass, styled, Dialog } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

const MUI_NAME = 'FsDirentSelectGrouped';

export interface FsDirentSelectGroupedClasses {
  root: string;
  dialogTitle: string;
  titleRow: string;
  selectedLabel: string;
  selectedBox: string;
  groupLabel: string;
  groupDivider: string;
  sectionDivider: string;
  formControlLabel: string;
  searchField: string;
  dialogContent: string;
}

export type FsDirentSelectGroupedClassKey = keyof FsDirentSelectGroupedClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    dialogTitle: ['dialogTitle'],
    titleRow: ['titleRow'],
    selectedLabel: ['selectedLabel'],
    selectedBox: ['selectedBox'],
    groupLabel: ['groupLabel'],
    groupDivider: ['groupDivider'],
    sectionDivider: ['sectionDivider'],
    formControlLabel: ['formControlLabel'],
    searchField: ['searchField'],
    dialogContent: ['dialogContent'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentSelectGroupedRoot = styled(Dialog, {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})(({ theme }) => ({
  [`& .${MUI_NAME}-dialogTitle`]: {
    paddingBottom: theme.spacing(1),
  },

  [`& .${MUI_NAME}-titleRow`]: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: theme.spacing(1),
  },

  [`& .${MUI_NAME}-selectedLabel`]: {
    fontWeight: 500,
    display: 'block',
    marginBottom: theme.spacing(0.5),
  },

  [`& .${MUI_NAME}-selectedBox`]: {
    backgroundColor: theme.palette.action.hover,
    borderRadius: theme.shape.borderRadius,
    paddingTop: theme.spacing(1),
    paddingBottom: theme.spacing(1),
    paddingLeft: theme.spacing(1.5),
    paddingRight: theme.spacing(1.5),
    marginBottom: theme.spacing(1.5),
  },

  [`& .${MUI_NAME}-groupLabel`]: {
    fontWeight: 500,
    display: 'block',
    marginBottom: theme.spacing(0.5),
  },

  [`& .${MUI_NAME}-groupDivider`]: {
    marginTop: theme.spacing(1),
    marginBottom: theme.spacing(1),
  },

  [`& .${MUI_NAME}-sectionDivider`]: {
    marginBottom: theme.spacing(1.5),
  },

  [`& .${MUI_NAME}-formControlLabel`]: {
    display: 'flex',
    marginLeft: 0,
  },

  [`& .${MUI_NAME}-searchField`]: {
    marginTop: theme.spacing(1),
  },

  [`& .${MUI_NAME}-dialogContent`]: {
    height: '80vh',
    overflowY: 'auto',
  },
}));
