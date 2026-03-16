import { useIntl } from 'react-intl';
import { ConfigOption, useFs } from '@dxs-ts/fs-api';
import { FsConfigOptionsProps } from './FsConfigOptionsProps';


export interface OwnerState {
  isDarkMode: boolean;
  isConfigOptionEnabled: (optionKey: keyof ConfigOption) => boolean;
  configDescription: (optionKey: keyof ConfigOption) => string;
}

export function useOwnerState(props: FsConfigOptionsProps): OwnerState {
  const intl = useIntl();
  const { isDarkMode } = useFs();

  function isConfigOptionEnabled(optionKey: keyof ConfigOption): boolean {
    return props.node?.configOptions?.some(configOption => configOption[optionKey] === true) ?? false;
  }

  function configDescription(optionKey: keyof ConfigOption): string {
    switch (optionKey) {
      case 'devMode':
        return intl.formatMessage({ id: 'fs.configOptions.optionKey.devMode.desc' });
      case 'disabledMode':
        return intl.formatMessage({ id: 'fs.configOptions.optionKey.disabledMode.disabled' });
      case 'anonymousMode':
        return intl.formatMessage({ id: 'fs.configOptions.optionKey.anonymousMode.disabled' });
      case 'assignableMode':
        return intl.formatMessage({ id: 'fs.configOptions.optionKey.assignableMode.disabled' });
      default:
        return intl.formatMessage({ id: 'fs.configOptions.optionKey.assignableMode.desc.none' });
    }
  }

  return ({ isDarkMode, isConfigOptionEnabled, configDescription });
}


