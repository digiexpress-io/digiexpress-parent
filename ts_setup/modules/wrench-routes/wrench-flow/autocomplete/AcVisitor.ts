import { HdesApi } from '@dxs-ts/wrench-api';
import { Position, languages, editor } from 'monaco-editor';

import { AcBuilder } from './AcBuilder';
import { TYPES } from './types';
import { AssetsQuery } from './AssetsQuery';
import { AstNav, AstNavNode, AstNavNodeDesc } from './AstNav';

const KEY_ID = "id";
const FIELD = ":";
const KEY_DESC = "description";
const KEY_INPUTS = "inputs";
const KEY_TASKS = "tasks";
interface TaskBodyPos {
  isEndOfLine: boolean, inTask: boolean,
}

export class AcVisitor {

  private _flow: HdesApi.AstFlow;
  private _result: languages.CompletionItem[] = [];

  private _site: HdesApi.Site;
  private _assetsQuery: AssetsQuery
  
  private _nav: AstNav;
  private _navDesc: AstNavNodeDesc;

  constructor(
    flow: HdesApi.AstFlow, 
    site: HdesApi.Site, 
    model: editor.ITextModel, 
    modelPosition: Position) {

    this._flow = flow;
    this._site = site;
    this._assetsQuery = new AssetsQuery(site);
    this._nav = new AstNav(flow, this._assetsQuery, model, modelPosition);
    this._navDesc = this._nav.getPositionDescription();
  }

  visit(): languages.CompletionItem[] {
    this.visitRoot(this._flow.src);
    console.log('desc', this._nav.getPositionDescription(), this._result);
    return [...this._result];
  }

  private ac() {
    return new AcBuilder(this._nav.model, this._nav.modelPosition);
  }

  private hasNonNull(name: string, node: HdesApi.AstFlowNode): boolean {
    return this.get(name, node) ? true : false;
  }
  private get(keyword: string, node: HdesApi.AstFlowNode): HdesApi.AstFlowNode {
    const result = node.children[keyword];
    return result;
  }

  private findAllTaskThen(taskId: string | undefined): { id: string, text: string }[] {
    return taskId ? [
      { id: "end", text: "then: end" },
      ...Object.entries(this._flow.src.tasks)
        .filter(([, body]) => body.id?.value)
        .filter(([, body]) => ( body.id?.value !== taskId))
        .map(([name, body]) => ({
          id: body.id?.value,
          text: `then: ${name}`
        }))
    ] : [{ id: "end", text: "then: end" }];
  }

  private visitRoot(flow: HdesApi.AstFlowRoot) {
    this.visitId(flow);
    this.visitDesc(flow);
    this.visitInputs(flow);
    this.visitTasks(flow);
    
    if(!this._navDesc.node) {
      return;
    }

    if(this._navDesc.node.type === 'FLOW_TASK' && !this._navDesc.node.value.then) {
      const task = this._navDesc.node.value;
      const taskId = task.id?.value;
      const selection = this.findAllTaskThen(taskId);
      
      
      for(const then of selection) {
        this._result.push(this.ac()
          .id(then.text)
          .append(false)
          .addField("then", { indent: 6, value: then.id })
          .build());
      }
    }

    if(this._navDesc.node.type === 'FLOW_TASK' && !this._navDesc.node.value.id) {
      const task = this._navDesc.node.value;
      this._result.push(this.ac()
        .id("id: ")
        .append(false)
        .addField("id", { indent: 6, value: task.keyword })
        .build());
    }

    if(this._navDesc.node.type === 'FLOW_TASK' && 
      !this._navDesc.node.value['decisionTable'] && 
      !this._navDesc.node.value['returns'] && 
      !this._navDesc.node.value['service'] && 
      !this._navDesc.node.value['switch']
    ) {
      const task = this._navDesc.node.value;
      this._result.push(this.ac()
        .id("id: ")
        .append(false)
        .addField("id", { indent: 6, value: task.keyword })
        .build());
    }
    
    if(this._navDesc.node.type === 'FLOW_TASK_THEN' && this._navDesc.description === 'ON_ELEMENT') {
      const task = this._navDesc.node.parent?.value!;
      const taskId = task.id?.value;
      const selection = this.findAllTaskThen(taskId);

      for(const then of selection) {
        const sufix = this._navDesc.node.value.value === then.id ? " - currently selected" : "";
        this._result.push(this.ac()
          .id(then.text + sufix)
          .append(false)
          .addField("then", { indent: 6, value: then.id })
          .build());
      }
    }

    if(this._navDesc.node.type === 'FLOW_TASK_ASSET_REF' && this._navDesc.description === 'ON_ELEMENT') {
      const task = this._navDesc.node.parent?.value!;
      const service: HdesApi.AstFlowNode | undefined = task["service"];
      const decisionTable: HdesApi.AstFlowNode | undefined = task["decisionTable"];
      const target = decisionTable ? decisionTable : service;
      if (!target) {
        return;
      }
      const ref = target.children["ref"];
      if (!ref || !this.in(ref)) {
        return;
      }

      const refs = decisionTable ? Object.values(this._site.decisions) : Object.values(this._site.services);
      for (const asset of refs) {
        const sufix = ref.value === asset.ast?.name ? " - currently selected" : "";
        this._result.push(this.ac()
          .id("ref: " + asset.ast?.name + sufix)
          .addField("ref", { indent: 8, value: asset.ast?.name })
          .build());
      }
    }



      
      //this.visitNewInput(flow);
      //this.visitInput(flow);
      //this.visitNewTask(flow);

    
  }



