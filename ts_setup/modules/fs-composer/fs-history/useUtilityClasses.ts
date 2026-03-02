import { generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { FsColors } from "../fs-theme";
import { OwnerState } from "./useOwnerState";

const MUI_NAME = 'FsHistory';

export interface FsHistoryClasses {
  root: string;
  section: string;
  caption: string;
  container: string;
  row: string;
  user: string;
  change: string;
  date: string;
}

export type FsHistoryClassKey = keyof FsHistoryClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    section: ['section'],
    caption: ['caption'],
    container: ['container'],
    row: ['row'],
    user: ['user'],
    change: ['change'],
    date: ['date'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsHistoryRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => ({
  [`& .${MUI_NAME}-section`]: {
    marginBottom: theme.spacing(2),
  },

  [`& .${MUI_NAME}-caption`]: {
    marginBottom: theme.spacing(1),
    display: 'block',
  },

  [`& .${MUI_NAME}-container`]: {
    display: 'flex',
    flexDirection: 'column',
    border: `1px solid ${ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border}`,

    '& > div:nth-of-type(odd)': {
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
    },
  },

  [`& .${MUI_NAME}-row`]: {
    display: 'flex',
    padding: theme.spacing(1, 1.5),
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.background : FsColors.light.background,
    borderBottom: `1px solid ${ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border}`,

    '&:last-child': {
      borderBottom: 'none'
    }
  },

  [`& .${MUI_NAME}-user`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    flex: 1,
    fontSize: '12px',
    fontWeight: 500,
  },

  [`& .${MUI_NAME}-change`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    flex: 2,
    fontSize: '12px',
  },

  [`& .${MUI_NAME}-date`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
    flex: 1,
    fontSize: '12px',
    textAlign: 'right',
  },
}));