import React from "react";
import { Box, Typography } from "@mui/material";


import PublishIcon from '@mui/icons-material/Publish';

import { FormattedMessage } from 'react-intl';

import Burger from '@the-wrench-io/react-burger';

import { Composer, Client } from '../../context';




const ReleasesValue: React.FC<{ value: Record<Client.ServiceDocumentId, Client.ServiceReleaseDocument> }> = ({ value }) => {
  const { session } = Composer.useComposer();
  const nav = Composer.useNav();
  const saved = true;
  const ok = true;

  return (<Burger.TreeItem nodeId='explorer.services.releasesItem' labelText={<FormattedMessage id="explorer.services.releasesItem" />}
    labelIcon={PublishIcon}
    labelInfo={<></>}
    labelcolor={saved ? "explorerItem" : "explorerItem.contrastText"}>

  </Burger.TreeItem>)


}

export default ReleasesValue;
