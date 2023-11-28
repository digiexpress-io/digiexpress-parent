
export type CustomerId = string;
export type ExternalId = string; //ssn or business id
export type CustomerBodyType = 'COMPANY' | 'PERSON';

export interface Person extends CustomerBody {
  firstName: string,
  lastName: string,
  protectionOrder?: boolean,
  type: 'PERSON'
}

export interface Company extends CustomerBody {
  type: 'COMPANY'
}

export interface CustomerAddress {
  locality: string,
  street: string,
  postalCode: string,
  country: string
}

export interface CustomerBody {
  username: string,
  type: CustomerBodyType,
  contact?: CustomerContact,
}

export interface CustomerContact {
  email: string,
  address: CustomerAddress,
  addressValue: string
}

export interface CustomerTask {
  id: string
}

export interface Customer {
  id: CustomerId,
  externalId: ExternalId,
  created: string,
  updated: string,
  body: CustomerBody,
  transactions: CustomerTransaction[]
}

export interface CustomerTransaction {
  id: string,
  commands: CustomerCommand[]
}

export type CustomerCommandType =
  'CreateCustomer' |
  'UpsertSuomiFiPerson' |
  'UpsertSuomiFiRep' |
  'ChangeCustomerFirstName' |
  'ChangeCustomerLastName' |
  'ChangeCustomerSsn' |
  'ChangeCustomerEmail' |
  'ChangeCustomerAddress' |
  'ArchiveCustomer'

export interface CustomerCommand {
  userId?: string,
  targetDate?: string,
  commandType: CustomerCommandType
}

export interface CustomerUpdateCommand<T extends CustomerCommandType> extends CustomerCommand {
  customerId: string, // SSN or Business ID or Internal ID
  commandType: T
}

export interface CreateCustomer extends CustomerCommand {
  commandType: 'CreateCustomer',
  externalId: ExternalId,
  body: CustomerBody
}

export interface UpsertSuomiFiPerson extends CustomerUpdateCommand<'UpsertSuomiFiPerson'> {
  username: string,
  firstName: string,
  lastName: string,
  protectionOrder?: boolean,
  contact?: CustomerContact
}

export interface UpsertSuomiFiRep extends CustomerUpdateCommand<'UpsertSuomiFiRep'> {
  name: string,
  type: CustomerBodyType,
}

export interface ChangeCustomerFirstName extends CustomerUpdateCommand<'ChangeCustomerFirstName'> {
  firstName: string,
}

export interface ChangeCustomerLastName extends CustomerUpdateCommand<'ChangeCustomerLastName'> {
  lastName: string,
}

export interface ChangeCustomerSsn extends CustomerUpdateCommand<'ChangeCustomerSsn'> {
  ssn: string,
}

export interface ChangeCustomerEmail extends CustomerUpdateCommand<'ChangeCustomerEmail'> {
  email: string,
}

export interface ChangeCustomerAddress extends CustomerUpdateCommand<'ChangeCustomerAddress'> {
  address: string,
}

export interface ArchiveCustomer extends CustomerUpdateCommand<'ArchiveCustomer'> {
}

export interface CustomerStore {
  findCustomers(searchString: string): Promise<Customer[]>
  getCustomer(id: CustomerId): Promise<Customer>
}