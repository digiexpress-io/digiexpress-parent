import * as Api from '../../service';
import { TopicsState } from './contextReducer';

interface TopicsContextType extends TopicsActions {
  topic?: Api.TopicId;
  link?: Api.TopicLinkId;
  actions: TopicsActions;
}

interface TopicsActions {
  setLink: (newLink?: Api.TopicLink) => void;
  setTopic: (newTopic: Api.Topic, props?: any) => void;
}


interface TopicsActionOverrides {
  setTopic?: (newTopic?: Api.Topic, props?: any) => Api.Topic | undefined;
  setLink?: (newLink?: Api.TopicLink) => Api.TopicLink | undefined;
}

const initContext = (state: TopicsState, actions: TopicsActions): TopicsContextType => {
  return {
    topic: state.topic,
    actions: actions,
    setLink: (newLink?: Api.TopicLink) => actions.setLink(newLink),
    setTopic: (newTopic: Api.Topic, props?: any) => actions.setTopic(newTopic, props),
  };
}


export type { TopicsContextType, TopicsActionOverrides, TopicsActions };
export { initContext };
