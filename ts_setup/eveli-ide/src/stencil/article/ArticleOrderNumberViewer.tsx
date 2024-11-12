import React from 'react';
import { IconButton, Popover, Typography, Tooltip, ListItem } from '@mui/material';

import { Composer } from '../context';
import FormatListNumberedIcon from '@mui/icons-material/FormatListNumbered';
import { FormattedMessage } from 'react-intl';


export const ArticleOrderNumberViewer: React.FC<{}> = () => {
  const { session } = Composer.useComposer();
  const [anchorEl, setAnchorEl] = React.useState(null);


  const handlePopover = (event: React.MouseEvent<any>) => {
    setAnchorEl(event.currentTarget);
  };

  const handlePopoverClose = () => {
    setAnchorEl(null);
  };

  const open = Boolean(anchorEl);

  return (<>
    <Tooltip title={<FormattedMessage id="article.order.view" />}>
      <IconButton sx={{ ml: 2, color: 'uiElements.main' }} onClick={handlePopover}>
        <FormatListNumberedIcon fontSize="large" />
      </IconButton>
    </Tooltip>


    <Popover
      sx={{ ml: 2 }}
      open={open}
      onClose={handlePopoverClose}
      anchorEl={anchorEl}
      anchorOrigin={{
        vertical: 'top',
        horizontal: 'right',
      }}
      transformOrigin={{
        vertical: 'center',
        horizontal: 'left',
      }}
    >

      <Typography variant='body2' sx={{ p: 1 }}>
        {session.articles
          .map(view => view.article)
          .sort((l0, l1) => l0.body.order - l1.body.order)
          .map(({ id, body }) => (
            <ListItem key={id} sx={body.parentId ? { ml: 2, color: 'article.dark', pb: 1, } : { pb: 1 }}>{`${body.order} - ${body.name}`}</ListItem>
          ))}
      </Typography>
    </Popover>
  </>
  )

}