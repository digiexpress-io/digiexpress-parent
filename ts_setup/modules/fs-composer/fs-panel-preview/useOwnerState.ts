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
  const pageProps = dirent?.type === 'ARTICLE_PAGE' ? dirent.props as Fs.PageProps : undefined;
  const flowProps = dirent?.type === 'FLOW' ? dirent.props as Fs.FlowProps : undefined;
  const content = pageProps?.content ?? flowProps?.content ?? '';

  const isPage = props.dirent?.type === 'ARTICLE_PAGE';
  const isFlow = props.dirent?.type === 'FLOW';


  return { isDarkMode, isPage, isFlow, content };
}
