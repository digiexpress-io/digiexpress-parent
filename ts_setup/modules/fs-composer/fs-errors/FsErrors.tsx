import React from 'react';
import { Typography } from '@mui/material';
import { FsColors, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-primitives';
import { FsErrorsProps, errorsMock } from './FsErrorsProps';
import { useOwnerState } from './useOwnerState';
import { FsErrorsRoot, useUtilityClasses } from './useUtilityClasses';

  const errorSummary = {
    total: errorsMock.length,
    error: errorsMock.filter(e => e.severity === 'ERROR').length,
    warning: errorsMock.filter(e => e.severity === 'WARNING').length
  };

export const FsErrors: React.FC<FsErrorsProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  if (!props.node) {
    return (
      <FsPanel
        title="Errors"
        icon={<FsIcons.Error />}
        activeNode={false}
        noNodeMessage="Select a node from the tree to view errors1."
      >
        <></>
      </FsPanel>
    );
  }

  return (
    <FsPanel
      title={`Errors: ${props.node.name}`}
      icon={<FsIcons.Error />}
      activeNode={true}
    >
      <FsErrorsRoot className={classes.root} ownerState={ownerState}>
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
          {errorsMock.map((error) => (
            <div key={error.id} className={`${classes.errorCard} ${error.severity === 'ERROR' ? classes.errorCardError : classes.errorCardWarning}`}>
              <div className={classes.errorHeader}>
                <div className={classes.errorIcon}>
                  {error.severity === 'ERROR' ?
                    <FsIcons.Error sx={{ color: ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight }} /> :
                    <FsIcons.Warning sx={{ color: ownerState.isDarkMode ? FsColors.semantic.warning : FsColors.semantic.warningLight }} />}
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