  private visitTaskBodyMapping(flow: HdesApi.AstFlowRoot, task: HdesApi.AstFlowNode, _props: TaskBodyPos) {
    const service: HdesApi.AstFlowNode | undefined = task["service"];
    const decisionTable: HdesApi.AstFlowNode | undefined = task["decisionTable"];
    const target = decisionTable ? decisionTable : service;
    if (!target) {
      return;
    }
    const inputs = target.children["inputs"];
    if (!inputs) {
      return;
    }
    const afterInputBlock = (this._nav.currentLine - 1) === inputs.end;
    if (!this.in(inputs) && !afterInputBlock) {
      return;
    }


    const ref = target.children["ref"];
    if (!ref) {
      return;
    }
    let linked: HdesApi.Entity<HdesApi.AstBody> | undefined = this._assetsQuery.findOne(ref.value);
    if (!linked) {
      return;
    }

    const headers = linked.ast?.headers.acceptDefs
    if (!headers) {
      return;
    }

    for (const typeDef of headers) {
      if(inputs.children[typeDef.name]) {
        continue;
      }
      this._result.push(
        this.ac()
        .id("add missing mapping: " + typeDef.name + " " + typeDef.valueType)
        .addField(typeDef.name, { indent: 10 })
        .build()
      );
    }

    // change mapping
    for (const [key, value] of Object.entries(inputs.children)) {
      if (value.end === this._nav.currentLine) {

        for (const typeDef of this._flow.headers.acceptDefs) {
          this._result.push(this.ac()
            .id("flow input: " + typeDef.name + " " + typeDef.valueType)
            .addField(key, { indent: 10, value: typeDef.name })
            .build());
        }

        this.visitTaskBodyMappingEntry(flow, task, { key, value })
        break;
      }
    }
  }

  private visitTaskBodyMappingEntry(flow: HdesApi.AstFlowRoot, currentTask: HdesApi.AstFlowNode, props: { key: string, value: HdesApi.AstFlowNode }) {
    for (const task of Object.values(flow.tasks)) {

      if (task.start > currentTask.start) {
        continue;
      }

      const service: HdesApi.AstFlowNode | undefined = task["service"];
      const decisionTable: HdesApi.AstFlowNode | undefined = task["decisionTable"];
      const target = decisionTable ? decisionTable : service;
      if (!target) {
        continue;
      }
      const ref = target.children["ref"];
      if (!ref) {
        continue;
      }
      let linked: HdesApi.Entity<HdesApi.AstBody> | undefined = this._assetsQuery.findOne(ref.value);
      if (!linked) {
        continue;
      }

      const headers = linked.ast?.headers.returnDefs
      if (!headers) {
        continue;
      }

      for (const typeDef of headers) {
        this._result.push(this.ac()
          .id("task output: " + task.id.value + "." + typeDef.name + " " + typeDef.valueType)
          .addField(props.key, { indent: 10, value: task.id.value + '.' + typeDef.name })
          .build());
      }
    }
  }


