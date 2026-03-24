import { FsDirentProps, useFsNav, useFsDirentProps } from '@dxs-ts/fs-api';
import { FsPropertiesProps } from './FsPropertiesProps';


export interface OwnerState {
  isDarkMode: boolean;
  direntProps: FsDirentProps | undefined;
}

export const useOwnerState = (props: FsPropertiesProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirentProps } = useFsDirentProps();

  const direntProps = props.dirent ? getDirentProps(props.dirent.id) : undefined;

  return ({ isDarkMode, direntProps });
}