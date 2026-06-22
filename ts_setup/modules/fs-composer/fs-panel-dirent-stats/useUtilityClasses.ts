import { generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { FsColors } from "../fs-theme";
import { OwnerState } from "./useOwnerState";

const MUI_NAME = 'FsPanelDirentStats';

export interface FsPanelDirentStatsClasses {
  root: string;
  sectionHeaderOpen: string;
  sectionHeaderCollapsed: string;
  sectionTitle: string;
  sectionExpandIconOpen: string;
  sectionExpandIconCollapsed: string;
  section: string;
  row: string;
  label: string;
  value: string;
  groupTitle: string;
  groupItem: string;
  assetTypeChip: string;
}

export type FsPanelDirentStatsClassKey = keyof FsPanelDirentStatsClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    sectionHeaderOpen: ['sectionHeaderOpen'],
    sectionHeaderCollapsed: ['sectionHeaderCollapsed'],
    sectionTitle: ['sectionTitle'],
    sectionExpandIconOpen: ['sectionExpandIconOpen'],
    sectionExpandIconCollapsed: ['sectionExpandIconCollapsed'],
    section: ['section'],
    row: ['row'],
    label: ['label'],
    value: ['value'],
    groupTitle: ['groupTitle'],
    groupItem: ['groupItem'],
    assetTypeChip: ['assetTypeChip'],
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
  marginBottom: theme.spacing(10),

  [`& .${MUI_NAME}-sectionHeaderOpen`]: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    cursor: 'pointer',
    userSelect: 'none',
  },

  [`& .${MUI_NAME}-sectionHeaderCollapsed`]: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    cursor: 'pointer',
    userSelect: 'none',
    borderBottom: `1px solid ${FsColors.light.border}`,
  },

  [`& .${MUI_NAME}-sectionExpandIconOpen`]: {
    display: 'flex',
    transform: 'rotate(180deg)',
    transition: 'transform 0.2s',
  },

  [`& .${MUI_NAME}-sectionExpandIconCollapsed`]: {
    display: 'flex',
    transform: 'rotate(0deg)',
    transition: 'transform 0.2s',
  },

  [`& .${MUI_NAME}-sectionTitle`]: {
    ...theme.typography.caption,
    fontWeight: 600,
    textTransform: 'uppercase',
    color: FsColors.light.textSecondary,
    padding: theme.spacing(0.75, 1.5),
  },

  [`& .${MUI_NAME}-section`]: {
    display: 'flex',
    flexDirection: 'column',
    borderLeft: `1px solid ${FsColors.light.border}`,
    borderRight: `1px solid ${FsColors.light.border}`,
    borderBottom: `1px solid ${FsColors.light.border}`,

    '& > div:nth-of-type(odd)': {
      backgroundColor: FsColors.light.surface,
    },
  },

  [`& .${MUI_NAME}-row`]: {
    display: 'flex',
    alignItems: 'center',
    padding: theme.spacing(0.75, 1.5),
    backgroundColor: FsColors.light.background,
    borderBottom: `1px solid ${FsColors.light.border}`,

    '&:last-child': {
      borderBottom: 'none',
    },
  },

  [`& .${MUI_NAME}-label`]: {
    ...theme.typography.subtitle2,
    color: FsColors.light.text,
    flex: 1,
  },

  [`& .${MUI_NAME}-value`]: {
    color: FsColors.light.textSecondary,
    ...theme.typography.subtitle2,
    fontWeight: 500
  },

  [`& .${MUI_NAME}-groupTitle`]: {
    ...theme.typography.subtitle2,
    fontWeight: 500,
    color: FsColors.light.text,
    padding: theme.spacing(0.5, 1.5),
    backgroundColor: FsColors.light.surface,
    borderBottom: `1px solid ${FsColors.light.border}`,
  },

  [`& .${MUI_NAME}-assetTypeChip`]: {
    color: FsColors.light.text,
    ...theme.typography.subtitle2,
  },

  [`& .${MUI_NAME}-groupItem`]: {
    ...theme.typography.subtitle2,
    color: FsColors.light.textSecondary,
    padding: theme.spacing(0.25, 3),
    borderBottom: `1px solid ${FsColors.light.border}`,

    '&:last-child': {
      borderBottom: 'none',
    },
  },
}));
