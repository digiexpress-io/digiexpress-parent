import { DialobRestApi } from "./types-rest-api";

export interface DashboardState {
  forms: DialobRestApi.FormListItem[];
  tags: DialobRestApi.FormTag[];
  items: DasboardItem[];
  loadedAt: Date;
}

export interface DasboardItem {
  id: string;
  metadata: DialobRestApi.FormMetadata;
  latestTagName?: string;
  latestTagDate?: Date;
}
