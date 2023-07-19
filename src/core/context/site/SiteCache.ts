import * as Api from '../../service';



export default class SiteCache {
  private _site: Api.Site;
  private _topics: Record<Api.TopicId, Api.TopicView> = {};
  private _children: Record<Api.TopicId, Api.Topic[]> = {};

  constructor(site: Api.Site) {
    this._site = site;
    const topics = Object.values(site.topics).sort((l0, l1) => l0.id.localeCompare(l1.id));
    
    topics.filter(t => t.parent).forEach(topic => {
        if(topic.parent && this._children[topic.parent]) {
          this._children[topic.parent].push(topic);
        } else if(topic.parent) {
          this._children[topic.parent] = [topic];
        }
      })
    
    topics.map(topic => this.visitView(topic)).forEach(t => this._topics[t.topic.id] = t);
  }
  get topics() {
    return this._topics;
  }
  private visitView(topic: Api.Topic) {
    const blob: Api.Blob | undefined = topic.blob ? this._site.blobs[topic.blob] : undefined;
    const parent: Api.Topic | undefined = topic.parent ? this._site[topic.parent] : undefined;
    const children: Api.Topic[] = this._children[topic.id] ? this._children[topic.id] : [];
  
    const links: Api.TopicLink[] = topic.links.map(l => this._site.links[l]).filter(l => l).sort((l0, l1) => l0.name.localeCompare(l1.name));
    const internalExternal: Api.TopicLink[] = links.filter(t => t.type === "internal" || t.type === "external");
    const phones: Api.TopicLink[] = links.filter(t => t.type === "phone");
    const workflows: Api.TopicLink[] = links.filter(t => t.type === "dialob" || t.type === "workflow");
    
    return new ImmutableTopicView({ id: topic.id, name: topic.name, topic, blob, parent, children, links, internalExternal, phones, workflows });
  }
}

class ImmutableTopicView implements Api.TopicView {
  private _topic: Api.Topic;
  private _blob?: Api.Blob  
  private _parent?: Api.Topic;
  private _children: Api.Topic[];

  private _links: Api.TopicLink[];
  private _internalExternal: Api.TopicLink[];
  private _phones: Api.TopicLink[];
  private _workflows: Api.TopicLink[];
  
  constructor(init: Api.TopicView) {
    this._topic = init.topic;
    this._blob = init.blob;  
    this._parent = init.parent;
    this._children = init.children;

    this._links = init.links;
    this._internalExternal = init.internalExternal;
    this._phones = init.phones;
    this._workflows = init.workflows;
  }
  get id(): Api.TopicId { return this._topic.id };
  get name(): string { return this._topic.name };
  get topic(): Api.Topic { return this._topic };
  get blob(): Api.Blob|undefined { return this._blob }  
  get parent(): Api.Topic|undefined { return this._parent }
  get children(): Api.Topic[] { return this._children }
  get links(): Api.TopicLink[] { return this._links }
  get internalExternal(): Api.TopicLink[] { return this._internalExternal }
  get phones(): Api.TopicLink[] { return this._phones }
  get workflows(): Api.TopicLink[] { return this._workflows }
}