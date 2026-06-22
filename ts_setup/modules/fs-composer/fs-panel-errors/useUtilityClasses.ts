import { generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { FsColors } from "../fs-theme";




const MUI_NAME = 'FsPanelErrors';

export interface FsPanelErrorsClasses {
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

export type FsPanelErrorsClassKey = keyof FsPanelErrorsClasses;

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

export const FsPanelErrorsRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})(({ theme }) => ({


  display: 'flex',
  flexDirection: 'column',
  gap: theme.spacing(1),
  padding: theme.spacing(1),

  [`& .${MUI_NAME}-errorSummary`]: {
    padding: theme.spacing(1),
    backgroundColor: FsColors.base.surface,
    border: `1px solid ${FsColors.base.border}`,
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
    backgroundColor: FsColors.base.surface,
  },

  [`& .${MUI_NAME}-statItemError`]: {
    border: `1px solid ${FsColors.semantic.danger}`,
  },

  [`& .${MUI_NAME}-statItemWarning`]: {
    border: `1px solid ${FsColors.semantic.warning}`,
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
    backgroundColor: FsColors.base.background,
    border: `1px solid ${FsColors.base.border}`,
  },

  [`& .${MUI_NAME}-errorCardError`]: {
    borderLeft: `4px solid ${FsColors.semantic.danger}`,
  },

  [`& .${MUI_NAME}-errorCardWarning`]: {
    borderLeft: `4px solid ${FsColors.semantic.warning}`,
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
    color: FsColors.base.textSecondary,
  },

  [`& .${MUI_NAME}-errorDescription`]: {
    ...theme.typography.subtitle2,
    color: FsColors.base.text,
    marginBottom: theme.spacing(1),
  },
}));