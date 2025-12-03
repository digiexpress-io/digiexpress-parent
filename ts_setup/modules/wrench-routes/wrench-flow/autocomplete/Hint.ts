import { HdesApi } from '@dxs-ts/wrench-api';
import { Position, editor, languages } from 'monaco-editor';
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