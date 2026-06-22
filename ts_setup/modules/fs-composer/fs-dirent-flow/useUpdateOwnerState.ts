import React from 'react';



import {
  Fs,
  useFsDirent,
  FsuChange,
  useFsuChange,
  useFsDirentBody,
} from '@dxs-ts/fs-api';

export interface UpdateOwnerState {
  dirent: Fs.DirentBase | undefined;
  id: string;
  isDirty: boolean;
  content: string;
  flow: Fs.WrenchAstBody<Fs.FlowAst>
  onChangeContent: (value: string) => void;
}

type _ChangeStateProps = {
  flowId: string;
  bodyType: Fs.BodyType;
  flowValue: string;
  treeId: string;
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() { return this._current.flowId; }
  get treeId() { return this._current.treeId; }
  get bodyType() { return this._current.bodyType; }
  get flowValue() { return this._current.flowValue; }
  get isDirty(): boolean {
    return JSON.stringify(this._origin) !== JSON.stringify(this._current);
  }

  getCurrentProps(): { bodyType: Fs.BodyType; id: string; changes: Record<string, any> } {
    return {
      bodyType: this._current.bodyType,
      id: this.id,
      changes: { ...this._current },
    };
  }
  withFlowValue(flowValue: string): _ChangeState {
    return new _ChangeState({ ...this._current, flowValue }, this._origin);
  }
}


export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { getDirent, applyTransientChanges } = useFsDirent();
  const { body: initBody } = useFsDirentBody();
  const setState = (callback: (prev: _ChangeState) => _ChangeState) => update(callback);
  const dirent = getDirent(props.direntId);

  const { state, update } = useFsuChange(props.direntId, () => new _ChangeState({
    flowId: props.direntId,
    bodyType: dirent?.type!,
    treeId: dirent?.commitIndex?.treeId!,
    flowValue: initBody.flows[props.direntId].ast.parseTree.value,
  }));

  const [flow, setFlow] = React.useState<Fs.WrenchAstBody<Fs.FlowAst>>(() => initBody.flows[props.direntId]);

  function onChangeContent(value: string) {
    onChangeCommands(value);
    setState(prev => prev.withFlowValue(value));
  }

  const onChangeCommands = useDebounce((bodySyntax: string) => {
    applyTransientChanges({
      id: props.direntId,
      bodyType: 'FLOW',
      bodyStatment: [],
      bodySyntax,
    }).then((body) => {
      const wb = body as Fs.WrenchAstBody<Fs.FlowAst>;
      setFlow(wb);
    });
  })

  return {
    dirent,
    flow,
    id: state.id,
    isDirty: state.isDirty,
    content: state.flowValue,
    onChangeContent
  };
};



const delay = 1000;
function useDebounce<T extends (...args: Parameters<T>) => void>(fn: T) {
  const handlerRef = React.useRef<ReturnType<typeof setTimeout>>();

  return React.useCallback((...args: Parameters<T>) => {
    clearTimeout(handlerRef.current);
    handlerRef.current = setTimeout(() => {
      fn(...args);
    }, delay);
  }, [fn]) as T;
}