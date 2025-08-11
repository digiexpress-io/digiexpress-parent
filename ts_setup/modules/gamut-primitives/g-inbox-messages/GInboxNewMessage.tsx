import React from 'react';
import { Button, Paper, TextField, Typography, useThemeProps } from '@mui/material';
import ReplyIcon from '@mui/icons-material/Reply';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';
import AttachFileIcon from '@mui/icons-material/AttachFile';

import { useIntl } from 'react-intl';
import { MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { ContractApi, useContracts } from '@dxs-ts/gamut-api';

export interface GInboxNewMessageProps {
  senderName?: string;
  offerName: string;
  minRows?: number | undefined;
  onReplyTo: (messageText: string) => void;
  contract: ContractApi.Contract;
}

export const GInboxNewMessage: React.FC<GInboxNewMessageProps> = (initProps) => {
  const intl = useIntl();
  const { appendContractAttachment, refresh } = useContracts();
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

  function handleChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    setMessageText(event.target.value);
  }

  function handleClearField() {
    setMessageText('');
  }

  const handleFileUpload = (event: React.ChangeEvent<HTMLInputElement>) => {

    const files = event.currentTarget.files;
    if (!files || files.length === 0) {
      return;
    }
    appendContractAttachment(ownerState.contract.exchangeId, files).then(() => {
      refresh();
    });
  };

  const triggerFileInput = () => {
    document.getElementById('file-upload-input')?.click();
  };


  return (
    <Paper className={classes.newMsgItem}>
      <div className={classes.newMsgTitle}>
        {props.senderName ?
          <Typography className={classes.newMsgSenderName}>
            {intl.formatMessage({ id: 'gamut.inbox.newMessage.replyingTo' })}
            {props.senderName}
          </Typography> :
          <Typography className={classes.newMsgSenderName}>
            {intl.formatMessage({ id: 'gamut.inbox.newMessage.sendNew' })}
          </Typography>}
        <Typography>{intl.formatMessage({ id: 'gamut.inbox.newMessage.replyingTo.subject' })}{intl.formatMessage({ id: 'gamut.textSeparatorColon' })}{props.offerName}</Typography>
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
          accept='.jpg, .jpeg, .png, .pdf'
          style={{ display: 'none' }}
          onChange={handleFileUpload}
        />
        <Button startIcon={<DeleteForeverIcon />} className={classes.newMsgCancelButton} variant='outlined' disabled={emptyMessage} onClick={handleClearField}>
          <Typography>{intl.formatMessage({ id: 'gamut.buttons.cancel' })}</Typography>
        </Button>
      </div>
    </Paper>
  )
}

