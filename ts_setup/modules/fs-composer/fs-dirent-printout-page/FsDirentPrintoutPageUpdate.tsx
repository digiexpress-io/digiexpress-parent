import React from 'react';
import MonacoReact, { OnChange, OnMount, BeforeMount } from '@monaco-editor/react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import * as monacoEditor from 'monaco-editor';
import { FsDirentFormField } from '../fs-utilities';
import { useUtilityClasses, FsDirentPrintoutPageRoot } from './useUtilityClasses';
import { PageOption, useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentPrintoutPageProps } from './FsDirentPrintoutPageProps';
import { PrintoutPageCompletionBuilder } from './autocomplete';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { InsertPageDialog } from './InsertPageDialog';
import { InsertImageDialog } from './InsertImageDialog';




export const FsDirentPrintoutPageUpdate: React.FC<FsDirentPrintoutPageProps> = ({ direntId }) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState({ direntId });
  const classes = useUtilityClasses();
  const { selectOptions, getDirent } = useFsDirent();

  const [imageDialogOpen, setImageDialogOpen] = React.useState(false);
  const [pageDialogOpen, setPageDialogOpen] = React.useState(false);
  const editorRef = React.useRef<monacoEditor.editor.IStandaloneCodeEditor | undefined>(undefined);
  const completionDisposable = React.useRef<monacoEditor.IDisposable | undefined>(undefined);

  React.useEffect(() => {
    return () => {
      completionDisposable.current?.dispose();
    };
  }, []);

  const imageResources = React.useMemo(() =>
    Object.values(selectOptions.direntProps)
      .filter((p): p is Fs.PrintoutResourceProps => p.type === 'PRINTOUT_RESOURCE' && p.contentType === 'image/*'),
    [selectOptions.direntProps]
  );

  const allPageOptions = React.useMemo((): PageOption[] => {
    const result: PageOption[] = [];
    for (const [id, props] of Object.entries(selectOptions.direntProps)) {
      if (props.type !== 'PRINTOUT_PAGE' || id === direntId) {
        continue;
      }
      const page = props as Fs.PrintoutPageProps;
      const serviceProps = selectOptions.direntProps[page.serviceId] as Fs.PrintoutProps | undefined;
      const localeProps = selectOptions.direntProps[page.localeId] as Fs.LanguageProps | undefined;
      if (!serviceProps) {
        continue;
      }
      const serviceName = serviceProps.printoutServiceName;
      const localeCode = localeProps ? localeProps.localeCode : '';
      const templateName = localeCode ? `${serviceName} - ${localeCode}` : serviceName;
      result.push({ id, templateName });
    }
    return result;
  }, [direntId, selectOptions.direntProps]);

  const handleChange: OnChange = (value) => {
    ownerState.onChangeContent(value ?? '');
  };

  const beforeMount: BeforeMount = React.useCallback((monaco) => {
    completionDisposable.current?.dispose();
    (monaco.editor as any).addCommand({ id: 'printout.openImageDialog', run: () => setImageDialogOpen(true) });
    (monaco.editor as any).addCommand({ id: 'printout.openPageDialog', run: () => setPageDialogOpen(true) });
    const pageProps = getDirent(direntId)?.props as Fs.PrintoutPageProps | undefined;
    completionDisposable.current = monaco.languages.registerCompletionItemProvider('yaml', {
      provideCompletionItems(model, position) {
        if (!pageProps) {
          return { suggestions: [] };
        }
        return {
          suggestions: new PrintoutPageCompletionBuilder()
            .withPageId(direntId)
            .withPageProps(pageProps)
            .withAllProps(selectOptions.direntProps)
            .withModel(model)
            .withPosition(position)
            .build(),
        };
      },
    });
  }, [direntId, getDirent, selectOptions.direntProps]);

  const onMount: OnMount = React.useCallback((editor) => {
    editorRef.current = editor;
    editor.addAction({
      id: 'printout.insertImage',
      label: 'Insert image',
      contextMenuGroupId: 'navigation',
      contextMenuOrder: 1,
      run: (ed) => ed.trigger('', 'printout.openImageDialog', {}),
    });
    editor.addAction({
      id: 'printout.insertContent',
      label: 'Insert content',
      contextMenuGroupId: 'navigation',
      contextMenuOrder: 2,
      run: (ed) => ed.trigger('', 'printout.openPageDialog', {}),
    });
  }, []);

  const handlePageSelect = React.useCallback((page: PageOption) => {
    const editor = editorRef.current;
    if (!editor) {
      return;
    }
    const position = editor.getPosition();
    if (!position) {
      return;
    }
    const pageProps = selectOptions.direntProps[page.id] as Fs.PrintoutPageProps | undefined;
    const content = pageProps?.content ?? '';
    editor.executeEdits('', [{
      range: {
        startLineNumber: position.lineNumber,
        startColumn: position.column,
        endLineNumber: position.lineNumber,
        endColumn: position.column,
      },
      text: content,
    }]);
    setPageDialogOpen(false);
  }, [selectOptions.direntProps]);

  const handleImageSelect = React.useCallback((resource: Fs.PrintoutResourceProps) => {
    const editor = editorRef.current;
    if (!editor) {
      return;
    }
    const position = editor.getPosition();
    if (!position) {
      return;
    }
    editor.executeEdits('', [{
      range: {
        startLineNumber: position.lineNumber,
        startColumn: position.column,
        endLineNumber: position.lineNumber,
        endColumn: position.column,
      },
      text: `#image(sys.inputs.resources.at("${resource.resourceName}"), width: 200pt)`,
    }]);
    setImageDialogOpen(false);
  }, []);

  return (
    <FsDirentPrintoutPageRoot className={classes.root}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.printoutPage.sectionTitle.edit' }, { name: ownerState.assetPath })}</Typography>
      <InsertImageDialog
        open={imageDialogOpen}
        direntId={direntId}
        images={imageResources}
        onSelect={handleImageSelect}
        onClose={() => setImageDialogOpen(false)}
      />
      <InsertPageDialog
        open={pageDialogOpen}
        pages={allPageOptions}
        currentTemplateIds={(getDirent(direntId)?.props as Fs.PrintoutPageProps)?.templateIds ?? []}
        onSelect={handlePageSelect}
        onClose={() => setPageDialogOpen(false)}
      />
      <div className={classes.formContainer}>
        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.printoutPage.contentField.label' })}>
          <MonacoReact
            height='100vh'
            value={ownerState.content}
            defaultLanguage='yaml'
            theme={'vs'}
            beforeMount={beforeMount}
            onMount={onMount}
            onChange={handleChange}
            options={{
              wordBasedSuggestions: 'off',
              minimap: { enabled: false },
            }}
          />
        </FsDirentFormField>
      </div>
    </FsDirentPrintoutPageRoot>
  );
};
