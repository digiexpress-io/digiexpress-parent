import { FsDirentTextFieldProps } from './FsDirentTextFieldProps';
import { useFsNav } from '@dxs-ts/fs-nav';

export interface OwnerState {
  isDarkMode: boolean;
  isRequired: boolean;
  showRequiredError: boolean;
}

export const useOwnerState = (props: FsDirentTextFieldProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  const isRequired = props.required === true;
  const showRequiredError = isRequired && !props.value?.trim();

  return { isDarkMode, isRequired, showRequiredError };
};
