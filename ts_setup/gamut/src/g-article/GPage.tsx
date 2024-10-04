import React from 'react';
import { SiteApi } from '../api-site';
import { GMarkdown } from '../g-md';

export const GPage: React.FC<{ children: SiteApi.TopicView | undefined }> = ({ children }) => {
  return (<GMarkdown>{children?.blob?.value}</GMarkdown>);
}