import * as React from 'react';
import { CanvasWidget } from '@projectstorm/react-canvas-core';
import createEngine, { DiagramModel, DefaultNodeModel, DefaultPortModel, RightAngleLinkFactory } from '@projectstorm/react-diagrams';
import { BaseModel } from '@projectstorm/react-canvas-core';
import { DemoCanvasWidget } from './helpers/DemoCanvasWidget';
import { DemoButton, DemoWorkspaceWidget } from './helpers/DemoWorkspaceWidget';


import DeClient from '@declient';
import FlowVisitor from './flow-visitor';

/**
 * Tests the drag on/off
 */
class CanvasDragToggle extends React.Component<any, any> {
  enableDrag = () => {
    const { engine } = this.props;
    const state = engine.getStateMachine().getCurrentState();
    state.dragCanvas.config.allowDrag = true;
  };

  disableDrag = () => {
    const { engine } = this.props;
    const state = engine.getStateMachine().getCurrentState();
    state.dragCanvas.config.allowDrag = false;
  };

  render() {
    const { engine } = this.props;
    return (
      <DemoWorkspaceWidget
        buttons={[
          <DemoButton key={1} onClick={this.enableDrag}>
            Enable canvas drag
          </DemoButton>,
          <DemoButton key={2} onClick={this.disableDrag}>
            Disable canvas drag
          </DemoButton>
        ]}
      >
        <DemoCanvasWidget>
          <CanvasWidget engine={engine} />
        </DemoCanvasWidget>
      </DemoWorkspaceWidget>
    );
  }
}

export default (props: {
  flow: DeClient.DefStateFlowAssocs,
  def: DeClient.DefinitionState,
  canvas: { width: number, height: number} 
}) => {
    
  const engine = createEngine();
  engine.getLinkFactories().registerFactory(new RightAngleLinkFactory());
  const model = new DiagramModel();
  model.addAll(...new FlowVisitor(props).visit())
  engine.setModel(model);
  return <CanvasDragToggle engine={engine} model={model} />;
};