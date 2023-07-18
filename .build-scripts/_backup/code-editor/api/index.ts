import { View, ViewCommand, ViewProps, LintMessage, LintRange, ViewEvents, ViewMode, ViewLang } from './EditorAPI';
import ViewImpl from './ViewImpl';
import OptimizeImpl from './ViewOptimize';
import ViewParser from './ViewParser';
import CodeMirror from 'codemirror';

declare namespace API {
  export type { View, ViewCommand, ViewProps, LintMessage, LintRange, ViewEvents, ViewMode, ViewLang };
  export type { CodeMirror };
}

namespace API {
  export const create = (ref: React.RefObject<HTMLTextAreaElement>, props: ViewProps): View => new ViewImpl(ref, props);
  export const parse = (editor: CodeMirror.Editor, changes: CodeMirror.EditorChange[]): ViewCommand[] | undefined => {
    return new ViewParser(editor, changes).visit();
  }
  export const optimize = (values1: ViewCommand[], values2?: ViewCommand[]): ViewCommand[] => OptimizeImpl(values1, values2);
}

export default API;