import { Node, Edge, Model } from '../../wrench-vis';
import { HdesApi } from '@dxs-ts/wrench-api';


type ModelType = 'switch' | 'decisionTable' | 'service' | 'flow' | 'returns' | 'form';


class ModelVisitor {
  private _fl: HdesApi.AstFlow;
  private _nested: boolean;
  private _visited: string[] = [];
  private _nodes: Node[] = [];
  private _edges: Edge[] = [];
  private _models: HdesApi.Site;


  constructor(fl: HdesApi.AstFlow, models: HdesApi.Site, nested?: { visited: string[]}) {
    this._fl = fl;
    this._models = models;
    this._nested = nested ? true : false;
    if(nested) {
      this._visited.push(...nested.visited);
    }
  }

  visit(): Model {
    const steps = Object.values(this._fl.parseTree.tasks);    
    
    const start: Node = {
      id: 'start', type: 'start', label: 'start', parents: []
    };

    if(!this._nested) {
      this._nodes.push(start);
    }
    
    const first = steps.filter(step => step.order === 0);
    if(first.length === 1) {
      this.visitStep(first[0], {parent: start});
    }

    if (steps.length === 0 && !this._nested) {
      this._edges.push({ from: 'start', to: 'end' });
      this._nodes.push({
        id: 'end', label: 'end',
        type: 'end',
        parents: []
      })
    }

    return { nodes: this._nodes, edges: this._edges, visited: this._visited };
  }

  visitEdge(step: HdesApi.AstFlowTaskNode, props: { parent: Node, index?: number }) {
    const id = this._fl.name + "/" + (step.id?.value ?? props.index);
    const parent = props.parent;

    if (parent) {
      // child to parent
      const parentId = parent.id;
      const refId = parentId + '->' + id
      if (this._visited.includes(refId)) {
        return
      }
      
      this._visited.push(refId)
      this._edges.push({ from: parentId, to: id})
    } else {
      // first entry
      //this._edges.push({ from: 'start', to: id})
      throw new Error("no parent");
    }
  }

  visitStep(step: HdesApi.AstFlowTaskNode, props: { parent: Node, index?: number }) {
    const id = this._fl.name + "/" + (step.id?.value ?? props.index);
    const parent = props.parent;

    this.visitEdge(step, props);

    if (this._visited.includes(id)) {
      return this.visitCyclicDependency(step, parent);
    }

    const parents: string[] = [];
    if (parent) {
      parents.push(...parent.parents);
      parents.push(parent.id);
    }
    const ref = this.visitRef(step);
    const group = this.visitType(step);
    const node: Node = {
      id: id,
      parents: parents,
      externalId: ref?.id,
      label: step.keyword,
      type: group,
      body: { step, ref }
    }

    this._nodes.push(node)
    this._visited.push(id);

    if (group === "switch") {
      this.visitSwitch(step, { parent: node, index: props.index });
    } else if (group === "decisionTable" || group === "returns" || group === "form") {
      this.visitThen(step.then, { parent: node, index: props.index });
    } else if (group === "service") {
      if(ref) {
        this.visitServiceAssoc(ref, { parent: node, index: props.index });
      }
      this.visitThen(step.then, { parent: node, index: props.index });
    }
  }
  
  visitServiceAssoc(entity: HdesApi.Entity<HdesApi.AstService>, props: { parent: Node, index?: number }) {
    if(!entity.ast) {
      return;
    }  
    
    const {parent} = props;
    const allAssocs = entity.associations.filter(assoc => assoc.owner);

    // in case multiple refs are defined, but only one is used at a time, filter it out
    const refName: string = parent.body?.step?.children?.service?.inputs?.refName?.value;
    const usedAssoc = allAssocs.filter(assoc => assoc.ref === refName);
    
    const assocs = refName ? usedAssoc : allAssocs;
    
    for (let caseInTask of assocs) {
      
      const ref = this.findRef(caseInTask.ref, caseInTask.refType);
      const parents: string[] = [];
      parents.push(...parent.parents);
      parents.push(parent.id);
      const group: ModelType = caseInTask.refType === "FLOW" ? "flow" : 'decisionTable';
      const id = caseInTask.ref + "/" + parent.id  + "/" + (caseInTask.id ? caseInTask.id : caseInTask.ref);
      const node: Node = {
        id: id,
        parents: parents,
        externalId: caseInTask.id,
        label: "::" + caseInTask.ref,
        type: group,
        body: { ref },
      }
      
      this._edges.push({ from: props.parent.id, to: node.id})
      this._nodes.push(node)
      
      const ast: HdesApi.AstBody | undefined = ref?.ast;
      
      if(ast && ast.bodyType === "FLOW") {
        const flow: HdesApi.AstFlow  = ast as any;
        if(this._visited.includes(flow.name)) {
          continue;
        }
        this._visited.push(flow.name);
        const nested = new ModelVisitor(flow, this._models, {visited: this._visited}).visit();
        this._edges.push(...nested.edges)
        this._nodes.push(...nested.nodes)
        this._visited.push(...nested.visited);
      }
    }
  }
  
