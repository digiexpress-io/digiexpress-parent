import { Fs } from "../fs-types";

export interface LinkChangeBodyType {
  id: string;
  urlValue: string;
  contentType: string;
  articles: string[];
  configOptions: string[];
  intlValues: Record<string, string>;
}

export type FsuChangeProps =
  | { bodyType: 'ARTICLE_LINK'; changes: LinkChangeBodyType };


export interface FsuChange {
  id: string;
  treeId: string;
  isDirty: boolean; 
  bodyType: Fs.BodyType;
  getCurrentProps(): { bodyType: Fs.BodyType; id: string, changes: Record<string, any> };
}

export interface FsuCreateChange {
  bodyType: Fs.BodyType;
  getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> };
}