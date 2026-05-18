export interface Model {
  nodes: Node[];
  edges: Edge[];
  visited: string[];
}

export interface Node {
  id: string,
  label: string,
  type: 'start' | 'end' | 'switch' | 'decisionTable' | 'service' | 'flow' | 'returns' | 'form',
  parents: string[],
  externalId?: string;
  body?: any;
}

export interface Edge {
  from: string,
  to: string
}

export interface VisProps {
  id: string;
  model: Model;
  events: {
    onClick: (id: string) => void;
    onDoubleClick: (id: string) => void;
  }
}
