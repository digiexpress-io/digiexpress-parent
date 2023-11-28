import { UserProfile, CustomerId, Customer, Person } from 'client';


export interface CustomerDescriptor {
  id: CustomerId;
  entry: Customer;
  profile: UserProfile;
  avatar: AvatarCode;

  toPerson(): Person;
}

export interface AvatarCode {
  twoletters: string;
  value: string;
}


