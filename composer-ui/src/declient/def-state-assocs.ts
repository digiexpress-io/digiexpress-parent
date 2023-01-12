import { DialobFormDocument, DialobFormRevisionDocument, DialobFormRevisionEntryDocument } from './dialob-types'
import { DefinitionState } from './client-types'


interface DefStateDialobAssocs {
  form: DialobFormDocument;
  rev: DialobFormRevisionDocument;
  entry: DialobFormRevisionEntryDocument;
}

interface DefStateAssocs {
  getDialob(formId: string): DefStateDialobAssocs | undefined;
}

class DefStateAssocsImpl implements DefStateAssocs {
  private _def: DefinitionState;
  private _dialobs_cached: Record<string, DefStateDialobAssocs> = {};

  constructor(init: {
    def: DefinitionState,
  }) {
    this._def = init.def;
  }

  getDialob(formId: string): DefStateDialobAssocs | undefined {
    if(this._dialobs_cached[formId]) {
      return this._dialobs_cached[formId];
    }
    
    const form = this._def.dialob.forms[formId];
    if(!form) {
      return undefined;      
    }
    
    let foundRev: DialobFormRevisionDocument | undefined = undefined;
    let foundRevEntry: DialobFormRevisionEntryDocument | undefined = undefined;
    for(const revision of Object.values(this._def.dialob.revs)) {
      const found = revision.entries.find(entry => entry.formId === formId);
      if(found) {
        foundRev = revision; 
        foundRevEntry = found; 
        break;
      }
    }
    
    if(!foundRev || !foundRevEntry) {
      return undefined;      
    }
    const result: DefStateDialobAssocs = { form, rev: foundRev, entry: foundRevEntry }
    this._dialobs_cached[formId] = result;
    return result;
  }
}


export type { DefStateDialobAssocs, DefStateAssocs };
export { DefStateAssocsImpl };



