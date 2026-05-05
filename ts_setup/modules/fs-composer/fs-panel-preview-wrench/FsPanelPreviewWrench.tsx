import React from 'react';
import { useIntl } from 'react-intl';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-panel';
import { FsPanelPreviewWrenchProps } from './FsPanelPreviewWrenchProps';
import { useOwnerState } from './useOwnerState';
import { FsPanelPreviewWrenchRoot, useUtilityClasses } from './useUtilityClasses';
import { Container } from './graph/Container';


export const FsPanelPreviewWrench: React.FC<FsPanelPreviewWrenchProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsPanel title={intl.formatMessage({ id: 'fs.panelPreview.title' })} icon={<FsIcon icon={FsIcons.Preview} large />} activeDirent={true}>
      <FsPanelPreviewWrenchRoot className={classes.root} ownerState={ownerState}>
        <div className={classes.editor}>
          {ownerState.flowAst && ownerState.wrenchBody && (
            <Container
              flow={ownerState.flowAst}
              site={ownerState.wrenchBody}
              onClick={() => {}}
              onDoubleClick={() => {}}
            />
          )}
        </div>
      </FsPanelPreviewWrenchRoot>
    </FsPanel>
  );
};
