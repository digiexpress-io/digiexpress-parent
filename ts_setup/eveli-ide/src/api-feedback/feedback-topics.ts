import { useLocale } from "@/api-locale";
import { FeedbackApi } from "./feedback-types";
import React from "react";
import { useIntl } from "react-intl";

const mainGroupId = 'feedback.main_topic';
const subGroupId = 'feedback.sub_topic';


function useFeedBackIntlTopic() {
  const { messages } = useLocale();

  const codes = React.useMemo(() => {
    const mainKeys: string[] = [];
    const subKeys: string[] = [];



    Object.entries(messages)
      .flatMap(([, translations]) => Object.keys(translations))
      .forEach(key => {

        if (key.startsWith(mainGroupId) && !mainKeys.includes(key)) {
          mainKeys.push(key);
        }
        if (key.startsWith(subGroupId) && !subKeys.includes(key)) {
          subKeys.push(key);
        }
      });
    return { mainKeys, subKeys };
  }, [messages]);

  return codes;
}


export function useFeedbackTopics() {
  const intl = useIntl();
  const codes = useFeedBackIntlTopic();
  

  async function getFeedbackTopics(templateOrFeedback: FeedbackApi.FeedbackContent): Promise<FeedbackApi.FeedbackTopic> {
    const main: FeedbackApi.FeedbackTopicItem[] = [];
    const sub: FeedbackApi.FeedbackTopicItem[] = [];
    
    const labelKeyJunk = templateOrFeedback.labelKey.toLocaleLowerCase()
    const labelKey = codes.mainKeys.find(e => e === labelKeyJunk)
    
    const subLabelKeyJunk = templateOrFeedback.subLabelKey?.toLocaleLowerCase();
    const subLabelKey = codes.subKeys.find(e => e === subLabelKeyJunk)


    if (!labelKey && labelKeyJunk) {
      main.push({ labelKey: labelKeyJunk, labelValue: '*' + (labelKeyJunk ?? '') })
    }

    if (!subLabelKey && subLabelKeyJunk) {
      sub.push({ labelKey: subLabelKeyJunk, labelValue: '*' + (subLabelKeyJunk ?? '') })
    }

    codes.mainKeys.map(labelKey => ({
      labelKey: labelKey.toLocaleLowerCase(),
      labelValue: intl.formatMessage({ id: labelKey, defaultMessage: labelKey })
    })).forEach(e => main.push(e));

    codes.subKeys.map(labelKey => ({
      labelKey: labelKey.toLocaleLowerCase(),
      labelValue: intl.formatMessage({ id: labelKey, defaultMessage: labelKey })
    })).forEach(e => sub.push(e));
    return { main, sub };
  }
  return { getFeedbackTopics }
}

