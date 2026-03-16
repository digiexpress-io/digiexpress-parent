import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-panel';
import { FsErrorsProps } from './FsErrorsProps';
import { useOwnerState } from './useOwnerState';
import { FsErrorsRoot, useUtilityClasses } from './useUtilityClasses';


export const FsErrors: React.FC<FsErrorsProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  if (!props.node) {
    return (
      <FsPanel title={intl.formatMessage({ id: 'fs.errors.title' })} icon={<FsIcon icon={FsIcons.Error} large />} activeNode={false} noNodeMessage={intl.formatMessage({ id: 'fs.errors.message.selectNode' })} />
    );
  }

  return (
    <FsPanel title={intl.formatMessage({ id: 'fs.errors.title.nodeName' }, { nodeName: props.node.name })} icon={<FsIcon icon={FsIcons.Error} large />} activeNode={true}>
      <FsErrorsRoot className={classes.root} ownerState={ownerState}>
        <div className={classes.errorSummary}>
          <Typography className={classes.summaryTitle}>{intl.formatMessage({ id: 'fs.errors.sectionTitle.summary' })}</Typography>
          <div className={classes.summaryStats}>
            <div className={`${classes.statItem} ${classes.statItemError}`}>
              <Typography className={classes.statCount}>{errorSummary.error}</Typography>
              <Typography className={classes.statLabel}>{intl.formatMessage({ id: 'fs.errors.statLabel.errors' })}</Typography>
            </div>
            <div className={`${classes.statItem} ${classes.statItemWarning}`}>
              <Typography className={classes.statCount}>{errorSummary.warning}</Typography>
              <Typography className={classes.statLabel}>{intl.formatMessage({ id: 'fs.errors.statLabel.warnings' })}</Typography>
            </div>
          </div>
        </div>

        <div className={classes.errorList}>
          {errorsMock.map((error) => (
            <div key={error.id} className={`${classes.errorCard} ${error.severity === 'ERROR' ? classes.errorCardError : classes.errorCardWarning}`}>
              <div className={classes.errorHeader}>
                <div className={classes.errorIcon}>
                  {error.severity === 'ERROR' ?
                    <FsIcon icon={FsIcons.Error} large color={ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight} /> :
                    <FsIcon icon={FsIcons.Warning} large color={ownerState.isDarkMode ? FsColors.semantic.warning : FsColors.semantic.warningLight} />}
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
      </FsErrorsRoot>
    </FsPanel>
  );
};

const errorsMock = [
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


const errorSummary = {
  total: errorsMock.length,
  error: errorsMock.filter(e => e.severity === 'ERROR').length,
  warning: errorsMock.filter(e => e.severity === 'WARNING').length
};