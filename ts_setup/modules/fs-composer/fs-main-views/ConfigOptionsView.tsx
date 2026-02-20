import React from 'react';
import { Typography, Box, Switch, Divider } from '@mui/material';
import { FsNode } from '@dxs-ts/fs-api';
import { FsIcons } from '../fs-theme';
import { ViewContainer } from './ViewContainer';

export interface ConfigOptionsViewProps {
  node: FsNode | undefined;
}

export const ConfigOptionsView: React.FC<ConfigOptionsViewProps> = ({ node }) => {

  // Config option descriptions
  const getConfigDescription = (key: string): string => {
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

  if (!node) {
    return (
      <ViewContainer
        title="Config Options"
        icon={<FsIcons.Settings />}
        activeNode={false}
        noNodeMessage="Select a node from the tree to view config options."
      >
        <></>
      </ViewContainer>
    );
  }

  if (!node.configOptions || node.configOptions.length === 0) {
    return (
      <ViewContainer
        title={`Config Options: ${node.name}`}
        icon={<FsIcons.Settings />}
        activeNode={true}
      >
        <Typography variant="body2" color="text.secondary">
          No config options available for this node.
        </Typography>
      </ViewContainer>
    );
  }

  return (
    <ViewContainer title={`Config Options: ${node.name}`} icon={<FsIcons.Settings />} activeNode={true}>
      <Box>
        {node.configOptions.map((configOption, index) => (
          <Box key={index} sx={{ mb: 2 }}>
            {Object.entries(configOption).map(([key, value]) => (
              <div key={key}>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between'}}>
                  <Typography variant="subtitle2" sx={{ fontWeight: 'bold' }}> {key}</Typography>
                  <Switch checked={!!value} />
                </Box>
                <Typography variant="subtitle2" color="text.secondary">
                  {getConfigDescription(key)}
                </Typography>
                <Divider sx={{mt: 1}} />
              </div>
            ))}
          </Box>
        ))}
      </Box>
    </ViewContainer>
  );
};