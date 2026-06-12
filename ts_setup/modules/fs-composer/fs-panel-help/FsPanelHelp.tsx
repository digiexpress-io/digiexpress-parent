import React from 'react';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-panel';
import { FsPanelHelpProps } from './FsPanelHelpProps';
import { useOwnerState } from './useOwnerState';
import { useUtilityClasses, FsPanelHelpRoot } from './useUtilityClasses';
import { createWidget } from '../fs-factory';

export const FsPanelHelp: React.FC<FsPanelHelpProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  const HelpView = props.dirent ? createWidget(props.dirent).views.HelpView : undefined;

  return (
    <FsPanel title="Help" icon={<FsIcon icon={FsIcons.Help} large />} activeDirent={true}>
      <FsPanelHelpRoot className={classes.root} ownerState={ownerState}>
        {HelpView && <HelpView direntId={props.dirent!.id} />}
      </FsPanelHelpRoot>
    </FsPanel>
  );
};
