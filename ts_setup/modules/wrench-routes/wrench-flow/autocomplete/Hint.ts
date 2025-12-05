import { HdesApi } from '@dxs-ts/wrench-api';
import { Position, editor, languages, IRange } from 'monaco-editor';
import { AssetsQuery } from './AssetsQuery';
import { AstNav, AstNavNodeDesc } from './AstNav';

export interface Container {
  flow: HdesApi.AstFlow;
  site: HdesApi.Site;
  model: editor.ITextModel;
  modelPosition: Position;
  assetsQuery: AssetsQuery;
  nav: AstNav;
  navDesc: AstNavNodeDesc;
}

export interface Hint {
  accept(container: Container): languages.CompletionItem[];
}

export const EXTERNAL_DIALOG = 'EXTERNAL_DIALOG';

export interface CompletionDialogProps {
  id: string
  value: string;
  append: boolean;
  position: Position;
  range: IRange;
  guided?: GuidedType;
}

export type GuidedType = 'service-task' | 'decision-task';

export const FIELD = ":";
export const VALUE_NEXT = "next";
export const VALUE_END = "end";

export const TYPES: HdesApi.ValueType[] = [
  'ARRAY',
  'TIME', 'DATE', 'DATE_TIME',
  'STRING',
  'INTEGER', 'LONG', 'DECIMAL',
  'BOOLEAN'
];

export type NodeKeywordTypes = 
  | "id"
  | "description"
  | "inputs"
  | "tasks"
  | "then"
  | "when"
  | "switch"
  | "required"
  | "type"
  | "decisionTable"
  | "userTask"
  | "ref"
  | "collection"
  | "service"
  | "debugValue";