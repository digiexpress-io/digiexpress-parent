import React from 'react';
import { Typography, Box, Switch, Divider } from '@mui/material';
import { FsNode, ConfigOption } from '@dxs-ts/fs-api';
import { FsIcons } from '../fs-theme';
import { ViewContainer } from './ViewContainer';

export interface ConfigOptionsViewProps {
  node: FsNode | undefined;
}

export const ConfigOptionsView: React.FC<ConfigOptionsViewProps> = ({ node }) => {

  const allConfigOptions: (keyof ConfigOption)[] = ['devMode', 'disabledMode', 'anonymousMode', 'assignableMode'];

  const getConfigDescription = (key: keyof ConfigOption): string => {
    switch (key) {
      case 'devMode':
        return 'Asset appears only in the development environment and will not be published publically in a release.';
      case 'disabledMode':
        return 'Asset is not active and will not appear in production.';
      case 'anonymousMode':
        return 'Service is available for anonymous users (does not require login)';
      case 'assignableMode':
        return 'Service can be assigned to customers';
      default:
        return 'No description available for this configuration option.';
    }
  };

  const isConfigOptionEnabled = (optionKey: keyof ConfigOption): boolean => {
    if (!node?.configOptions || node.configOptions.length === 0) {
      return false;
    }

    return node.configOptions.some(configOption => {
      return configOption[optionKey] === true;
    });
  };

  if (!node) {
    return (
      <ViewContainer title="Config Options" icon={<FsIcons.Settings />} activeNode={false} noNodeMessage="Select a node from the tree to view config options.">
        <></>
      </ViewContainer>
    );
  }

  return (
    <ViewContainer title={`Config Options: ${node.name}`} icon={<FsIcons.Settings />} activeNode={true}>
      <Box>
        {allConfigOptions.map((optionKey) => (
          <div key={optionKey}>
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <Typography variant="subtitle2" sx={{ fontWeight: 'bold' }}> {optionKey}</Typography>
              <Switch checked={isConfigOptionEnabled(optionKey)} />
            </Box>
            <Typography variant="subtitle2" color="text.secondary">
              {getConfigDescription(optionKey)}
            </Typography>
            <Divider sx={{ mt: 1 }} />
          </div>
        ))}
      </Box>
    </ViewContainer>
  );
};