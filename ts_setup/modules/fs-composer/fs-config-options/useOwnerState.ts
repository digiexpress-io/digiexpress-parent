import { useIntl } from 'react-intl';
import { FsDirentConfigOption, useFsNav } from '@dxs-ts/fs-api';
import { FsConfigOptionsProps } from './FsConfigOptionsProps';


export interface OwnerState {
  isDarkMode: boolean;
  isConfigOptionEnabled: (optionKey: keyof FsDirentConfigOption) => boolean;
  configDescription: (optionKey: keyof FsDirentConfigOption) => string;
}

export function useOwnerState(props: FsConfigOptionsProps): OwnerState {
  const intl = useIntl();
  const { isDarkMode } = useFsNav();

  function isConfigOptionEnabled(optionKey: keyof FsDirentConfigOption): boolean {
    return props.dirent?.configOptions?.some(configOption => configOption[optionKey] === true) ?? false;
  }

  function configDescription(optionKey: keyof FsDirentConfigOption): string {
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


