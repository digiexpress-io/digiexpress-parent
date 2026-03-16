import React from 'react';
import { Typography, Switch, Divider } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-panel';
import { FsConfigOptionsProps } from './FsConfigOptionsProps';
import { useOwnerState } from './useOwnerState';
import { FsConfigOptionsRoot, useUtilityClasses } from './useUtilityClasses';
import { ConfigOption } from '@dxs-ts/fs-api';

const allConfigOptions: (keyof ConfigOption)[] = ['devMode', 'disabledMode', 'anonymousMode', 'assignableMode'];

export const FsConfigOptions: React.FC<FsConfigOptionsProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  const direntName = props.node ? props.node.name : 'eeee';

  if (!props.node) {
    return (
      <FsPanel
        title={intl.formatMessage({ id: 'fs.configOptions.title' })}
        icon={<FsIcon icon={FsIcons.Settings} large />} activeNode={false}
        noNodeMessage={intl.formatMessage({ id: 'fs.configOptions.message.selectDirent' })}>
      </FsPanel>
    );
  }

  return (
    <FsPanel title={intl.formatMessage({ id: 'fs.configOptions.title.direntName' }, { direntName })}
      icon={<FsIcon icon={FsIcons.Settings} large />} activeNode={true}>
      <FsConfigOptionsRoot className={classes.root} ownerState={ownerState}>
        {allConfigOptions.map((optionKey) => (
          <div key={optionKey} className={classes.optionItem}>
            <div className={classes.optionHeader}>
              <Typography className={classes.optionTitle}>{optionKey}</Typography>
              <Switch checked={ownerState.isConfigOptionEnabled(optionKey)} />
            </div>
            <Typography className={classes.optionDescription}>
              {ownerState.configDescription(optionKey)}
            </Typography>
            <Divider className={classes.divider} />
          </div>
        ))}
      </FsConfigOptionsRoot>
    </FsPanel>
  );
};
