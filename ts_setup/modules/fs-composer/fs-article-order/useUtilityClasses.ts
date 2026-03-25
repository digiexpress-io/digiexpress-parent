import { generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { FsColors } from "../fs-theme";
import { OwnerState } from "./useOwnerState";

const MUI_NAME = 'FsArticleOrder';

export interface FsArticleOrderClasses {
  root: string;
  header: string;
  container: string;
  row: string;
  orderNumber: string;
  name: string;
  description: string;
}

export type FsArticleOrderClassKey = keyof FsArticleOrderClasses;

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

export const FsArticleOrderRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => ({
  [`& .${MUI_NAME}-header`]: {
    display: 'flex',
    padding: theme.spacing(0.75, 1.5),
    borderBottom: `1px solid ${ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border}`,

    '& .MuiTypography-root': {
      ...theme.typography.caption,
      fontWeight: 600,
      textTransform: 'uppercase',
      color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
    },
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
      borderBottom: 'none',
    },
  },

  [`& .${MUI_NAME}-orderNumber`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
    width: '60px',
    flexShrink: 0,
    fontFamily: 'monospace',
    fontSize: '12px',
  },

  [`& .${MUI_NAME}-name`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    flex: 1,
    fontSize: '12px',
    fontWeight: 400,
  },

  [`& .${MUI_NAME}-description`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
    flex: 2,
    fontSize: '11px',
  },
}));
