import React from 'react';
import * as Api from '../../service';
import { useViews, useSite } from '../site/useContext';
import { TopicsContext } from './Context';
import { TopicsContextType } from './ContextTypes';


const useContext = () => {
  const result: TopicsContextType = React.useContext(TopicsContext);
  return result;
}
const useTopic = (): Api.TopicView | undefined => {
  const views: Record<Api.TopicId, Api.TopicView> = useViews();
  const topicId = useContext().topic;
  return topicId ? views[topicId] : undefined;
}
const useLink = (linkId: string): Api.TopicLink => {
  const site = useSite();
  if(!site){
    throw new Error("Site not loaded")
  }
  return site.links[linkId];
}
const useBlob = (): Api.Blob | undefined => {
  const topic = useTopic();
  return topic ? topic.blob : undefined;
}

export { useContext, useTopic, useLink , useBlob};