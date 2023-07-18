import { Hdes } from '../deps'

declare namespace Session {
  
  interface ContextType {
    session: Instance;
    actions: Actions;
    service: Hdes.Service;
    theme: 'light' | 'dark';
  }
    
  interface Instance {  
    models: Hdes.ModelAPI.Models;
    errors: Hdes.ModelAPI.ServerError[];
    editor: Editor;
    getEditor(id: string): EditorContent | undefined;
    getModel(id: string): Hdes.ModelAPI.Model;
  }
  
  interface Editor {
    contents: EditorContent[];
    getContent(id: string): EditorContent | undefined; 
  }
  
  interface EditorContent {
    id: string;
    origin: Hdes.ResourceAPI.Asset;
    start: (Hdes.AstAPI.FlCommand | Hdes.AstAPI.FtCommand | Hdes.AstAPI.DtCommand)[]
    
    saved: boolean;       // is the resource saved
    content?: (Hdes.AstAPI.FlCommand | Hdes.AstAPI.FtCommand | Hdes.AstAPI.DtCommand)[];     // user changes
    body?: string;        // src + user changes
    errors: EditorError[];
  }
  
  interface EditorError {
    id: string;
    msg: string;
  }

  interface EditorContentMutator extends EditorContent {
    withSaved(value: boolean): EditorContentMutator;
    withErrors(errors: EditorError[]): EditorContentMutator;
    withContent(content: (Hdes.AstAPI.FlCommand | Hdes.AstAPI.FtCommand | Hdes.AstAPI.DtCommand)[]): EditorContentMutator;
  }
  
  interface EditorMutator extends Editor {
    withSaved(id: string, value: boolean): EditorMutator;
    withErrors(id: string, errors: EditorError[]): EditorMutator;
    withContent(content: Hdes.ResourceAPI.Asset): EditorMutator;
    withContentChange(id: string, content: (Hdes.AstAPI.FlCommand | Hdes.AstAPI.FtCommand | Hdes.AstAPI.DtCommand)[]): EditorMutator;
    withoutContent(id: string): EditorMutator;
  }
  
  interface InstanceMutator extends Instance {
    editor: EditorMutator;
    
    withModels(models: Hdes.ModelAPI.Models): InstanceMutator;
    withErrors(errors: Hdes.ModelAPI.ServerError[]): InstanceMutator;
    withEditor(editor: Editor): InstanceMutator;
  }
  
  interface Actions {
    setReload(): Promise<void>;
    getModels(): Promise<void>;
    getAsset(id: string): Promise<void>;
    copyAsset(props: {from: string, to: string}): Promise<Hdes.ResourceAPI.Asset>;
    createAsset(props: {name: string, serviceType: Hdes.ModelAPI.ServiceType}): Promise<Hdes.ResourceAPI.Asset>;
    saveAsset(asset: Hdes.ResourceAPI.Asset, body: string): Promise<void>;
    setEditor(props: {id: string, saved?: boolean, errors?: Session.EditorError[], content?: (Hdes.AstAPI.FlCommand | Hdes.AstAPI.FtCommand | Hdes.AstAPI.DtCommand)[] }): Promise<void>;
  }
}

export default Session;

