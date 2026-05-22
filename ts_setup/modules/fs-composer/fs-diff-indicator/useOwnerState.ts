import { useFsTheme } from '../fs-theme';
import { useFsu } from '@dxs-ts/fs-api';
import { FsDiffIndicatorProps } from './FsDiffIndicatorProps';

export interface OwnerState {
  isDarkMode: boolean;
  isChanged: boolean;
}

export const useOwnerState = (props: FsDiffIndicatorProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  const { isChange, getChange } = useFsu();
  const isChanged = isChange(props.direntId) && getChange(props.direntId).isChanged;
  return { isDarkMode, isChanged };
};
