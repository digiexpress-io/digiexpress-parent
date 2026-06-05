import React from 'react';
import { useFsTheme } from '../fs-theme';
import {
  Fs,
  useFsDirent,
  useFsu,
  FsuChange
} from '@dxs-ts/fs-api';

export interface TextFields {
  content: string;
  assetDescription: string;
}

export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  id: string;
  isChanged: boolean;
  isExpanded: boolean;
  content: string;
  assetDescription: string;
  configOptions: Fs.ConfigOption[];
  tagLabels: string[];
  onChangeContent: (value: string) => void;
  onChangeConfigOptions: (value: string[]) => void;
  onChangeLabels: (value: string[]) => void;
  onChangeDescription: (value: string) => void;
  onBlurDescription: () => void;
  onToggleExpanded: () => void;
  onCancel: () => void;
}

type _ChangeStateProps = {
  flowId: string;
  bodyType: Fs.BodyType;
  flowValue: string;
  assetDescription: string;
  configOptions: Fs.ConfigOption[];
  devMode: boolean;
  disabledMode: boolean;
  tagLabels: string[];
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;
  // Holds a pointer to the ref object — dereferenced at save time so getCurrentProps()
  // always returns the latest editor content regardless of when this instance was created.
  private _liveContent: React.MutableRefObject<string>;

  constructor(props: _ChangeStateProps, liveContent: React.MutableRefObject<string>, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
    this._liveContent = liveContent;
  }

  get id() { return this._current.flowId; }
  get bodyType() { return this._current.bodyType; }
  get assetDescription() { return this._current.assetDescription; }
  get configOptions() { return this._current.configOptions; }
  get tagLabels() { return this._current.tagLabels; }

  get isChanged(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    return {
      bodyType: this._current.bodyType,
      id: this.id,
      changes: { ...this._current, flowValue: this._liveContent.current },
    };
  }

  withFlowValue(flowValue: string): _ChangeState {
    return new _ChangeState({ ...this._current, flowValue }, this._liveContent, this._origin);
  }

  withDescription(assetDescription: string): _ChangeState {
    return new _ChangeState({ ...this._current, assetDescription }, this._liveContent, this._origin);
  }

  withConfigOptions(configOptions: Fs.ConfigOption[]): _ChangeState {
    return new _ChangeState({ ...this._current, configOptions, devMode: configOptions.includes('DEV_MODE'), disabledMode: configOptions.includes('DISABLED_MODE') }, this._liveContent, this._origin);
  }

  withTagLabels(tagLabels: string[]): _ChangeState {
    return new _ChangeState({ ...this._current, tagLabels }, this._liveContent, this._origin);
  }
}


export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent, fetchDirentBody } = useFsDirent();
  const { withNewChange, withChange, cancel } = useFsu();

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => withChange(props.direntId, callback);

  const dirent = getDirent(props.direntId);

  const [fields, setFields] = React.useState<TextFields>({ content: '', assetDescription: dirent?.props?.assetDescription ?? '' });
  const [isExpanded, setIsExpanded] = React.useState(false);
  const originalContentRef = React.useRef<string>('');
  const liveContentRef = React.useRef<string>('');
  const debounceRef = React.useRef<ReturnType<typeof setTimeout> | undefined>(undefined);

  const state = withNewChange(props.direntId, () => new _ChangeState(
    {
      flowId: props.direntId,
      bodyType: dirent!.type,
      flowValue: liveContentRef.current,
      assetDescription: dirent?.props?.assetDescription ?? '',
      configOptions: (dirent?.props?.configOptions ?? []) as Fs.ConfigOption[],
      devMode: (dirent?.props?.configOptions ?? []).includes('DEV_MODE'),
      disabledMode: (dirent?.props?.configOptions ?? []).includes('DISABLED_MODE'),
      tagLabels: (dirent?.props?.labels ?? []).map(l => l.value),
    },
    liveContentRef,
  ));

  React.useEffect(() => {
    fetchDirentBody(props.direntId, 'FLOW').then((body) => {
      const wb = body as Fs.WrenchBody;
      const yaml = wb.flows[props.direntId]?.ast?.parseTree?.value ?? '';
      originalContentRef.current = yaml;
      liveContentRef.current = yaml;
      setFields({ content: yaml, assetDescription: dirent?.props?.assetDescription ?? '' });
      cancel(props.direntId);
    });
  }, [props.direntId]);

  function onChangeContent(value: string) {
    liveContentRef.current = value;
    setFields(prev => ({ ...prev, content: value }));
    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }
    debounceRef.current = setTimeout(() => {
      setState(prev => prev.withFlowValue(value));
    }, 500);
  }

  function onChangeDescription(value: string) {
    setFields(prev => ({ ...prev, assetDescription: value }));
  }

  function onBlurDescription() {
    setState(prev => prev.withDescription(fields.assetDescription));
  }

  function onChangeConfigOptions(value: string[]) {
    setState(prev => prev.withConfigOptions(value as Fs.ConfigOption[]));
  }

  function onChangeLabels(value: string[]) {
    setState(prev => prev.withTagLabels(value));
  }

  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }

  function onCancel() {
    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }
    const original = originalContentRef.current;
    liveContentRef.current = original;
    setFields({ content: original, assetDescription: dirent?.props?.assetDescription ?? '' });
    cancel(props.direntId);
  }

  const isChanged = state.isChanged || fields.assetDescription !== state.assetDescription;

  return {
    isDarkMode,
    dirent,
    id: state.id,
    isChanged,
    isExpanded,
    content: fields.content,
    assetDescription: fields.assetDescription,
    configOptions: state.configOptions,
    tagLabels: state.tagLabels,
    onChangeContent,
    onChangeConfigOptions,
    onChangeLabels,
    onChangeDescription,
    onBlurDescription,
    onToggleExpanded,
    onCancel,
  };
};
