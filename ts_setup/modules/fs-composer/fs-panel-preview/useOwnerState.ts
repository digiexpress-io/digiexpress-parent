import React from 'react';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
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
  const { isDarkMode } = useFsNav();
  const { activeDirent } = useFsNav();
  const { getDirent, fetchDirentBody } = useFsDirent();

  const dirent = activeDirent ? getDirent(activeDirent.id) : undefined;
  const pageProps = dirent?.type === 'ARTICLE_PAGE' ? dirent.props as Fs.PageProps : undefined;

  const isPage = dirent?.type === 'ARTICLE_PAGE';
  const isFlow = dirent?.type === 'FLOW';

  const [pageContent, setPageContent] = React.useState('');

  React.useEffect(() => {
    if (isPage && dirent) {
      fetchDirentBody(dirent.id, 'ARTICLE_PAGE')
        .then(body => setPageContent((body as Fs.ArticlePageBody).content));
    } else {
      setPageContent(pageProps?.content ?? '');
    }
  }, [activeDirent?.id]);

  return {
    isDarkMode, isPage, isFlow,
    dirent,
    content: {
      pageContent,
    }
  };
}
