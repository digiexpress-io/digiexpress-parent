import StencilClient from './';

const articles: StencilClient.Article[] = [
  { id: "a1", body: { order: 100, name: "residence", } },
  { id: "a1.1", body: { order: 100, parentId: "a1", name: "utilities" } },
  { id: "a1.2", body: { order: 200, parentId: "a1", name: "new_address" } },
  { id: "a1.3", body: { order: 300, parentId: "a1", name: "renovation" } },
  { id: "a1.4", body: { order: 400, parentId: "a1", name: "new_home_construction" } },
  { id: "a1.5", body: { order: 500, parentId: "a1", name: "social_housing" } },


  { id: "a2", body: { order: 200, name: "health_services", } },
  { id: "a2.1", body: { order: 100, parentId: "a2", name: "hospitals" } },
  { id: "a2.2", body: { order: 200, parentId: "a2", name: "emergency_care" } },
  { id: "a2.3", body: { order: 300, parentId: "a2", name: "specialist_services" } },
  { id: "a2.4", body: { order: 400, parentId: "a2", name: "elder_care" } },
  { id: "a2.5", body: { order: 500, parentId: "a2", name: "mental_health" } },
];

const pages: StencilClient.Page[] = [
  { id: "p1", body: { article: "a1", content: "# Residence topic\n\n", locale: "l3" }, created: "04-11-2020", modified: "05-11-2020" },
  { id: "p2", body: { article: "a1", content: "# Asuinpaikka", locale: "l1" }, created: "04-11-2020", modified: "06-11-2020" },
  { id: "p3", body: { article: "a1.1", content: "# Utilities", locale: "l3" }, created: "09-10-2020", modified: "09-11-2020" },
  { id: "p4", body: { article: "a1.1", content: "# apuohjelmat", locale: "l1" }, created: "10-12-2020", modified: "12-12-2020" },
  { id: "p5", body: { article: "a1.2", content: "# Registering new address", locale: "l3" }, created: "04-11-2020", modified: "05-11-2020" },
  { id: "p6", body: { article: "a1.2", content: "# Osoitetiedot", locale: "l1" }, created: "04-11-2020", modified: "05-11-2020" },
  { id: "p7", body: { article: "a1.3", content: "# Renovation permits", locale: "l3" }, created: "17-04-2020", modified: "24-04-2020" },
  { id: "p8", body: { article: "a1.4", content: "# Uuden kodin rakentaminen", locale: "l1" }, created: "04-11-2020", modified: "05-11-2020" },
  { id: "p9", body: { article: "a1.5", content: "# Sosiaalinen asunto", locale: "l1" }, created: "01-01-2020", modified: "16-02-2020" },


  { id: "p10", body: { article: "a2", content: "# Healthcare and services", locale: "l3" }, created: "04-11-2020", modified: "05-11-2020" },
  { id: "p11", body: { article: "a2", content: "# Terveydenhuolto ja palvelut", locale: "l1" }, created: "04-11-2020", modified: "06-11-2020" },
  { id: "p12", body: { article: "a2.1", content: "# Hospitals around the city", locale: "l3" }, created: "09-10-2020", modified: "09-11-2020" },
  { id: "p13", body: { article: "a2.1", content: "# sairaala", locale: "l1" }, created: "10-12-2020", modified: "12-12-2020" },
  { id: "p14", body: { article: "a2.2", content: "# Emergency", locale: "l3" }, created: "04-11-2020", modified: "05-11-2020" },
  { id: "p15", body: { article: "a2.2", content: "# Hätä", locale: "l1" }, created: "04-11-2020", modified: "05-11-2020" },
  { id: "p16", body: { article: "a2.3", content: "# Elderly care services", locale: "l3" }, created: "17-04-2020", modified: "24-04-2020" },
  { id: "p17", body: { article: "a2.4", content: "# Vanhustenhoito", locale: "l1" }, created: "04-11-2020", modified: "05-11-2020" },
  { id: "p18", body: { article: "a2.5", content: "# Mielenterveys", locale: "l1" }, created: "01-01-2020", modified: "16-02-2020" },
];

