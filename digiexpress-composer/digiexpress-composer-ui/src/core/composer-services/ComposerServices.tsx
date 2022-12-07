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

import { Composer, Client } from '../context';


const ID = 'explorer.services.headItem';


const ComposerServices: React.FC<{ value: Client.ServiceDefinitionDocument }> = ({ value }) => {
  const intl = useIntl();
  const nav = Composer.useNav();

  

  return (<>M</>);

}

export default ComposerServices;
