import { StencilClient } from '@the-stencil-io/composer';

export type StencilTree = StencilClient.Site;

export type SiteContentType = string;

export interface StencilLocalizedSite {
  locale: string;
  topics: Record<string, StencilTopic>;
  blobs: Record<string, StencilTopicBlob>;
  links: Record<string, StencilTopicLink>;
}

export interface StencilDef {
  sites: Record<string, StencilLocalizedSite>;
}


export interface StencilTopicBlob {
  id: string;
  value: string;
}

export interface StencilTopic {
  id: string;
  name: string;
  links: string[];
  headings: StencilTopicHeading[];
  parent?: string;
  blob?: string;
}

export interface StencilTopicHeading {
  id: string;
  name: string;
  order: number;
  level: number;
}

export interface StencilTopicLink {
  id: string;
  path: string;
  type: string;
  name: string;
  value: string;
  global: boolean;
  workflow: boolean;
}

export interface StencilTreeArticle {
  
}

export class StencilTreeArticleImpl implements StencilTreeArticle {
  
}





