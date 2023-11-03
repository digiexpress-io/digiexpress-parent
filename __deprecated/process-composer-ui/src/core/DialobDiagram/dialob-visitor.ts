import { DefaultNodeModel, DefaultPortModel, RightAngleLinkModel, LinkModel } from '@projectstorm/react-diagrams';
import { BaseModel, AbstractModelFactory } from '@projectstorm/react-canvas-core';

import DeClient from '@declient';
import * as Hdes from '@hdes-types';


class RightAnglePortModel extends DefaultPortModel {
  createLinkModel(_factory?: AbstractModelFactory<LinkModel>) {
    return new RightAngleLinkModel();
  }
}

/*
----x axis------>
  |
  |
  y axis
  |
  |
  V
*/
const portHight = 17.5;
    
function getLabel(props: { label?: DeClient.DialobDataLabel }) {

  const { label } = props;
  if (label) {
    const result = label['en'] ?? label['fi'] ?? Object.values(label)[0];
    if (result.length > 20) {
      return result.substring(0, 20) + " ..."
    }
    return result;
  }
  return undefined;
}


function getLiteralName(props: { id: string, label?: DeClient.DialobDataLabel, type: string }) {
  console.log(props);
  const { id, type } = props;
  const label = getLabel(props);
  return `${id}/${label}: ${type}`;
}

function getName(props: { id: string, label?: DeClient.DialobDataLabel }) {
  console.log(props);
  const { id } = props;
  const label = getLabel(props)
  return label ?? id;
}

interface Previous {
  node?: DefaultNodeModel;
  id?: string;
  src?: DeClient.DialobDataType;
  location: PreviousLocation;
  previous: Previous | undefined
}
interface PreviousLocation {
  x: number, y: number
}

export default class DialobVisitor {
  private _src: DeClient.DefStateDialobAssocs;
  private _def: DeClient.DefinitionState;
  private _canvas: { width: number, height: number };
  private _result: BaseModel[] = [];
  private _visited: string[] = [];
  private _spacing = {
    x: 160, y: 50
  }

  constructor(props: {
    canvas: { width: number, height: number }
    dialob: DeClient.DefStateDialobAssocs,
    def: DeClient.DefinitionState
  }) {
    this._src = props.dialob;
    this._def = props.def;
    this._canvas = props.canvas;
  }

  public visit(): BaseModel[] {
    console.log(this._src.form.data.data);
    const first = this._src.form.data.data['questionnaire'] as DeClient.DialobDataRoot;
    const location = { x: -250, y: 10 };
    this.visitRoot({ src: first, previous: { location, previous: undefined } });
    return this._result;
  }

  private visitRoot(props: { src: DeClient.DialobDataRoot, previous: Previous }) {
    let previous: Previous = props.previous;
    for (const pageId of props.src.items) {
      const page = this._src.form.data.data[pageId] as DeClient.DialobDataGroup;
      previous = this.visitPage({ src: page, previous })
    }
  }

  private visitPage(props: { src: DeClient.DialobDataGroup, previous: Previous }): Previous {
    const { src, previous } = props;
    const { id } = src;
    const name = getName(src);

    // construct the node
    const node = new DefaultNodeModel(name, 'rgb(209, 67, 67)');
    node.setPosition(previous.location.x + this._spacing.x + 100, previous.location.y);
    this._result.push(node);
    this._visited.push(id);

    const self: Previous = { node, id, src, location: node.getPosition(), previous };
    let visited = self;
    src.items
      .map(id => this._src.form.data.data[id])
      .forEach(src => {
        const completed = this.visitNode({ src, previous: visited });
        if(completed) {
          visited = completed;
        }
      })
    return self;
  }

  private visitNode(props: { src: DeClient.DialobDataType, previous: Previous }): Previous | undefined {
    const { src, previous } = props;
    const { id, type } = src;
    switch (type) {
      case 'boolean':
      case 'date':
      case 'note':
      case 'text':
      case 'time': return this.visitLiteralNode(props);
      case 'group': return this.visitGroupNode(props as any);
      case 'rowgroup': return this.visitRowGroupNode(props);
      case 'list': return this.visitListNode(props);
    }
  }

  private visitListNode(props: { src: DeClient.DialobDataType, previous: Previous }) {
    return undefined;
  }

  private visitRowGroupNode(props: { src: DeClient.DialobDataType, previous: Previous }) {
    return undefined;
  }

  private visitGroupNode(props: { src: DeClient.DialobDataGroup, previous: Previous }): Previous {

    const { src, previous } = props;
    const { id } = src;
    const parentNode = previous.node!

    const node = new DefaultNodeModel(getName(src), 'rgb(192,255,0)');
    const ports = portHight * (parentNode.getInPorts().length + parentNode.getOutPorts().length);
    node.setPosition(previous.location.x, previous.location.y + this._spacing.y + ports);
    this._result.push(node);

    const self: Previous = { node, id, src, location: node.getPosition(), previous };
    src.items
      .map(id => this._src.form.data.data[id])
      .forEach(src => this.visitNode({ src, previous: self }))

    return self;
  }

  private visitLiteralNode(props: { src: DeClient.DialobDataType, previous: Previous }) {
    const { src, previous } = props;
    const node = previous.node!
    const port = node.addInPort(getLiteralName(src));

    return undefined;
  }

  private visitNodeX(props: { src: DeClient.DialobDataType, previous: Previous }) {

    const { src, previous } = props;
    const id = src.id;

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
    const parent: Previous = { node, id, src, location: node.getPosition(), previous: undefined };

  }

}

