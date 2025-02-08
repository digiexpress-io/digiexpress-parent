

export interface Publication {
  id: string;
  name: string;
  externalId: string | undefined;

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