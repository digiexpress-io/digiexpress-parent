
type LocaleCode = string;
type TopicId = string;
type TopicLinkId = string;
type BlobId = string;
type TopicLinkType = "phone" | "dialob" | "internal" | "external" | "workflow" | string;
interface Site {
  id: string;
  images: string;
  locale: string;
  loader?: boolean;

  topics: Record<string, Topic>;
  blobs: Record<string, Blob>;
  links: Record<string, TopicLink>;
}

interface Blob {
  id: BlobId;
  value: string;
}

interface Topic {
  id: TopicId;
  name: string;
  links: string[];
  headings: TopicHeading[];
  parent?: string | null;
  blob?: string;
}

interface TopicLink {
  id: TopicLinkId;
  type: TopicLinkType;
  name: string;
  value: string;
  global?: boolean;
  workflow?: boolean;
  secured?: boolean;
  path?: string;
}

interface TopicHeading {
  id: string;
  name: string;
  order: number;
  level: number;
}

interface FallbackSites {
  loading: Record<LocaleCode, Site>;
  maintainance: Record<LocaleCode, Site>;
}

interface Service {
  getSite: (locale: string) => Promise<Site>;
  getSiteLoading: (locale: string) => Site;
}

interface TopicView {
  id: TopicId;
  name: string;
  topic: Topic;
  blob?: Blob  
  parent?: Topic;
  children: Topic[];

  links: TopicLink[];
  internalExternal: TopicLink[];
  phones: TopicLink[];
  workflows: TopicLink[];
}


interface ServiceConfig {
  content: { url: string, predefined?: Site };
  fallbackSites?: FallbackSites;
  defaultLocale: string;
  dev?: boolean;
}

export type { ServiceConfig, Service, TopicHeading, TopicLink, Topic, Blob, Site, TopicLinkType, LocaleCode, FallbackSites, TopicId, TopicLinkId, TopicView, BlobId };