  private visitNewTask(flow: HdesApi.AstFlowRoot) {
    const tasks = this.get(KEY_TASKS, flow);
    if (tasks == null) {
      return;
    }
    let isAround = tasks.start < this._nav.currentLine;
    let isEndOfLine = false;
    const allTasks: HdesApi.AstFlowNode[] = Object.values(tasks.children);
    for (const task of allTasks) {
      if (this.isEndOfLine(task)) {
        isEndOfLine = true;
        break;
      }
      if (this.in(task)) {
        isAround = false;
      }
    }

    if (isAround || isEndOfLine) {
      this._result.push(this.ac()
        .id("new switch task")
        .append(isEndOfLine)
        .addField("- name", { indent: 2 })
        .addField("id", { indent: 6, value: "task-id" })
        .addField("switch", { indent: 6 })
        .addField("- caseName1", { indent: 8 })
        .addField("when", { indent: 12, value: "when-boolean-expression" })
        .addField("then", { indent: 12, value: "next-task-id" })
        .addField("- caseName2", { indent: 8 })
        .addField("when", { indent: 12, value: "when-boolean-expression" })
        .addField("then", { indent: 12, value: "next-task-id" })
        .build());

      this._result.push(this.ac()
        .id("new service task")
        .append(isEndOfLine)
        .addField("- {name}", { indent: 2 })
        .addField("id", { indent: 6, value: "{id}" })
        .addField("then", { indent: 6, value: "next" })
        .addField("{serviceType}", { indent: 6 })
        .addField("ref", { indent: 8, value: "{ref}" })
        .addField("collection", { indent: 8, value: "false" })
        .addField("inputs", { indent: 8 })
        .guided("service-task")
        .build());

      this._result.push(this.ac()
        .id("new decision task")
        .append(isEndOfLine)
        .addField("- {name}", { indent: 2 })
        .addField("id", { indent: 6, value: "{id}" })
        .addField("then", { indent: 6, value: "next" })
        .addField("{serviceType}", { indent: 6 })
        .addField("ref", { indent: 8, value: "{ref}" })
        .addField("collection", { indent: 8, value: "false" })
        .addField("inputs", { indent: 8 })
        .guided("decision-task")
        .build());
    }
  }


  visitNewInput(flow: HdesApi.AstFlowRoot) {
    const inputs = this.get(KEY_INPUTS, flow);
    if (!inputs) {
      return;
    }


    let isAround = this.in(inputs, this.get(KEY_TASKS, flow));
    let isEndOfLine = false;
    const allInputs: HdesApi.AstFlowNode[] = Object.values(inputs.children);
    for (const input of allInputs) {
      if (this.isEndOfLine(input)) {
        isEndOfLine = true;
        break;
      }
      if (this.in(input)) {
        isAround = false;
      }
    }

    if (isAround || isEndOfLine) {
      this._result.push(this.ac().id("new input")
        .append(isEndOfLine)
        .addField("{name}", { indent: 2 })
        .addField("required", { indent: 4, value: "true" })
        .addField("type", { indent: 4, value: "STRING" })
        .addField("debugValue", { indent: 4, value: "\"test-string\"" })
        .build());
    }
  }

  private visitInput(flow: HdesApi.AstFlowRoot) {
    const inputs = flow.inputs;
    if (!inputs) {
      return;
    }
    const inputsSorted = Object.values(inputs).sort((v1, v2) => v1.start - v2.start);
    for (const input of inputsSorted) {


        this.visitInputRequired(input);
        this.visitInputType(input);
        this.visitDebugValue(input);
    }
  }

  private visitInputType(input: HdesApi.AstFlowInputNode) {
    if (input.type && this._nav.currentLine !== input.type.start) {
      return;
    }
    for (const type of TYPES) {
      this._result.push(this.ac().id("type: " + type).addField("type", { indent: 4, value: type }).build());
    }
  }

