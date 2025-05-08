import React from 'react';
import { Box, Divider, Typography, useThemeProps } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';
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
  const intl = useIntl();
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
  const contract = subject ? getContract(subject.contractId) : undefined;
  const offerName = site && contract ? getLocalisedOfferName(site, contract.offer.name) : '';

  if (!subject || !site || !contract) {
    console.error("no site / contract", { subject, site, contract });
    return <>...no site / contract</>
  }

  function handleReplyTo(subjectId: CommsApi.SubjectId, text: string) {
    replyTo({ subjectId, text });
  }

  const FormReview: React.ElementType<GInboxFormReviewProps> = props.slots?.formReview ?? GInboxFormReview;
  const Message: React.ElementType<GInboxMessageProps> = props.slots?.message ?? GInboxMessage;
  const NewMessage: React.ElementType<GInboxNewMessageProps> = props.slots?.newMessage ?? GInboxNewMessage;
  const Attachments: React.ElementType<GInboxAttachmentsProps> = props.slots?.attachments ?? GInboxAttachments;

  return (
    <GInboxMessagesRoot className={classes.root}>
      <Box className={classes.title}>
        <Typography>
          {intl.formatMessage({ id: 'gamut.forms.taskRefId' })}
          {intl.formatMessage({ id: 'gamut.noValueIndicatorColon' })}
          {contract.referenceId}
        </Typography>
      </Box>
      <Box className={classes.title}>
        <Typography><FormattedMessage id='gamut.inbox.subjectAttachment.title' /></Typography>
      </Box>
      <>
        <div className={classes.header}>
          <FormReview formName={offerName} formId={contract.offer.formId} />

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


        {contract?.status === 'COMPLETED' || contract?.status === 'REJECTED' || subject.exchange.length === 0 ?
          (<div className={classes.msgNotAllowed}><GInboxMessageNotAllowed /></div>) : (
            <div className={classes.newMessage}>
              <NewMessage offerName={offerName} onReplyTo={(messageText: string) => handleReplyTo(subject.id, messageText)} contract={contract} />
            </div>
          )}
      </>

    </GInboxMessagesRoot>
  )
}

