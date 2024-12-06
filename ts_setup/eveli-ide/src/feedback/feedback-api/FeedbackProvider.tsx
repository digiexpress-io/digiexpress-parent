import React from 'react';
import { FeedbackApi } from './feedback-types';

export interface FeedbackContextType {
  getOneTemplate: (taskId: FeedbackApi.TaskId) => Promise<FeedbackApi.FeedbackTemplate>;
  createOneFeedback: (taskId: FeedbackApi.TaskId, body: FeedbackApi.CreateFeedbackCommand) => Promise<FeedbackApi.Feedback>;
  findAllFeedback: () => Promise<FeedbackApi.Feedback[]>;
}

export const FeedbackContext = React.createContext<FeedbackContextType>({} as any);



export interface FeedbackProviderProps {
  children: React.ReactNode;
  fetchTemplateGET: FeedbackApi.FetchTemplateGET;
  fetchFeedbackPOST: FeedbackApi.FetchFeedbackPOST;
  fetchFeedbackGET: FeedbackApi.FetchFeedbackGET;
}
export const FeedbackProvider: React.FC<FeedbackProviderProps> = (props) => {

  // create the context 
  const contextValue: FeedbackContextType = React.useMemo(() => {

    function getOneTemplate(taskId: FeedbackApi.TaskId): Promise<FeedbackApi.FeedbackTemplate> {
      return props.fetchTemplateGET(taskId)
        .then(resp => resp.json());
    }

    function createOneFeedback(taskId: FeedbackApi.TaskId, body: FeedbackApi.CreateFeedbackCommand): Promise<FeedbackApi.Feedback> {
      return props.fetchFeedbackPOST(taskId, body)
        .then(resp => resp.json());
    }

    function findAllFeedback(): Promise<FeedbackApi.Feedback[]> {
      return props.fetchFeedbackGET()
        .then(resp => resp.json());
    }


    // return all methods
    return {
      getOneTemplate, createOneFeedback, findAllFeedback
    };
  }, [props.fetchFeedbackGET, props.fetchFeedbackPOST, props.fetchTemplateGET]);

  return (<FeedbackContext.Provider value={contextValue}>{props.children}</FeedbackContext.Provider>);
}

export function useFeedback() {
  const result: FeedbackContextType = React.useContext(FeedbackContext);
  return result;
}
