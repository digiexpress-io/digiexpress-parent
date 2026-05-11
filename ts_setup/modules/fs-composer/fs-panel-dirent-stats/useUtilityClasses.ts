import { generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { FsColors } from "../fs-theme";
import { OwnerState } from "./useOwnerState";

const MUI_NAME = 'FsPanelDirentStats';

export interface FsPanelDirentStatsClasses {
  root: string;
  sectionTitle: string;
  section: string;
  row: string;
  label: string;
  value: string;
  groupTitle: string;
  groupItem: string;
}

export type FsPanelDirentStatsClassKey = keyof FsPanelDirentStatsClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    sectionTitle: ['sectionTitle'],
    section: ['section'],
    row: ['row'],
    label: ['label'],
    value: ['value'],
    groupTitle: ['groupTitle'],
    groupItem: ['groupItem'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsPanelDirentStatsRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => ({
  display: 'flex',
  flexDirection: 'column',
  gap: theme.spacing(2),

  [`& .${MUI_NAME}-sectionTitle`]: {
    ...theme.typography.caption,
    fontWeight: 600,
    textTransform: 'uppercase',
    color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
    padding: theme.spacing(0.75, 1.5),
    borderBottom: `1px solid ${ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
  },

  [`& .${MUI_NAME}-section`]: {
    display: 'flex',
    flexDirection: 'column',
    border: `1px solid ${ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border}`,

    '& > div:nth-of-type(odd)': {
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
    },
  },

  [`& .${MUI_NAME}-row`]: {
    display: 'flex',
    alignItems: 'center',
    padding: theme.spacing(0.75, 1.5),
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.background : FsColors.light.background,
    borderBottom: `1px solid ${ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border}`,

    '&:last-child': {
      borderBottom: 'none',
    },
  },

  [`& .${MUI_NAME}-label`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    flex: 1,
    fontSize: '12px',
    fontWeight: 400,
  },

  [`& .${MUI_NAME}-value`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
    fontSize: '12px',
    fontFamily: 'monospace',
  },

  [`& .${MUI_NAME}-groupTitle`]: {
    ...theme.typography.caption,
    fontWeight: 600,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    padding: theme.spacing(0.5, 1.5),
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
    borderBottom: `1px solid ${ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
  },

  [`& .${MUI_NAME}-groupItem`]: {
    ...theme.typography.caption,
    color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
    padding: theme.spacing(0.25, 3),
    borderBottom: `1px solid ${ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border}`,

    '&:last-child': {
      borderBottom: 'none',
    },
  },
}));
