import React from 'react';
import { FeedbackApi } from './feedback-types';
import { useFeedbackTopics } from './feedback-topics';

export interface FeedbackBackend {
  getOneTemplate: (taskId: FeedbackApi.TaskId) => Promise<FeedbackApi.FeedbackTemplate>;
  createOneFeedback: (taskId: FeedbackApi.TaskId, body: FeedbackApi.CreateFeedbackCommand) => Promise<FeedbackApi.Feedback>;
  modifyOneFeedback: (taskId: FeedbackApi.TaskId, body: FeedbackApi.ModifyOneFeedbackCommand) => Promise<FeedbackApi.Feedback>;
  rankOneFeedback: (taskId: FeedbackApi.TaskId, body: FeedbackApi.UpsertFeedbackRankingCommand) => Promise<FeedbackApi.Feedback>;
  findAllFeedback: () => Promise<FeedbackApi.Feedback[]>;
  getOneFeedback: (taskId: FeedbackApi.TaskId) => Promise<FeedbackApi.Feedback | undefined>;
  isTaskFeedbackEnabled: (taskId: FeedbackApi.TaskId) => Promise<true | false>;
  deleteOneFeedback: (taskId: FeedbackApi.TaskId) => Promise<FeedbackApi.Feedback>;
  getFeedbackSentimentAndSubcategory: (feedbackId: string) => Promise<FeedbackApi.SentimentAndSubcategoryResponse | undefined>;
  getSimilarFeedback: (feedbackId: string) => Promise<FeedbackApi.SimilarityResponse | undefined>;
}


export interface FeedbackContextType extends FeedbackBackend {
  getFeedbackTopics: (templateOrFeedback: FeedbackApi.FeedbackContent) => Promise<FeedbackApi.FeedbackTopic>;
}

export const FeedbackContext = React.createContext<FeedbackContextType>({} as any);

export interface FeedbackProviderProps {
  children: React.ReactNode;
  backend: FeedbackBackend;
}

export const FeedbackProvider: React.FC<FeedbackProviderProps> = (props) => {

  const { getFeedbackTopics } = useFeedbackTopics();

  // create the context 
  const contextValue: FeedbackContextType = React.useMemo(() => ({
    ...props.backend, getFeedbackTopics    
  }), [props.backend, getFeedbackTopics]);

  return (<FeedbackContext.Provider value={contextValue}>{props.children}</FeedbackContext.Provider>);
}

export function useFeedback() {
  const result: FeedbackContextType = React.useContext(FeedbackContext);
  return result;
}
