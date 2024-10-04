import { SiteApi } from './site-types';

export class SiteCache {
  private _site: SiteApi.Site ;
  private _topics: Record<SiteApi.TopicId, SiteApi.TopicView> = {};
  private _children: Record<SiteApi.TopicId, SiteApi.Topic[]> = {};

  constructor(site: SiteApi.Site ) {
    this._site = site;
    const topics = Object.values(site.topics).sort((l0, l1) => l0.id.localeCompare(l1.id));

    topics.filter(t => t.parent).forEach(topic => {
      if (topic.parent && this._children[topic.parent]) {
        this._children[topic.parent].push(topic);
      } else if (topic.parent) {
        this._children[topic.parent] = [topic];
      }
    })

    topics.map(topic => this.visitView(topic)).forEach(t => this._topics[t.topic.id] = t);
  }
  get topics() {
    return this._topics;
  }
  private visitView(topic: SiteApi.Topic) {
    const blob: SiteApi.Blob | undefined = topic.blob ? this._site.blobs[topic.blob] : undefined;
    const parent: SiteApi.Topic | undefined = topic.parent ? this._site.topics[topic.parent] : undefined;
    const children: SiteApi.Topic[] = this._children[topic.id] ? this._children[topic.id] : [];

    const links: SiteApi.TopicLink[] = topic.links.map(l => this._site.links[l]).filter(l => l).sort((l0, l1) => l0.name.localeCompare(l1.name));
    const internalExternal: SiteApi.TopicLink[] = links.filter(t => t.type === "internal" || t.type === "external");
    const phones: SiteApi.TopicLink[] = links.filter(t => t.type === "phone");
    const workflows: SiteApi.TopicLink[] = links.filter(t => t.type === "dialob" || t.type === "workflow");

    return new ImmutableTopicView({ id: topic.id, name: topic.name, topic, blob, parent, children, links, internalExternal, phones, workflows });
  }
}

export class ImmutableTopicView implements SiteApi.TopicView {
  private _topic: SiteApi.Topic;
  private _blob?: SiteApi.Blob
  private _parent?: SiteApi.Topic;
  private _children: SiteApi.Topic[];

  private _links: SiteApi.TopicLink[];
  private _internalExternal: SiteApi.TopicLink[];
  private _phones: SiteApi.TopicLink[];
  private _workflows: SiteApi.TopicLink[];

  constructor(init: SiteApi.TopicView) {
    this._topic = init.topic;
    this._blob = init.blob;
    this._parent = init.parent;
    this._children = init.children;

    this._links = init.links;
    this._internalExternal = init.internalExternal;
    this._phones = init.phones;
    this._workflows = init.workflows;
  }
  get id(): SiteApi.TopicId { return this._topic.id };
  get name(): string { return this._topic.name };
  get topic(): SiteApi.Topic { return this._topic };
  get blob(): SiteApi.Blob | undefined { return this._blob }
  get parent(): SiteApi.Topic | undefined { return this._parent }
  get children(): SiteApi.Topic[] { return this._children }
  get links(): SiteApi.TopicLink[] { return this._links }
  get internalExternal(): SiteApi.TopicLink[] { return this._internalExternal }
  get phones(): SiteApi.TopicLink[] { return this._phones }
  get workflows(): SiteApi.TopicLink[] { return this._workflows }
}