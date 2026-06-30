import { useFsDirent, Fs } from '@dxs-ts/fs-api';
import { FsPanelArticleLocaleOverviewProps } from './FsPanelArticleLocaleOverviewProps';


export type LocaleOption = Fs.SelectOption & { enabled: boolean };

export interface OwnerState {
  articles: Fs.DirentBase[];
  locales: LocaleOption[];
  getDirentName: (id: string) => string | undefined;
  isPageInLocale: (articleId: string, localeCode: string) => boolean;
}

export const useOwnerState = (_props: FsPanelArticleLocaleOverviewProps): OwnerState => {
  const { getDirent, selectOptions, getDirentName } = useFsDirent();

  const articles = selectOptions.articles
    .map(opt => getDirent(opt.value))
    .filter((a): a is Fs.DirentBase => a !== undefined)
    .sort((a, b) => {
      const aOrder = (a.props as Fs.ArticleProps)?.orderNumber ?? 0;
      const bOrder = (b.props as Fs.ArticleProps)?.orderNumber ?? 0;
      return aOrder - bOrder;
    });

  const allLocaleProps = Object.values(selectOptions.direntProps)
    .filter(p => p.type === 'LOCALE') as Fs.LanguageProps[];

  const locales: LocaleOption[] = selectOptions.languages.map(l => {
    const lp = allLocaleProps.find(p => p.id === l.value);
    return { ...l, enabled: lp?.enabled !== false };
  });

  const pages = Object.values(selectOptions.direntProps)
    .filter(p => p.type === 'ARTICLE_PAGE') as Fs.PageProps[];

  function isPageInLocale(articleId: string, localeCode: string): boolean {
    return pages.some(p => p.articleId === articleId && p.localeCode === localeCode);
  }

  return { articles, locales, getDirentName, isPageInLocale };
};
