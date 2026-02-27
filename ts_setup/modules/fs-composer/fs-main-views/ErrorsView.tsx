import React from 'react';
import { Typography, styled, generateUtilityClass } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsNode } from '@dxs-ts/fs-api';
import { FsColors, FsIcons } from '../fs-theme';
import { useFs } from '@dxs-ts/fs-api';
import { ViewContainer } from './ViewContainer';


  const errors = [
    {
      id: 1,
      severity: 'WARNING',
      title: 'Missing Translation Warning',
      description: 'Main.article does not have a Finnish language page',
      affectedFile: 'fi.language',
      timestamp: '12.02.2025 14:30'
    },
    {
      id: 2,
      severity: 'ERROR',
      title: 'Missing Markdown level 1 heading error',
      description: 'Page in main.article cannot be rendered in portal if no level 1 heading is defined.',
      affectedFile: 'main.article',
      timestamp: '12.02.2025 14:28'
    },
    {
      id: 3,
      severity: 'WARNING',
      title: 'Deprecated Service Reference',
      description: 'This node references "old-message.service" which has been marked as deprecated.',
      affectedFile: 'main.article',
      timestamp: '10.02.2025 09:15'
    },
    {
      id: 4,
      severity: 'ERROR',
      title: 'Broken Reference Link',
      description: 'The reference to "ref.article" could not be resolved in the tree structure.',
      affectedFile: 'main.article',
      timestamp: '11.02.2025 16:45'
    }
  ];


export interface ErrorsViewProps {
  node: FsNode | undefined;
}

export const ErrorsView: React.FC<ErrorsViewProps> = ({ node }) => {
  const { isDarkMode } = useFs();
  const classes = useUtilityClasses(isDarkMode);

  if (!node) {
    return (
      <ViewContainer
        title="Errors"
        icon={<FsIcons.Error />}
        activeNode={false}
        noNodeMessage="Select a node from the tree to view errors1."
      >
        <></>
      </ViewContainer>
    );
  }


  const errorSummary = {
    total: errors.length,
    error: errors.filter(e => e.severity === 'ERROR').length,
    warning: errors.filter(e => e.severity === 'WARNING').length
  };

  const mainContent = (
    <ErrorsViewRoot className={classes.root} isDarkMode={isDarkMode}>
      <div className={classes.errorSummary}>
        <Typography className={classes.summaryTitle}>Error Summary</Typography>
        <div className={classes.summaryStats}>
          <div className={`${classes.statItem} ${classes.statItemError}`}>
            <Typography className={classes.statCount}>{errorSummary.error}</Typography>
            <Typography className={classes.statLabel}>Errors</Typography>
          </div>
          <div className={`${classes.statItem} ${classes.statItemWarning}`}>
            <Typography className={classes.statCount}>{errorSummary.warning}</Typography>
            <Typography className={classes.statLabel}>Warnings</Typography>
          </div>
        </div>
      </div>

      <div className={classes.errorList}>
        {errors.map((error) => (
          <div key={error.id} className={`${classes.errorCard} ${error.severity === 'ERROR' ? classes.errorCardError : classes.errorCardWarning}`}>
            <div className={classes.errorHeader}>
              <div className={classes.errorIcon}>
                {error.severity === 'ERROR' ?
                  <FsIcons.Error sx={{ color: isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight }} /> :
                  <FsIcons.Warning sx={{ color: isDarkMode ? FsColors.semantic.warning : FsColors.semantic.warningLight }} />}
              </div>
              <Typography className={classes.errorTitle}>{error.title}</Typography>
              <Typography className={classes.errorTimestamp}>{error.timestamp}</Typography>
            </div>

            <Typography className={classes.errorDescription}>
              {error.description}
            </Typography>

          </div>
        ))}
      </div>
    </ErrorsViewRoot>
  );

  return (
    <ViewContainer
      title={`Errors: ${node.name}`}
      icon={<FsIcons.Error />}
      activeNode={true}
    >
      {mainContent}
    </ViewContainer>
  );
};

const MUI_NAME = 'ErrorsView';

export interface ErrorsViewClasses {
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

export type ErrorsViewClassKey = keyof ErrorsViewClasses;

const useUtilityClasses = (isDarkMode: boolean) => {
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

const ErrorsViewRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'isDarkMode',
})<{ isDarkMode: boolean }>(({ theme, isDarkMode }) => ({
  display: 'flex',
  flexDirection: 'column',
  gap: theme.spacing(1),
  padding: theme.spacing(1),

  [`& .${MUI_NAME}-errorSummary`]: {
    padding: theme.spacing(1),
    backgroundColor: isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
    border: `1px solid ${isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
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
    backgroundColor: isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
  },

  [`& .${MUI_NAME}-statItemError`]: {
    border: `1px solid ${isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight}`,
  },

  [`& .${MUI_NAME}-statItemWarning`]: {
    border: `1px solid ${isDarkMode ? FsColors.semantic.warning : FsColors.semantic.warningLight}`,
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
    backgroundColor: isDarkMode ? FsColors.dark.background : FsColors.light.background,
    border: `1px solid ${isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
  },

  [`& .${MUI_NAME}-errorCardError`]: {
    borderLeft: `4px solid ${isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight}`,
  },

  [`& .${MUI_NAME}-errorCardWarning`]: {
    borderLeft: `4px solid ${isDarkMode ? FsColors.semantic.warning : FsColors.semantic.warningLight}`,
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
    color: isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
  },

  [`& .${MUI_NAME}-errorDescription`]: {
    ...theme.typography.subtitle2,
    color: isDarkMode ? FsColors.dark.text : FsColors.light.text,
    marginBottom: theme.spacing(1),
  },
}));
