import * as Api from './Service';


const maintainace_en: Api.Site = {
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
  links: {}
}

const loading_en: Api.Site = {
  id: "index",
  images: "", 
  locale: "en",
  loader: true,
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
  links: {}
}

const DefaultFallbackSites = (): Api.FallbackSites => {
  return {
    loading: {
      en: loading_en
    },
    maintainance: {
      en: maintainace_en
    }
  };
}

export default DefaultFallbackSites;
