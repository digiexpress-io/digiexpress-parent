import { useFsNav } from '@dxs-ts/fs-api';
import { FsPanelHistoryProps } from './FsPanelHistoryProps';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsPanelHistoryProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode});
}
