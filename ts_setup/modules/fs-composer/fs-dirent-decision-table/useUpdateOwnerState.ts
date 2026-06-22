import React from 'react';

import {
  Fs,
  useFsDirent,
  FsuChange,
  useFsuChange,
  useFsDirentBody
} from '@dxs-ts/fs-api';

export interface UpdateOwnerState {
  dirent: Fs.DirentBase;
  id: string;
  isDirty: boolean;
  wrenchBody: Fs.WrenchBody;
  decision: Fs.DecisionAst;
  onChangeCommands: (commands: Fs.AstCommand[]) => void;

  onDragStart: (index: number) => any,
  onDragOver: (index: number) => any,
  onDrop: (index: number) => any,

  confirmDelete: { type: 'ROW' | 'COLUMN', id: string } | null;
  setConfirmDelete: (value: UpdateOwnerState['confirmDelete']) => void;
  editMode: {
    cell?: Fs.DecisionAstCell;
    header?: Fs.TypeDef;
    meta?: boolean;
    upload?: boolean;
    download?: boolean;
    rowsColumns?: boolean;
  } | undefined
  setEditMode: (value: UpdateOwnerState['editMode']) => void;
}

type _ChangeStateProps = {
  decisionTableId: string;
  bodyType: Fs.BodyType;
  nodes: Fs.AstCommand[];
  treeId: string;
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
  get treeId() { return this._current.treeId; }
  get nodes() { return this._current.nodes; }
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
  const { getDirent, applyTransientChanges } = useFsDirent();
  const dirent = getDirent(props.direntId)!;
  const { body: initBody } = useFsDirentBody();
  const [editMode, setEditMode] = React.useState<UpdateOwnerState['editMode']>();
  const [draggedIndex, setDraggedIndex] = React.useState<number | null>(null);
  const [confirmDelete, setConfirmDelete] = React.useState<UpdateOwnerState['confirmDelete']>(null);
  const [decision, setDecision] = React.useState<Fs.DecisionAst>(() => initBody.decisions[props.direntId]?.ast);

  const setState = (callback: (prev: _ChangeState) => _ChangeState) => update(callback)

  const { state, update } = useFsuChange(props.direntId, () => new _ChangeState({
    decisionTableId: props.direntId,
    bodyType: dirent!.type,
    treeId: dirent?.commitIndex?.treeId!,
    nodes: initBody.decisions[props.direntId]?.commands!,
  }));


  const onChangeCommands = (newCommands: Fs.AstCommand[]) => {
    const allCommands = [...state.nodes, ...newCommands];
    applyTransientChanges({
      id: props.direntId,
      bodyType: 'DECISION_TABLE',
      bodyStatment: allCommands,
    }).then((body) => {
      const wb = body as Fs.WrenchAstBody<Fs.DecisionAst>;
      setState(prev => prev.withNodes(allCommands));
      setDecision(wb.ast);
    });
  }

  const onDragStart = (index: number) => () => {
    setDraggedIndex(index);
  }

  const onDragOver = (_index: number) => (e: React.DragEvent) => {
    e.preventDefault();
  }

  const onDrop = (targetIndex: number) => () => {
    if (draggedIndex === null || draggedIndex === targetIndex) return;

    const sourceRow = decision!.rows[draggedIndex];
    const targetRow = decision!.rows[targetIndex];

    onChangeCommands([
      {
        type: 'MOVE_ROW' as const,
        id: sourceRow.id,
        value: targetRow.id,
      }
    ]);
    setDraggedIndex(null);
  }

  return {
    dirent,
    id: state.id,
    isDirty: state.isDirty,
    wrenchBody: initBody,
    decision,
    confirmDelete,
    setConfirmDelete,
    onChangeCommands,
    onDragStart,
    onDragOver,
    onDrop,
    editMode, setEditMode
  };
};
