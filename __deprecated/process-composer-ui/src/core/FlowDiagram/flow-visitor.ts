import { DefaultNodeModel, DefaultPortModel, RightAngleLinkModel, LinkModel } from '@projectstorm/react-diagrams';
import { BaseModel, AbstractModelFactory } from '@projectstorm/react-canvas-core';

import DeClient from '@declient';
import * as Hdes from '@hdes-types';


class RightAnglePortModel extends DefaultPortModel {
  createLinkModel(_factory?: AbstractModelFactory<LinkModel>) {
    return new RightAngleLinkModel();
  }
}

type ModelType = 'switch' | 'decisionTable' | 'service' | 'flow' | undefined;

function getType(step: Hdes.AstFlowTaskNode): ModelType {
  if (step.decisionTable) {
    return "decisionTable";
  } else if (step.service) {
    return "service";
  } else if (step.switch) {
    return "switch";
  }
  return undefined;
}


/*
 *
----x axis------>
  |
  |
  y
  a
  x
  i
  s
  |
  |
  V
*  
*/


interface Previous {
  node?: DefaultNodeModel,
  id?: string,
  src?: Hdes.AstFlowTaskNode
  location: PreviousLocation
}
interface PreviousLocation {
  x: number, y: number
}

export default class FlowVisitor {
  private _src: DeClient.DefStateFlowAssocs;
  private _def: DeClient.DefinitionState;
  private _canvas: { width: number, height: number };
  private _result: BaseModel[] = [];
  private _visited: string[] = [];
  private _spacing = {
    x: 10, y: 10
  }

  constructor(props: {
    canvas: { width: number, height: number }
    flow: DeClient.DefStateFlowAssocs,
    def: DeClient.DefinitionState
  }) {
    this._src = props.flow;
    this._def = props.def;
    this._canvas = props.canvas;
  }

  public visit(): BaseModel[] {
    const steps = Object.values(this._src.flow.ast.src.tasks);
    const [first] = steps;

    const location = { x: this._canvas.width / 2 - 100, y: 10 };
    this.visitNode({ src: first, previous: { location } });
    return this._result;
  }

  private visitNode(props: { src: Hdes.AstFlowTaskNode, previous: Previous }) {

    const { src, previous } = props;
    const id = src.id.value;

    // construct the node
    const node = new DefaultNodeModel(id, 'rgb(209, 67, 67)');
    node.setPosition(previous.location.x, previous.location.y + this._spacing.y + 100);
    this._result.push(node);
    this._visited.push(id);


    // construct connections
    if (previous.node) {
      const port2 = node.addPort(new RightAnglePortModel(false, previous.id!, 'previous'));
      const port1 = previous.node.addPort(new RightAnglePortModel(false, id, 'next'));
      const link1 = port1.link(port2);
      this._result.push(link1);
    }

    // construct next in line
    const parent: Previous = { node, id, src, location: node.getPosition() };
    const type = getType(src);

    if (type === "switch") {
      this.visitSwitch({ src, previous: parent });
    } else if (type === "decisionTable") {
      this.visitThen({ then: src.then, previous: parent });
    } else if (type === "service") {
      this.visitThen({ then: src.then, previous: parent });
    }
  }

  private visitSwitch(props: { src: Hdes.AstFlowTaskNode, previous: Previous }) {
    console.error("not implemented");

  }
  private visitThen(props: { then: Hdes.AstFlowNode, previous: Previous }) {
    const { then, previous } = props;
    if (!then.value) {
      return;
    }
    if (then.value === 'end') {
      return this.visitEnd(props);
    }
    const next = Object.values(this._src.flow.ast.src.tasks).filter(step => step.id?.value === then.value);
    if (!next.length) {
      return;
    }
    const src: Hdes.AstFlowTaskNode = next[0];
    return this.visitNode({ src, previous });
  }


  visitEnd(props: { then: Hdes.AstFlowNode, previous: Previous, index?: number }) {
    const id = 'end-' + props.previous.id + (props.index ? props.index : '');
    const parentId = props.previous.id;
    const refId = parentId + '->' + id
    if (this._visited.includes(refId)) {
      return
    }

    /*props: { parent: Vis.Node, index?: number }
    this._nodes.push({
      id, label: 'end', shape: 'circle',
      x: props.parent.x,
      y: props.parent.y + this._seperation.level,
      parents: [...props.parent.parents, props.parent.id]
    });
    this._visited.push(refId)
    this._edges.push({ from: parentId, to: id })
    */
  }
}

