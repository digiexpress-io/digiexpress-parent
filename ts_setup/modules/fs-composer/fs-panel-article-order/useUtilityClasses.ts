import { generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { FsColors } from "../fs-theme";
import { OwnerState } from "./useOwnerState";

const MUI_NAME = 'FsPanelArticleOrder';

export interface FsPanelArticleOrderClasses {
  root: string;
  header: string;
  container: string;
  row: string;
  orderNumber: string;
  name: string;
  description: string;
}

export type FsPanelArticleOrderClassKey = keyof FsPanelArticleOrderClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    header: ['header'],
    container: ['container'],
    row: ['row'],
    orderNumber: ['orderNumber'],
    name: ['name'],
    description: ['description'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsPanelArticleOrderRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme }) => ({
  [`& .${MUI_NAME}-header`]: {
    display: 'flex',
    padding: theme.spacing(0.75, 1.5),
    borderBottom: `1px solid FsColors.light.border`,

    '& .MuiTypography-root': {
      ...theme.typography.caption,
      fontWeight: 600,
      textTransform: 'uppercase',
      color: FsColors.light.textSecondary,
    },
  },

  [`& .${MUI_NAME}-container`]: {
    display: 'flex',
    flexDirection: 'column',
    border: `1px solid ${FsColors.light.border}`,

    '& > div:nth-of-type(odd)': {
      backgroundColor: FsColors.light.surface,
    },
  },

  [`& .${MUI_NAME}-row`]: {
    display: 'flex',
    padding: theme.spacing(1, 1.5),
    backgroundColor: FsColors.light.background,
    borderBottom: `1px solid ${FsColors.light.border}`,

    '&:last-child': {
      borderBottom: 'none',
    },
  },

  [`& .${MUI_NAME}-orderNumber`]: {
    ...theme.typography.subtitle2,
    color: FsColors.light.textSecondary,
    width: '60px',
    flexShrink: 0,
    fontFamily: 'monospace',
    fontSize: '12px',
  },

  [`& .${MUI_NAME}-name`]: {
    ...theme.typography.subtitle2,
    color: FsColors.light.text,
    flex: 1,
    fontSize: '12px',
    fontWeight: 400,
  },

  [`& .${MUI_NAME}-description`]: {
    ...theme.typography.subtitle2,
    color: FsColors.light.textSecondary,
    flex: 2,
    fontSize: '11px',
  },
}));
