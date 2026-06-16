import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsDirent } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-panel';
import { FsPanelPropertiesProps } from './FsPanelPropertiesProps';
import { useOwnerState } from './useOwnerState';
import { FsPanelPropertiesRoot, usePanelProperties } from './useUtilityClasses';
import { createWidget } from '../fs-factory';



export const FsPanelProperties: React.FC<FsPanelPropertiesProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = usePanelProperties();

  const { dirent } = ownerState;
  const { getDirentName } = useFsDirent();
  const displayName = props.dirent ? getDirentName(props.dirent.id) : undefined;

  if (!dirent) {
    return (
      <FsPanel title={intl.formatMessage({ id: 'fs.properties.title' })} icon={<FsIcon icon={FsIcons.Settings} large />}
        activeDirent={false}
        noDirentMessage={intl.formatMessage({ id: 'fs.properties.message.selectDirent' })}>
        <></>
      </FsPanel>
    );
  }

  const labels = (dirent.props?.labels ?? []).map(l => l.key);
  const description = dirent.props?.assetDescription;
  const widget = createWidget(dirent);

  return (
    <FsPanel title={intl.formatMessage({ id: 'fs.properties.title.direntName' }, { direntName: displayName })} icon={<FsIcon icon={FsIcons.Settings} large />} activeDirent={true}>
      <FsPanelPropertiesRoot className={classes.root} ownerState={ownerState}>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.description' })}</Typography>
          <Typography className={classes.propertyValue}>{description}</Typography>
        </div>

        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.labels' })}</Typography>
          <div className={classes.propertyList}>
            {labels.map((label, index) => (
              <div key={index} className={classes.label}>
                <Typography component="span">{label ?? "-"}</Typography>
              </div>
            ))}
          </div>
        </div>

<widget.views.PropertiesView direntId={dirent.id} />

      </FsPanelPropertiesRoot>
    </FsPanel>
  );
};
