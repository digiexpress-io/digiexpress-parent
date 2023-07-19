import * as Api from '../../service';
import { TopicsActionOverrides, TopicsActions } from './ContextTypes';


interface TopicsStateData {
  loaded: boolean;
  topic?: Api.TopicId;
  topicProps?: any;
  link?: Api.TopicLinkId;
  parent?: TopicsState;
}

interface TopicsState extends TopicsStateData {
  withTopic(topic?: Api.TopicId, topicProps?: any): TopicsState;
  withLink(link?: Api.TopicLinkId): TopicsState;
}

class ImmutableTopicsState implements TopicsState {
  private _loaded: boolean;
  private _topic?: Api.TopicId;
  private _topicProps?: any;
  private _link?: Api.TopicLinkId;
  private _parent?: TopicsState;

  constructor(init: {
    topic?: Api.TopicId;
    link?: Api.TopicLinkId;
    parent?: TopicsState;
    topicProps?: any
  }) {
    this._loaded = init.topic != null;
    this._topic = init.topic;
    this._topicProps = init.topicProps;
    this._link = init.link;
    this._parent = init.parent;
  }
  withTopic(topic?: Api.TopicId, topicProps?: any): TopicsState {
    return new ImmutableTopicsState(this.init({ parent: this, topic, topicProps }));
  }
  withLink(link?: Api.TopicLinkId): TopicsState {
    return new ImmutableTopicsState(this.init({ parent: this, link }));
  }

  init(arg: {}): TopicsStateData {
    const newState: TopicsStateData = {
      loaded: this._loaded,
      topic: this._topic,
      topicProps: this._topicProps,
      link: this._link,
      parent: this,
    };
    return Object.assign(newState, arg);
  }
  get topic() {
    return this._topic;
  }
  get topicProps() {
    return this._topicProps;
  }
  get link() {
    return this._link;
  }
  get parent() {
    return this._parent;
  }
  get loaded() {
    return this._loaded;
  }
}

interface ContextAction {
  type: "setTopic" | "setLink",
  defaultTopicId?: string;
  topic?: Api.Topic,
  topicProps?: any,
  link?: Api.TopicLink,
}


class TopicsReducerDispatch implements TopicsActions {

  private _sessionDispatch: React.Dispatch<ContextAction>;
  private _overrides: TopicsActionOverrides;
  constructor(session: React.Dispatch<ContextAction>, overrides: TopicsActionOverrides) {
    console.log("portal: init Topics dispatch");
    this._sessionDispatch = session;
    this._overrides = overrides;
  }
  setTopic(topic: Api.Topic, topicProps?: any) {
    const newTopic = this._overrides.setTopic ? this._overrides.setTopic(topic, topicProps) : topic;
    this._sessionDispatch({ type: "setTopic", topic: newTopic, topicProps })
  }
  setLink(link?: Api.TopicLink) {
    const newLink = this._overrides.setLink ? this._overrides.setLink(link) : link
    this._sessionDispatch({ type: "setLink", link: newLink })
  }
}

const contextReducer = (oldState: TopicsState, action: ContextAction): TopicsState => {
  switch (action.type) {
    case "setTopic": {
      const newTopic = action.topic;
      if(oldState.topic && newTopic && newTopic.id === oldState.topic) {
        return oldState;
      }
      return oldState.withTopic(newTopic?.id);
    }
    case "setLink": {
      const newLink = action.link;
      if (newLink && !(newLink.type === "dialob" || newLink.type === "workflow")) {
        return oldState;
      }
      return oldState.withLink(newLink?.id);
    }
  }
}


export type { TopicsState, ContextAction };
export { contextReducer, ImmutableTopicsState, TopicsReducerDispatch };

