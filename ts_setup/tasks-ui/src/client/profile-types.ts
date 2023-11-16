import { RoleId, UserId } from "./org-types";

export interface UserProfile {
  name: string,
  userId: UserId,
  roles: RoleId[],
  today: Date,
}
