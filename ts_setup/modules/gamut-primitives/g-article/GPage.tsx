import React from 'react';
import { SiteApi } from '@dxs-ts/gamut-api';
import { GMarkdown } from '@dxs-ts/gamut-md';

export const GPage: React.FC<{ children: SiteApi.TopicView }> = ({ children }) => {
  return (<GMarkdown>{children?.blob?.value}</GMarkdown>);
}