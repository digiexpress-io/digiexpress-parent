import { useIntl } from 'react-intl';
import { Fs, useFsNav } from '@dxs-ts/fs-api';
import { FsPanelConfigOptionsProps } from './FsPanelConfigOptionsProps';


export interface OwnerState {
  isDarkMode: boolean;
  isConfigOptionEnabled: (optionKey: Fs.ConfigOption) => boolean;
  configDescription: (optionKey: Fs.ConfigOption) => string;
}

export function useOwnerState(props: FsPanelConfigOptionsProps): OwnerState {
  const intl = useIntl();
  const { isDarkMode } = useFsNav();
  const configOptions = props.dirent?.configOptions ?? [];

  function isConfigOptionEnabled(optionKey: Fs.ConfigOption): boolean {
    return configOptions.includes(optionKey);
  }

  function configDescription(optionKey: Fs.ConfigOption): string {
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



