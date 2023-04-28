import { CanvasWidget } from '@projectstorm/react-canvas-core';
import createEngine, { DiagramModel, RightAngleLinkFactory, DiagramEngine } from '@projectstorm/react-diagrams';

import { DemoCanvasWidget } from './helpers/DemoCanvasWidget';
import { DemoWorkspaceWidget } from './helpers/DemoWorkspaceWidget';


import DeClient from '@declient';
import FlowVisitor from './flow-visitor';


export default (props: {
  flow: DeClient.DefStateFlowAssocs,
  def: DeClient.DefinitionState,
  canvas: { width: number, height: number }
}) => {

  const model = new DiagramModel();
  model.addAll(...new FlowVisitor(props).visit())

  const engine = createEngine();
  engine.getLinkFactories().registerFactory(new RightAngleLinkFactory());
  engine.setModel(model);
  return (<DemoWorkspaceWidget><DemoCanvasWidget><CanvasWidget engine={engine} /></DemoCanvasWidget></DemoWorkspaceWidget>);
};