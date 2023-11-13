
export type TenantId = string;
export type FormTechnicalName = string;
export type FormTitle = string;
export type SessionId = string;
export type FormId = string;

export interface Tenant {
  id: TenantId;
  name: string;
}
export interface TenantEntry {
  id: FormTechnicalName; //technicalName, (resys tenant: MyTenantTestForm)
  metadata: {
    label: FormTitle; //formName, (resys tenant: TenantTestForm)
    created: string;
    lastSaved: string;
    tenantId: TenantId;
  }
}

export interface DialobTag {
  id: string;
  name: string;
  formName: string;
}

export interface DialobVariable {
  context: string | undefined, // defined if context variable, undefined if expression variable
  contextType: string,
  defaultValue: any | null | undefined,
  name: string
}

/*
{ //session
        "id": "04b76ebca2a317971cccea303f685cb0",
        "metadata": {
            "formId": "73b9cf712017bf2dbda638c70122075c",
            "status": "OPEN",
            "tenantId": "7c0161b5-2956-48c0-8aad-36b5763d560e",
            "created": "2023-07-18T07:03:33.886+00:00",
            "lastAnswer": "2023-07-18T07:04:36.168+00:00",
            "owner": "3caa74e7-0182-4afa-aadb-8b968984d768"
        }
    },
https://demo.dialob.io/api/questionnaires/?formName=TenantTestForm&tenantId=7c0161b5-2956-48c0-8aad-36b5763d560e
 curl  -H 'accept: application/json' localhost:92/dialob/api/forms/multiRow2 | json_pp  

  curl  -H 'accept: application/json' localhost:92/dialob/api/questionnaires/multiRow2/7c0161b5-2956-48c0-8aad-36b5763d560e | json_pp  

*/

export interface DialobSession {
  id: SessionId,
  metadata: {
    formId: string,
    status: "NEW" | "OPEN" | "COMPLETED",
    tenantId: TenantId,
    created: Date,
    lastAnswer: Date,
    owner: string,
  }
}

export interface DialobForm {
  _id: FormId;
  name: FormTechnicalName;
  metadata: {
    label: string;
    languages: string[];
    labels?: string[];
  },
  data: Record<string, {
    id: string,
    type: string,
    label?: Record<string, string>, // locale-locale label
  }>,
  variables?: DialobVariable[];
}


export interface TenantEntryPagination {
  page: number; //starts from 1
  total: { pages: number, records: number };
  records: TenantEntry[];
}

export interface TenantStore {
  getTenantEntries(tenantId: string): Promise<TenantEntryPagination>
  getTenants(): Promise<Tenant[]>
  getDialobTags(dialobFormId: string): Promise<DialobTag[]>;
  getDialobForm(dialobFormId: string): Promise<DialobForm>;
  getDialobSessions(props: { formId: FormId, technicalName: FormTechnicalName, tenantId: TenantId }): Promise<DialobSession[]>
}

