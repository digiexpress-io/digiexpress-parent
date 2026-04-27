import { useFsNav, useFsDirent, Fs } from '@dxs-ts/fs-api';
import { FsPanelArticleOrderProps } from './FsPanelArticleOrderProps';


export interface OwnerState {
  isDarkMode: boolean;
  articles: Fs.DirentBase[];
}

export const useOwnerState = (_props: FsPanelArticleOrderProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent, selectOptions } = useFsDirent();

  const articles = selectOptions.articles
    .map(opt => getDirent(opt.value))
    .filter((a): a is Fs.DirentBase => a !== undefined)
    .sort((a, b) => {
      const aOrder = (a.props as Fs.ArticleProps)?.orderNumber ?? 0;
      const bOrder = (b.props as Fs.ArticleProps)?.orderNumber ?? 0;
      return aOrder - bOrder;
    });

  return { isDarkMode, articles };
};
