import React from 'react';
import { useIntl } from 'react-intl';
import MDEditor from '@uiw/react-md-editor';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-panel';
import { FsPanelPreviewWrench } from '../fs-panel-preview-wrench';
import { FsPanelPreviewProps } from './FsPanelPreviewProps';
import { useOwnerState } from './useOwnerState';
import { FsPanelPreviewRoot, useUtilityClasses } from './useUtilityClasses';
import { FsPanelPreviewPrintoutPage } from '../fs-dirent-printout-page';


export const FsPanelPreview: React.FC<FsPanelPreviewProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  if (!props.dirent) {
    return (
      <FsPanel title={intl.formatMessage({ id: 'fs.panelPreview.title' })} icon={<FsIcon icon={FsIcons.Preview} large />} activeDirent={false} noDirentMessage={intl.formatMessage({ id: 'fs.panelPreview.message.selectDirent' })} />
    );
  }

  if (ownerState.isFlow && ownerState.dirent) {
    return <FsPanelPreviewWrench dirent={ownerState.dirent} />;
  }

  if (ownerState.isArticleTemplate) {
    return (
      <FsPanel title={intl.formatMessage({ id: 'fs.panelPreview.title' })} icon={<FsIcon icon={FsIcons.Preview} large />} activeDirent={true}>
        <FsPanelPreviewRoot className={classes.root}>
          <div data-color-mode={'light'}>
            <MDEditor.Markdown source={ownerState.content.templateContent} />
          </div>
        </FsPanelPreviewRoot>
      </FsPanel>
    );
  }

  if (ownerState.isPrintoutPage) {
    return (
      <FsPanel title={intl.formatMessage({ id: 'fs.panelPreview.title' })} icon={<FsIcon icon={FsIcons.Preview} large />} activeDirent={true}>
        <FsPanelPreviewRoot className={classes.root}>
          <FsPanelPreviewPrintoutPage />
        </FsPanelPreviewRoot>
      </FsPanel>
    );
  }

  if (ownerState.isArticlePage) {
    return (
      <FsPanel title={intl.formatMessage({ id: 'fs.panelPreview.title' })} icon={<FsIcon icon={FsIcons.Preview} large />} activeDirent={true}>
        <FsPanelPreviewRoot className={classes.root}>
          <div data-color-mode={'light'}>
            <MDEditor.Markdown source={ownerState.content.pageContent} />
          </div>
        </FsPanelPreviewRoot>
      </FsPanel>
    );
  }

  return (
    <FsPanel title={intl.formatMessage({ id: 'fs.panelPreview.title' })} icon={<FsIcon icon={FsIcons.Preview} large />} activeDirent={true}>
      <FsPanelPreviewRoot className={classes.root}>
        <div data-color-mode={'light'}>
          <MDEditor.Markdown source={ownerState.content.pageContent} />
        </div>
      </FsPanelPreviewRoot>
    </FsPanel>
  );
};
