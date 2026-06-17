import { FsDirentSelectSingleProps } from './FsDirentSelectSingleProps';
import { useFsTheme } from '../../fs-theme';

export interface OwnerState {
  isDarkMode: boolean;
  isRequired: boolean;
  showRequiredError: boolean;
}

export const useOwnerState = (props: FsDirentSelectSingleProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  const isRequired = props.required === true;
  const showRequiredError = isRequired && !props.value;

  return { isDarkMode, isRequired, showRequiredError };
};
