import React from 'react';
import { Typography, Switch, Divider } from '@mui/material';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-panel';
import { FsConfigOptionsProps, allConfigOptions } from './FsConfigOptionsProps';
import { useOwnerState } from './useOwnerState';
import { FsConfigOptionsRoot, useUtilityClasses } from './useUtilityClasses';


export const FsConfigOptions: React.FC<FsConfigOptionsProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();


  if (!props.node) {
    return (
      <FsPanel title="Config Options" icon={<FsIcon icon={FsIcons.Settings} large />} activeNode={false} noNodeMessage="Select a node from the tree to view config options.">
        <></>
      </FsPanel>
    );
  }

  return (
    <FsPanel title={`Config Options: ${props.node.name}`} icon={<FsIcon icon={FsIcons.Settings} large />} activeNode={true}>
      <FsConfigOptionsRoot className={classes.root} ownerState={ownerState}>
        {allConfigOptions.map((optionKey) => (
          <div key={optionKey} className={classes.optionItem}>
            <div className={classes.optionHeader}>
              <Typography className={classes.optionTitle}>{optionKey}</Typography>
              <Switch checked={ownerState.isConfigOptionEnabled(optionKey)} />
            </div>
            <Typography className={classes.optionDescription}>
              {ownerState.getConfigDescription(optionKey)}
            </Typography>
            <Divider className={classes.divider} />
          </div>
        ))}
      </FsConfigOptionsRoot>
    </FsPanel>
  );
};
