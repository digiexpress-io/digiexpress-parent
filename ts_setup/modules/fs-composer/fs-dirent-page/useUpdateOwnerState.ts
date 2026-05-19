import React from 'react';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { FsDirentSelectSingleOption } from '../fs-dirent-select-single';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  articleName: string;
  localeCode: string;
  description: string;
  configOptions: Fs.ConfigOption[];
  availableConfigOptions: Fs.SelectOption[];
  localeOptions: FsDirentSelectSingleOption[];
  isExpanded: boolean;
  onChangeLocaleCode: (value: string) => void;
  onChangeDescription: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onToggleExpanded: () => void;
  content: string;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent, getArticleName, selectOptions, getConfigOptionsForType, fetchDirentBody } = useFsDirent();

  const dirent = getDirent(props.direntId)!;
  const pageProps = dirent.props as Fs.PageProps;
  const [content, setContent] = React.useState(pageProps.content ?? '');
  const [localeCode, setLocaleCode] = React.useState(pageProps.localeCode);
  const [description, setDescription] = React.useState(pageProps.description ?? '');
  const [isExpanded, setIsExpanded] = React.useState(false);
  const [configOptions, setConfigOptions] = React.useState<Fs.ConfigOption[]>(
    (dirent?.props?.configOptions ?? []) as Fs.ConfigOption[]
  );

  const articleName = getArticleName(pageProps.articleId) ?? '';
  const localeOptions: FsDirentSelectSingleOption[] = selectOptions.languages;
  const availableConfigOptions: Fs.SelectOption[] = getConfigOptionsForType('ARTICLE_PAGE');


  React.useEffect(() => {
    fetchDirentBody(props.direntId, 'ARTICLE_PAGE')
      .then(body => setContent((body as Fs.ArticlePageBody).content));
  }, [props.direntId])



  function onChangeLocaleCode(value: string) {
    setLocaleCode(value);
  }

  function onChangeDescription(value: string) {
    setDescription(value);
  }

  function onChangeConfigOptions(value: string[]) {
    setConfigOptions(value as Fs.ConfigOption[]);
  }

  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }

  return ({
    isDarkMode,
    dirent,
    content,
    localeCode,
    description,
    articleName,
    configOptions,
    availableConfigOptions,
    localeOptions,
    isExpanded,
    onChangeLocaleCode,
    onChangeDescription,
    onChangeConfigOptions,
    onToggleExpanded,
  });
};