  private visitInputRequired(input: HdesApi.AstFlowInputNode) {
    if (input.required && this._nav.currentLine !== input.required.start) {
      return;
    }
    this._result.push(this.ac().id("required: true").addField("required", { indent: 4, value: "true" }).build());
    this._result.push(this.ac().id("required: false").addField("required", { indent: 4, value: "false" }).build());
  }

  private visitDebugValue(input: HdesApi.AstFlowInputNode) {
    if (input.debugValue) {
      return;
    }
    
    const builder = this.ac().id("debugValue");
    if (this.in(input)) {
      builder.addValue("").append(true);
    }
    this._result.push(builder.addField("debugValue", { indent: 4, value: "\"\"" }).build())
  }

  private visitInputs(flow: HdesApi.AstFlowRoot) {
    const node = this.get(KEY_INPUTS, flow);
    if (node) {
      return;
    }

    const AFTER = [KEY_ID, KEY_DESC];
    const after = AFTER
      .filter(name => this.hasNonNull(name, flow))
      .map(name => this.get(name, flow));
      
    if (!after.length || !this.isAfter(after)) {
      return;
    }
    this._result.push(this.ac().id("inputs block")
      .addField(KEY_INPUTS)
      .addField("myInputParam", { indent: 2 })
      .addField("required", { indent: 4, value: true })
      .addField("type", { indent: 4, value: "STRING" })
      .addField("debugValue", { indent: 4, value: "\"test-string\"" })
      .build());
  }

  private visitTasks(flow: HdesApi.AstFlowRoot) {
    if (this.get(KEY_TASKS, flow)) {
      return;
    }
    const inputs = this.get(KEY_INPUTS, flow);
    if (!inputs) {
      return;
    }
    if (!this.isAfter([inputs])) {
      return;
    }
    this._result.push(this.ac().id("tasks block").addField(KEY_TASKS).build());
  }

  private visitId(flow: HdesApi.AstFlowRoot) {
    const BEFORE = [KEY_DESC, KEY_INPUTS, KEY_TASKS];
    const node = flow.id;
    if (node != null) {
      return;
    }

    const before = BEFORE
      .filter(name => this.hasNonNull(name, flow))
      .map(name => this.get(name, flow));

    if (!this.isBefore(before)) {
      return;
    }
    this._result.push(this.ac().id("id").addField(KEY_ID).build());
  }

  private visitDesc(flow: HdesApi.AstFlowRoot) {
    const BEFORE = [KEY_INPUTS, KEY_TASKS];
    if (flow.description || !flow.id) {
      return;
    }

    const before = BEFORE
      .filter(name => this.hasNonNull(name, flow))
      .map(name => this.get(name, flow));
    if (!this.isBefore(before)) {
      return;
    }
    if (!this.isAfter([flow.id])) {
      return;
    }

    this._result.push(this.ac().id('description').addField(KEY_DESC).build());
  }

  private isEndOfLine(node: HdesApi.AstFlowNode) {
    const sameLine = node.end === this._nav.currentLine;

    if (!sameLine) {
      return false;
    }

    const last = Object.values(node.children).filter(v => v.end === node.end).reduce(v => v);
    if (!last) {
      return this._nav.currentColumn >= node.value.length;
    }
    return this._nav.currentColumn >= last.source.value.length
  }
  
  private in(node: { start: number, end: number}, endNode?: { start: number, end: number}) {

    let ending = node.end;
    if(endNode) {
      endNode.start - 1;
    }

    return this._nav.currentLine <= ending && this._nav.currentLine >= node.start;
  }

  private isBefore(nodes: (HdesApi.AstFlowNode | undefined | null)[]): boolean {
    for (const current of nodes) {
      if (!current) {
        continue;
      }
      if (this._nav.currentLine >= current.start) {
        return false;
      }
    }
    return true;
  }

  private isAfter(nodes: HdesApi.AstFlowNode[]): boolean {
    for (const current of nodes) {
      if (!(this._nav.currentLine > current.end)) {
        return false;
      }
    }
    return true;
  }
}