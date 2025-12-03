
import { Position, languages, editor, IRange } from 'monaco-editor';
import { EXTERNAL_DIALOG, FIELD, FlowAstAutocomplete, GuidedType } from './types';



export class AcBuilder {
  private _id?: string;
  private _value: string = '';
  private _append = false;
  private _guided: GuidedType | undefined;
  private _model: editor.ITextModel;
  private _position: Position;

  constructor(model: editor.ITextModel, position: Position) {
    this._model = model;
    this._position = position;
  }

  lineNumber(lineNumber: number): AcBuilder {
    this._position = this._position.with(lineNumber, this._position.column);
    return this;
  }

  id(id: string): AcBuilder {
    this._id = id;
    return this;
  }
  private getIndent(indent: number): string {
    var result = "";
    for (var index = 0; index < indent; index++) {
      result += " ";
    }
    return result;
  }
  append(append: boolean) {
    this._append = append;
    return this;
  }
  guided(guided: GuidedType) {
    this._guided = guided;
    return this;
  }
  addField(fieldName: string, props?: {
    indent?: number
    value?: any
  }) {
    const prefix = props?.indent ? this.getIndent(props.indent) : '';
    const sufix = props?.value ? ' ' + props.value : '';

    if(this._value) {
      this._value += '\r\n'
    }

    this._value += prefix + fieldName + FIELD + sufix;
    return this;
  }
  addValue(value: string) {
    this._value += value;
    return this;
  }
  build(): languages.CompletionItem {
    if (!this._id) {
      throw new Error("id must be defined!");
    }

    const line = 
      this._model.getLineContent(this._position.lineNumber);

    const range: IRange = {
      startLineNumber: this._position.lineNumber,
      endLineNumber: this._position.lineNumber,
      startColumn: 1,//1,
      endColumn: 1//line.length == 0 ? 1 : line.length,
    };

    const insertText = this._append ? line + '\r\n' + this._value : this._value;

    const autocomplete: FlowAstAutocomplete | undefined = this._guided ? {
      id: this._id,
      value: insertText,
      guided: this._guided,
      append: this._append,
      position: this._position,
      range
    } : undefined;

    return {
      label: this._id,
      kind: languages.CompletionItemKind.Function,
      insertText: this._guided ? '' : insertText,
      range: range,
      filterText: line,
      command: this._guided ? { 
        id: EXTERNAL_DIALOG, 
        title: this._guided,
        arguments: [{ autocomplete }],
      } : undefined
    }
  }
}
