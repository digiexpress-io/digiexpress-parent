import { UserProfileAndOrg, CustomerId, Customer, Person, CustomerBodyType } from 'client';

export interface CustomerDescriptor {
  id: CustomerId;
  entry: Customer;
  profile: UserProfileAndOrg;
  avatar: AvatarCode;

  displayName: string;
  customerType: CustomerBodyType;
  tasks: string[]; // task id-s
  created: Date;
  lastLogin: Date;

  toPerson(): Person;
}

export interface AvatarCode {
  twoletters: string;
  value: string;
}


