import { Fs } from '@dxs-ts/fs-api';
import { languages } from 'monaco-editor';
import { PrintoutPageContainer } from './PrintoutPageCompletionBuilder';

export class Hint_IncludeTemplate {
  static accept(container: PrintoutPageContainer): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];

    const pageProps = container.allProps[container.pageId] as Fs.PrintoutPageProps | undefined;
    if (!pageProps || !pageProps.templateIds) {
      return result;
    }

    const line = container.model.getLineContent(container.modelPosition.lineNumber);

    for (const [depPageId, props] of Object.entries(container.allProps)) {
      if (props.type !== 'PRINTOUT_PAGE' || depPageId === container.pageId) {
        continue;
      }

      const depPageProps = props as Fs.PrintoutPageProps;

      const serviceProps = container.allProps[depPageProps.serviceId] as Fs.PrintoutProps | undefined;
      if (!serviceProps) {
        continue;
      }

      const localeProps = container.allProps[depPageProps.localeId] as Fs.LanguageProps | undefined;
      const localeCode = localeProps ? localeProps.localeCode : '';
      const serviceName = serviceProps.printoutServiceName;
  

      const range = {
        startLineNumber: container.modelPosition.lineNumber,
        endLineNumber: container.modelPosition.lineNumber,
        startColumn: 1,
        endColumn: line.length + 1,
      };

      result.push({
        label: 'Insert page content',
        kind: languages.CompletionItemKind.File,
        insertText: '',
        documentation: `Service: ${serviceName}\nLocale: ${localeCode}\nTemplate resolved by name on backend`,
        range,
        filterText: line,
        command: { id: 'printout.openPageDialog', title: 'Insert page content' },
      });
    }

    return result;
  }
}
