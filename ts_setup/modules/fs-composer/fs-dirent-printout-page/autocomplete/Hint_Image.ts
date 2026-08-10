import { Fs } from '@dxs-ts/fs-api';
import { languages } from 'monaco-editor';
import { PrintoutPageContainer } from './PrintoutPageCompletionBuilder';

export class Hint_Image {
  static accept(container: PrintoutPageContainer): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];

    const line = container.model.getLineContent(container.modelPosition.lineNumber);

  

    const linkedImages = Object.values(container.allProps)
      .filter((p): p is Fs.PrintoutResourceProps => p.type === 'PRINTOUT_RESOURCE')
      .filter(p => p.contentType === 'image/*');

    for (const image of linkedImages) {
      const range = {
        startLineNumber: container.modelPosition.lineNumber,
        endLineNumber: container.modelPosition.lineNumber,
        startColumn: 1,
        endColumn: line.length + 1,
      };

      result.push({
        label: `#image(resources.at("${image.resourceName}"), ...)`,
        kind: languages.CompletionItemKind.Value,
        insertText: `#image(sys.inputs.resources.at("${image.resourceName}"), width: 400pt)`,
        detail: `Image resource: ${image.resourceName}`,
        documentation: `Image: ${image.resourceName}\nResource ID: ${image.id}\nUsage: sys.inputs.resources.${image.resourceName}`,
        range,
        filterText: line,
      });
    }

    return result;
  }
}
