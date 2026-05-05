
// BUG - https://github.com/kieler/elkjs/issues/142
import ELK, { ElkExtendedEdge, ElkNode } from 'elkjs/lib/elk.bundled.js';

import { Node as xyflowNode, Edge as xyflowEdge, MarkerType, Position } from '@xyflow/react';
import { Model, Edge, Node } from './vis-types';

const elk = new ELK();

const elkOptions = {
  'elk.algorithm': 'layered',
  'elk.direction': 'DOWN',
  'elk.layered.spacing.nodeNodeBetweenLayers': '100',
  'elk.spacing.nodeNode': '80',
};

class ModelMapper {
  private _model: Model;
  private _model_nodes: Record<string, Node> = {};
  private _model_edges: Record<string, Edge> = {};

  constructor(model: Model) {
    this._model = model;
  }

  toElk() : { children: ElkNode[], edges: ElkExtendedEdge[]} {
    return {
      children: this._model.nodes.map(e => this.mapToElkNode(e)),
      edges: this._model.edges.map(e => this.mapToElkEdge(e)),
    }
  }

  mapToElkNode(data: Node): ElkNode {
    this._model_nodes[data.id] = data;
    return {
      id: data.id,
      ...this.getSize(data)
    }
  }

  getSize(data: Node) {
    if(data.type === 'switch') {
      return { height: 200, weight: 200 }
    }
    if(data.type === 'decisionTable' || data.type === 'returns') {
      return { height: 100, width: 200 }
    }
    if(data.type === 'service') {
      return { height: 100, width: 200 }
    }
    return {
      width: 100,
      height: 100,
    }
  }

  mapToElkEdge(data: Edge): ElkExtendedEdge {
    const id = data.from + "/" + data.to;
    this._model_edges[id] = data;
    return {
      id,
      sources: [data.from],
      targets: [data.to],
    }
  }

  toXYFlow(elk: ElkNode): { nodes: xyflowNode[], edges: xyflowEdge[] } {
    const nodes = elk.children?.map(n => this.mapToXYNode(n)) ?? [];
    const edges = elk.edges?.map(n => this.mapToXYEdge(n)) ?? [];
    return { edges, nodes }
  }

  mapToXYNode(node: ElkNode): xyflowNode {
    const data = this._model_nodes[node.id];

    return {
      position: { x: node.x ?? 0, y: node.y ?? 0 },
      id: node.id,
      data: { label: data.label, type: data.type, externalId: data.externalId },
      type: data.type,
      targetPosition: Position.Top,
      sourcePosition: Position.Bottom,
    }
  }

  mapToXYEdge(node: ElkExtendedEdge): xyflowEdge {
    return {
      id: node.id,
      source: node.sources[0],
      target: node.targets[0],
      markerEnd: {
        type: MarkerType.Arrow
      }
    }
  }
}


export const elkLayout = (model: Model): Promise<{ nodes: xyflowNode[], edges: xyflowEdge[] }> => {
  const mapper = new ModelMapper(model);
  const graph: ElkNode = {
    id: 'root',
    layoutOptions: elkOptions,
    ...mapper.toElk()
  };

  return elk
    .layout(graph)
    .then((layoutedGraph) => mapper.toXYFlow(layoutedGraph))
    .catch(error => {
      console.error(error);
      return { nodes: [], edges: [] }
    });
};
