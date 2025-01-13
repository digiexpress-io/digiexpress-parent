import React from 'react';
import { Button, TextField, Typography, useThemeProps } from '@mui/material';
import ReplyIcon from '@mui/icons-material/Reply';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';
import AttachFileIcon from '@mui/icons-material/AttachFile';

import { useIntl } from 'react-intl';
import { GInboxNewMessageRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { ContractApi } from '../api-contract';

export interface GInboxNewMessageProps {
  senderName?: string;
  offerName: string;
  minRows?: number | undefined;
  onReplyTo: (messageText: string) => void;
  contract: ContractApi.Contract;
}

export const GInboxNewMessage: React.FC<GInboxNewMessageProps> = (initProps) => {
  const intl = useIntl();

  const [messageText, setMessageText] = React.useState('');
  const emptyMessage = messageText.trim() === '';

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const ownerState = {
    ...props,
    minRows: 5
  };

  const classes = useUtilityClasses();

  const handleChange = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setMessageText(event.target.value);
  };

  const handleFileUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
    const files = event.target.files;

    if (files && files.length > 0) {
      Array.from(files).forEach((file) => {
        console.log(`Name: ${file.name}`);
        console.log(`Size: ${file.size} bytes`);
        console.log(`Type: ${file.type}`);
        console.log(`Contract ID: ${props.contract.id}`);
      });
    }
  };

  const triggerFileInput = () => {
    document.getElementById('file-upload-input')?.click();
  };


  return (
    <GInboxNewMessageRoot className={classes.newMsgRoot}>
      <div className={classes.newMsgTitle}>
        {props.senderName ?
          <Typography className={classes.newMsgSenderName}>
            {intl.formatMessage({ id: 'gamut.inbox.newMessage.replyingTo' })}
            {props.senderName}
          </Typography> :
          <Typography className={classes.newMsgSenderName}>
            {intl.formatMessage({ id: 'gamut.inbox.newMessage.sendNew' })}
          </Typography>}
        <Typography>{intl.formatMessage({ id: 'gamut.inbox.newMessage.replyingTo.subject' })}{props.offerName}</Typography>
      </div>

      <TextField multiline minRows={ownerState.minRows}
        onChange={handleChange}
        value={messageText}
        placeholder={intl.formatMessage({ id: 'gamut.inbox.newMessage.placeholder' })}
      />

      <div className={classes.newMsgButtons}>
        <Button startIcon={<ReplyIcon />} variant='contained' disabled={emptyMessage} onClick={() => props.onReplyTo(messageText)}>
          {intl.formatMessage({ id: 'gamut.buttons.reply' })}
        </Button>
        <Button className={classes.newMsgAddButton} startIcon={<AttachFileIcon />} variant='outlined' onClick={triggerFileInput}>
          {intl.formatMessage({ id: 'gamut.buttons.attachment.add' })}
        </Button>
        <input
          id="file-upload-input"
          type="file"
          multiple
          style={{ display: 'none' }}
          onChange={handleFileUpload}
        />
        <Button startIcon={<DeleteForeverIcon />} className={classes.newMsgCancelButton} variant='outlined' disabled={emptyMessage}>
          <Typography>{intl.formatMessage({ id: 'gamut.buttons.cancel' })}</Typography>
        </Button>
      </div>
    </GInboxNewMessageRoot>
  )
}

