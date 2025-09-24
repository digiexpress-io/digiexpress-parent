import { DialobRestApi } from "./types-rest-api";

export interface DashboardState {
  forms: DialobRestApi.FormListItem[];
  tags: DialobRestApi.FormTag[];
  items: DashboardItem[];
  loadedAt: Date;
}

export interface DashboardItem {
  id: string;
  metadata: DialobRestApi.FormMetadata;
  latestTagName?: string;
  latestTagDate?: Date;
}
