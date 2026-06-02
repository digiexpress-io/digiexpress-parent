import React from 'react';
import { Fs, useFsDirent, useFsu, FsuChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';

type _ChangeStateProps = {
  pageId: string;
  bodyType: Fs.BodyType;
  content: string;
  description: string;
  labels: string[];
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() {
    return this._current.pageId;
  }
  get bodyType() {
    return this._current.bodyType;
  }
  get content() {
    return this._current.content;
  }
  get description() {
    return this._current.description;
  }
  get labels() {
    return this._current.labels;
  }
  get isChanged(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    const c = this._current;
    return {
      bodyType: c.bodyType,
      id: c.pageId,
      changes: {
        pageId: c.pageId,
        content: c.content || undefined,
        description: c.description || undefined,
        labels: c.labels.length ? c.labels : undefined,
      },
    };
  }

  withContent(content: string): _ChangeState {
    return new _ChangeState({ ...this._current, content }, this._origin);
  }
  withDescription(description: string): _ChangeState {
    return new _ChangeState({ ...this._current, description }, this._origin);
  }
  withLabels(labels: string[]): _ChangeState {
    return new _ChangeState({ ...this._current, labels }, this._origin);
  }
}

interface _TextFields {
  content: string;
  description: string;
}

export interface UpdateOwnerState {
  isDarkMode: boolean;
  isChanged: boolean;
  isExpanded: boolean;
  content: string;
  description: string;
  labels: string[];
  labelOptions: string[];
  onChangeContent: (v: string) => void;
  onBlurContent: () => void;
  onChangeDescription: (v: string) => void;
  onBlurDescription: () => void;
  onChangeLabels: (value: string[]) => void;
  onToggleExpanded: () => void;
  onCancel: () => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent, selectOptions } = useFsDirent();
  const { withNewChange, withChange, cancel } = useFsu();

  const dirent = getDirent(props.direntId)!;
  const pageProps = dirent.props as Fs.PrintoutPageProps;

  const state = withNewChange(props.direntId, () => new _ChangeState({
    pageId: props.direntId,
    bodyType: dirent.type,
    content: pageProps.content ?? '',
    description: pageProps.description ?? '',
    labels: (pageProps.labels ?? []).map(l => l.value),
  }));

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => withChange(props.direntId, callback);

  const [isExpanded, setIsExpanded] = React.useState(false);
  const [fields, setFields] = React.useState<_TextFields>({
    content: pageProps.content ?? '',
    description: pageProps.description ?? '',
  });

  const isChangesPresent = state.isChanged
    || fields.content !== state.content
    || fields.description !== state.description;

  function onChangeContent(v: string) {
    setFields(prev => ({ ...prev, content: v }));
  }
  function onBlurContent() {
    setState(prev => prev.withContent(fields.content));
  }
  function onChangeDescription(v: string) {
    setFields(prev => ({ ...prev, description: v }));
  }
  function onBlurDescription() {
    setState(prev => prev.withDescription(fields.description));
  }
  function onChangeLabels(value: string[]) {
    setState(prev => prev.withLabels(value));
  }
  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }
  function onCancel() {
    setFields({
      content: pageProps.content ?? '',
      description: pageProps.description ?? '',
    });
    setIsExpanded(false);
    cancel(props.direntId);
  }

  return {
    isDarkMode,
    isChanged: isChangesPresent,
    isExpanded,
    content: fields.content,
    description: fields.description,
    labels: state.labels,
    labelOptions: selectOptions.labels,
    onChangeContent,
    onBlurContent,
    onChangeDescription,
    onBlurDescription,
    onChangeLabels,
    onToggleExpanded,
    onCancel,
  };
};
