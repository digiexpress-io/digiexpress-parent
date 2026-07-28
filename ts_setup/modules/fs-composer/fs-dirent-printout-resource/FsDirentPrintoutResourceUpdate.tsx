import React from 'react';
import { Typography, Button } from '@mui/material';
import { useIntl } from 'react-intl';
import MonacoReact from '@monaco-editor/react';
import { FsDirentFormField, FsDirentTextField } from '../fs-utilities';
import { useUtilityClasses, FsDirentPrintoutResourceRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentPrintoutResourceProps } from './FsDirentPrintoutResourceProps';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';

export const FsDirentPrintoutResourceUpdate: React.FC<FsDirentPrintoutResourceProps> = ({ direntId }) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState({ direntId });
  const classes = useUtilityClasses();
  const { getDirent, selectOptions } = useFsDirent();
  const fileInputRef = React.useRef<HTMLInputElement>(null);
  const [fileName, setFileName] = React.useState('');

  function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) {
      return;
    }
    setFileName(file.name);
    const reader = new FileReader();
    reader.onload = () => {
      ownerState.onChangeUploadBody(reader.result as string);
    };
    if (ownerState.contentType === 'image/*') {
      reader.readAsDataURL(file);
    } else {
      reader.readAsText(file);
    }
  }
  const resourceProps = getDirent(direntId)!.props as Fs.PrintoutResourceProps;
  const previewSrc = ownerState.uploadBody || resourceProps.content || undefined;

  const connectedPages = resourceProps.printoutPageIds.map(pageId => {
    const pageProps = selectOptions.direntProps[pageId] as Fs.PrintoutPageProps | undefined;
    const printoutName = pageProps
      ? (selectOptions.printouts.find(p => p.value === pageProps.serviceId)?.label ?? pageProps.serviceId)
      : pageId;
    const localeName = pageProps
      ? (selectOptions.languages.find(l => l.value === pageProps.localeId)?.label ?? pageProps.localeId)
      : pageId;
    return { id: pageId, label: `${printoutName} / ${localeName}` };
  });

  return (
    <FsDirentPrintoutResourceRoot className={classes.root}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.printoutResource.sectionTitle.edit' }, { name: ownerState.assetPath })}</Typography>
      <div className={classes.formContainer}>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.nameField.label' })}>
          <FsDirentTextField required placeholder={intl.formatMessage({ id: 'fs.dirent.printoutResource.nameField.placeholder' })}
            value={ownerState.resourceName}
            onChange={ownerState.onChangeResourceName}
          />
        </FsDirentFormField>


        {ownerState.contentType === 'text/*' ? (
          <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.printoutResource.uploadBodyFieldText.label' })}>
            <MonacoReact
              height='300px'
              defaultLanguage='yaml'
              options={{ wordBasedSuggestions: 'off', minimap: { enabled: false } }}
            />
          </FsDirentFormField>
        ) : (
            <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.printoutResource.uploadBodyField.label' })}>
              <input
                ref={fileInputRef}
                type='file'
                accept='image/*'
                style={{ display: 'none' }}
                onChange={handleFileChange}
              />
              <FsDirentTextField disabled value={fileName} placeholder={intl.formatMessage({ id: 'fs.dirent.printoutResource.uploadBodyField.placeholder' })} />
              <Button variant='text' size='small' className={classes.uploadButton} onClick={() => fileInputRef.current?.click()}>
                {intl.formatMessage({ id: 'fs.dirent.printoutResource.uploadBodyField.button' })}
              </Button>
          </FsDirentFormField>
        )}

        {ownerState.contentType === 'image/*' && previewSrc && (
          <FsDirentFormField label={intl.formatMessage({ id: 'fs.properties.propertyLabel.preview' })}>
            <img
              src={previewSrc}
              alt={ownerState.resourceName}
              style={{ maxWidth: '100%', maxHeight: '200px', objectFit: 'contain' }}
            />
          </FsDirentFormField>
        )}

        {ownerState.contentType === 'text/*' && previewSrc && (
          <FsDirentFormField label={intl.formatMessage({ id: 'fs.properties.propertyLabel.preview' })}>
            <pre style={{ margin: 0, overflowX: 'auto', whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>
              <code>{previewSrc}</code>
            </pre>
          </FsDirentFormField>
        )}

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.printoutResource.printoutPagesField.label' })}>
          {connectedPages.map(e => <Typography>{e.label}</Typography>)}
        </FsDirentFormField>

      </div>
    </FsDirentPrintoutResourceRoot>
  );
};
