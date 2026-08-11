import { languages } from 'monaco-editor';
import { PrintoutPageContainer } from './PrintoutPageCompletionBuilder';

export class Hint_Image {
  static accept(container: PrintoutPageContainer): languages.CompletionItem[] {
    return [{
      label: 'Insert image',
      kind: languages.CompletionItemKind.Function,
      insertText: '',
      detail: 'Open image picker dialog',
      range: {
        startLineNumber: container.modelPosition.lineNumber,
        startColumn: container.modelPosition.column,
        endLineNumber: container.modelPosition.lineNumber,
        endColumn: container.modelPosition.column,
      },
      command: { id: 'printout.openImageDialog', title: 'Insert image' },
    }];
  }
}
