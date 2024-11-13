import React from 'react';
import { Button, TextField, Typography, useThemeProps } from '@mui/material';
import ReplyIcon from '@mui/icons-material/Reply';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';
import AttachFileIcon from '@mui/icons-material/AttachFile';

import { useIntl } from 'react-intl';
import { GInboxNewMessageRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';

export interface GInboxNewMessageProps {
  senderName?: string;
  subjectName: string;
  minRows?: number | undefined;
  onReplyTo: (messageText: string) => void;
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
        <Typography>{intl.formatMessage({ id: 'gamut.inbox.newMessage.replyingTo.subject' })}{props.subjectName}</Typography>
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
        <Button className={classes.newMsgAddButton} startIcon={<AttachFileIcon />} variant='outlined'>
          {intl.formatMessage({ id: 'gamut.buttons.attachment.add' })}
        </Button>
        <Button startIcon={<DeleteForeverIcon />} className={classes.newMsgCancelButton} variant='outlined' disabled={emptyMessage}>
          <Typography>{intl.formatMessage({ id: 'gamut.buttons.cancel' })}</Typography>
        </Button>
      </div>
    </GInboxNewMessageRoot>
  )
}

