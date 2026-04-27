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

  const dirent = props.dirent ? getDirent(props.dirent.id) : undefined;
  const page = dirent?.type === 'ARTICLE_PAGE' ? dirent : undefined;
  const flow = dirent?.type === 'FLOW' ? dirent : undefined;
  const content = page?.content ?? flow?.content ?? '';

  const isPage = props.dirent?.type === 'ARTICLE_PAGE';
  const isFlow = props.dirent?.type === 'FLOW';


  return { isDarkMode, isPage, isFlow, content };
}
