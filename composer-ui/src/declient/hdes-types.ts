import {AstFlow, AstService, AstDecision, AstBody} from './hdes-ast-types';

export interface HdesTree {
  flows: Record<string, HdesAstFlow>;
  services: Map<string, HdesAstService>;
  decisions: Map<string, HdesAstDecision>;
}


export interface HdesEntity<B extends AstBody> {
  id: string;
  ast: B;
}


export interface HdesBodyEntity extends AstBody {
  id: string
}

export class HdesBodyEntityImpl implements HdesBodyEntity {
  private _delegate: HdesEntity<AstBody>;
  constructor(delegate: HdesEntity<AstBody>) {
    this._delegate = delegate;
  }
  get id() { return this._delegate.id }
  get name() { return this._delegate.ast.name }
  get description() { return this._delegate.ast.description }
  get headers() { return this._delegate.ast.headers };
  get bodyType() { return this._delegate.ast.bodyType };
}



export interface HdesAstFlow extends HdesEntity<AstFlow> {}
export interface HdesAstService extends HdesEntity<AstService> {}
export interface HdesAstDecision extends HdesEntity<AstDecision> {}
