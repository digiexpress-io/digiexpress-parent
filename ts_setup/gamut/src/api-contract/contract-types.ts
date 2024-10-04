import { DateTime } from "luxon";
import { ProductApi } from "../api-product";
import { OfferApi } from "../api-offer";
import { BookingApi } from "../api-bookings";


export namespace ContractApi {

}

export declare namespace ContractApi {
  // task exists, form is submitted
  export type ContractId = string;

  export type ContractStatus = (
    'NEW' // task worker has not started working on the task
    | 'OPEN' // task worker has started working on the task
    | "COMPLETED"  // task worker has completed work
    | "REJECTED" // task worker has completed work and rejected the task
  )

  export interface ContractDocument {
    id: string;
    name: string;
    created: DateTime;
    size: number
  }

  export interface Contract {
    id: ContractId;
    exchangeId: string;
    created: DateTime; // task created
    updated: DateTime | undefined; // task updated
    status: string;
    reviewUri: string;
    documents: readonly ContractDocument[];
    product: ProductApi.Product;
    offer: OfferApi.Offer;
    booking: BookingApi.Booking | undefined;
  }

  export type GetContractFetchGET = () => Promise<Response>;
  export type AppendContractAttachmentFetchPOST = (contractId: ContractId, files: FileList) => Promise<void>;

  export interface ContractContextType {
    contracts: readonly Contract[];
    isPending: boolean;
    contractStats: { awaitingDecision: number, decided: number },
    getContract(contractId: ContractId): Contract | undefined;
    appendContractAttachment: (contractId: ContractId, files: FileList) => Promise<Contract>;
    refresh(): Promise<void>;
  }

}