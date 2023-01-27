import {AstFlow, AstService, AstDecision} from './hdes-ast-types';

export interface HdesTree {
  flows: Record<string, HdesAstFlow>;
  services: Map<string, HdesAstService>;
  decisions: Map<string, HdesAstDecision>;
}

export interface HdesAstFlow {
  id: string;
  ast: AstFlow;
}
export interface HdesAstService {
  id: string;
  ast: AstService;
}
export interface HdesAstDecision {
  id: string;
  ast: AstDecision;
}

