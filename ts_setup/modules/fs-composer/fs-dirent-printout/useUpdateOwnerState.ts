import { Fs, useFsDirent, FsuChange, useFsuChange } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { useFsNav } from '@dxs-ts/fs-nav';

export interface ConnectedPage {
  id: string;
  localeName: string;
}

type _ChangeStateProps = {
  printoutId: string;
  bodyType: Fs.BodyType;
  serviceName: string;
  orchestratorName: string;
  intlValues: Record<string, string>;
}

export interface UpdateOwnerState {
  isDarkMode: boolean;
  assetPath: string | undefined;
  isDirty: boolean;
  serviceName: string;
  orchestratorName: string;
  flows: Fs.SelectOption[];
  connectedPages: ConnectedPage[];
  onChangeServiceName: (value: string) => void;
  onChangeOrchestratorName: (value: string) => void;
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() {
    return this._current.printoutId;
  }
  get bodyType() {
    return this._current.bodyType;
  }
  get serviceName() {
    return this._current.serviceName;
  }
  get orchestratorName() {
    return this._current.orchestratorName;
  }
  get intlValues() {
    return this._current.intlValues;
  }
  get isDirty(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    const c = this._current;
    return {
      bodyType: c.bodyType,
      id: c.printoutId,
      changes: {
        serviceId: c.printoutId,
        serviceName: c.serviceName || undefined,
        orchestratorName: c.orchestratorName || undefined,
        localeLabels: Object.entries(c.intlValues).map(([locale, labelValue]) => ({ locale, labelValue })),
      },
    };
  }

  withServiceName(serviceName: string): _ChangeState {
    return new _ChangeState({ ...this._current, serviceName }, this._origin);
  }
  withOrchestratorName(orchestratorName: string): _ChangeState {
    return new _ChangeState({ ...this._current, orchestratorName }, this._origin);
  }
  withIntlValues(locale: string, labelValue: string): _ChangeState {
    return new _ChangeState({
      ...this._current,
      intlValues: { ...this._current.intlValues, [locale]: labelValue },
    }, this._origin);
  }
}


export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { activeTabPath } = useFsNav();
  const { getDirent, selectOptions } = useFsDirent();


  const dirent = getDirent(props.direntId)!;
  const printoutProps = dirent.props as Fs.PrintoutProps;

  const { state, update } = useFsuChange(props.direntId, () => new _ChangeState({
    printoutId: props.direntId,
    bodyType: dirent.type,
    serviceName: printoutProps.printoutServiceName ?? dirent.name ?? '',
    orchestratorName: printoutProps.orchestratorName ?? '',
    intlValues: printoutProps.intlValues ?? {},
  }));

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => update(callback);

  const connectedPages: ConnectedPage[] = Object.values(selectOptions.direntProps)
    .filter(p => p.type === 'PRINTOUT_PAGE' && (p as Fs.PrintoutPageProps).serviceId === props.direntId)
    .map(p => {
      const pageProps = p as Fs.PrintoutPageProps;
      const localeName = selectOptions.languages.find(l => l.value === pageProps.localeId)?.label ?? pageProps.localeId;
      return { id: p.id, localeName };
    });

  function onChangeServiceName(value: string) {
    setState(prev => prev.withServiceName(value));
  }
  function onChangeOrchestratorName(value: string) {
    setState(prev => prev.withOrchestratorName(value));
  }
  return {
    isDarkMode,
    assetPath: activeTabPath,
    isDirty: state.isDirty,
    serviceName: state.serviceName,
    orchestratorName: state.orchestratorName,
    flows: selectOptions.flows,
    connectedPages,
    onChangeServiceName,
    onChangeOrchestratorName,
  };
};