  visitEnd(props: { parent: Node, index?: number }) {
    const id = 'end-' + props.parent.id + (props.index ? props.index : '');
    const parentId = props.parent.id;
    const refId = parentId + '->' + id
    if (this._visited.includes(refId)) {
      return
    }
    
    this._nodes.push({
        id, label: 'end',
        type: 'end',
        parents: [...props.parent.parents, props.parent.id]
    });
    this._visited.push(refId)
    this._edges.push({ from: parentId, to: id})
  }
  
  visitThen(then: HdesApi.AstFlowNode, props: { parent: Node, index?: number }) {
    if (!then.value) {
      return;
    }
    
    if(then.value === 'end') {
      return this.visitEnd(props);
    }

    const next = Object.values(this._fl.parseTree.tasks).filter(step => step.id?.value === then.value);
    if (!next.length) {
      return;
    }

    const step: HdesApi.AstFlowTaskNode = next[0];
    return this.visitStep(step, props);
  }

  visitSwitch(step: HdesApi.AstFlowTaskNode, props: { parent: Node, index?: number }) {
    if (!step.switch) {
      return;
    }

    const cases = Object.values(step.switch);
    let index = 0
    let evenX = 0
    let oddX = 0
    for (let caseInTask of cases) {
      let caseX
      if (index === 0) {
        caseX = 0;
      } else if (index % 2 === 0) {
        // even
        caseX = evenX;
      } else {
        // odd
        caseX = oddX * -1;
      }
      index++;
      this.visitThen(caseInTask.then, {
        parent: props.parent,
        index: props.index ? props.index : 0 + caseX
      });
    }
  }

  visitType(step: HdesApi.AstFlowTaskNode): ModelType {
    if (step.decisionTable) {
      return "decisionTable";
    } else if (step.service) {
      return "service";
    } else if (step.returns) {
        return "returns";
    } else if ((step as any).form) {
      return "form";
    } else if (step.switch && Object.keys(step.switch).length > 0) {
      return "switch";
    }
    return "service";
  }

  visitRef(step: HdesApi.AstFlowTaskNode): HdesApi.Entity<any> | undefined {

    const ref = step.ref?.ref?.value;
    if (!ref) {
      return undefined;
    }
    if (step.decisionTable) {
      return this.findRef(ref, "DECISION_TABLE");
    } else if (step.service) {
      return this.findRef(ref, "FLOW_TASK");
    }
    return undefined;
  }
  
  findRef(name: string, type: HdesApi.AstBodyType): HdesApi.Entity<any> | undefined {
    const models: HdesApi.Entity<any>[] = [];
    if (type === "DECISION_TABLE") {
      models.push(...Object.values(this._models.decisions));
    } else if (type === "FLOW_TASK") {
      models.push(...Object.values(this._models.services));
    } else if (type === "FLOW") {
      models.push(...Object.values(this._models.flows));
    }
    const result = models.filter(m => m.ast && m.ast.name === name)
    if (result.length === 0) {
      return undefined;
    }
    return result[0];
  }


  visitCyclicDependency(step: HdesApi.AstFlowTaskNode, parent?: Node) {
    const id = this._fl.name + "/" + step.id.value;
    const parents: string[] = [];
    if (parent) {
      parents.push(...parent.parents);
      parents.push(parent.id);
    }
    const node = this._nodes.filter(n => n.id === id)[0];
    node.parents.push(...parents);
  }
}


namespace GraphAPI {
  export const create = (props: {
    fl: HdesApi.AstFlow,
    models: HdesApi.Site
  }) => {

    try {
      return new ModelVisitor(props.fl, props.models).visit();
    } catch(error) {
      console.error(error)
    }
  };
}


export default GraphAPI;