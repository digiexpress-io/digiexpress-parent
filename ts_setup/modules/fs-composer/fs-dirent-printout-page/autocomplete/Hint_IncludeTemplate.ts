import { languages } from 'monaco-editor';
import { PrintoutPageContainer } from './PrintoutPageCompletionBuilder';

export class Hint_IncludeTemplate {
  static accept(container: PrintoutPageContainer): languages.CompletionItem[] {
    return [{
      label: 'Insert page content',
      kind: languages.CompletionItemKind.Function,
      insertText: '',
      detail: 'Open page picker dialog',
      range: {
        startLineNumber: container.modelPosition.lineNumber,
        startColumn: container.modelPosition.column,
        endLineNumber: container.modelPosition.lineNumber,
        endColumn: container.modelPosition.column,
      },
      command: { id: 'printout.openPageDialog', title: 'Insert page content' },
    }];
  }
}
