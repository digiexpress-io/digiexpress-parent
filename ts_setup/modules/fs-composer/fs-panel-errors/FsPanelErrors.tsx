import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-panel';
import { FsPanelErrorsProps } from './FsPanelErrorsProps';
import { useOwnerState } from './useOwnerState';
import { FsPanelErrorsRoot, useUtilityClasses } from './useUtilityClasses';


export const FsPanelErrors: React.FC<FsPanelErrorsProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  /*
  if (!props.dirent) {
    return (
      <FsPanel title={intl.formatMessage({ id: 'fs.errors.title' })} icon={<FsIcon icon={FsIcons.Error} large />} activeDirent={false} noDirentMessage={intl.formatMessage({ id: 'fs.errors.message.selectDirent' })} />
    );
  }

  const errors = props.dirent.errors;
  const criticalCount = errors.filter(e => e.severity === 'CRITICAL').length;
  const warningCount = errors.filter(e => e.severity === 'WARNING').length;

  return (
    <FsPanel title={intl.formatMessage({ id: 'fs.errors.title.direntName' }, { direntName: props.dirent.name })} icon={<FsIcon icon={FsIcons.Error} large />} activeDirent={true}>
      <FsPanelErrorsRoot className={classes.root} ownerState={ownerState}>
        <div className={classes.errorSummary}>
          <Typography className={classes.summaryTitle}>{intl.formatMessage({ id: 'fs.errors.sectionTitle.summary' })}</Typography>
          <div className={classes.summaryStats}>
            {errors.length === 0 && (
              <Typography className={classes.statLabel}>{intl.formatMessage({ id: 'fs.errors.message.noErrors' })}</Typography>
            )}
            {criticalCount > 0 && (
              <div className={`${classes.statItem} ${classes.statItemError}`}>
                <Typography className={classes.statCount}>{criticalCount}</Typography>
                <Typography className={classes.statLabel}>{intl.formatMessage({ id: 'fs.errors.statLabel.errors' })}</Typography>
              </div>
            )}
            {warningCount > 0 && (
              <div className={`${classes.statItem} ${classes.statItemWarning}`}>
                <Typography className={classes.statCount}>{warningCount}</Typography>
                <Typography className={classes.statLabel}>{intl.formatMessage({ id: 'fs.errors.statLabel.warnings' })}</Typography>
              </div>
            )}
          </div>
        </div>

        <div className={classes.errorList}>
          {errors.map((error, index) => (
            <div key={index} className={`${classes.errorCard} ${error.severity === 'CRITICAL' ? classes.errorCardError : classes.errorCardWarning}`}>
              <div className={classes.errorHeader}>
                <div className={classes.errorIcon}>
                  {error.severity === 'CRITICAL' ?
                    <FsIcon icon={FsIcons.Error} large color={ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight} /> :
                    <FsIcon icon={FsIcons.Warning} large color={ownerState.isDarkMode ? FsColors.semantic.warning : FsColors.semantic.warningLight} />}
                </div>
                <Typography className={classes.errorTitle}>{error.code}</Typography>
              </div>

              <Typography className={classes.errorDescription}>
                {error.message}
              </Typography>
            </div>
          ))}
        </div>
      </FsPanelErrorsRoot>
    </FsPanel>
  );
  */

  return <>errors</>
};

