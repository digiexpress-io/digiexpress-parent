import { RoleId, UserId } from "./org-types";

export interface Profile {
  name: string,
  userId: UserId,
  roles: RoleId[],
  today: Date,
  commit?: string,
  contentType: "OK" | "NOT_CREATED" | "EMPTY" | "ERRORS" | "NO_CONNECTION" | "BACKEND_NOT_FOUND",
}

export interface ProfileStore {
  getProfile(): Promise<Profile>;
  createProfile(): Promise<Profile>;
}
