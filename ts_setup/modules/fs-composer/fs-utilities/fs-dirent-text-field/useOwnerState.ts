import { FsDirentTextFieldProps } from './FsDirentTextFieldProps';
import { useFsTheme } from '../../fs-theme';

export interface OwnerState {
  isDarkMode: boolean;
  isRequired: boolean;
  showRequiredError: boolean;
}

export const useOwnerState = (props: FsDirentTextFieldProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  const isRequired = props.required === true;
  const showRequiredError = isRequired && !props.value?.trim();

  return { isDarkMode, isRequired, showRequiredError };
};
