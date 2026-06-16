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
  isChanged: boolean; // TODO remove this duplicate same as isDirty
  //isDirty: boolean; TODO:: u
  bodyType: Fs.BodyType;
  getCurrentProps(): { bodyType: Fs.BodyType; id: string, changes: Record<string, any> };
}

export interface FsuCreateChange {
  bodyType: Fs.BodyType;
  getCurrentProps(): { bodyType: Fs.BodyType; changes: Record<string, any> };
}