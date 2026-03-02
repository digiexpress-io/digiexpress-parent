import React from 'react';
import { Typography, Switch, Divider } from '@mui/material';
import { ConfigOption } from '@dxs-ts/fs-api';
import { FsIcons } from '../fs-theme';
import { ViewContainer } from '../fs-main-views';
import { FsConfigOptionsProps } from './FsConfigOptionsProps';
import { useOwnerState } from './useOwnerState';
import { FsConfigOptionsRoot, useUtilityClasses } from './useUtilityClasses';



const allConfigOptions: (keyof ConfigOption)[] = ['devMode', 'disabledMode', 'anonymousMode', 'assignableMode'];


export const FsConfigOptions: React.FC<FsConfigOptionsProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();


  if (!props.node) {
    return (
      <ViewContainer title="Config Options" icon={<FsIcons.Settings />} activeNode={false} noNodeMessage="Select a node from the tree to view config options.">
        <></>
      </ViewContainer>
    );
  }

  return (
    <ViewContainer title={`Config Options: ${props.node.name}`} icon={<FsIcons.Settings />} activeNode={true}>
      <FsConfigOptionsRoot className={classes.root} ownerState={ownerState}>
        {allConfigOptions.map((optionKey) => (
          <div key={optionKey} className={classes.optionItem}>
            <div className={classes.optionHeader}>
              <Typography variant="subtitle2" className={classes.optionTitle}>{optionKey}</Typography>
              <Switch checked={ownerState.isConfigOptionEnabled(optionKey)} />
            </div>
            <Typography variant="subtitle2" className={classes.optionDescription}>
              {ownerState.getConfigDescription(optionKey)}
            </Typography>
            <Divider className={classes.divider} />
          </div>
        ))}
      </FsConfigOptionsRoot>
    </ViewContainer>
  );
};
