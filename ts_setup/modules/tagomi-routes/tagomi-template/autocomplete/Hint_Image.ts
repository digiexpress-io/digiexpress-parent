import { languages } from 'monaco-editor';
import { TagomiContainer } from './TagomiCompletionBuilder';

export class Hint_Image {
  static accept(container: TagomiContainer): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    
    const line = container.model.getLineContent(container.modelPosition.lineNumber);
    const trimmedLine = line.trim();
    
    if (!trimmedLine.startsWith('#image') && !trimmedLine.startsWith('#')) {
      return result;
    }

    const linkedImages = Object.values(container.site.resources).filter(
      r => r.contentType === 'image/*' && r.templateIds.includes(container.templateId)
    );

    if (linkedImages.length === 0) {
      return result;
    }

    for (const image of linkedImages) {
      const resourceName = image.resourceName;
      const label = `#image.decode(..., ${resourceName})`;

      const range = {
        startLineNumber: container.modelPosition.lineNumber,
        endLineNumber: container.modelPosition.lineNumber,
        startColumn: 1,
        endColumn: line.length + 1
      };

      result.push({
        label: label,
        kind: languages.CompletionItemKind.Value,
        insertText: `#image.decode(sys.inputs.resources.${resourceName}, width: 400pt)`,
        detail: `Image resource: ${resourceName}`,
        documentation: `Image: ${resourceName}\nResource ID: ${image.id}\nUsage: sys.inputs.resources.${resourceName}`,
        range: range,
        filterText: line
      });
    }

    return result;
  }
}
