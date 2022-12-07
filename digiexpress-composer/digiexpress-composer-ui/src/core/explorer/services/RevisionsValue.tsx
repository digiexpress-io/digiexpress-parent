import React from "react";
import { Box, Typography } from "@mui/material";

import HistoryIcon from '@mui/icons-material/History';
import FolderOutlinedIcon from '@mui/icons-material/FolderOutlined';
import ConstructionIcon from '@mui/icons-material/Construction';
import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined';
import ArticleOutlinedIcon from '@mui/icons-material/ArticleOutlined';
import EditIcon from '@mui/icons-material/ModeEdit';
import PriorityHighIcon from '@mui/icons-material/PriorityHigh';
import LowPriorityIcon from '@mui/icons-material/LowPriority';

import { FormattedMessage } from 'react-intl';

import Burger from '@the-wrench-io/react-burger';

import { Composer, Client } from '../../context';




const RevisionValue: React.FC<{ value: Record<Client.ServiceDocumentId, Client.ServiceRevisionDocument> }> = ({ value }) => {
  const { session } = Composer.useComposer();
  const nav = Composer.useNav();
  const saved = true;
  const ok = true;

    return (<Burger.TreeItem nodeId='explorer.services.revisionsItem' labelText={<FormattedMessage id='explorer.services.revisionsItem' />}
    labelIcon={HistoryIcon}
    labelInfo={<></>}
    labelcolor={saved ? "explorerItem" : "explorerItem.contrastText"}>

  </Burger.TreeItem>)
   
}

export default RevisionValue;
