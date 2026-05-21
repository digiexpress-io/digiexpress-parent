
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
