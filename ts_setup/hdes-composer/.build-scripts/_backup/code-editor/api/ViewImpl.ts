import CodeMirror from 'codemirror';

import * as EditorAPI from './EditorAPI';
import ViewParser from './ViewParser';


interface Annotation {
  from: CodeMirror.Position;
  message?: string;
  severity?: string;
  to?: CodeMirror.Position;
}

class ViewImpl implements EditorAPI.View {
  private _editor: CodeMirror.Editor;
  private _events: EditorAPI.ViewEvents = {};

  constructor(area: React.RefObject<HTMLTextAreaElement>, props: EditorAPI.ViewProps) {
    if (!area.current) {
      throw new Error("codemirror ref is not initiated");
    }

    const editor = CodeMirror.fromTextArea(area.current, {
      lineNumbers: true,
      tabSize: 2,
      firstLineNumber: 0,
      theme: props.theme,
      extraKeys: {
        'Ctrl-Space': 'autocomplete'
      },
      scrollbarStyle: "simple",
      gutters: ['CodeMirror-lint-markers'],
      mode: props.mode,
      lint: {
        getAnnotations: (
          content: string, 
          updateLintingCallback, 
          options: any, 
          codeMirror: CodeMirror.Editor) => this.getAnnotations(content, updateLintingCallback, options, codeMirror),
        async: true
      },
      hintOptions: {
        completeSingle: false,
        hint: {
          resolve: (_cm: CodeMirror.Editor, pos: CodeMirror.Position): CodeMirror.HintFunction =>  
            (cm: CodeMirror.Editor, _options: CodeMirror.ShowHintOptions) => this.getHints(pos, cm.getLine(pos.line)) 
        },
      }
    });

    editor.on("changes", (editor: CodeMirror.Editor, changes: CodeMirror.EditorChange[]) => this.onChanges(editor, changes))
    editor.setValue(props.src);
    this._editor = editor;
  }

  getAnnotations(
    _content: string,
    updateLintingCallback: {
      (annotations: Annotation[]): void;
      (codeMirror: CodeMirror.Editor, annotations: Annotation[]): void;
    },
    _options: any,
    _codeMirror: CodeMirror.Editor,
  ): void {

    if (!this._events.lint) {
      return;
    }

    const annotations = this._events.lint().map(message => {
      const severity = message.type === 'ERROR' ? 'error' : 'warning'
      const start = message.range ? message.range.start : 0
      const end = message.range ? message.range.end : 0
      return {
        from: CodeMirror.Pos(message.line, start),
        to: CodeMirror.Pos(message.line, end),
        message: message.value,
        severity: severity
      }
    })
    updateLintingCallback(annotations);
  }

  getHints(pos: CodeMirror.Position, content: string): CodeMirror.Hints {
    if (!this._events.hint) {
      return { from: pos, to: pos, list: [] };
    }
    return this._events.hint(pos, content);
  }
  onChanges(editor: CodeMirror.Editor, changes: CodeMirror.EditorChange[]) {
    if (!this._events.onChanges) {
      return;
    }

    const commands = new ViewParser(editor, changes).visit();
    if (commands) {
      this._events.onChanges(commands, editor.getValue());
    }
  }
  withValue(value: string): EditorAPI.View {
    this._editor.setValue(value);
    return this;
  }
  withEvents(events: EditorAPI.ViewEvents): EditorAPI.View {
    this._events = {
      hint: events.hint ? events.hint : this._events?.hint,
      lint: events.lint ? events.lint : this._events?.lint,
      onChanges: events.onChanges ? events.onChanges : this._events?.onChanges
    };
    return this;
  }
}


export default ViewImpl;


