import React from 'react';
import { Typography, Button } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentSelectSingle } from '../fs-dirent-select-single';
import { useUtilityClasses, FsDirentPrintoutResourceRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';
import { FsDirentPrintoutResourceCreateProps } from './FsDirentPrintoutResourceProps';

export const FsDirentPrintoutResourceCreate: React.FC<FsDirentPrintoutResourceCreateProps> = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
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

  return (
    <FsDirentPrintoutResourceRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.printoutResource.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.nameField.label' })}</Typography>
        <FsDirentTextField
          required
          placeholder={intl.formatMessage({ id: 'fs.dirent.printoutResource.nameField.placeholder' })}
          value={ownerState.resourceName}
          onChange={ownerState.onChangeResourceName}
          onBlur={ownerState.onBlurResourceName}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.printoutResource.contentTypeField.label' })}</Typography>
        <FsDirentSelectSingle
          options={ownerState.contentTypeOptions}
          value={ownerState.contentType}
          onChange={ownerState.onChangeContentType}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.printoutResource.uploadBodyField.label' })}</Typography>
        <input
          ref={fileInputRef}
          type='file'
          accept={ownerState.contentType === 'image/*' ? 'image/*' : 'text/*'}
          style={{ display: 'none' }}
          onChange={handleFileChange}
        />
        <FsDirentTextField disabled value={fileName} placeholder={intl.formatMessage({ id: 'fs.dirent.printoutResource.uploadBodyField.placeholder' })} />
        <Button variant='outlined' size='small' sx={{ alignSelf: 'flex-start' }} onClick={() => fileInputRef.current?.click()}>
          {intl.formatMessage({ id: 'fs.dirent.printoutResource.uploadBodyField.button' })}
        </Button>

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel onClick={ownerState.onCancel} />
          <FsDirentButtonSave onClick={ownerState.onSave} disabled={!ownerState.isChanged} />
        </div>

      </div>
    </FsDirentPrintoutResourceRoot>
  );
};
