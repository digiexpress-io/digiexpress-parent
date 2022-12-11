import * as React from 'react';
import { CanvasWidget } from '@projectstorm/react-canvas-core';
import createEngine, { DiagramModel, DefaultNodeModel, DefaultPortModel } from '@projectstorm/react-diagrams';
import { BaseModel } from '@projectstorm/react-canvas-core';
import { DemoCanvasWidget } from './helpers/DemoCanvasWidget';
import { DemoButton, DemoWorkspaceWidget } from './helpers/DemoWorkspaceWidget';

import { Composer, Client } from '../context';

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

function toRecord<T extends Record<string, any>, K extends keyof T>(array: T[], selector: K): Record<T[K], T> {
  return array.reduce((acc, item) => ({ ...acc, [item[selector]]: item }), {} as Record<T[K], T>)
}

interface ModelConfig {
  start: number,
  boxHight: number;
  spaceHight: number,
  portHight: number;
}

type ProcessValue_value = string;

class DefinitionVisitor {
  private _site: Client.SiteDefinition;
  private _result: BaseModel[] = [];
  private _stencil_to_dialob_ports: Record<ProcessValue_value, DefaultPortModel[]> = {};
  private _dialob_models_by_flowId: Record<string, { node: DefaultNodeModel, form: Client.FormDocument }[]> = {};
  private _processes: Client.ProcessValue[] = [];

  constructor(site: Client.SiteDefinition) {
    this._site = site;
  }

  public visit(): BaseModel[] {
    const boxHight = 50;
    const portHight = 17.5;
    const spaceHight = 10;
    const locationY = 20;
    const localized = this._site.stencil.sites['fi'];

    const props: ModelConfig = { start: locationY, spaceHight, boxHight, portHight }

    this.visitStencil(localized, props);
    this.visitDialob(localized, props);
    this.visitHdes(props);
    return this._result;
  }

  private visitHdes(props: ModelConfig) {

    let locationY = props.start;
    const defHeight = props.boxHight;
    const wkHeight = props.portHight;

    const visited: string[] = [];
    const forms = this._site.dialob.forms;

    for (const processValue of this._processes) {
      const flow = this._site.hdes.flows[processValue.flowId];
      if (!flow) {
        console.error('Flow not found: ', processValue);
        continue;
      }
      if (visited.includes(processValue.flowId)) {
        continue;
      }
      visited.push(processValue.flowId);



      const node1 = new DefaultNodeModel("flow:: " + flow.ast.name, 'rgb(209, 67, 67)');
      const y = locationY + props.spaceHight
      node1.setPosition(1000, y);

      const forms = this._dialob_models_by_flowId[processValue.flowId] ?? [];
      for (const incoming of forms) {
        const port1 = incoming.node.addOutPort(flow.ast.name);
        const port2 = node1.addInPort(incoming.form.data.name);
        const link1 = port1.link(port2);
        this._result.push(link1);
      }

      /*
            const incoming = this._stencil_ports[link.value] ?? [];
            const portLinks = incoming.map(port1 => port1.link(port2));
            this._result.push(...portLinks);
      */
      this._result.push(node1);
      const height = wkHeight * forms.length + defHeight;
      locationY += height;
    }

  }


  private visitDialob(localized: Client.LocalizedSite, props: ModelConfig) {
    const processes = toRecord(this._site.definition.processes, 'name');
    let locationY = props.start;
    const defHeight = props.boxHight;
    const wkHeight = props.portHight;

    const forms = this._site.dialob.forms;

    const visited: string[] = [];

    for (const link of Object.values(localized.links).filter(link => link.workflow).sort((a, b) => a.name.localeCompare(b.value))) {
      const processValue = processes[link.value];
      if (!processValue) {
        console.error('Process not found: ', link);
        continue;
      }
      if (visited.includes(processValue.formId)) {
        continue;
      }
      this._processes.push(processValue);
      const dialob = forms[processValue.formId];
      visited.push(processValue.formId);

      if (!dialob) {
        console.error('Dialob not found: ', processValue);
        continue;
      }

      const node1 = new DefaultNodeModel("dialob:: " + dialob.data.name, 'rgb(192,255,0)');
      const y = locationY + props.spaceHight
      node1.setPosition(600, y);
      var port2 = node1.addInPort('In');

      const incoming = this._stencil_to_dialob_ports[link.value] ?? [];
      const portLinks = incoming.map(port1 => port1.link(port2));
      this._result.push(...portLinks);

      this._result.push(node1);
      const height = wkHeight*2 + defHeight;
      locationY += height;

      if (!this._dialob_models_by_flowId[processValue.flowId]) {
        this._dialob_models_by_flowId[processValue.flowId] = [];
      }
      this._dialob_models_by_flowId[processValue.flowId].push({ node: node1, form: dialob });
    }
  }

  private visitStencil(localized: Client.LocalizedSite, props: ModelConfig) {
    let locationY = props.start;
    const defHeight = props.boxHight;
    const wkHeight = props.portHight;
    for (const topic of Object.values(localized.topics).sort((a, b) => a.name.localeCompare(b.name))) {
      const links = topic.links.map(link => localized.links[link]).filter(link => link.workflow);
      var node1 = new DefaultNodeModel("article:: " + topic.name, 'rgb(0,192,255)');

      for (const link of links) {
        var port1 = node1.addOutPort('workflow:: ' + link.value);

        if (!this._stencil_to_dialob_ports[link.value]) {
          this._stencil_to_dialob_ports[link.value] = [];
        }
        this._stencil_to_dialob_ports[link.value].push(port1);
      }

      const y = locationY + props.spaceHight;
      node1.setPosition(100, y);
      this._result.push(node1);
      const height = links.length * wkHeight + defHeight;
      locationY += height;
    }
  }
}



export default (props: { site: Client.SiteDefinition }) => {
  //1) setup the diagram engine
  var engine = createEngine();

  //2) setup the diagram model
  var model = new DiagramModel();

  /*
    //3-A) create a default node
    var node1 = new DefaultNodeModel('Node 1', 'rgb(0,192,255)');
    var port1 = node1.addOutPort('Out');
    node1.setPosition(100, 100);
  
    //3-B) create another default node
    var node2 = new DefaultNodeModel('Node 2', 'rgb(192,255,0)');
    var port2 = node2.addInPort('In');
    node2.setPosition(400, 100);
  
    //3-C) link the 2 nodes together
    var link1 = port1.link(port2);
  
    //4) add the models to the root graph
    model.addAll(node1, node2, link1);
    */

  model.addAll(...new DefinitionVisitor(props.site).visit())

  //5) load model into engine
  engine.setModel(model);

  //6) render the diagram!
  return <CanvasDragToggle engine={engine} model={model} />;
};