import React from 'react';
import { useFsTheme } from '../fs-theme';
import {
  Fs,
  useFsDirent,
  useFsu,
  FsuChange,
  useFsuChange
} from '@dxs-ts/fs-api';

export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  id: string;
  isDirty: boolean;
  wrenchBody: Fs.WrenchBody | undefined;
  decision: Fs.DecisionAst | undefined;
  onChangeCommands: (commands: Fs.AstCommand[]) => void;
}

type _ChangeStateProps = {
  decisionTableId: string;
  bodyType: Fs.BodyType;
  nodes: Fs.AstCommand[];
}

class _ChangeState implements FsuChange {
  private _origin: _ChangeStateProps;
  private _current: _ChangeStateProps;

  constructor(props: _ChangeStateProps, origin?: _ChangeStateProps) {
    this._current = props;
    this._origin = origin ?? props;
  }

  get id() { return this._current.decisionTableId; }
  get bodyType() { return this._current.bodyType; }

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

  withNodes(nodes: Fs.AstCommand[]): _ChangeState {
    return new _ChangeState({ ...this._current, nodes }, this._origin);
  }

}


export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent, fetchDirentBody, applyTransientChanges } = useFsDirent();
  const { withNewChange, withChange, cancel, push } = useFsu();

  const dirent = getDirent(props.direntId);

  const [wrenchBody, setWrenchBody] = React.useState<Fs.WrenchBody | undefined>(undefined);
  const [decision, setDecision] = React.useState<Fs.DecisionAst | undefined>(undefined);
  const [commands, setCommands] = React.useState<Fs.AstCommand[]>([]);
  const [initialCommands, setInitialCommands] = React.useState<Fs.AstCommand[]>([]);
  const [initialDecision, setInitialDecision] = React.useState<Fs.DecisionAst | undefined>(undefined);

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => update(callback)

  const { state, update } = useFsuChange(props.direntId, () => new _ChangeState({
    decisionTableId: props.direntId,
    bodyType: dirent!.type,
    nodes: commands,
  }));

  React.useEffect(() => {
    fetchDirentBody(props.direntId, 'DECISION_TABLE')
      .then((body) => {
        const wb = body as Fs.WrenchBody;
        const loaded = wb.decisions[props.direntId]?.commands ?? [];
        const loadedDecision = wb.decisions[props.direntId]?.ast;
        setWrenchBody(wb);
        setDecision(loadedDecision);
        setCommands(loaded);
        setInitialCommands(loaded);
        setInitialDecision(loadedDecision);
        cancel(props.direntId);
      });
  }, [props.direntId]);

  const onChangeCommands = React.useCallback((newCommands: Fs.AstCommand[]) => {
    const allCommands = [...commands, ...newCommands];
    setCommands(allCommands);
    setState(prev => prev.withNodes(allCommands));

    applyTransientChanges({
      id: props.direntId,
      bodyType: 'DECISION_TABLE',
      bodyStatment: allCommands,
    }).then((body) => {
      const wb = body as Fs.WrenchAstBody<Fs.DecisionAst>;
      setDecision(wb.ast);
      push(props.direntId);
    });
  }, [commands, props.direntId, applyTransientChanges, withChange, push]);

  return {
    isDarkMode,
    dirent,
    id: state.id,
    isDirty: state.isDirty,
    wrenchBody,
    decision,
    onChangeCommands,
  };
};
