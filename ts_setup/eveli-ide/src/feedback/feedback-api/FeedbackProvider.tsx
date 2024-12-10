import React from 'react';
import { FeedbackApi } from './feedback-types';

export interface FeedbackContextType {
  getOneTemplate: (taskId: FeedbackApi.TaskId) => Promise<FeedbackApi.FeedbackTemplate>;
  createOneFeedback: (taskId: FeedbackApi.TaskId, body: FeedbackApi.CreateFeedbackCommand) => Promise<FeedbackApi.Feedback>;
  findAllFeedback: () => Promise<FeedbackApi.Feedback[]>;
  getOneFeedback: (taskId: FeedbackApi.TaskId) => Promise<FeedbackApi.Feedback>;
  deleteOneFeedback: (taskId: FeedbackApi.TaskId) => Promise<FeedbackApi.Feedback>;
}

export const FeedbackContext = React.createContext<FeedbackContextType>({} as any);



export interface FeedbackProviderProps {
  children: React.ReactNode;
  fetchTemplateGET: FeedbackApi.FetchTemplateGET;
  fetchFeedbackPOST: FeedbackApi.FetchFeedbackPOST;
  fetchFeedbackGET: FeedbackApi.FetchFeedbackGET;
  fetchFeedbackDELETE: FeedbackApi.FetchFeedbackDELETE;
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

    function getOneFeedback(taskId: FeedbackApi.TaskId): Promise<FeedbackApi.Feedback> {
      return props.fetchFeedbackGET(taskId)
        .then(resp => {
          if(resp.ok) {
            return resp.json();
          }
          return undefined;
        });
    }

    function deleteOneFeedback(taskId: FeedbackApi.TaskId): Promise<FeedbackApi.Feedback> {
      return props.fetchFeedbackDELETE(taskId)
        .then(resp => resp.json());
    }

    // return all methods
    return {
      getOneTemplate, createOneFeedback, findAllFeedback, getOneFeedback, deleteOneFeedback
    };
  }, [props.fetchFeedbackGET, props.fetchFeedbackPOST, props.fetchTemplateGET]);

  return (<FeedbackContext.Provider value={contextValue}>{props.children}</FeedbackContext.Provider>);
}

export function useFeedback() {
  const result: FeedbackContextType = React.useContext(FeedbackContext);
  return result;
}
