import { useFsNav, useFsDirent, ArticleEntry } from '@dxs-ts/fs-api';
import { FsArticleOrderProps } from './FsArticleOrderProps';


export interface OwnerState {
  isDarkMode: boolean;
  articles: ArticleEntry[];
}

export const useOwnerState = (_props: FsArticleOrderProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  const { getArticles } = useFsDirent();

  const articles = getArticles()
    .filter((dirent): dirent is ArticleEntry => dirent.type === 'article')
    .sort((a, b) => a.orderNumber - b.orderNumber);

  return { isDarkMode, articles };
};
