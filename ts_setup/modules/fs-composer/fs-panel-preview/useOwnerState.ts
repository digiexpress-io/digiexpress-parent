import { Fs, useFsNav, useFsDirent } from '@dxs-ts/fs-api';
import { FsPanelPreviewProps } from './FsPanelPreviewProps';


export interface OwnerState {
  isDarkMode: boolean;
  isPage: boolean;
  isFlow: boolean;
  content: string;
}

export const useOwnerState = (props: FsPanelPreviewProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent } = useFsDirent();

  const isPage = props.dirent?.type === 'ARTICLE_PAGE';
  const isFlow = props.dirent?.type === 'FLOW';
  const page = isPage ? getDirent<Fs.Page>(props.dirent!.id) : undefined;
  const flow = isFlow ? getDirent<Fs.Flow>(props.dirent!.id) : undefined;
  const content = page?.content ?? flow?.content ?? '';

  return { isDarkMode, isPage, isFlow, content };
}
