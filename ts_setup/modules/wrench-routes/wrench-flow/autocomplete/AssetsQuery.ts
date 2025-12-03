import { HdesApi } from '@dxs-ts/wrench-api';


export class AssetsQuery {

  private _decisionsByName: Record<string, HdesApi.Entity<HdesApi.AstDecision>> = {};
  private _servicesByName: Record<string, HdesApi.Entity<HdesApi.AstService>> = {};

  constructor(site: HdesApi.Site) {
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

  findOne(id: string): HdesApi.Entity<HdesApi.AstBody> | undefined {
    const linked: HdesApi.Entity<HdesApi.AstBody> = this._decisionsByName[id];
    if(linked) {
      return linked;
    }
    return this._servicesByName[id];
  }
}