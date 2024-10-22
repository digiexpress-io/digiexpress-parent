
export interface RestPage {
  size: number
  totalElements: number
  totalPages: number
  number: number
}

export interface RestLink {
  href: string;
  cursor?: string;
  array?: boolean;
  templated?: boolean;
}

export interface RestLinks {
  [s: string]: RestLink;
}

export interface RestCollectionResponse<T> {
  _embedded: {
    [s: string]: T[]
  },
  _links: RestLinks
}

export interface RestPageResponse<T> extends RestCollectionResponse<T> {
  page: RestPage
}