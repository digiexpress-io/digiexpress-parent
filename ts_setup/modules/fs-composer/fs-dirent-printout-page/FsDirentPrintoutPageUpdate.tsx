import React from 'react';
import MonacoReact, { OnChange, OnMount, BeforeMount } from '@monaco-editor/react';
import { Dialog, DialogContent, DialogTitle, List, ListItemButton, ListItemText, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import * as monacoEditor from 'monaco-editor';
import { FsDirentFormField } from '../fs-utilities';
import { FsIcons } from '../fs-theme';
import { useUtilityClasses, FsDirentPrintoutPageRoot, FsDirentPrintoutPageDialogList } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentPrintoutPageProps } from './FsDirentPrintoutPageProps';
import { PrintoutPageCompletionBuilder } from './autocomplete';

const InsertImageDialog: React.FC<{
  open: boolean;
  direntId: string;
  images: Fs.PrintoutResourceProps[];
  onSelect: (resource: Fs.PrintoutResourceProps) => void;
  onClose: () => void;
}> = ({ open, direntId, images, onSelect, onClose }) => {
  const classes = useUtilityClasses();
  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Insert image</DialogTitle>
      <DialogContent>
        <FsDirentPrintoutPageDialogList>
          <List>
            {images.map(image => (
              <ListItemButton key={image.id} className={classes.dialogListItem} onClick={() => onSelect(image)}>
                <ListItemText primary={image.resourceName} />
                <div className={classes.dialogItemEnd}>
                  {image.printoutPageIds.includes(direntId) && (
                    <FsIcons.Checkmark className={classes.dialogCheckmark} />
                  )}
                  {image.content && (
                    <img
                      className={classes.dialogThumbnail}
                      src={`data:${image.contentType === 'image/*' ? 'image/png' : image.contentType};base64,${image.content}`}
                    />
                  )}
                </div>
              </ListItemButton>
            ))}
          </List>
        </FsDirentPrintoutPageDialogList>
      </DialogContent>
    </Dialog>
  );
};

export const FsDirentPrintoutPageUpdate: React.FC<FsDirentPrintoutPageProps> = ({ direntId }) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState({ direntId });
  const classes = useUtilityClasses();
  const { selectOptions, getDirent } = useFsDirent();

  const [dialogOpen, setDialogOpen] = React.useState(false);
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

  const handleChange: OnChange = (value) => {
    ownerState.onChangeContent(value ?? '');
  };

  const beforeMount: BeforeMount = React.useCallback((monaco) => {
    completionDisposable.current?.dispose();
    (monaco.editor as any).addCommand({ id: 'printout.openImageDialog', run: () => setDialogOpen(true) });
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
      text: `#image(sys.inputs.resources.at("${resource.resourceName}"), width: 200pt)`,
    }]);
    setDialogOpen(false);
  }, []);

  return (
    <FsDirentPrintoutPageRoot className={classes.root}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.printoutPage.sectionTitle.edit' }, { name: ownerState.assetPath })}</Typography>
      <InsertImageDialog
        open={dialogOpen}
        direntId={direntId}
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
