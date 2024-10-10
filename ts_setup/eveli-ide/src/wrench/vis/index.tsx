import React from 'react';
// @ts-ignore
import Graph from 'vis-react';


declare namespace Vis {

  interface Model {
    nodes: Node[];
    edges: Edge[];
    visited: string[];
  }

  interface Node {
    id: string,
    label: string,
    shape: 'circle' | 'diamond' | 'box' | undefined,
    parents: string[],
    color?: string;
    group?: string,
    externalId?: string;
    body?: any;
    widthConstraint?: { maximum: number, minimum: number }
    x: number,
    y: number,
  }

  interface Edge {
    from: string,
    to: string
  }

  interface InitProps {
    model?: Model;
    options: any,
    events: {
      onClick: (id: string) => void;
      onDoubleClick: (id: string) => void;
    }
  }
}


interface VisEvents {
  select: (event: { nodes: Vis.Node[], edges: Vis.Edge[] }) => void;
  doubleClick: (event: { nodes: Vis.Node[], edges: Vis.Edge[] }) => void;
}

namespace Vis {

  export const create = (init: InitProps): React.ReactElement => {
    const { model } = init;
    if (!model) {
      return (<span></span>);
    }

    const events: VisEvents = {
      select: (event) => {
        const { nodes } = event
        //console.log("vis.events.select", nodes, edges);
        init.events.onClick(nodes[0].id);
      },

      doubleClick: (event) => {
        const { nodes } = event;
        const selected = nodes[0];
        if(!selected) {
          return;
        }
        
        const stepId: string = selected as any;
        const values = model.nodes.filter(n => n.id === stepId)
        if (values.length > 0 && values[0].externalId) {
          init.events.onDoubleClick(values[0].externalId)
        }
      }
    };

    const props = {
      events: events,
      value: model,
      options: init.options
    };    
    return (<Graph graph={props.value} options={props.options} events={props.events} />);
  }
}

export default Vis;

