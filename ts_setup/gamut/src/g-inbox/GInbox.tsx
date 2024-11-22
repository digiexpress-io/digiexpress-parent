import React from 'react';
import { useThemeProps } from '@mui/system';
import { useUtilityClasses, GInboxRoot, MUI_NAME } from './useUtilityClasses';
import { IntlShape, useIntl } from 'react-intl';

import { GInboxItem, GInboxItemProps } from './GInboxItem';
import { GInboxFormReview, GInboxFormReviewProps } from '../g-inbox-form-review';
import { GInboxAttachments, GInboxAttachmentsProps } from '../g-inbox-attachments';

import { CommsApi, useComms } from '../api-comms';
import { IamApi, useIam } from '../api-iam';
import { useContracts } from '../api-contract';
import { useSite } from '../api-site';
import { useOffers } from '../api-offer';



export interface GInboxProps {
  children?: React.ReactNode;

  slots?: {
    item?: React.ElementType<GInboxItemProps>;
    attachment?: React.ElementType<GInboxAttachmentsProps>;
    formReview?: React.ElementType<GInboxFormReviewProps>;
  }

  slotProps: {
    item: Partial<GInboxItemProps>;
    attachment: Partial<GInboxAttachmentsProps>;
    formReview: Partial<GInboxFormReviewProps>;
  }
}


export const GInbox: React.FC<GInboxProps> = (initProps) => {
  const intl = useIntl();
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const classes = useUtilityClasses();
  const { subjects } = useComms();
  const { getContract } = useContracts();
  const { getLocalisedOfferName } = useOffers();
  const iam = useIam();

  const { site } = useSite();

  const InboxItem: React.ElementType<GInboxItemProps> = props.slots?.item ?? GInboxItem;
  const Attachments: React.ElementType<GInboxAttachmentsProps> = props.slots?.attachment ?? GInboxAttachments;
  const FormReview: React.ElementType<GInboxFormReviewProps> = props.slots?.formReview ?? GInboxFormReview;

  const getSenderName = (subject: CommsApi.Subject, iam: IamApi.IamBackendContextType, intl: IntlShape): string => {
    switch (true) {
      case iam.user !== undefined && Boolean(iam.user.userId):
        return (iam.user.userId);
      case subject.lastExchange === undefined:
        return (intl.formatMessage({ id: 'gamut.inbox.noMessages' }));
      case subject.lastExchange?.userName === '' || subject.lastExchange?.userName === undefined:
        return (intl.formatMessage({ id: 'cust.inbox.message.sender-name.org-user' }));
      default:
        return subject.lastExchange.userName;
    }
  };

  return (
    <GInboxRoot className={classes.root}>
      {subjects
        .map((subject) => {
          const contract = getContract(subject.contractId);

          return {
            ...subject,
            contractUpdated: contract?.updated ? contract.updated.toJSDate() : new Date(0),
          };
        })
        .sort((a, b) => new Date(b.contractUpdated).getTime() - new Date(a.contractUpdated).getTime())
        .map((subject) => {
          const contractId = subject.contractId;
          const contract = getContract(contractId);
          if (!site || !contract) {
            return <>no links</>
          }
          const offerName = getLocalisedOfferName(site, contract?.offer.name!)

          return (<InboxItem
            id={subject.id}
            key={subject.id}
            onClick={props.slotProps.item.onClick!}
            senderName={getSenderName(subject, iam, intl)}
            sentAt={subject.lastExchange?.created ?? subject.created}
            title={offerName}
            subTitle={subject.lastExchange?.commentText ?? ''}
            contractStatus={contract && contract.status ? intl.formatMessage({ id: `gamut.forms.status.${contract.status}` }) : 'status unknown'}
          >
            <FormReview
              key={subject.id}
              name={offerName}
              subjectId={subject.id}
              onClick={props.slotProps.formReview.onClick!}
            />

            {subject.documents.map((doc) => (
              <Attachments
                key={doc.id}
                subjectId={subject.id}
                attachmentId={doc.id}
                name={doc.name}
                onClick={props.slotProps.attachment.onClick!}
              />
            ))}
          </InboxItem>)
        })}
    </GInboxRoot>)
}
