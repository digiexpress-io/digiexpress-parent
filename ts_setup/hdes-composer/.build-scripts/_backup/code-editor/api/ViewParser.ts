import CodeMirror from 'codemirror';
import * as EditorAPI from './EditorAPI';

class ViewParser {
  private _editor: CodeMirror.Editor;
  private _changes: CodeMirror.EditorChange[];
  private _result: EditorAPI.ViewCommand[] = [];

  constructor(editor: CodeMirror.Editor, changes: CodeMirror.EditorChange[]) {
    this._editor = editor;
    this._changes = changes;
  }

  visit(): EditorAPI.ViewCommand[] | undefined {
    let init = false;
    for (let change of this._changes) {
      if (change.origin === 'setValue') {
        init = true;
        continue;
      }
      this.visitChange(change);
    }
    
    if(init && this._result.length === 0) {
      return undefined;
    }
    
    return this._result;
  }

  visitChange(change: CodeMirror.EditorChange) {
    //const start = { inclusive: change.from.sticky === 'after'}
    if (change.from.line === change.to.line && change.text.length === 1) {
      this.visitSetOneLine(change);
    } else {
      this.visitRemoveLines(change)
      this.visitSetManyLines(change)
    }
  }

  visitRemoveLines(change: CodeMirror.EditorChange) {
    if (!change.removed) {
      throw new Error("code editor removed lines error");
    };
    if(change.removed.length === 1) {
      return; 
    }
    const line = change.from.line;
    const id = line + 1;
    const to = id + change.removed.length -2;
    this._result.push({ id: id, type: 'DELETE', value: to + "" });
  }

  visitSetManyLines(change: CodeMirror.EditorChange) {
    const line = change.from.line;
    if(change.from.sticky !== "before") {
      this._result.push({ id: line, type: 'SET', value: this._editor.getLine(line) });
    }
    for (let index = 1; index < change.text.length; index++) {
      const id = line + index;
      this._result.push({ id: id, type: 'ADD', value: this._editor.getLine(id) });
    }
  }

  visitSetOneLine(change: CodeMirror.EditorChange) {
    const line = change.from.line;
    this._result.push({ id: line, type: 'SET', value: this._editor.getLine(line) });
  }

}

export default ViewParser;

