import { ConfigOption, useFs } from '@dxs-ts/fs-api';
import { FsConfigOptionsProps } from './FsConfigOptionsProps';



export interface OwnerState {
  isDarkMode: boolean;
  isConfigOptionEnabled: (optionKey: keyof ConfigOption) => boolean;
  getConfigDescription:(optionKey: keyof ConfigOption) => string;
}


export const useOwnerState = (props: FsConfigOptionsProps): OwnerState => {
  const { isDarkMode } = useFs();

  function isConfigOptionEnabled(optionKey: keyof ConfigOption): boolean {
    return props.node?.configOptions?.some(configOption => configOption[optionKey] === true) ?? false;
  }

  const getConfigDescription = (optionKey: keyof ConfigOption): string => {
    switch (optionKey) {
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
  

  return ({ isDarkMode, isConfigOptionEnabled, getConfigDescription });
}