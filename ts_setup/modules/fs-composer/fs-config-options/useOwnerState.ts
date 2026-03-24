import { useIntl } from 'react-intl';
import { FsDirentConfigOption, useFsNav, useFsDirentProps } from '@dxs-ts/fs-api';
import { FsConfigOptionsProps } from './FsConfigOptionsProps';


export interface OwnerState {
  isDarkMode: boolean;
  isConfigOptionEnabled: (optionKey: FsDirentConfigOption) => boolean;
  configDescription: (optionKey: FsDirentConfigOption) => string;
}

export function useOwnerState(props: FsConfigOptionsProps): OwnerState {
  const intl = useIntl();
  const { isDarkMode } = useFsNav();
  const { getDirentProps } = useFsDirentProps();
  const direntProps = getDirentProps(props.dirent?.id ?? '');

  function isConfigOptionEnabled(optionKey: FsDirentConfigOption): boolean {
    return direntProps.configOptions.includes(optionKey);
  }

  function configDescription(optionKey: FsDirentConfigOption): string {
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