const links: StencilClient.Link[] = [
  { id: "l1", body: { articles: ["a1", "a2", "a2.4"], contentType: "internal", value: "http://www.housing-good-municipality.fi/fi", labels: [{ locale: "l1", labelValue: "new housing developments" }] } },
  { id: "l2", body: { articles: ["a1"], contentType: "internal", value: "http://www.housing-good-municipality.fi/en", labels: [{ locale: "l3", labelValue: "new housing developments" }] } },
  { id: "l3", body: { articles: ["a1"], contentType: "external", value: "http://www.finland.fi", labels: [{ locale: "l1", labelValue: "move to finland" }] } },
  { id: "l4", body: { articles: ["a1"], contentType: "phone", value: "+664-5277-7733", labels: [{ locale: "l3", labelValue: "office phone" }] } },
  { id: "l5", body: { articles: ["a1"], contentType: "phone", value: "+664-4321-1223", labels: [{ locale: "l1", labelValue: "päätilintarkastajan puhelin" }] } },
  { id: "l6", body: { articles: ["a1"], contentType: "internal", value: "http://www.us/service/about", labels: [{ locale: "l3", labelValue: "about the service" }] } },
  { id: "l7", body: { articles: ["a2"], contentType: "phone", value: "+664-4185-6512", labels: [{ locale: "l1", labelValue: "secretary phone" }] } },
  { id: "l8", body: { articles: ["a2"], contentType: "phone", value: "+124-2241-1188", labels: [{ locale: "l3", labelValue: "manager phone" }] } },
  { id: "l9", body: { articles: ["a2"], contentType: "internal", value: "http://www.hospitals.com", labels: [{ locale: "l1", labelValue: "hospital information" }] } },
  { id: "l10", body: { articles: ["a2"], contentType: "external", value: "http://www.health.fi", labels: [{ locale: "l1", labelValue: "terveys" }] } },
  { id: "l11", body: { articles: ["a2"], contentType: "internal", value: "http://www.vanhukset.fi", labels: [{ locale: "l1", labelValue: "elder care" }] } },
  { id: "l12", body: { articles: ["a2"], contentType: "phone", value: "+664-4185-6512", labels: [{ locale: "l1", labelValue: "main secretary phone" }] } },
  { id: "l13", body: { articles: ["a2"], contentType: "phone", value: "+124-2241-1188", labels: [{ locale: "l3", labelValue: "main manager phone" }] } },
  { id: "l14", body: { articles: ["a2"], contentType: "internal", value: "http://www.example.com", labels: [{ locale: "l1", labelValue: "general information" }] } },
  { id: "l15", body: { articles: ["a2"], contentType: "external", value: "http://www.adult-learning.com", labels: [{ locale: "l1", labelValue: "vocational training" }] } },
  { id: "l16", body: { articles: ["a2"], contentType: "internal", value: "http://www.learn.fi", labels: [{ locale: "l1", labelValue: "education" }] } },


];

const workflows: StencilClient.Workflow[] = [
  { id: "w1", body: { articles: ["a1"], value: "generalProcess1", labels: [{ locale: "l1", labelValue: "General Inquiry" }] } },
  { id: "w2", body: { articles: ["a1"], value: "greatFlow", labels: [{ locale: "l3", labelValue: "Default Form" }] } },
  { id: "w3", body: { articles: ["a1"], value: "inquiryMgr", labels: [{ locale: "l3", labelValue: "Super good question" }] } },
  { id: "w4", body: { articles: ["a1.2"], value: "generalProcess1", labels: [{ locale: "l1", labelValue: "General Process" }] } },
  { id: "w5", body: { articles: ["a1.3"], value: "greatFlow", labels: [{ locale: "l3", labelValue: "Default Flow" }] } },
  { id: "w6", body: { articles: ["a1.3"], value: "inquiryMgr", labels: [{ locale: "l1", labelValue: "General Question" }] } },
];

const locales: StencilClient.SiteLocale[] = [
  { id: "l1", body: { enabled: true, value: "fi" } },
  { id: "l2", body: { enabled: true, value: "sv" } },
  { id: "l3", body: { enabled: false, value: "en" } }
]
const releases: StencilClient.Release[] = [
  { id: "r3", body: { name: "LATEST", note: "", articles: [], links: [], workflows: [], locales: [], pages: [], created: "03/10/2021" } },
  { id: "r1", body: { name: "v1.5", note: "super note here", articles: [], links: [], workflows: [], locales: [], pages: [], created: "04/02/2021" } },
  { id: "r2", body: { name: "v1.6", note: "Even better note here", articles: [], links: [], workflows: [], locales: [], pages: [], created: "12/02/2021" } },

];

