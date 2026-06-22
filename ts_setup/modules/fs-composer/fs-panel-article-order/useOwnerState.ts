import { useFsDirent, Fs } from '@dxs-ts/fs-api';


export interface OwnerState {
  articles: Fs.DirentBase[];
  getDirentName: (id: string) => string | undefined;
}

export const useOwnerState = (): OwnerState => {
  const { getDirent, selectOptions, getDirentName } = useFsDirent();


  const articles = selectOptions.articles
    .map(opt => getDirent(opt.value))
    .filter((a): a is Fs.DirentBase => a !== undefined)
    .sort((a, b) => {
      const aOrder = (a.props as Fs.ArticleProps)?.orderNumber ?? 0;
      const bOrder = (b.props as Fs.ArticleProps)?.orderNumber ?? 0;
      return aOrder - bOrder;
    });

  return { articles, getDirentName };
};
