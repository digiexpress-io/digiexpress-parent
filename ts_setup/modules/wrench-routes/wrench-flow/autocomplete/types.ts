
import { HdesApi } from '@dxs-ts/wrench-api';
import { Position, IRange } from 'monaco-editor';


export const EXTERNAL_DIALOG = 'EXTERNAL_DIALOG';


export interface FlowAstAutocomplete {
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