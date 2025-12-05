
import { FeedbackApi } from "./feedback-types";
import React from "react";
import { useIntl } from "react-intl";

const mainGroupId = 'feedback.main_topic';


function useFeedBackIntlTopic() {
  const { messages } = useIntl();

  const codes = React.useMemo(() => {
    const mainKeys: string[] = [];

    Object.keys(messages)
      .filter(key => key.startsWith(mainGroupId) && !mainKeys.includes(key))
      .forEach(key => mainKeys.push(key));

    return mainKeys;
  }, [messages]);
  return codes;
}


export function useFeedbackTopics() {
  const intl = useIntl();
  const codes = useFeedBackIntlTopic();

  const getFeedbackTopics: (dirtyValue: string) => FeedbackApi.FeedbackTopic[] = React.useCallback((dirtyValue) => {

    const cleanedValue = (dirtyValue ?? '').trim().toLocaleLowerCase();
    const found = codes.find(topic => {
      const candidate = topic.toLocaleLowerCase();
      return candidate.endsWith(`.${cleanedValue}`) || candidate === cleanedValue;
    });


    const failSafe: FeedbackApi.FeedbackTopic[] = [];
    if (!found) {
      console.error('Feedback topic not found', { dirtyValue, codes })
      const missingValue = dirtyValue.includes(".") ? dirtyValue : `${mainGroupId}.${dirtyValue}`;
      failSafe.push({
        labelKey: missingValue,
        labelValue: '*' + missingValue,
        selected: true
      })
    }

    return [
      ...codes.map(labelKey => ({
        labelKey: labelKey.toLocaleLowerCase(),
        labelValue: intl.formatMessage({ id: labelKey, defaultMessage: labelKey }),
        selected: found === labelKey
      })),
      ...failSafe
    ];

  }, [intl.locale])

  return { getFeedbackTopics }
}

