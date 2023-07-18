import Session from './Session';
import { Hdes, CodeEditor } from '../deps';


const parseModels = (models?: Hdes.ModelAPI.Models): Hdes.ModelAPI.Models => {

  const flow: Hdes.ModelAPI.Model[] = models?.FLOW ? models?.FLOW : [];
  const task: Hdes.ModelAPI.Model[] = models?.FLOW_TASK ? models?.FLOW_TASK : [];
  const dt: Hdes.ModelAPI.Model[] = models?.DT ? models?.DT : [];
  const tag: Hdes.ModelAPI.Model[] = models?.TAG ? models?.TAG : [];

  return {
    "FLOW": flow,
    "FLOW_TASK": task,
    "DT": dt,
    "TAG": tag
  };
}

class EditorData implements Session.EditorMutator {
  private _contents: Record<string, Session.EditorContentMutator>;

  constructor(contents?: Record<string, Session.EditorContentMutator>) {
    this._contents = contents ? contents : {};
  }

  next(id: string, builder: (mutator: Session.EditorContentMutator) => Session.EditorContentMutator) {
    const content = this._contents[id];
    if (!content) {
      throw new Error(`Editor content can't be saved because there is no content with id: '${id}'!`);
    }
    const newContent = builder(content);
    const contents: Record<string, Session.EditorContentMutator> = Object.assign({}, this._contents);
    contents[newContent.id] = newContent;
    return new EditorData(contents);
  }
  get contents() {
    return Object.values(this._contents);
  }
  getContent(id: string): Session.EditorContent {
    return this._contents[id];
  }
  withSaved(id: string, value: boolean): Session.EditorMutator {
    return this.next(id, (content) => content.withSaved(value));
  }
  withErrors(id: string, errors: Session.EditorError[]) {
    return this.next(id, (content) => content.withErrors(errors));
  }
  withContent(asset: Hdes.ResourceAPI.Asset) {
    let start: (Hdes.AstAPI.FlCommand | Hdes.AstAPI.FtCommand | Hdes.AstAPI.DtCommand)[] = [];
    if (asset.type === "FLOW" || asset.type === "FLOW_TASK") {
      start = JSON.parse(asset.content);
    } else if(asset.type === "DT") {
      start = JSON.parse(asset.content);
    } else {
      start = []; 
    }
    
    const newContent = new EditorContentMutatorData(asset, start, {});
    const contents: Record<string, Session.EditorContentMutator> = Object.assign({}, this._contents);
    contents[newContent.id] = newContent;
    return new EditorData(contents);
  }
  withContentChange(id: string, newContent: (Hdes.AstAPI.FlCommand | Hdes.AstAPI.FtCommand | Hdes.AstAPI.DtCommand)[]) {
    const origin = this.getContent(id).origin;
    const session = newContent;
    
    if (origin.type === "FLOW" || origin.type === "FLOW_TASK") {
      return this.next(id, (content) => {
        const previous = content.content ? content.content : [];
        const newContent = CodeEditor.API.optimize(previous as any, session as any);
        return content.withSaved(false).withContent(newContent);
      });
    }
    return this.next(id, (content) => {
      const previous = content.content ? content.content : [];
      const newContent = [...previous as any, ...session as any];
      return content.withSaved(false).withContent(newContent);
      
    });
  }
  withoutContent(id: string) {
    const contents: Record<string, Session.EditorContentMutator> = {};
    for (let v of Object.values(this._contents)) {
      if (v.id === id) {
        continue;
      }
      contents[v.id] = v;
    }
    return new EditorData(contents);
  }
}

class EditorContentMutatorData implements Session.EditorContentMutator {
  private _id: string;
  private _start: (Hdes.AstAPI.FlCommand | Hdes.AstAPI.FtCommand | Hdes.AstAPI.DtCommand)[];
  private _origin: Hdes.ResourceAPI.Asset;
  private _saved: boolean;
  private _content: (Hdes.AstAPI.FlCommand | Hdes.AstAPI.FtCommand | Hdes.AstAPI.DtCommand)[] | undefined;
  private _errors: Session.EditorError[];

