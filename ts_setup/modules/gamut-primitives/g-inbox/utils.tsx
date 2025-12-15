import { useIntl } from 'react-intl';
import { CommsApi, ContractApi, OfferApi, useComms, useContracts, useIam, useOffers, useSite } from '@dxs-ts/gamut-api';


export interface InboxItem {
  contract: ContractApi.Contract,
  contractStatus: string;
  offerName: string;
  subject: CommsApi.Subject;
  senderName: string;
  attachmentCount: number;

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
  const { getContract } = useContracts();
  const { getLocalisedOfferName, getOffer } = useOffers();
  const intl = useIntl();
  const iam = useIam();

  const visitor = new InboxItemsProcessorVisitor(
    site,
    getContract,
    getLocalisedOfferName,
    getOffer,
    intl,
    iam
  );

  return visitor.visit(subjects);
}



class InboxItemsProcessorVisitor {
  constructor(
    private site: ReturnType<typeof useSite>['site'],
    private getContract: (id: string) => ContractApi.Contract | undefined,
    private getLocalisedOfferName: (site: any, offerName: string) => string,
    private getOffer: (id: string) => OfferApi.Offer | undefined,
    private intl: ReturnType<typeof useIntl>,
    private iam: ReturnType<typeof useIam>
  ) { }

  visit(subjects: readonly CommsApi.Subject[]): InboxItem[] {
    const onlyFirstSubjects = subjects.filter(subject =>
      subjects.find(i => i.contractId === subject.contractId) === subject
    );

    return onlyFirstSubjects
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
        const contract = this.getContract(contractId);
        if (!this.site || !contract) {
          return undefined;
        }
        const offerName = this.getLocalisedOfferName(this.site, contract.offer.name);
        const contractStatus = this.getContractStatus(contract);
        const subForms = this.getSubOffers(contract);
        const senderName = this.getSenderName(subject);
        const attachmentCount = subject.documents.length + subForms.length + contract.documents.length;

        return { contract, subject, offerName, senderName, contractStatus, subForms, attachmentCount };
      })
      .filter((item): item is InboxItem => !!item);
  }

  private getContractStatus(contract: ContractApi.Contract): string {
    if (!contract.status) {
      return 'status unknown';
    }

    switch (contract.status) {
      case 'OPEN': return this.intl.formatMessage({ id: 'gamut.forms.status.OPEN' });
      case 'NEW': return this.intl.formatMessage({ id: 'gamut.forms.status.NEW' });
      case 'COMPLETED': return this.intl.formatMessage({ id: 'gamut.forms.status.COMPLETED' });
      case 'REJECTED': return this.intl.formatMessage({ id: 'gamut.forms.status.REJECTED' });
      case 'DELEGATED': return this.intl.formatMessage({ id: 'gamut.forms.status.DELEGATED' });
      case 'TRANSFERRED': return this.intl.formatMessage({ id: 'gamut.forms.status.TRANSFERRED' });
      case 'WAITING': return this.intl.formatMessage({ id: 'gamut.forms.status.WAITING' });
      default: return contract.status;
    }
  }

  private getSubOffers(contract: ContractApi.Contract): InboxItem['subForms'] {
    const offerName = this.getLocalisedOfferName(this.site!, contract.offer.name);

    return contract.subforms.map((entry) => {
      const subOffer = entry.formInProgress ? this.getOffer(entry.id) : undefined;

      if (subOffer?.formId) {
        const formName = this.getLocalisedOfferName(this.site!, entry.id);
        return { isOpen: true as const, formName, offer: subOffer, formId: subOffer.formId };
      }

      if (entry.formId) {
        return { isOpen: true as const, formName: offerName, formId: entry.formId };
      }
      return undefined;
    }).filter((item): item is NonNullable<typeof item> => !!item);
  }

  private getSenderName(subject: CommsApi.Subject): string {
    switch (true) {
      case this.iam.user !== undefined && Boolean(this.iam.user.userId):
        return this.iam.user.userId;
      case subject.lastExchange === undefined:
        return this.intl.formatMessage({ id: 'gamut.inbox.noMessages' });
      case subject.lastExchange?.userName === '' || subject.lastExchange?.userName === undefined:
        return this.intl.formatMessage({ id: 'cust.inbox.message.sender-name.org-user' });
      default:
        return subject.lastExchange!.userName;
    }
  }
}
