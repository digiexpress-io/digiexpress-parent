import React from 'react';
import MonacoReact, { OnChange, OnMount, BeforeMount } from '@monaco-editor/react';
import { Dialog, DialogContent, DialogTitle, List, ListItemButton, ListItemText, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import * as monacoEditor from 'monaco-editor';
import { FsDirentFormField } from '../fs-utilities';
import { useUtilityClasses, FsDirentPrintoutPageRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentPrintoutPageProps } from './FsDirentPrintoutPageProps';

const _InsertImageDialog: React.FC<{
  open: boolean;
  images: Fs.PrintoutResourceProps[];
  onSelect: (resource: Fs.PrintoutResourceProps) => void;
  onClose: () => void;
}> = ({ open, images, onSelect, onClose }) => {
  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Insert image</DialogTitle>
      <DialogContent>
        <List>
          {images.map(image => (
            <ListItemButton key={image.id} onClick={() => onSelect(image)}>
              <ListItemText primary={image.resourceName} />
            </ListItemButton>
          ))}
        </List>
      </DialogContent>
    </Dialog>
  );
};

export const FsDirentPrintoutPageUpdate: React.FC<FsDirentPrintoutPageProps> = ({ direntId }) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState({ direntId });
  const classes = useUtilityClasses();
  const { selectOptions } = useFsDirent();

  const [dialogOpen, setDialogOpen] = React.useState(false);
  const editorRef = React.useRef<monacoEditor.editor.IStandaloneCodeEditor | undefined>(undefined);

  const imageResources = React.useMemo(() =>
    Object.values(selectOptions.direntProps)
      .filter((p): p is Fs.PrintoutResourceProps => p.type === 'PRINTOUT_RESOURCE' && p.contentType === 'image/*'),
    [selectOptions.direntProps]
  );

  const handleChange: OnChange = (value) => {
    ownerState.onChangeContent(value ?? '');
  };

  const beforeMount: BeforeMount = React.useCallback((monaco) => {
    monaco.editor.addCommand({
      id: 'printout.openImageDialog',
      run: () => setDialogOpen(true),
    });
    monaco.languages.registerCompletionItemProvider('yaml', {
      provideCompletionItems(_model, position) {
        return {
          suggestions: [{
            label: 'Insert image',
            kind: monaco.languages.CompletionItemKind.Function,
            insertText: '',
            range: {
              startLineNumber: position.lineNumber,
              startColumn: position.column,
              endLineNumber: position.lineNumber,
              endColumn: position.column,
            },
            command: { id: 'printout.openImageDialog', title: 'Insert image' },
          }],
        };
      },
    });
  }, []);

  const onMount: OnMount = React.useCallback((editor) => {
    editorRef.current = editor;
    editor.addAction({
      id: 'printout.insertImage',
      label: 'Insert image',
      contextMenuGroupId: 'navigation',
      contextMenuOrder: 1,
      run: (ed) => ed.trigger('', 'printout.openImageDialog', {}),
    });
  }, []);

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
      text: `#image(sys.inputs.resources.at("${resource.resourceName}"), width: 400pt)`,
    }]);
    setDialogOpen(false);
  }, []);

  return (
    <FsDirentPrintoutPageRoot className={classes.root}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.printoutPage.sectionTitle.edit' }, { name: ownerState.assetPath })}</Typography>
      <_InsertImageDialog
        open={dialogOpen}
        images={imageResources}
        onSelect={handleImageSelect}
        onClose={() => setDialogOpen(false)}
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
