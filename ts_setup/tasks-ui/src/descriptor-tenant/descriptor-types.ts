
export type TenantId = string;
export type FormTechnicalName = string;
type FormTitle = string;

export interface PaletteType {

}

export interface TenantPaletteType {

}

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

export interface TenantEntryDescriptor {
  source: TenantEntry;
  formName: FormTechnicalName;
  formTitle: FormTitle;
  created: Date;
  lastSaved: Date;
}

export interface TenantDescriptor {
  tenant: Tenant;
  entries: TenantEntryDescriptor[];
}

export interface DescriptorState {
  withEntries(entries: TenantEntryDescriptor): DescriptorState;
}
