import Vis from '../../../vis';
import { HdesApi } from '../../client';


type ModelType = 'switch' | 'decisionTable' | 'service' | 'flow' | undefined;

class ModelVisitor {
  private _fl: HdesApi.AstFlow;
  private _nested: boolean;
  private _visited: string[] = [];
  private _nodes: Vis.Node[] = [];
  private _edges: Vis.Edge[] = [];
  private _models: HdesApi.Site;
  private _start: { x: number, y: number };
  private _constraints: {
    width: { maximum: number, minimum: number }
  };
  private _seperation: {
    level: number;
    switch: number;
  };

  constructor(fl: HdesApi.AstFlow, models: HdesApi.Site, nested?: {x: number, y: number, visited: string[]}) {
    this._fl = fl;
    this._models = models;
    this._nested = nested ? true : false;
    this._constraints = {
      width: { maximum: 150, minimum: 150 }
    }
    this._seperation = {
      level: 100,
      switch: 80 + this._constraints.width.minimum
    };
    this._start = {
      x: nested ? nested.x : -500, 
      y: nested ? nested.y : -500
    };
    
    if(nested) {
      this._visited.push(...nested.visited);
    }
  }

  visit(): Vis.Model {
    const steps = Object.values(this._fl.src.tasks);    
    
    const start: Vis.Node = {id: 'start', label: 'start', shape: 'circle', x: 0, y: this._start.y, parents: []};
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
        id: 'end', label: 'end', shape: 'circle',
        parents: [],
        x: 0,
        y: this._start.y + this._seperation.level
      })
    }

    return { nodes: this._nodes, edges: this._edges, visited: this._visited };
  }

  visitEdge(step: HdesApi.AstFlowTaskNode, props: { parent: Vis.Node, index?: number }) {
    const id = this._fl.name + "/" + step.id.value;
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

  visitStep(step: HdesApi.AstFlowTaskNode, props: { parent: Vis.Node, index?: number }) {
    const id = this._fl.name + "/" + step.id.value;
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
    const { x, y } = this.visitCoords(step, props);

    const node: Vis.Node = {
      id: id,
      parents: parents,
      externalId: ref?.id,
      label: step.keyword,
      group: group,
      color: this.visitColor(group),
      shape: this.visitShape(group),
      x: x, y: y,
      body: { step, ref },
      widthConstraint: this._constraints.width
    }

    this._nodes.push(node)
    this._visited.push(id);

    if (group === "switch") {
      this.visitSwitch(step, { parent: node, index: props.index });
    } else if (group === "decisionTable") {
      this.visitThen(step.then, { parent: node, index: props.index });
    } else if (group === "service") {
      if(ref) {
        this.visitServiceAssoc(ref, { parent: node, index: props.index });
      }
      this.visitThen(step.then, { parent: node, index: props.index });
    }

    this.visitOverlapping(node);
  }
  
  visitServiceAssoc(entity: HdesApi.Entity<HdesApi.AstService>, props: { parent: Vis.Node, index?: number }) {
    if(!entity.ast) {
      return;
    }  
    
    const {parent} = props;
    const allAssocs = entity.associations.filter(assoc => assoc.owner);

    // in case multiple refs are defined, but only one is used at a time, filter it out
    const refName: string = parent.body?.step?.children?.service?.inputs?.refName?.value;
    const usedAssoc = allAssocs.filter(assoc => assoc.ref === refName);
    
    const assocs = refName ? usedAssoc : allAssocs;
    
    let index = 0
    let evenX = 0
    let oddX = 0
    for (let caseInTask of assocs) {
      let caseX
      if (index === 0) {
        caseX = 0;
      } else if (index % 2 === 0) {
        // even
        evenX += this._seperation.switch;
        caseX = evenX;
      } else {
        // odd
        oddX += this._seperation.switch;
        caseX = oddX * -1;
      }
      index++;
      
      const ref = this.findRef(caseInTask.ref, caseInTask.refType);
      const parents: string[] = [];
      parents.push(...parent.parents);
      parents.push(parent.id);
      const group: ModelType = caseInTask.refType === "FLOW" ? "flow" : 'decisionTable';
      const id = caseInTask.ref + "/" + parent.id  + "/" + (caseInTask.id ? caseInTask.id : caseInTask.ref);
      const offsetX = assocs.length === 1 ? (150 + id.length) : 100;
      const offsetY = assocs.length === 1 ? 0 : 50;
      const node: Vis.Node = {
        id: id,
        parents: parents,
        externalId: caseInTask.id,
        label: "::" + caseInTask.ref,
        group: group,
        color: this.visitColor(group),
        shape: this.visitShape(group),
        x: caseX + offsetX, y: parent.y + offsetY,
        body: { ref },
        widthConstraint: this._constraints.width
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
        const nested = new ModelVisitor(flow, this._models, {x: node.x, y: node.y, visited: this._visited}).visit();
        this._edges.push(...nested.edges)
        this._nodes.push(...nested.nodes)
        this._visited.push(...nested.visited);
        
      }
    }
  }
  
  visitEnd(props: { parent: Vis.Node, index?: number }) {
    const id = 'end-' + props.parent.id + (props.index ? props.index : '');
    const parentId = props.parent.id;
    const refId = parentId + '->' + id
    if (this._visited.includes(refId)) {
      return
    }
    
    this._nodes.push({
        id, label: 'end', shape: 'circle', 
        x: props.parent.x, 
        y: props.parent.y + this._seperation.level,
        parents: [...props.parent.parents, props.parent.id]
    });
    this._visited.push(refId)
    this._edges.push({ from: parentId, to: id})
  }
  
  visitThen(then: HdesApi.AstFlowNode, props: { parent: Vis.Node, index?: number }) {
    if (!then.value) {
      return;
    }
    
    if(then.value === 'end') {
      return this.visitEnd(props);
    }

    const next = Object.values(this._fl.src.tasks).filter(step => step.id?.value === then.value);
    if (!next.length) {
      return;
    }

    const step: HdesApi.AstFlowTaskNode = next[0];
    return this.visitStep(step, props);
  }

  visitSwitch(step: HdesApi.AstFlowTaskNode, props: { parent: Vis.Node, index?: number }) {
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
        evenX += this._seperation.switch;
        caseX = evenX;
      } else {
        // odd
        oddX += this._seperation.switch;
        caseX = oddX * -1;
      }
      index++;
      this.visitThen(caseInTask.then, {
        parent: props.parent,
        index: props.index ? props.index : 0 + caseX
      });
    }
  }

  visitOverlapping(node: Vis.Node) {
    let overlapping = this.findPos(node);
    if (overlapping) {
      //push everything down
      for (const overlap of this._nodes) {
        if (!overlap.parents || overlap.id === node.id) {
          continue;
        }
        if (overlap.parents.includes(overlapping.id) || overlapping.id === overlap.id) {
          overlap.y = overlap.y + this._seperation.level;
        }
      }
    }
  }

  findPos(props: { x: number, y: number, id: string }) {
    for (const v of this._nodes) {
      if (v.id === props.id) {
        continue;
      }
      if (v.x === props.x && v.y === props.y) {
        return v;
      }
    }
    return undefined;
  }

  visitCoords(
    _step: HdesApi.AstFlowTaskNode,
    props: {
      parent?: Vis.Node,
      index?: number
    }): { x: number, y: number } {

    const parent = props.parent;
    const switchIndex = props.index;

    const parentY = parent ? parent.y : this._start.y;
    const parentOffset = parent ? parent.x : 0;
    const y = parentY + this._seperation.level
    const x = (switchIndex ? switchIndex : 0) + parentOffset;

    return { x, y };
  }

  visitShape(type: ModelType): 'diamond' | 'box' {
    if (type === "switch") {
      return 'diamond';
    }
    return 'box' as any;
  }


  visitColor(type: ModelType): string {
    if (type === "service") {
      return '#eaa9ff';
    }
    return '#cce2ff';
  }

  visitType(step: HdesApi.AstFlowTaskNode): ModelType {
    if (step.decisionTable) {
      return "decisionTable";
    } else if (step.service) {
      return "service";
    } else if (step.switch) {
      return "switch";
    }
    return undefined;
  }

  visitRef(step: HdesApi.AstFlowTaskNode): HdesApi.Entity<any> | undefined {
    const ref = step.ref?.ref.value;
    if (!ref) {
      return undefined;
    }
    if (step.decisionTable) {
      return this.findRef(ref, "DT");
    } else if (step.service) {
      return this.findRef(ref, "FLOW_TASK");
    }
    return undefined;
  }
  
  findRef(name: string, type: HdesApi.AstBodyType): HdesApi.Entity<any> | undefined {
    const models: HdesApi.Entity<any>[] = [];
    if (type === "DT") {
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


  visitCyclicDependency(step: HdesApi.AstFlowTaskNode, parent?: Vis.Node) {
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

    return new ModelVisitor(props.fl, props.models).visit();
  };
}


export default GraphAPI;