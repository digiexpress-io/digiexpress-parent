import React from 'react';
import { Fs, useFsDirent, useFsNav } from '@dxs-ts/fs-api';
import { FsDirentSelectSingleOption } from '../fs-dirent-select-single';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  articleId: string;
  localeCode: string;
  description: string;
  configOptions: Fs.ConfigOption[];
  availableConfigOptions: Fs.SelectOption[];
  articleOptions: FsDirentSelectSingleOption[];
  localeOptions: FsDirentSelectSingleOption[];
  onChangeArticleId: (value: string) => void;
  onChangeLocaleCode: (value: string) => void;
  onChangeDescription: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
  content: string;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent, selectOptions, getConfigOptionsForType, fetchDirentBody } = useFsDirent();

  const dirent = getDirent(props.direntId)!;
  const pageProps = dirent.props as Fs.PageProps;
  const [content, setContent] = React.useState(pageProps.content!);
  const [articleId, setArticleId] = React.useState(pageProps.articleId);
  const [localeCode, setLocaleCode] = React.useState(pageProps.localeCode);
  const [description, setDescription] = React.useState(pageProps.description ?? '');
  const [configOptions, setConfigOptions] = React.useState<Fs.ConfigOption[]>(
    (dirent?.props?.configOptions ?? []) as Fs.ConfigOption[]
  );

  const articleOptions: FsDirentSelectSingleOption[] = selectOptions.articles;
  const localeOptions: FsDirentSelectSingleOption[] = selectOptions.languages.map(lang => ({ value: lang, label: lang }));
  const availableConfigOptions: Fs.SelectOption[] = getConfigOptionsForType('ARTICLE_PAGE');


  React.useEffect(() => {
    fetchDirentBody(props.direntId, 'ARTICLE_PAGE')
      .then(body => setContent((body as Fs.ArticlePageBody).content));
  }, [props.direntId])

  function onChangeArticleId(value: string) {
    setArticleId(value);
  }

  function onChangeLocaleCode(value: string) {
    setLocaleCode(value);
  }

  function onChangeDescription(value: string) {
    setDescription(value);
  }

  function onChangeConfigOptions(value: string[]) {
    setConfigOptions(value as Fs.ConfigOption[]);
  }

  return ({
    isDarkMode,
    dirent,
    content,
    articleId,
    localeCode,
    description,
    configOptions,
    availableConfigOptions,
    articleOptions,
    localeOptions,
    onChangeArticleId,
    onChangeLocaleCode,
    onChangeDescription,
    onChangeConfigOptions,
  });
};
