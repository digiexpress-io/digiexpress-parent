
export type CustomerId = string;


export interface CustomerTask {
  id: string
}

export interface Customer {
  id: CustomerId,
  firstName: string,
  lastName: string,
  ssn: string,
  tasks: CustomerTask[]
}


