import React from 'react';
import { useFsNav, useFsDirent, Fs } from '@dxs-ts/fs-api';

export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.Dirent | undefined;
  name: string;
  dialobFormName: string;
  dialobFormTag: string;
  flowName: string;
  validityStart: string;
  validityEnd: string;
  articles: string[];
  configOptions: Fs.ConfigOption[];
  intlValues: Record<string, string>;
  locales: string[];
  isExpanded: boolean;
  onChangeName: (value: string) => void;
  onChangeDialobFormName: (value: string) => void;
  onChangeDialobFormTag: (value: string) => void;
  onChangeFlowName: (value: string) => void;
  onChangeValidityStart: (value: string) => void;
  onChangeValidityEnd: (value: string) => void;
  onChangeArticles: (value: string[]) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onChangeIntlValues: (locale: string, value: string) => void;
  onToggleExpanded: () => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent, selectOptions } = useFsDirent();

  const dirent = getDirent(props.direntId);
  const service = dirent?.type === 'ARTICLE_WORKFLOW' ? dirent : undefined;

  const [name, setName] = React.useState(dirent?.name ?? '');
  const [dialobFormName, setDialobFormName] = React.useState(service?.dialobFormName ?? '');
  const [dialobFormTag, setDialobFormTag] = React.useState(service?.dialobFormTag ?? '');
  const [flowName, setFlowName] = React.useState(service?.flowName ?? '');
  const [validityStart, setValidityStart] = React.useState(service?.validityStart ?? '');
  const [validityEnd, setValidityEnd] = React.useState(service?.validityEnd ?? '');
  const [articles, setArticles] = React.useState<string[]>((service?.articles ?? []) as string[]);
  const [configOptions, setConfigOptions] = React.useState<Fs.ConfigOption[]>(
    (dirent?.configOptions ?? []) as Fs.ConfigOption[]
  );
  const [intlValues, setIntlValues] = React.useState<Record<string, string>>(service?.intlValues ?? {});
  const [isExpanded, setIsExpanded] = React.useState(false);

  const locales = selectOptions.languages;

  function onChangeName(value: string) {
    setName(value);
  }

  function onChangeDialobFormName(value: string) {
    setDialobFormName(value);
  }

  function onChangeDialobFormTag(value: string) {
    setDialobFormTag(value);
  }

  function onChangeFlowName(value: string) {
    setFlowName(value);
  }

  function onChangeValidityStart(value: string) {
    setValidityStart(value);
  }

  function onChangeValidityEnd(value: string) {
    setValidityEnd(value);
  }

  function onChangeArticles(value: string[]) {
    setArticles(value);
  }

  function onChangeConfigOptions(value: string[]) {
    setConfigOptions(value as Fs.ConfigOption[]);
  }

  function onChangeIntlValues(locale: string, value: string) {
    setIntlValues(prev => ({ ...prev, [locale]: value }));
  }

  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }

  return ({
    isDarkMode,
    dirent,
    name,
    dialobFormName,
    dialobFormTag,
    flowName,
    validityStart,
    validityEnd,
    articles,
    configOptions,
    intlValues,
    locales,
    isExpanded,
    onChangeName,
    onChangeDialobFormName,
    onChangeDialobFormTag,
    onChangeFlowName,
    onChangeValidityStart,
    onChangeValidityEnd,
    onChangeArticles,
    onChangeConfigOptions,
    onChangeIntlValues,
    onToggleExpanded,
  });
};
