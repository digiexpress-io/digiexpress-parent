export namespace PublicationApi {

}

export declare namespace PublicationApi {

  export interface Publication {
    id: string;
    name: string;
    externalId: string | undefined;
    external: boolean | undefined;
    description: string;
    createdBy: string;
    createdAt: string; // offset date time
    startsAt: string; // offset date time
    status: 'BUILDING' | 'READY' | 'ERROR' | 'DEPLOYED';
    errors: Object;
    sources: {
      stencil: Object;
      wrench: Object;
      dialob: Object[];
    } | undefined; // only when loaded on demand
  }

  export interface PublicationInit {
    name: string;
    liveDate: string | null;
    description: string | null;
    stencilTag: string | null;
    wrenchTag: string | null;
  }



  export interface PublicationUpload {
    name: string;
    externalId: string | undefined;
    description: string;
    createdBy: string;
    startsAt: string; // offset date time
    sources: {
      stencil: Object;
      wrench: Object;
      dialob: Object[];
    } | undefined; // only when loaded on demand
  }

  export interface AssetTag {
    name: string
    description: string
    user: string
    created: Date
  }


  export interface AssetFormTag {
    formLabel: string;
    formName: string;
    tagFormId: string;
    tagName: string;
  }

  export interface AssetService {
    id: string;
    type: string;
    body: {
      name: string;
      formName: string;
      formTag: string;
      flowName: string;
      updated?: Date;
    }
  }
}