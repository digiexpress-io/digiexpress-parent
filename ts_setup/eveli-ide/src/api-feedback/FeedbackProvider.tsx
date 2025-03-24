import React from 'react';
import { FeedbackApi } from './feedback-types';
import { useFetch } from '@dxs-ts/eveli-fetch';

export interface FeedbackContextType {
  getOneTemplate: (taskId: FeedbackApi.TaskId) => Promise<FeedbackApi.FeedbackTemplate>;
  createOneFeedback: (taskId: FeedbackApi.TaskId, body: FeedbackApi.CreateFeedbackCommand) => Promise<FeedbackApi.Feedback>;
  modifyOneFeedback: (taskId: FeedbackApi.TaskId, body: FeedbackApi.ModifyOneFeedbackCommand) => Promise<FeedbackApi.Feedback>;
  rankOneFeedback: (taskId: FeedbackApi.TaskId, body: FeedbackApi.UpsertFeedbackRankingCommand) => Promise<FeedbackApi.Feedback>;
  findAllFeedback: () => Promise<FeedbackApi.Feedback[]>;
  getOneFeedback: (taskId: FeedbackApi.TaskId) => Promise<FeedbackApi.Feedback | undefined>;
  isTaskFeedbackEnabled: (taskId: FeedbackApi.TaskId) => Promise<true | false>;
  deleteOneFeedback: (taskId: FeedbackApi.TaskId) => Promise<FeedbackApi.Feedback>;
}

export const FeedbackContext = React.createContext<FeedbackContextType>({} as any);



export interface FeedbackProviderProps {
  children: React.ReactNode;
}
export const FeedbackProvider: React.FC<FeedbackProviderProps> = (props) => {
  const { getOneTemplate } = useFetch('worker/rest/api/feedback/$feedbackId/templates.GET', {});
  const { isTaskFeedbackEnabled } = useFetch('worker/rest/api/feedback/$feedbackId/enabled.GET', {});
  const { createOneFeedback } = useFetch('worker/rest/api/feedback/$feedbackId.POST', {});
  const { deleteOneFeedback } = useFetch('worker/rest/api/feedback/$feedbackId.DELETE', {})
  const { modifyOneFeedback, rankOneFeedback } = useFetch('worker/rest/api/feedback/$feedbackId.PUT', {})
  const { findAllFeedback } = useFetch('worker/rest/api/feedback.GET', {});
  const { getOneFeedback } = useFetch('worker/rest/api/feedback/$feedbackId.GET', {});


  // create the context 
  const contextValue: FeedbackContextType = React.useMemo(() => {
    // return all methods
    return {
      getOneTemplate, 
      createOneFeedback, 
      findAllFeedback, 
      getOneFeedback, 
      deleteOneFeedback, 
      modifyOneFeedback, 
      rankOneFeedback,
      isTaskFeedbackEnabled
    };
  }, [
    getOneTemplate, 
    createOneFeedback, 
    findAllFeedback, 
    getOneFeedback, 
    deleteOneFeedback, 
    modifyOneFeedback, 
    rankOneFeedback,
    isTaskFeedbackEnabled
  ]);

  return (<FeedbackContext.Provider value={contextValue}>{props.children}</FeedbackContext.Provider>);
}

export function useFeedback() {
  const result: FeedbackContextType = React.useContext(FeedbackContext);
  return result;
}
