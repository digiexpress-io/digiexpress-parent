import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
import { useFsTheme } from '../fs-theme';
import { FsPanelPreviewProps } from './FsPanelPreviewProps';


export interface OwnerState {
  isDarkMode: boolean;
  isArticlePage: boolean;
  isArticleTemplate: boolean;
  isPrintoutPage: boolean;
  isFlow: boolean;
  dirent: Fs.DirentBase | undefined;
  content: {
    pageContent: string;
    templateContent: string;
  };
}

export const useOwnerState = (props: FsPanelPreviewProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  const { activeDirent } = useFsNav();
  const { getDirent } = useFsDirent();

  const dirent = activeDirent ? getDirent(activeDirent.id) : undefined;
  const pageProps = dirent?.type === 'ARTICLE_PAGE' ? dirent.props as Fs.PageProps : undefined;
  const templateProps = dirent?.type === 'ARTICLE_TEMPLATE' ? dirent.props as Fs.TemplateProps : undefined;

  const isArticlePage = dirent?.type === 'ARTICLE_PAGE';
  const isPrintoutPage = dirent?.type === 'PRINTOUT_PAGE';
  const isArticleTemplate = dirent?.type === 'ARTICLE_TEMPLATE';
  const isFlow = dirent?.type === 'FLOW';

  return {
    isDarkMode, isArticlePage, isFlow, isArticleTemplate, isPrintoutPage,
    dirent,
    content: {
      pageContent: pageProps?.content ?? '',
      templateContent: templateProps?.content ?? '',
    }
  };
}
