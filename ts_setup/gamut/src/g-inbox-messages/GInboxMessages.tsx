import React from 'react';
import { Box, Divider, Typography, useThemeProps } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { GTooltip } from '../g-tooltip';
import { GInboxMessagesRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';

import { GInboxFormReview, GInboxFormReviewProps } from '../g-inbox-form-review';
import { GInboxAttachments, GInboxAttachmentsProps } from '../g-inbox-attachments';
import { GInboxNewMessageProps, GInboxNewMessage } from './GInboxNewMessage';
import { GInboxMessage, GInboxMessageProps } from './GInboxMessage';
import { GInboxMessageNotAllowed } from './GInboxMessageNotAllowed';

import { useComms, CommsApi } from '../api-comms';
import { useContracts } from '../api-contract';
import { useOffers } from '../api-offer';
import { useSite } from '../api-site';



export interface GInboxMessagesProps {
  subjectId: string;
  slots?: {
    formReview?: React.ElementType<GInboxFormReviewProps>
    message?: React.ElementType<GInboxMessageProps>;
    newMessage?: React.ElementType<GInboxNewMessageProps>;
    attachments?: React.ElementType<GInboxAttachmentsProps>;
  }

  slotProps: {
    formReview: Partial<GInboxFormReviewProps>
    message: Partial<GInboxMessageProps>;
    newMessage: Partial<GInboxNewMessageProps>;
    attachments: Partial<GInboxAttachmentsProps>;
  }
}


export const GInboxMessages: React.FC<GInboxMessagesProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const classes = useUtilityClasses();
  const { getSubject } = useComms();
  const { getLocalisedOfferName } = useOffers();
  const { site } = useSite();
  const { replyTo } = useComms();
  const { getContract } = useContracts();

  const subject = getSubject(props.subjectId);



  function handleReplyTo(subjectId: CommsApi.SubjectId, text: string) {
    replyTo({ subjectId, text });
  }

  if (!subject) {
    return <></>;
  }

  const contract = getContract(subject?.id);
  if (!site || !contract) {
    return <>...no site / contract</>
  }
  const offerName = getLocalisedOfferName(site, contract?.offer.name);

  const FormReview: React.ElementType<GInboxFormReviewProps> = props.slots?.formReview ?? GInboxFormReview;
  const Message: React.ElementType<GInboxMessageProps> = props.slots?.message ?? GInboxMessage;
  const NewMessage: React.ElementType<GInboxNewMessageProps> = props.slots?.newMessage ?? GInboxNewMessage;
  const Attachments: React.ElementType<GInboxAttachmentsProps> = props.slots?.attachments ?? GInboxAttachments;

  return (
    <GInboxMessagesRoot className={classes.root}>
      <Box className={classes.title}>
        <GTooltip title={'This is a tooltip with a test sentence. It is very helpful indeed!'}>
          <Typography><FormattedMessage id='gamut.inbox.subjectAttachment.title' /></Typography>
        </GTooltip>
      </Box>
      <>
        <div className={classes.header}>
          <FormReview name={offerName} onClick={props.slotProps.formReview.onClick!} subjectId={subject.id} />

          {subject?.documents.map((doc) => (
            <Attachments name={doc.name}
              subjectId={subject.id}
              attachmentId={doc.id}
              onClick={() => { }}
              key={subject.id}
            />
          ))}
        </div>

        <Divider />
        <Box className={classes.title}>
          <Typography><FormattedMessage id='gamut.inbox.messages.title' /></Typography>
        </Box>
        <div className={classes.messages}>
          {subject?.exchange.map((exchange) => (
            <Message commentText={exchange.commentText}
              created={exchange.created}
              isMyMessage={exchange.isMyMessage}
              senderName={exchange.userName}
              key={exchange.id}
            />
          ))}
        </div>


        {contract?.status === 'COMPLETED' || contract?.status === 'REJECTED' ?
          (<div className={classes.msgNotAllowedRoot}><GInboxMessageNotAllowed /></div>) : (
            <div className={classes.newMessage}>
              <NewMessage offerName={offerName} onReplyTo={(messageText: string) => handleReplyTo(subject.id, messageText)} />
            </div>
          )}
      </>

    </GInboxMessagesRoot>
  )
}

