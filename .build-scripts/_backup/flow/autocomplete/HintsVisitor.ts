import { Hdes } from '../deps';

type HelperType = "ADD_INPUT" | "ADD_DT" | "ADD_FT" | "ADD_SWITCH";
type Helper = (context: HelperContext) => void;
  
interface HelperContext {
  type: HelperType,
  cm: CodeMirror.Editor,
  data: CodeMirror.Hints,
  cur: CodeMirror.Hint,
  pos: CodeMirror.Position,
  src: Hdes.AstAPI.FlAutocomplete
}


interface HintsProps {
  pos: CodeMirror.Position,
  content: string,
  fl: Hdes.AstAPI.Fl,
  helper: Helper;
};

const INPUT = 'InputAutocomplete'
const TASK = 'TaskAutocomplete'
const SWITCH = 'SwitchAutocomplete'
const INPUT_MAPPING = 'TaskInputMappingAutocomplete'


class HintsVisitor {
  private _pos: CodeMirror.Position;
  private _fl: Hdes.AstAPI.Fl;
  private _hints: CodeMirror.Hint[] = [];
  private _empty: boolean;
  private _helper: Helper;

  constructor(props: HintsProps) {
    this._fl = props.fl;
    this._pos = props.pos;
    this._empty = props.content.trim().length === 0;
    this._helper = props.helper;
  }

  visit(): CodeMirror.Hints {
    //    console.log(this._fl.autocomplete, this._pos);

    for (const autocomplete of this._fl.autocomplete) {
      if(!autocomplete) {
        continue; // ? bug
      }
      if (this.visitRange(autocomplete)) {
        this.visitHint(autocomplete);
      }
    }
    this._hints.sort((a, b) => (a.displayText as string).localeCompare((b.displayText as string)))
    return {
      from: this._pos,
      to: this._pos,
      list: this._hints
    }
  }

  visitHint(src: Hdes.AstAPI.FlAutocomplete) {
    if (TASK === src.id) {
      this.visitTask(src);
    } else if (INPUT === src.id) {
      this.visitInput(src);
    } else if (SWITCH === src.id) {
      this.visitSwitch(src);
    } else if (INPUT_MAPPING === src.id) {
      this.visitMapping(src);
    } else {
      this._hints.push({ text: src.value[0], displayText: src.value[0] })
    }
  }

  visitTask(src: Hdes.AstAPI.FlAutocomplete) {
    this._hints.push({
      text: src.value[0],
      displayText: 'new DT task',
      hint: (cm, self, data) => {
        this._helper({type: "ADD_DT", cm: cm, data: self, cur: data, pos: this._pos, src: src });
      }
    })

    this._hints.push({
      text: src.value[0],
      displayText: 'new SERVICE task',
      hint: (cm, self, data) => {
        this._helper({type: "ADD_FT", cm: cm, data: self, cur: data, pos: this._pos, src: src });
      }
    })
  }

  visitInput(src: Hdes.AstAPI.FlAutocomplete) {
    this._hints.push({
      text: src.value[0],
      displayText: 'new INPUT',
      hint: (cm, self, data) => {
        this._helper({type: "ADD_INPUT", cm: cm, data: self, cur: data, pos: this._pos, src: src });
      }
    })
  }

  visitSwitch(src: Hdes.AstAPI.FlAutocomplete) {
    this._hints.push({
      text: src.value[0],
      displayText: 'new SWITCH task',
      hint: (cm, self, data) => {
        this._helper({type: "ADD_SWITCH", cm: cm, data: self, cur: data, pos: this._pos, src: src });
      }
    })

  }

  visitMapping(src: Hdes.AstAPI.FlAutocomplete) {
    for (let value of src.value) {
      this._hints.push({
        text: value,
        displayText: value,
        hint: (editor, _self, _data) => {
          const doc = editor.getDoc()
          const cursor = doc.getCursor()
          const content = editor.getLine(cursor.line)
          const autocompleteLines = ' ' + value
          const range = src.range.filter(r => r.start <= cursor.line && r.end >= cursor.line)[0]

          doc.replaceRange(autocompleteLines,
            { line: cursor.line, ch: range.column ? range.column : 0 },
            { line: cursor.line, ch: content.length }, '+input')
        }
      })
    }
  }

  visitRange(autocomplete: Hdes.AstAPI.FlAutocomplete) {
    const line = this._pos.line;
    if(!autocomplete.range) {
      console.error(autocomplete);
      return false;
    }
    for (let range of autocomplete.range) {

      // start is always defined
      if (range.start > line) {
        continue;
      }
      // end defined
      if (!range.insert !== false && range.end >= line) {
        return true
      } else if (this._empty && range.insert !== false && range.end + 1 >= line) {
        return true
      }

    }
  }

}

export type { Helper, HelperType, HelperContext };
export const createHints = (props: HintsProps) => new HintsVisitor(props).visit();