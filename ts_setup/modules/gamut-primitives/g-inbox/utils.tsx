import { useIntl } from 'react-intl';
import { CommsApi, ContractApi, OfferApi, useComms, useContracts, useIam, useOffers, useSite } from '@dxs-ts/gamut-api';



export function useSenderName(subject: CommsApi.Subject): string {
  const intl = useIntl();
  const iam = useIam();
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
}




export interface InboxItem {
  contract: ContractApi.Contract,
  contractStatus: string;
  offerName: string;
  subject: CommsApi.Subject;

  subForms: {
    isOpen: true | false;
    offer?: OfferApi.Offer;
    formId: string;
    formName: string;
  }[];
}

export function useInboxItems(): InboxItem[] {
  const { site } = useSite();
  const { subjects } = useComms();
  const onlyFirstSubjects = subjects.filter(subject => subjects.find(i => i.contractId === subject.contractId) === subject);
  const { getContract } = useContracts();
  const { getLocalisedOfferName } = useOffers();

  return onlyFirstSubjects.sort((a, b) => {
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
        return undefined
      }
      const offerName = getLocalisedOfferName(site, contract?.offer.name!);
      const contractStatus = _useContractStatus(contract);
      const subForms = _useSubOffers(contract);
      return { contract, subject, offerName, contractStatus, subForms }

    }).filter(a => !!a);
}


function _useSubOffers(contract: ContractApi.Contract): InboxItem['subForms'] {
  const { getLocalisedOfferName, getOffer } = useOffers();
  const { site } = useSite();
  const offerName = getLocalisedOfferName(site!, contract?.offer.name!);

  return contract.subforms.map((entry) => {
    const subOffer = entry.formInProgress ? getOffer(entry.id) : undefined;

    if (subOffer?.formId) {
      const formName = getLocalisedOfferName(site!, entry.id);
      return { isOpen: true, formName, offer: subOffer, formId: subOffer.formId }
    }

    if (entry.formId) {
      return { isOpen: true, formName: offerName, formId: entry.formId }
    }
    return undefined;
  }).filter(a => !!a)
}



function _useContractStatus(contract: ContractApi.Contract): string {
  const intl = useIntl();
  if (!contract.status) {
    return 'status unknown';
  }

  switch (contract.status) {
    case 'OPEN': return intl.formatMessage({ id: 'gamut.forms.status.OPEN' });
    case 'NEW': return intl.formatMessage({ id: 'gamut.forms.status.NEW' });
    case 'COMPLETED': return intl.formatMessage({ id: 'gamut.forms.status.COMPLETED' });
    case 'REJECTED': return intl.formatMessage({ id: 'gamut.forms.status.REJECTED' });
    case 'DELEGATED': return intl.formatMessage({ id: 'gamut.forms.status.DELEGATED' });
    case 'TRANSFERRED': return intl.formatMessage({ id: 'gamut.forms.status.TRANSFERRED' });
    case 'WAITING': return intl.formatMessage({ id: 'gamut.forms.status.WAITING' });
    default: return contract.status;
  }
}