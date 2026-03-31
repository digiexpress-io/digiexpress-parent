import { Fs, useFsNav, useFsDirent } from '@dxs-ts/fs-api';
import { FsPanelPreviewProps } from './FsPanelPreviewProps';


export interface OwnerState {
  isDarkMode: boolean;
  isPage: boolean;
  content: string;
}

export const useOwnerState = (props: FsPanelPreviewProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent } = useFsDirent();

  const isPage = props.dirent?.type === 'page';
  const page = isPage ? getDirent<Fs.Page>(props.dirent!.id) : undefined;
  const content = page?.content ?? '';

  return { isDarkMode, isPage, content };
}
