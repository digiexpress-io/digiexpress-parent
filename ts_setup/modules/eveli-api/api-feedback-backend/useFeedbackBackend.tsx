import React from "react";
import { useFetch } from "@dxs-ts/envir-fetch";
import { FeedbackBackend } from "@dxs-ts/task-feedback";
import { useIam } from "../api-iam";



export function useFeedbackBackend(): FeedbackBackend {
  const { user } = useIam();
  const { getOneTemplate } = useFetch('worker/rest/api/feedback/$feedbackId/templates.GET', {});
  const { isTaskFeedbackEnabled } = useFetch('worker/rest/api/feedback/$feedbackId/enabled.GET', {});
  const { createOneFeedback } = useFetch('worker/rest/api/feedback/$feedbackId.POST', {});
  const { deleteOneFeedback } = useFetch('worker/rest/api/feedback/$feedbackId.DELETE', {})
  const { modifyOneFeedback, rankOneFeedback } = useFetch('worker/rest/api/feedback/$feedbackId.PUT', {})
  const { findAllFeedback } = useFetch('worker/rest/api/feedback.GET', {});
  const { getOneFeedback } = useFetch('worker/rest/api/feedback/$feedbackId.GET', {});
  const { getFeedbackSentimentAndSubcategory } = useFetch('worker/rest/api/feedback/$feedbackId/sentiment-and-subcategory.GET', {});
  const { getSimilarFeedback } = useFetch('worker/rest/api/feedback/$feedbackId/similar.GET', {});

  return React.useMemo(() => ({
    getOneTemplate,
    createOneFeedback,
    findAllFeedback,
    getOneFeedback,
    deleteOneFeedback,
    modifyOneFeedback,
    rankOneFeedback,
    isTaskFeedbackEnabled,
    getFeedbackSentimentAndSubcategory,
    getSimilarFeedback,
  }), [user]);
}