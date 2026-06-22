import { FsDirentSelectSingleProps } from './FsDirentSelectSingleProps';

export interface OwnerState {
  isRequired: boolean;
  showRequiredError: boolean;
}

export const useOwnerState = (props: FsDirentSelectSingleProps): OwnerState => {
  const isRequired = props.required === true;
  const showRequiredError = isRequired && !props.value;

  return { isRequired, showRequiredError };
};