  constructor(
    asset: Hdes.ResourceAPI.Asset,
    start: (Hdes.AstAPI.FlCommand | Hdes.AstAPI.FtCommand | Hdes.AstAPI.DtCommand)[],
    props: {
      saved?: boolean;
      content?: (Hdes.AstAPI.FlCommand | Hdes.AstAPI.FtCommand | Hdes.AstAPI.DtCommand)[],
      errors?: Session.EditorError[];
    }) {
    this._id = asset.id;
    this._start = start;
    this._origin = asset;
    this._saved = props.saved !== undefined ? props.saved : true;
    this._content = props.content !== undefined ? props.content : undefined;
    this._errors = props.errors !== undefined ? props.errors : asset.errors.map(e => ({ id: e.id, msg: e.message }));
  }
  get id() {
    return this._id;
  }
  get start() {
    return this._start;
  }
  get origin() {
    return this._origin;
  }
  get saved() {
    return this._saved;
  }
  get content() {
    return this._content;
  }
  get errors() {
    return this._errors;
  }
  get body() {
    if (!this._content) {
      return undefined;
    }
    const src: any[] = this._start;
    const changes: any[] = this._content;
    
    const result = {
      id: this._origin.id,
      type: this._origin.type,
      name: this._origin.name,
      messages: [],
      content: JSON.stringify([...src, ...changes])
    }
    return JSON.stringify(result);
  }

  withSaved(value: boolean) {
    return new EditorContentMutatorData(this._origin, this._start, {
      content: this._content,
      errors: this._errors,
      saved: value
    })
  }
  withErrors(errors: Session.EditorError[]) {
    return new EditorContentMutatorData(this._origin, this._start, {
      content: this._content,
      errors: errors,
      saved: this._saved
    })
  }
  withContent(content: (Hdes.AstAPI.FlCommand | Hdes.AstAPI.FtCommand | Hdes.AstAPI.DtCommand)[]) {
    return new EditorContentMutatorData(this._origin, this._start, {
      content: content,
      errors: this._errors,
      saved: this._saved
    })
  }
}

class ResourceData implements Session.InstanceMutator {
  private _models: Hdes.ModelAPI.Models;
  private _modelsById: Record<string, Hdes.ModelAPI.Model>;
  private _errors: Hdes.ModelAPI.ServerError[];
  private _editor: Session.EditorMutator;

  constructor(props: {
    models?: Hdes.ModelAPI.Models,
    errors?: Hdes.ModelAPI.ServerError[],
    editor?: Session.EditorMutator,
  }) {

    this._models = parseModels(props.models ? props.models : undefined);
    this._modelsById = {};
    this._errors = props.errors ? props.errors : [];
    this._editor = props.editor ? props.editor : new EditorData({});

    if (props.models) {
      for (const models of Object.values(props.models)) {
        for (const model of models) {
          this._modelsById[model.id] = model;
        }
      }
    }
  }

  getModel(id: string) {
    return this._modelsById[id];
  }
  getEditor(id: string) {
    return this._editor.getContent(id);
  }
  get editor(): Session.EditorMutator {
    return this._editor;
  }
  withEditor(editor: Session.EditorMutator) {
    return new ResourceData({ models: this._models, errors: this._errors, editor })
  }
  get models() {
    return this._models;
  }
  withModels(models?: Hdes.ModelAPI.Models): Session.InstanceMutator {
    return new ResourceData({ models, errors: this._errors })
  }
  get errors() {
    return this._errors;
  }
  withErrors(errors?: Hdes.ModelAPI.ServerError[]): Session.InstanceMutator {
    return new ResourceData({ models: this._models, errors })
  }
}

export default ResourceData;
