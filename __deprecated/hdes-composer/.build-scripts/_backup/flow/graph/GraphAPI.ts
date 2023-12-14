import { Hdes, Vis } from '../deps';


type ModelType = 'switch' | 'decisionTable' | 'service' | undefined;

class ModelVisitor {
  private _fl: Hdes.AstAPI.Fl;
  private _visited: string[] = [];
  private _nodes: Vis.Node[] = [];
  private _edges: Vis.Edge[] = [];
  private _models: Hdes.ModelAPI.Models;
  private _start: { x: number, y: number };
  private _constraints: {
    width: { maximum: number, minimum: number }
  };
  private _seperation: {
    level: number;
    switch: number;
  };

  constructor(fl: Hdes.AstAPI.Fl, models: Hdes.ModelAPI.Models) {
    this._fl = fl;
    this._models = models;
    this._constraints = {
      width: { maximum: 150, minimum: 150 }
    }
    this._seperation = {
      level: 100,
      switch: 80 + this._constraints.width.minimum
    };
    this._start = {
      x: -500, y: -500
    };
  }

  visit(): Vis.Model {
    const steps = Object.values(this._fl.src.tasks);    
    
    const start: Vis.Node = {id: 'start', label: 'start', shape: 'circle', x: 0, y: this._start.y, parents: []};
    this._nodes.push(start);
    
    const first = steps.filter(step => step.order === 0);
    if(first.length === 1) {
      this.visitStep(first[0], {parent: start});
    }

    if (steps.length === 0) {
      this._edges.push({ from: 'start', to: 'end' });
      this._nodes.push({
        id: 'end', label: 'end', shape: 'circle',
        parents: [],
        x: 0,
        y: this._start.y + this._seperation.level
      })
    }

    return { nodes: this._nodes, edges: this._edges };
  }

  visitEdge(step: Hdes.AstAPI.FlTask, props: { parent: Vis.Node, index?: number }) {
    const id = step.id.value;
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

  visitStep(step: Hdes.AstAPI.FlTask, props: { parent: Vis.Node, index?: number }) {
    const id = step.id.value;
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
    this._visited.push(step.id.value);

    if (group === "switch") {
      this.visitSwitch(step, { parent: node, index: props.index });
    } else if (group === "decisionTable") {
      this.visitThen(step.then, { parent: node, index: props.index });
    } else if (group === "service") {
      this.visitThen(step.then, { parent: node, index: props.index });
    }

    this.visitOverlapping(node);
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
  
  visitThen(then: Hdes.AstAPI.FlNode, props: { parent: Vis.Node, index?: number }) {
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

    const step: Hdes.AstAPI.FlTask = next[0];
    return this.visitStep(step, props);
  }

  visitSwitch(step: Hdes.AstAPI.FlTask, props: { parent: Vis.Node, index?: number }) {
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
    _step: Hdes.AstAPI.FlTask,
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

  visitType(step: Hdes.AstAPI.FlTask): ModelType {
    if (step.decisionTable) {
      return "decisionTable";
    } else if (step.service) {
      return "service";
    } else if (step.switch) {
      return "switch";
    }
    return undefined;
  }

  visitRef(step: Hdes.AstAPI.FlTask): Hdes.ModelAPI.Model | undefined {
    const ref = step.ref?.ref.value;
    if (!ref) {
      return undefined;
    }

    const models: Hdes.ModelAPI.Model[] = [];
    if (step.decisionTable) {
      models.push(...this._models.DT);
    } else if (step.service) {
      models.push(...this._models.FLOW_TASK);
    }
    const result = models.filter(m => m.name === ref)
    if (result.length === 0) {
      return undefined;
    }
    return result[0];
  }

  visitCyclicDependency(step: Hdes.AstAPI.FlTask, parent?: Vis.Node) {
    const id = step.id.value;
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
    fl: Hdes.AstAPI.Fl,
    models: Hdes.ModelAPI.Models
  }) => {

    return new ModelVisitor(props.fl, props.models).visit();
  };
}


export default GraphAPI;