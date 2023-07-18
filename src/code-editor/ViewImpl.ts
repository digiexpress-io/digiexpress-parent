import CodeMirror from 'codemirror';

interface Annotation {
  from: CodeMirror.Position;
  message?: string;
  severity?: string;
  to?: CodeMirror.Position;
}

interface ViewEvents {
  onChanges?: (content: string) => void;  
  lint?: () => LintMessage[];
  hint?: (props: HintProps) => CodeMirror.Hints;
}
type ViewLang = "yaml" | "groovy" | "json";
interface ViewProps {
  id: string;
  mode: ViewLang;
  src: string;
  onChange?: (newValue: string) => void;
  lint?: () => LintMessage[];
  hint?: (props: HintProps) => CodeMirror.Hints;
}
interface HintProps {pos: CodeMirror.Position, content: string, emptyLine: boolean, cm: CodeMirror.Editor};
interface LintMessage { line: number; value: string; type: "ERROR" | "WARNING"; range?: LintRange; }
interface LintRange { start: number; end: number; column?: number; insert?: boolean; }

interface View {
  id: string;
  withValue(value: string): View;
  withEvents(events: ViewEvents): View
}

class ViewImpl implements View {
  private _editor: CodeMirror.Editor;
  private _events: ViewEvents = {};
  private _id: string;

  constructor(area: React.RefObject<HTMLTextAreaElement>, props: ViewProps) {
    if (!area.current) {
      throw new Error("codemirror ref is not initiated");
    }
    
    const onChangeCallback = props.onChange;
    if(onChangeCallback) {
      this._events.onChanges = (content) => onChangeCallback(content)
    }
    const onLintCallback = props.lint;
    if(onLintCallback) {
      this._events.lint = () => onLintCallback();
    }
    if(props.hint) {
      this._events.hint = (hintProps: HintProps) => {
        if(props.hint) {
          return props.hint(hintProps);
        }
        return { from: hintProps.pos, to: hintProps.pos, list: [] };
      };
    }
    
    
    this._id = props.id;
    
    const editor = CodeMirror.fromTextArea(area.current, {
      lineNumbers: true,
      tabSize: 2,
      firstLineNumber: 0,
      theme: 'eclipse',
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


    editor.setValue(props.src);
    editor.on("changes", (editor: CodeMirror.Editor, changes: CodeMirror.EditorChange[]) => this.onChanges(editor, changes))
    this._editor = editor;
  }

  get id() {
    return this._id;
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
    
    const emptyLine: boolean = this._editor.getLine(pos.line).trim().length === 0;
    const cm: CodeMirror.Editor = this._editor;
    return this._events.hint({pos, content, emptyLine, cm});
  }
  onChanges(editor: CodeMirror.Editor, _changes: CodeMirror.EditorChange[]) {
    const callback = this._events.onChanges;
    if (!callback) {
      return;
    }
    callback(editor.getValue());
  }
  withValue(value: string): View {
    this._editor.setValue(value);
    return this;
  }
  withEvents(events: ViewEvents): View {
    this._events = {
      hint: events.hint ? events.hint : this._events?.hint,
      lint: events.lint ? events.lint : this._events?.lint,
      onChanges: events.onChanges ? events.onChanges : this._events?.onChanges
    };
    return this;
  }
}

const createView = (area: React.RefObject<HTMLTextAreaElement>, props: ViewProps): View => new ViewImpl(area, props);

export { createView }
export type {View, ViewProps, HintProps};