const templates: StencilClient.Template[] = [
  {
    id: 't1',
    body: {
      content: '# Header 1',
      description: 'General Page template',
      name: 'Page Template 1',
      type: 'page'
    },
  }
]
const getSite = async (): Promise<StencilClient.Site> => {
  return {
    name: "mock", contentType: "OK",
    releases: toRecord(releases),
    pages: toRecord(pages),
    links: toRecord(links),
    articles: toRecord(articles),
    workflows: toRecord(workflows),
    locales: toRecord(locales),
    templates: toRecord(templates),
  };
}
const createMock = (): StencilClient.Service => {

  return {
    getSite,
    async getReleaseContent(_release: StencilClient.Release): Promise<{}> {
      return {};
    },
    create: () => new MockCreateBuilder(),
    update: () => new MockUpdateBuilder(),
    delete: () => new MockDeleteBuilder(),
    version: () => new MockVersionBuilder(),
  } as any;
}

class MockCreateBuilder implements StencilClient.CreateBuilder {
  async site(): Promise<StencilClient.Site> {
    return getSite();
  }
  async importData(init: string): Promise<void> {
    return init as any;
  }
  async release(init: StencilClient.CreateRelease): Promise<StencilClient.Release> {
    return init as any;
  }
  async locale(init: StencilClient.CreateLocale): Promise<StencilClient.SiteLocale> {
    return init as any;
  }
  async article(init: StencilClient.CreateArticle): Promise<StencilClient.Article> {
    return init as any;
  }
  async page(init: StencilClient.CreatePage): Promise<StencilClient.Page> {
    return init as any;
  }
  async link(init: StencilClient.CreateLink): Promise<StencilClient.Link> {
    return init as any;
  }
  async workflow(init: StencilClient.CreateWorkflow): Promise<StencilClient.Workflow> {
    return init as any;
  }
  async template(init: StencilClient.CreateTemplate): Promise<StencilClient.Template> {
    return init as any;
  }
}

class MockUpdateBuilder implements StencilClient.UpdateBuilder {
  async locale(init: StencilClient.LocaleMutator): Promise<StencilClient.SiteLocale> {
    return init as any;
  }
  async article(init: StencilClient.ArticleMutator): Promise<StencilClient.Article> {
    return init as any;
  }
  async pages(init: StencilClient.PageMutator[]): Promise<StencilClient.Page[]> {
    console.log("saving pages", init);
    return init as any;
  }
  async link(init: StencilClient.LinkMutator): Promise<StencilClient.Link> {
    return init as any;
  }
  async workflow(init: StencilClient.WorkflowMutator): Promise<StencilClient.Workflow> {
    return init as any;
  }
  async template(init: StencilClient.TemplateMutator): Promise<StencilClient.Template> {
    return init as any;
  }
}

class MockDeleteBuilder implements StencilClient.DeleteBuilder {
  async locale(init: StencilClient.LocaleId): Promise<void> {
    return init as any;
  }
  async release(init: StencilClient.ReleaseId): Promise<void> {
    return init as any;
  }
  async article(init: StencilClient.ArticleId): Promise<void> {
    return init as any;
  }
  async page(init: StencilClient.PageId): Promise<void> {
    return init as any;
  }
  async link(init: StencilClient.LinkId): Promise<void> {
    return init as any;
  }
  async workflow(init: StencilClient.WorkflowId): Promise<void> {
    return init as any;
  }
  async template(init: StencilClient.TemplateId): Promise<void> {
    return init as any;
  }
  async workflowArticlePage(workflow: StencilClient.WorkflowId, article: StencilClient.ArticleId, locale: StencilClient.Locale): Promise<void> {
    const body = { workflow, article, locale };
    console.log("delete workflow article", body);
  }
  async linkArticlePage(link: StencilClient.LinkId, article: StencilClient.ArticleId, locale: StencilClient.Locale): Promise<void> {
    const body = { link, article, locale };
    console.log("delete link article", body);
  }
}

class MockVersionBuilder implements StencilClient.VersionBuilder {
  async version(): Promise<StencilClient.VersionEntity> {
    const date = new Date().toLocaleDateString("en-GB");
    return { version: "mock", built: date.toString() };
  }
}

const toRecord = (entities: { id: string }[]): Record<string, any> => {
  const result: Record<string, any> = {};
  for (const entity of entities) {
    result[entity.id] = entity;
  }
  return result;
}

export default createMock;