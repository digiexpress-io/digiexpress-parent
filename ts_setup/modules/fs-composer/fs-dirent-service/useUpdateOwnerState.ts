import React from 'react';
import { useFsNav, useFsDirentProps, FsDirentData, mockFsData, mockFsDirentProperties, FsDirentConfigOption, ServiceEntry } from '@dxs-ts/fs-api';


const data = new FsDirentData(mockFsData, mockFsDirentProperties);

export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: ServiceEntry | undefined;
  name: string;
  dialobFormName: string;
  dialobFormTag: string;
  flowName: string;
  validityStart: string;
  validityEnd: string;
  articles: string[];
  configOptions: FsDirentConfigOption[];
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
  const { getDirent } = useFsDirentProps();

  const dirent = getDirent(props.direntId) as ServiceEntry | undefined;

  const [name, setName] = React.useState(dirent?.name ?? '');
  const [dialobFormName, setDialobFormName] = React.useState(dirent?.dialobFormName ?? '');
  const [dialobFormTag, setDialobFormTag] = React.useState(dirent?.dialobFormTag ?? '');
  const [flowName, setFlowName] = React.useState(dirent?.flowName ?? '');
  const [validityStart, setValidityStart] = React.useState(dirent?.validityStart ?? '');
  const [validityEnd, setValidityEnd] = React.useState(dirent?.validityEnd ?? '');
  const [articles, setArticles] = React.useState<string[]>((dirent?.articles ?? []) as string[]);
  const [configOptions, setConfigOptions] = React.useState<FsDirentConfigOption[]>(
    (dirent?.configOptions ?? []) as FsDirentConfigOption[]
  );
  const [intlValues, setIntlValues] = React.useState<Record<string, string>>(dirent?.intlValues ?? {});
  const [isExpanded, setIsExpanded] = React.useState(false);

  const locales = data.languages;

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
    setConfigOptions(value as FsDirentConfigOption[]);
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
