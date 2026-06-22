import { FsDirentButtonSaveProps } from './FsDirentButtonSaveProps';
import { FsDirentButtonOpenProps } from './FsDirentButtonOpenProps';

export interface OwnerState {
  disabled: boolean;
}

export const useOwnerState = (props: FsDirentButtonOpenProps | FsDirentButtonSaveProps): OwnerState => {
  return { disabled: props.disabled ?? false };
};
