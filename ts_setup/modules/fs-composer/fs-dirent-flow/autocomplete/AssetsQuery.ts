import { Fs } from '@dxs-ts/fs-api';


export class AssetsQuery {

  private _decisionsByName: Record<string, Fs.WrenchAstBody<Fs.DecisionAst>> = {};
  private _servicesByName: Record<string, Fs.WrenchAstBody<Fs.FlowTaskAst>> = {};

  constructor(site: Fs.WrenchBody) {
    Object.values(site.decisions).forEach(d => {
      if (d.ast) {
        this._decisionsByName[d.ast?.name] = d;
      }
    });
    Object.values(site.services).forEach(d => {
      if (d.ast) {
        this._servicesByName[d.ast?.name] = d;
      }
    });
  }

  findOne(id: string): Fs.WrenchAstBody<Fs.DecisionAst | Fs.FlowTaskAst> | undefined {
    const linked: Fs.WrenchAstBody<Fs.DecisionAst> = this._decisionsByName[id];
    if(linked) {
      return linked;
    }
    return this._servicesByName[id];
  }
}