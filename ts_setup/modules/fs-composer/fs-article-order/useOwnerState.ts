import { useFsNav, useFsDirent, ArticleEntry } from '@dxs-ts/fs-api';
import { FsArticleOrderProps } from './FsArticleOrderProps';


export interface OwnerState {
  isDarkMode: boolean;
  articles: ArticleEntry[];
}

export const useOwnerState = (_props: FsArticleOrderProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent, selectOptions } = useFsDirent();

  const articles = selectOptions.articles
    .map(opt => getDirent<ArticleEntry>(opt.value))
    .filter((a): a is ArticleEntry => a !== undefined)
    .sort((a, b) => a.orderNumber - b.orderNumber);

  return { isDarkMode, articles };
};
