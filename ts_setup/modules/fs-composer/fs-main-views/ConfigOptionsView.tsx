import React from 'react';
import { Typography, Box, Switch, Divider, styled, generateUtilityClass } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsNode, ConfigOption } from '@dxs-ts/fs-api';
import { FsColors, FsIcons } from '../fs-theme';
import { useFs } from '@dxs-ts/fs-api';
import { ViewContainer } from './ViewContainer';


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
const allConfigOptions: (keyof ConfigOption)[] = ['devMode', 'disabledMode', 'anonymousMode', 'assignableMode'];

export interface ConfigOptionsViewProps {
  node: FsNode | undefined;
}

export const ConfigOptionsView: React.FC<ConfigOptionsViewProps> = ({ node }) => {
  const { isDarkMode } = useFs();
  const classes = useUtilityClasses(isDarkMode);

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
      <ConfigOptionsViewRoot className={classes.root} isDarkMode={isDarkMode}>
        {allConfigOptions.map((optionKey) => (
          <div key={optionKey} className={classes.optionItem}>
            <div className={classes.optionHeader}>
              <Typography variant="subtitle2" className={classes.optionTitle}>{optionKey}</Typography>
              <Switch checked={isConfigOptionEnabled(optionKey)} />
            </div>
            <Typography variant="subtitle2" className={classes.optionDescription}>
              {getConfigDescription(optionKey)}
            </Typography>
            <Divider className={classes.divider} />
          </div>
        ))}
      </ConfigOptionsViewRoot>
    </ViewContainer>
  );
};

const MUI_NAME = 'ConfigOptionsView';

export interface ConfigOptionsViewClasses {
  root: string;
  optionItem: string;
  optionHeader: string;
  optionTitle: string;
  optionDescription: string;
  divider: string;
}

export type ConfigOptionsViewClassKey = keyof ConfigOptionsViewClasses;

const useUtilityClasses = (isDarkMode: boolean) => {
  const slots = {
    root: ['root'],
    optionItem: ['optionItem'],
    optionHeader: ['optionHeader'],
    optionTitle: ['optionTitle'],
    optionDescription: ['optionDescription'],
    divider: ['divider'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

const ConfigOptionsViewRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'isDarkMode',
})<{ isDarkMode: boolean }>(({ theme, isDarkMode }) => ({
  display: 'flex',
  flexDirection: 'column',

  [`& .${MUI_NAME}-optionItem`]: {
    marginBottom: theme.spacing(1),
  },

  [`& .${MUI_NAME}-optionHeader`]: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
  },

  [`& .${MUI_NAME}-optionTitle`]: {
    fontWeight: 'bold',
  },

  [`& .${MUI_NAME}-optionDescription`]: {
    color: isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
  },

  [`& .${MUI_NAME}-divider`]: {
    marginTop: theme.spacing(1),
  },
}));