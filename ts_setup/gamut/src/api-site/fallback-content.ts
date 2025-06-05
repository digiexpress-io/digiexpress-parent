import { SiteApi } from "./site-types"

export const maintainace_en: SiteApi.Site = {
  id: "index",
  images: "", 
  locale: "en",
  topics: {
    "000_index": {
      id: "000_index",
      name: "apex-services",
      blob: "index",
      headings: [{
        "id": "1",
        "name": "# Apex Services",
        "order": 1,
        "level": 1
      }],
      links: []
    } 
  }, 
  blobs: {
    "index": {
      id: "index",
      value: "## Site is under maintainace \r\n Please check back later"
    }
  },
  links: {},
  workflowsInOtherLocales: {}
}

export const loading_en: SiteApi.Site = {
  id: "index",
  images: "",
  locale: "en",
  topics: {
    "000_index": {
      id: "000_index",
      name: "apex-services",
      blob: "index",
      headings: [{
        "id": "1",
        "name": "# Apex Services",
        "order": 1,
        "level": 1
      }],
      links: []
    }
  },
  blobs: {
    "index": {
      id: "index",
      value: "## Site is loading \r\n Please hold"
    }
  },
  links: {},
  workflowsInOtherLocales: {}
}