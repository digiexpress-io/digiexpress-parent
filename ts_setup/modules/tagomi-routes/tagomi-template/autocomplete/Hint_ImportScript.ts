import { languages } from 'monaco-editor';
import { TagomiContainer } from './TagomiCompletionBuilder';

export class Hint_ImportScript {
  static accept(container: TagomiContainer): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    
    const line = container.model.getLineContent(container.modelPosition.lineNumber);
    const trimmedLine = line.trim();
    
    if (!trimmedLine.startsWith('#import') && !trimmedLine.startsWith('#')) {
      return result;
    }

    const linkedScripts = Object.values(container.site.resources).filter(
      r => r.contentType === 'text/*' && r.templateIds.includes(container.templateId)
    );

    if (linkedScripts.length === 0) {
      return result;
    }

    for (const script of linkedScripts) {
      const fileName = `${script.resourceName}.typ`;
      const label = `#import "${fileName}"`;

      const range = {
        startLineNumber: container.modelPosition.lineNumber,
        endLineNumber: container.modelPosition.lineNumber,
        startColumn: 1,
        endColumn: line.length + 1
      };

      result.push({
        label: label,
        kind: languages.CompletionItemKind.File,
        insertText: `#import "${fileName}"`,
        detail: `Import script: ${script.resourceName}`,
        documentation: `Script resource: ${script.resourceName}\nResource ID: ${script.id}`,
        range: range,
        filterText: line
      });
    }

    return result;
  }
}
