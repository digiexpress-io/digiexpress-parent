export interface User {
  authenticated: boolean;
  authorized: boolean;
  name: string|null
  roles: string[]|null
  email: string|null
  userId: string|null
}