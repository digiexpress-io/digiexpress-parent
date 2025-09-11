export type UserActivityType = 'ACCESS' | 'CHANGE';

export interface EveliHealthUserActivity {
  id: string;
  createdAt: string;
  targetId: string;
  targetIdType: string;
  taskRef: string;
  type: UserActivityType;
  userFor: string;
  userName: string;
}
export interface EveliHealthTaskActivity {
  id: string;
  createdAt: string;
  targetId: string;
  targetIdType: string;
  taskRef: string;
  type: UserActivityType;
  userFor: string;
  userName: string;
  diagnosis?: string;
  diagnosisDescription?: string;
  customerId?: string;
}
