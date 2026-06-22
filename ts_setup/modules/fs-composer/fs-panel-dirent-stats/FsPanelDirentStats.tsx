import React from 'react';
import { Box, Collapse, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-panel';
import { useOwnerState } from './useOwnerState';
import { FsPanelDirentStatsRoot, useUtilityClasses } from './useUtilityClasses';


export const FsPanelDirentStats: React.FC = () => {
  const intl = useIntl();
  const ownerState = useOwnerState();
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
          <div className={ownerState.overviewExpanded ? classes.sectionHeaderOpen : classes.sectionHeaderCollapsed} onClick={ownerState.onToggleOverview}>
            <Typography className={classes.sectionTitle}>
              {intl.formatMessage({ id: 'fs.direntStats.section.overview' })}
            </Typography>
            <span className={ownerState.overviewExpanded ? classes.sectionExpandIconOpen : classes.sectionExpandIconCollapsed}>
              <FsIcon icon={FsIcons.ExpandMore} small />
            </span>
          </div>
          <Collapse in={ownerState.overviewExpanded}>
            <div className={classes.section}>
              {ownerState.assetCounts.map(({ type, count }) => (
                <div key={type} className={classes.row}>
                  <Typography className={classes.label}>{intl.formatMessage({id: `fs.direntStats.section.overview.assetType.${type}`})}</Typography>
                  <Typography className={classes.value}>{count}</Typography>
                </div>
              ))}
            </div>
          </Collapse>
        </div>

        {/* Section 2: Dangling Assets */}
        <div>
          <div className={ownerState.danglingExpanded ? classes.sectionHeaderOpen : classes.sectionHeaderCollapsed} onClick={ownerState.onToggleDangling}>
            <Typography className={classes.sectionTitle}>
              {intl.formatMessage({ id: 'fs.direntStats.section.dangling' })}
            </Typography>
            <span className={ownerState.danglingExpanded ? classes.sectionExpandIconOpen : classes.sectionExpandIconCollapsed}>
              <FsIcon icon={FsIcons.ExpandMore} small />
            </span>
          </div>
          <Collapse in={ownerState.danglingExpanded}>
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
          </Collapse>
        </div>

        {/* Section 3: Disabled Assets */}
        <div>
          <div className={ownerState.disabledExpanded ? classes.sectionHeaderOpen : classes.sectionHeaderCollapsed} onClick={ownerState.onToggleDisabled}>
            <Typography className={classes.sectionTitle}>
              {intl.formatMessage({ id: 'fs.direntStats.section.disabled' })}
            </Typography>
            <span className={ownerState.disabledExpanded ? classes.sectionExpandIconOpen : classes.sectionExpandIconCollapsed}>
              <FsIcon icon={FsIcons.ExpandMore} small />
            </span>
          </div>
          <Collapse in={ownerState.disabledExpanded}>
            <div className={classes.section}>
              {ownerState.disabledAssets.map((asset, i) => (
                <div key={i} className={classes.row}>
                  <Typography className={classes.label}>{asset.fullPath}</Typography>
                  <Box className={classes.assetTypeChip}>
                    {intl.formatMessage({ id: `fs.direntStats.section.overview.assetType.${asset.type}` })}
                  </Box>
                </div>
              ))}
            </div>
          </Collapse>
        </div>

      </FsPanelDirentStatsRoot>
    </FsPanel>
  );
};
