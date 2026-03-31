import { useFsNav, useFsDirent, FsDirent } from '@dxs-ts/fs-api';
import { FsArticleOrderProps } from './FsArticleOrderProps';


export interface OwnerState {
  isDarkMode: boolean;
  articles: FsDirent.Article[];
}

export const useOwnerState = (_props: FsArticleOrderProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent, selectOptions } = useFsDirent();

  const articles = selectOptions.articles
    .map(opt => getDirent<FsDirent.Article>(opt.value))
    .filter((a): a is FsDirent.Article => a !== undefined)
    .sort((a, b) => a.orderNumber - b.orderNumber);

  return { isDarkMode, articles };
};
