import React from "react";
import { Box, Typography } from "@mui/material";

import FolderOutlinedIcon from '@mui/icons-material/FolderOutlined';
import ConstructionIcon from '@mui/icons-material/Construction';
import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined';
import ArticleOutlinedIcon from '@mui/icons-material/ArticleOutlined';
import EditIcon from '@mui/icons-material/ModeEdit';
import PriorityHighIcon from '@mui/icons-material/PriorityHigh';
import LowPriorityIcon from '@mui/icons-material/LowPriority';
import SummarizeIcon from '@mui/icons-material/Summarize';
import AppsIcon from '@mui/icons-material/Apps';
import { FormattedMessage, useIntl } from 'react-intl';

import Burger from '@the-wrench-io/react-burger';

import { Composer, Client } from '../../context';


const ID = 'explorer.services.headItem';


const RevisionValue: React.FC<{ value: Client.Site }> = ({ value }) => {
  const intl = useIntl();
  const nav = Composer.useNav();
  const saved = true;
  const ok = true;

  const config = Object.values(value.configs).find(() => true);
    if(!config) {
    console.log("Service explorer:: no config in site");
    return null;
  }

  const revision = Object.values(value.revisions).find(() => true);
  if(!revision) {
    console.log("Service explorer:: no revisions in site");
    return null;
  }
  const head = value.definitions[revision.head];
    if(!head) {
    console.log("Service explorer:: no head in site");
    return null;
  }
  
  

  return (<Burger.TreeItem nodeId={ID} labelText={<FormattedMessage id={ID} values={{ name: config.service.id }}/>}
    labelIcon={AppsIcon}
    labelInfo={<></>}
    labelcolor={saved ? "explorerItem" : "explorerItem.contrastText"}>

    <Burger.TreeItemOption nodeId={`${ID}.info1`}
      color='explorerItem'
      icon={SummarizeIcon}
      onClick={() => {}}
      labelText={<FormattedMessage id={`${ID}.info1`} values={{items: head.processes.length}}/>}>
    </Burger.TreeItemOption>


    <Burger.TreeItemOption nodeId={`${ID}.edit`}
      color='workflow'
      icon={EditIcon}
      onClick={() => nav.handleInTab({ article: head, name: intl.formatMessage({id: 'tabs.services'}) })}
      labelText={<FormattedMessage id="buttons.edit"/>}>
    </Burger.TreeItemOption>

  </Burger.TreeItem>)

}

export default RevisionValue;
