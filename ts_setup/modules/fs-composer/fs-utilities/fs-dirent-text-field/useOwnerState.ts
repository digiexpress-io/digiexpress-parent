import { FsDirentTextFieldProps } from './FsDirentTextFieldProps';

export interface OwnerState {
  isRequired: boolean;
  showRequiredError: boolean;
}

export const useOwnerState = (props: FsDirentTextFieldProps): OwnerState => {
  const isRequired = props.required === true;
  const showRequiredError = isRequired && !props.value?.trim();

  return { isRequired, showRequiredError };
};
