import { alpha, generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { FsColors } from "../fs-theme";
import { OwnerState } from "./useOwnerState";

const MUI_NAME = 'FsPanelArticleLocaleOverview';

export interface FsPanelArticleLocaleOverviewClasses {
  root: string;
  header: string;
  container: string;
  row: string;
  name: string;
  localeCell: string;
  localeCellDisabled: string;
  desc: string;
}

export type FsPanelArticleLocaleOverviewClassKey = keyof FsPanelArticleLocaleOverviewClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    header: ['header'],
    container: ['container'],
    row: ['row'],
    name: ['name'],
    localeCell: ['localeCell'],
    localeCellDisabled: ['localeCellDisabled'],
    desc: ['desc']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsPanelArticleLocaleOverviewRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme }) => ({
  [`& .${MUI_NAME}-header`]: {
    display: 'flex',
    padding: theme.spacing(0, 1.5),
    borderBottom: `1px solid ${FsColors.base.border}`,
    borderLeft: '1px solid transparent',
    borderRight: '1px solid transparent',

    '& .MuiTypography-root': {
      ...theme.typography.caption,
      fontWeight: 600,
      textTransform: 'uppercase',
      color: FsColors.base.textSecondary,
    },
  },

  [`& .${MUI_NAME}-container`]: {
    display: 'flex',
    flexDirection: 'column',
    border: `1px solid ${FsColors.base.border}`,

    '& > div:nth-of-type(odd)': {
      backgroundColor: FsColors.base.surface,
    },
  },

  [`& .${MUI_NAME}-row`]: {
    display: 'flex',
    alignItems: 'stretch',
    padding: theme.spacing(0, 1.5),
    backgroundColor: FsColors.base.background,
    borderBottom: `1px solid ${FsColors.base.border}`,

    '&:last-child': {
      borderBottom: 'none',
    },
  },

  [`& .${MUI_NAME}-name`]: {
    ...theme.typography.subtitle2,
    color: FsColors.base.text,
    flex: 1,
    fontWeight: 400,
    padding: theme.spacing(1, 0),
    display: 'flex',
    alignItems: 'center',
  },

    [`& .${MUI_NAME}-desc`]: {
    ...theme.typography.caption,
      color: FsColors.base.text,
    flex: 1,
    paddingLeft: theme.spacing(1.5),
    fontWeight: 400,
  },

  [`& .${MUI_NAME}-localeCell`]: {
    width: '60px',
    flexShrink: 0,
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    color: FsColors.base.text,
    padding: theme.spacing(0.75, 0),
  },

  [`& .${MUI_NAME}-localeCellDisabled`]: {
    backgroundColor: alpha(FsColors.semantic.danger, 0.1),
  },


}));
