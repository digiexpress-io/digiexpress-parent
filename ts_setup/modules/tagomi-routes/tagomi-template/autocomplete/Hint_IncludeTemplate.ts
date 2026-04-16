import { languages } from 'monaco-editor';
import { TagomiContainer } from './TagomiCompletionBuilder';

export class Hint_IncludeTemplate {
  static accept(container: TagomiContainer): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    
    const currentTemplate = container.site.templates[container.templateId];
    if (!currentTemplate || !currentTemplate.templateIds || currentTemplate.templateIds.length === 0) {
      return result;
    }

    const line = container.model.getLineContent(container.modelPosition.lineNumber);
    const trimmedLine = line.trim();
    
    if (!trimmedLine.startsWith('#include') && !trimmedLine.startsWith('#')) {
      return result;
    }

    for (const depTemplateId of currentTemplate.templateIds) {
      if (depTemplateId === container.templateId) {
        continue;
      }

      const depTemplate = container.site.templates[depTemplateId];
      if (!depTemplate) {
        continue;
      }

      const service = container.site.services[depTemplate.serviceId];
      if (!service) {
        continue;
      }

      const locale = container.site.locales[depTemplate.localeId];
      const localeCode = locale ? locale.localeCode : '';
      const serviceName = service.serviceName;
      const templateName = localeCode ? `${serviceName} - ${localeCode}` : serviceName;
      const fileName = `${templateName}.typ`;
      const localeInfo = locale ? ` (${localeCode})` : '';
      const label = `#include "${fileName}"`;

      const range = {
        startLineNumber: container.modelPosition.lineNumber,
        endLineNumber: container.modelPosition.lineNumber,
        startColumn: 1,
        endColumn: line.length + 1
      };

      result.push({
        label: label,
        kind: languages.CompletionItemKind.File,
        insertText: `#include "${fileName}"`,
        detail: `Include template: ${serviceName}${localeInfo}`,
        documentation: `Service: ${serviceName}\nLocale: ${localeCode}\nTemplate resolved by name on backend`,
        range: range,
        filterText: line
      });
    }

    return result;
  }
}
