import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-panel';
import { FsPanelOverviewProps } from './FsPanelOverviewProps';
import { useOwnerState } from './useOwnerState';
import { FsPanelOverviewRoot, useUtilityClasses } from './useUtilityClasses';


function ConfigIcon(props: { type: Fs.ConfigOption, className: string }) {
  const intl = useIntl();
  const { type, className } = props;
  switch (type) {
    case 'devMode':        return <FsIcon small icon={FsIcons.DevMode}    className={className} tooltip={intl.formatMessage({ id: 'fs.direntMenu.chip.devMode' })} />;
    case 'assignableMode': return <FsIcon small icon={FsIcons.Assignment} className={className} tooltip={intl.formatMessage({ id: 'fs.direntMenu.chip.assignable' })} />;
    case 'disabledMode':   return <FsIcon small icon={FsIcons.Disabled}   className={className} tooltip={intl.formatMessage({ id: 'fs.direntMenu.chip.disabled' })} />;
    case 'anonymousMode':  return <FsIcon small icon={FsIcons.Anonymous}  className={className} tooltip={intl.formatMessage({ id: 'fs.direntMenu.chip.anonymous' })} />;
  }
}


export const FsPanelOverview: React.FC<FsPanelOverviewProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsPanel
      title={intl.formatMessage({ id: 'fs.panelOverview.title' })}
      icon={<FsIcon icon={FsIcons.Tree} large />}
      activeDirent={true}
    >
      <FsPanelOverviewRoot className={classes.root} ownerState={ownerState}>
        {ownerState.rows.length === 0 ? (
          <Typography>{intl.formatMessage({ id: 'fs.panelOverview.message.noAssets' })}</Typography>
        ) : (
          <>
            <div className={classes.header}>
              <Typography className={classes.headerName}>{intl.formatMessage({ id: 'fs.panelOverview.column.name' })}</Typography>
              <Typography className={classes.headerConfigOptions}>{intl.formatMessage({ id: 'fs.panelOverview.column.configOptions' })}</Typography>
              <Typography className={classes.headerDate}>{intl.formatMessage({ id: 'fs.panelOverview.column.lastUpdated' })}</Typography>
            </div>
            <div className={classes.container}>
              {ownerState.rows.map((row) => (
                <div key={row.id} className={row.isChild ? classes.childRow : classes.row}>
                  {row.isChild && <FsIcon icon={FsIcons.ChildItem} className={classes.childIcon} small/>}
                  {row.type === 'folder' && <FsIcon icon={FsIcons.Folder} className={classes.typeIcon} small/>}
                  <Typography className={row.isChild ? classes.name : classes.parentName}>{row.name}</Typography>
                  <div className={classes.configOptionsCell}>
                    {row.configOptions.map((opt) => (
                      <ConfigIcon key={opt} type={opt} className={classes.configIcon} />
                    ))}
                  </div>
                  <Typography className={classes.date}>{row.lastDate}</Typography>
                </div>
              ))}
            </div>
          </>
        )}
      </FsPanelOverviewRoot>
    </FsPanel>
  );
};
