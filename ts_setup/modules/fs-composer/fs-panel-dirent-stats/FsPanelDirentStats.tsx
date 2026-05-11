import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-panel';
import { FsPanelDirentStatsProps } from './FsPanelDirentStatsProps';
import { useOwnerState } from './useOwnerState';
import { FsPanelDirentStatsRoot, useUtilityClasses } from './useUtilityClasses';


export const FsPanelDirentStats: React.FC<FsPanelDirentStatsProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsPanel
      title={intl.formatMessage({ id: 'fs.direntStats.title' })}
      icon={<FsIcon icon={FsIcons.Stats} large />}
      activeDirent={true}
    >
      <FsPanelDirentStatsRoot className={classes.root} ownerState={ownerState}>

        {/* Section 1: Asset Overview */}
        <div>
          <Typography className={classes.sectionTitle}>
            {intl.formatMessage({ id: 'fs.direntStats.section.overview' })}
          </Typography>
          <div className={classes.section}>
            {ownerState.assetCounts.map(({ type, count }) => (
              <div key={type} className={classes.row}>
                <Typography className={classes.label}>{intl.formatMessage({id: `fs.direntStats.section.overview.assetType.${type}`})}</Typography>
                <Typography className={classes.value}>{count}</Typography>
              </div>
            ))}
          </div>
        </div>

        {/* Section 2: Dangling Assets */}
        <div>
          <Typography className={classes.sectionTitle}>
            {intl.formatMessage({ id: 'fs.direntStats.section.dangling' })}
          </Typography>
          <div className={classes.section}>
            {ownerState.danglingGroups
              .filter(group => group.items === 'TODO' || group.items.length > 0)
              .map((group) => (
                <React.Fragment key={group.label}>
                  <Typography className={classes.groupTitle}>{group.label}: {intl.formatMessage({ id: group.descriptionId })}</Typography>
                  {group.items === 'TODO' ? (
                    <Typography className={classes.groupItem}>TODO</Typography>
                  ) : group.items.map((name, i) => (
                    <Typography key={i} className={classes.groupItem}>{name}</Typography>
                  ))}
                </React.Fragment>
              ))}
          </div>
        </div>

        {/* Section 3: Disabled Assets */}
        <div>
          <Typography className={classes.sectionTitle}>
            {intl.formatMessage({ id: 'fs.direntStats.section.disabled' })}
          </Typography>
          <div className={classes.section}>
            {ownerState.disabledAssets.map((asset, i) => (
              <div key={i} className={classes.row}>
                <Typography className={classes.label}>{asset.name}</Typography>
                <Typography className={classes.value}>{asset.type}</Typography>
              </div>
            ))}
          </div>
        </div>

      </FsPanelDirentStatsRoot>
    </FsPanel>
  );
};
