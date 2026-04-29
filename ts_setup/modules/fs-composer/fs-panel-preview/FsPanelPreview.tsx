import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import MDEditor from '@uiw/react-md-editor';
import MonacoReact from '@monaco-editor/react';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-panel';
import { FsPanelPreviewProps } from './FsPanelPreviewProps';
import { useOwnerState } from './useOwnerState';
import { FsPanelPreviewRoot, useUtilityClasses } from './useUtilityClasses';


export const FsPanelPreview: React.FC<FsPanelPreviewProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  if (!props.dirent) {
    return (
      <FsPanel title={intl.formatMessage({ id: 'fs.panelPreview.title' })} icon={<FsIcon icon={FsIcons.Preview} large />} activeDirent={false} noDirentMessage={intl.formatMessage({ id: 'fs.panelPreview.message.selectDirent' })} />
    );
  }

  if (ownerState.isFlow) {
    return (
      <FsPanel title={intl.formatMessage({ id: 'fs.panelPreview.title' })} icon={<FsIcon icon={FsIcons.Preview} large />} activeDirent={true}>
        <FsPanelPreviewRoot className={classes.root} ownerState={ownerState}>
          <div className={classes.editor}>
            NICE BUBBLE THING
          </div>
        </FsPanelPreviewRoot>
      </FsPanel>
    );
  }

  if (!ownerState.isPage) {
    return (
      <FsPanel title={intl.formatMessage({ id: 'fs.panelPreview.title' })} icon={<FsIcon icon={FsIcons.Preview} large />} activeDirent={true}>
        <FsPanelPreviewRoot className={classes.root} ownerState={ownerState}>
          <Typography className={classes.content}>{intl.formatMessage({ id: 'fs.panelPreview.message.notAvailable' })}</Typography>
        </FsPanelPreviewRoot>
      </FsPanel>
    );
  }

  return (
    <FsPanel title={intl.formatMessage({ id: 'fs.panelPreview.title' })} icon={<FsIcon icon={FsIcons.Preview} large />} activeDirent={true}>
      <FsPanelPreviewRoot className={classes.root} ownerState={ownerState}>
        <div data-color-mode={ownerState.isDarkMode ? 'dark' : 'light'}>
          <MDEditor.Markdown source={ownerState.content.pageContent} />
        </div>
      </FsPanelPreviewRoot>
    </FsPanel>
  );
};
