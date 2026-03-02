import { generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { FsColors } from "../fs-theme";
import { OwnerState } from "./useOwnerState";




const MUI_NAME = 'FsErrors';

export interface FsErrorsClasses {
  root: string;
  errorSummary: string;
  summaryTitle: string;
  summaryStats: string;
  statItem: string;
  statItemError: string;
  statItemWarning: string;
  statCount: string;
  statLabel: string;
  errorList: string;
  errorCard: string;
  errorCardError: string;
  errorCardWarning: string;
  errorHeader: string;
  errorIcon: string;
  errorTitle: string;
  errorTimestamp: string;
  errorDescription: string;
}

export type FsErrorsClassKey = keyof FsErrorsClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    errorSummary: ['errorSummary'],
    summaryTitle: ['summaryTitle'],
    summaryStats: ['summaryStats'],
    statItem: ['statItem'],
    statItemError: ['statItemError'],
    statItemWarning: ['statItemWarning'],
    statCount: ['statCount'],
    statLabel: ['statLabel'],
    errorList: ['errorList'],
    errorCard: ['errorCard'],
    errorCardError: ['errorCardError'],
    errorCardWarning: ['errorCardWarning'],
    errorHeader: ['errorHeader'],
    errorIcon: ['errorIcon'],
    errorTitle: ['errorTitle'],
    errorTimestamp: ['errorTimestamp'],
    errorDescription: ['errorDescription'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsErrorsRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => ({


  display: 'flex',
  flexDirection: 'column',
  gap: theme.spacing(1),
  padding: theme.spacing(1),

  [`& .${MUI_NAME}-errorSummary`]: {
    padding: theme.spacing(1),
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
    border: `1px solid ${ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
  },

  [`& .${MUI_NAME}-summaryTitle`]: {
    ...theme.typography.subtitle2,
    fontWeight: 500,
    marginBottom: theme.spacing(1),
  },

  [`& .${MUI_NAME}-summaryStats`]: {
    display: 'flex',
    gap: theme.spacing(1),
  },

  [`& .${MUI_NAME}-statItem`]: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    padding: theme.spacing(1),
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
  },

  [`& .${MUI_NAME}-statItemError`]: {
    border: `1px solid ${ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight}`,
  },

  [`& .${MUI_NAME}-statItemWarning`]: {
    border: `1px solid ${ownerState.isDarkMode ? FsColors.semantic.warning : FsColors.semantic.warningLight}`,
  },

  [`& .${MUI_NAME}-statCount`]: {
    ...theme.typography.subtitle2,
    fontWeight: 500,
  },

  [`& .${MUI_NAME}-statLabel`]: {
    ...theme.typography.caption,
    textTransform: 'uppercase',
    fontWeight: 500,
  },

  [`& .${MUI_NAME}-errorList`]: {
    display: 'flex',
    flexDirection: 'column',
    gap: theme.spacing(1),
  },

  [`& .${MUI_NAME}-errorCard`]: {
    padding: theme.spacing(1),
    marginTop: theme.spacing(1),
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.background : FsColors.light.background,
    border: `1px solid ${ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
  },

  [`& .${MUI_NAME}-errorCardError`]: {
    borderLeft: `4px solid ${ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight}`,
  },

  [`& .${MUI_NAME}-errorCardWarning`]: {
    borderLeft: `4px solid ${ownerState.isDarkMode ? FsColors.semantic.warning : FsColors.semantic.warningLight}`,
  },

  [`& .${MUI_NAME}-errorHeader`]: {
    display: 'flex',
    gap: theme.spacing(1),
    marginBottom: theme.spacing(1),
  },

  [`& .${MUI_NAME}-errorIcon`]: {
    fontSize: '16px',
  },

  [`& .${MUI_NAME}-errorTitle`]: {
    ...theme.typography.subtitle2,
    fontWeight: 500,
    flex: 1,
  },

  [`& .${MUI_NAME}-errorTimestamp`]: {
    ...theme.typography.caption,
    color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
  },

  [`& .${MUI_NAME}-errorDescription`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    marginBottom: theme.spacing(1),
  },
}));