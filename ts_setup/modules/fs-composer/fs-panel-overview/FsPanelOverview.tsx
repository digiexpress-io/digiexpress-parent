import React from 'react';
import { Typography, TableSortLabel } from '@mui/material';
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
  const [sortDirection, setSortDirection] = React.useState<'asc' | 'desc'>('desc');

  function handleSortToggle() {
    setSortDirection(prev => prev === 'asc' ? 'desc' : 'asc');
  }

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
              <TableSortLabel
                className={classes.headerDate}
                active={true}
                direction={sortDirection}
                onClick={handleSortToggle}
              >
                {intl.formatMessage({ id: 'fs.panelOverview.column.lastUpdated' })}
              </TableSortLabel>
            </div>
            <div className={classes.container}>
              {ownerState.rows.map((row) => (
                <div key={row.id} className={row.depth === 0 ? classes.row : classes.childRow} style={{ paddingLeft: `${row.depth * 16}px` }}>
                  {row.depth > 0 && <FsIcon icon={FsIcons.ChildItem} className={classes.childIcon} small/>}
                  {row.type === 'FOLDER' && <FsIcon icon={FsIcons.Folder} className={classes.typeIcon} small />}
                  <Typography className={row.depth === 0 ? classes.parentName : classes.name}>{row.name}</Typography>
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
