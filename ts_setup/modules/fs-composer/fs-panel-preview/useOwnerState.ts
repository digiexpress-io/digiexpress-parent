import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
import { useFsTheme } from '../fs-theme';
import { FsPanelPreviewProps } from './FsPanelPreviewProps';


export interface OwnerState {
  isDarkMode: boolean;
  isPage: boolean;
  isFlow: boolean;
  dirent: Fs.DirentBase | undefined;
  content: {
    pageContent: string;
  };
}

export const useOwnerState = (props: FsPanelPreviewProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  const { activeDirent } = useFsNav();
  const { getDirent } = useFsDirent();

  const dirent = activeDirent ? getDirent(activeDirent.id) : undefined;
  const pageProps = dirent?.type === 'ARTICLE_PAGE' ? dirent.props as Fs.PageProps : undefined;

  const isPage = dirent?.type === 'ARTICLE_PAGE';
  const isFlow = dirent?.type === 'FLOW';

  return {
    isDarkMode, isPage, isFlow,
    dirent,
    content: {
      pageContent: pageProps?.content ?? '',
    }
  };
}
