import React from 'react';
import { useUtilityClasses, GInboxRoot, MUI_NAME } from './useUtilityClasses';
import { IntlShape, useIntl } from 'react-intl';
import { Typography, useThemeProps, Grid2 } from '@mui/material';

import { GInboxItem, GInboxItemProps } from './GInboxItem';
import { GInboxFormReview, GInboxFormReviewProps } from '../g-inbox-form-review';
import { GInboxAttachments, GInboxAttachmentsProps } from '../g-inbox-attachments';

import { CommsApi, OfferApi, useComms } from '@dxs-ts/gamut-api';
import { IamApi, useIam } from '@dxs-ts/gamut-api';
import { useContracts } from '@dxs-ts/gamut-api';
import { useSite } from '@dxs-ts/gamut-api';
import { useOffers } from '@dxs-ts/gamut-api';
import { GFlex } from '../g-flex';
import { GInboxFormAssignedNotComplete } from '../g-inbox-form-assigned-not-complete';



export interface GInboxProps {
  children?: React.ReactNode;
  onOpenOffer: (offer: OfferApi.Offer) => void;
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
  const { getLocalisedOfferName, getOffer } = useOffers();
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
      <GFlex variant='header'>
        <Grid2 container sx={{ width: '100%' }}>
          <Grid2 size={{ xs: 12, sm: 12, md: 3, lg: 2 }}>
            <Typography fontWeight='bold'>
              {intl.formatMessage({ id: 'gamut.forms.taskRefId' })}
            </Typography>
          </Grid2>

          <Grid2 size={{ xs: 12, sm: 12, md: 3, lg: 4, xl: 4 }} sx={{ justifyContent: 'flex-start !important' }}>
            <Typography fontWeight='bold'>
              {intl.formatMessage({ id: 'gamut.forms.formName' })}
            </Typography>
          </Grid2>

          <Grid2 size={{ xs: 12, sm: 12, md: 4, lg: 4 }} sx={{ display: 'flex', justifyContent: 'center' }}>
            <Typography fontWeight='bold'>
              {intl.formatMessage({ id: 'gamut.forms.attachments' })}
            </Typography>
          </Grid2>

          <Grid2 size={{ xs: 12, sm: 12, md: 2, lg: 2 }} sx={{ display: 'flex', justifyContent: 'flex-end' }}>
            <Typography fontWeight='bold'>
              {intl.formatMessage({ id: 'gamut.forms.lastModified' })}
            </Typography>
          </Grid2>
        </Grid2>
      </GFlex>



      {subjects
        .map((subject) => {
          const contract = getContract(subject.contractId);
          return {
            ...subject,
            contractUpdated: contract?.updated ? contract.updated.toJSDate() : new Date(0),
          };
        })
        .sort((a, b) => {
          const aViewed = a.isViewed ? 1 : 0;
          const bViewed = b.isViewed ? 1 : 0;

          if (aViewed !== bViewed) {
            return aViewed - bViewed;
          }

          const aDate = a.lastExchange?.created ?? a.created;
          const bDate = b.lastExchange?.created ?? b.created;

          return bDate.toMillis() - aDate.toMillis();
        })
        .map((subject) => {
          const contractId = subject.contractId;
          const contract = getContract(contractId);
          if (!site || !contract) {
            return <React.Fragment key={contractId}></React.Fragment>
          }
          const offerName = getLocalisedOfferName(site, contract?.offer.name!)

          return (<InboxItem
            id={subject.id}
            taskRefId={contract.referenceId}
            key={subject.id}
            onClick={props.slotProps.item.onClick!}
            senderName={getSenderName(subject, iam, intl)}
            sentAt={subject.lastExchange?.created ?? subject.created}
            title={offerName}
            subTitle={subject.lastExchange?.commentText ?? ''}
            contractStatus={(() => {
              if (!contract || !contract.status) return 'status unknown';
              switch (contract.status) {
                case 'OPEN':
                  return intl.formatMessage({ id: 'gamut.forms.status.OPEN' });
                case 'NEW':
                  return intl.formatMessage({ id: 'gamut.forms.status.NEW' });
                case 'COMPLETED':
                  return intl.formatMessage({ id: 'gamut.forms.status.COMPLETED' });
                case 'REJECTED':
                  return intl.formatMessage({ id: 'gamut.forms.status.REJECTED' });
                case 'DELEGATED':
                  return intl.formatMessage({ id: 'gamut.forms.status.DELEGATED' });
                case 'TRANSFERRED':
                  return intl.formatMessage({ id: 'gamut.forms.status.TRANSFERRED' });
                case 'WAITING':
                  return intl.formatMessage({ id: 'gamut.forms.status.WAITING' });
                default:
                  return contract.status;
              }
            })()}
          >
            <FormReview
              formName={offerName}
              formId={subject.formId}
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

            {contract.subforms.length > 0 ? contract.subforms.map((entry) => {
              const subOffer = entry.formInProgress ? getOffer(entry.id) : undefined;

              if (subOffer?.formId) {
                const name = getLocalisedOfferName(site, entry.id);
                return (
                  <GInboxFormAssignedNotComplete key={entry.id} onClick={() => props.onOpenOffer(subOffer)} formName={name} />
                )
              }

              if (entry.formId) {
                return (<FormReview formName={offerName} formId={entry.formId} />)
              }
              return (<></>);


            }) : (<></>)
            }
          </InboxItem>
          )
        })}
    </GInboxRoot>
  )
}
