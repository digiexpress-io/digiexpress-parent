import { DialobFormDocument, DialobFormRevisionDocument, DialobFormRevisionEntryDocument } from './dialob-types'
import { HdesAstFlow } from './hdes-types'
import { DefinitionState } from './client-types'
import { StencilTopic, StencilTopicLink } from './stencil-types'


interface DefStateDialobAssocs {
  form: DialobFormDocument;
  rev: DialobFormRevisionDocument;
  entry: DialobFormRevisionEntryDocument;
}

interface DefStateFlowAssocs {
  flow: HdesAstFlow;
}

interface DefStateWorkflowAssocs {
  locales: string[],
  values: WorkflowAssocsValue[]
}

interface WorkflowAssocsValue {
  topic: StencilTopic,
  workflow: StencilTopicLink,
  locale: string,
}

interface DefStateAssocs {
  getDialob(formId: string): DefStateDialobAssocs | undefined;
  getFlow(flowId: string): DefStateFlowAssocs | undefined;
  getWorkflow(workflowName: string): DefStateWorkflowAssocs | undefined;
}

class DefStateAssocsImpl implements DefStateAssocs {
  private _def: DefinitionState;
  private _dialobs_cached: Record<string, DefStateDialobAssocs> = {};
  private _flows_cached: Record<string, DefStateFlowAssocs> = {};
  private _workflows_cached: Record<string, DefStateWorkflowAssocs> = {};


  constructor(init: {
    def: DefinitionState,
  }) {
    this._def = init.def;
    console.log(init);
  }

  getWorkflow(workflowName: string): DefStateWorkflowAssocs | undefined {
    if (this._workflows_cached[workflowName]) {
      return this._workflows_cached[workflowName];
    }
    
    const locales: string[] = [];
    const values: WorkflowAssocsValue[] = Object.values(this._def.stencil.sites)
      .map(({locale, links, topics}) => {
       
       const values = Object.values(links)
          .filter(link => link.value === workflowName)
          .map(workflow => ({ locale, workflow,  topic: topics[workflow.path] }));
          
        if(values.length > 0 && !locales.includes(locale)) {
          locales.push(locale);
        }
        return values;
      }).flat();

    const result: DefStateWorkflowAssocs = { locales, values } 
    this._workflows_cached[workflowName] = result;
    return result;
  }

  getFlow(flowId: string): DefStateFlowAssocs | undefined {
    if (this._flows_cached[flowId]) {
      return this._flows_cached[flowId];
    }

    const flow = this._def.hdes.flows[flowId];
    if (!flow) {
      console.error(flowId)
      return undefined;
    }

    const result: DefStateFlowAssocs = { flow }
    this._flows_cached[flowId] = result;
    return result;
  }
  getDialob(formId: string): DefStateDialobAssocs | undefined {
    if (this._dialobs_cached[formId]) {
      return this._dialobs_cached[formId];
    }

    const form = this._def.dialob.forms[formId];
    if (!form) {
      return undefined;
    }

    let foundRev: DialobFormRevisionDocument | undefined = undefined;
    let foundRevEntry: DialobFormRevisionEntryDocument | undefined = undefined;
    for (const revision of Object.values(this._def.dialob.revs)) {
      const found = revision.entries.find(entry => entry.formId === formId);
      if (found) {
        foundRev = revision;
        foundRevEntry = found;
        break;
      }
    }

    if (!foundRev || !foundRevEntry) {
      return undefined;
    }
    const result: DefStateDialobAssocs = { form, rev: foundRev, entry: foundRevEntry }
    this._dialobs_cached[formId] = result;
    return result;
  }
}


export type { DefStateDialobAssocs, DefStateAssocs, WorkflowAssocsValue, DefStateFlowAssocs };
export { DefStateAssocsImpl };



