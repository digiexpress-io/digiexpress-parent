import { FsPanelHistoryProps } from './FsPanelHistoryProps';
import { useFsNav } from '@dxs-ts/fs-nav';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsPanelHistoryProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode});
}
