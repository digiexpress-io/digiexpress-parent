import React from 'react';
import { List, Divider, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import Portal from '@the-stencil-io/portal';

import { LinkInternal } from './LinkInternal';
import { LinkPhone } from './LinkPhone';
import { LinkExternal } from './LinkExternal';


interface LinksProps {
}

const Links: React.FC<LinksProps> = () => {
  const { topic, site } = Portal.useSite();  
  
  if (!topic || !site) {
    return null;
  }

  const links = topic.links.map(link => site.links[link]);
  const phones = links.filter(link => link.type === "phone").map((link, index) => <LinkPhone key={index}>{link}</LinkPhone>);
  const internal = links.filter(link => link.type === "internal").map((link, index) => <LinkInternal key={index}>{link}</LinkInternal>)
  const external = links.filter(link => link.type === "external").map((link, index) => <LinkExternal key={index}>{link}</LinkExternal>)

  return (<>
    <Typography variant="h4" style={{ paddingTop: '10px' }}>
      <FormattedMessage id='links.title' defaultMessage='Links' />
      {phones.length === 0 ? null : <><List>{phones}</List><Divider /></>}
      {internal.length === 0 ? null : <><List>{internal}</List><Divider /></>}
      {external.length === 0 ? null : <><List>{external}</List></>}</Typography>
  </>)
}

export { Links }

