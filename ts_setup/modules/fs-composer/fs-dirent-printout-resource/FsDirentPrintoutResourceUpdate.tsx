import React from 'react';
import { Typography, Button } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentFormField, FsDirentSelectMulti, FsDirentTextField } from '../fs-utilities';
import { useUtilityClasses, FsDirentPrintoutResourceRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentPrintoutResourceProps } from './FsDirentPrintoutResourceProps';

export const FsDirentPrintoutResourceUpdate: React.FC<FsDirentPrintoutResourceProps> = ({ direntId }) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState({ direntId });
  const classes = useUtilityClasses();
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

  const previewSrc = ownerState.uploadBody || undefined;

  return (
    <FsDirentPrintoutResourceRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.printoutResource.sectionTitle.edit' }, { name: ownerState.assetPath })}</Typography>
      <div className={classes.formContainer}>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.nameField.label' })}>
          <FsDirentTextField required placeholder={intl.formatMessage({ id: 'fs.dirent.printoutResource.nameField.placeholder' })}
            value={ownerState.resourceName}
            onChange={ownerState.onChangeResourceName}
          />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.printoutResource.printoutPagesField.label' })}>
          <FsDirentSelectMulti
            options={ownerState.printoutPageOptions}
            value={ownerState.printoutPageIds}
            onChange={ownerState.onChangePrintoutPageIds}
          />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.printoutResource.uploadBodyField.label' })}>
          <input
            ref={fileInputRef}
            type='file'
            accept={ownerState.contentType === 'image/*' ? 'image/*' : 'text/*'}
            style={{ display: 'none' }}
            onChange={handleFileChange}
          />

          <FsDirentTextField disabled value={fileName} placeholder={intl.formatMessage({ id: 'fs.dirent.printoutResource.uploadBodyField.placeholder' })} />

          <Button variant='text' size='small' className={classes.uploadButton} onClick={() => fileInputRef.current?.click()}>
            {intl.formatMessage({ id: 'fs.dirent.printoutResource.uploadBodyField.button' })}
          </Button>

        </FsDirentFormField>

        {ownerState.contentType === 'image/*' && previewSrc && (
          <img
            src={previewSrc}
            alt={ownerState.resourceName}
            style={{ maxWidth: '100%', maxHeight: '200px', objectFit: 'contain' }}
          />
        )}


      </div>
    </FsDirentPrintoutResourceRoot>
  );
};
