import React from 'react';
import { Box, IconButton, Popover, Typography, Tooltip, ListItem } from '@mui/material';
import { useSnackbar } from 'notistack';

import Burger from '@the-wrench-io/react-burger';

import { Composer, StencilClient } from '../context';
import PageviewOutlinedIcon from '@mui/icons-material/PageviewOutlined';
import { FormattedMessage } from 'react-intl';

const DUMMY_ID = "none-selected"


const OrderNumberTooltip: React.FC<{}> = () => {
  const { session } = Composer.useComposer();
  const [anchorEl, setAnchorEl] = React.useState(null);


  const handlePopover = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handlePopoverClose = () => {
    setAnchorEl(null);
  };

  const open = Boolean(anchorEl);

  return (<>
    <Tooltip title={<FormattedMessage id="article.order.view" />}>
      <IconButton sx={{ ml: 2, color: 'uiElements.main' }}  onClick={handlePopover}>
        <PageviewOutlinedIcon fontSize="large" />
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
           <ListItem key={id} sx={ body.parentId ? { ml: 2, color: 'article.dark', pb: 1,  } : {pb: 1} }>{`${body.order} - ${body.name}`}</ListItem>
          ))}
      </Typography>
    </Popover>
  </>
  )

}


const ArticleComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {

  const { service, actions, session } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();
  const [name, setName] = React.useState("");
  const [order, setOrder] = React.useState(0);
  const [parentId, setParentId] = React.useState("");
  const [devMode, setDevMode] = React.useState<boolean>(false);
    
  const handleCreate = () => {
    const entity: StencilClient.CreateArticle = { name, parentId: parentId && parentId !== DUMMY_ID ? parentId : undefined, order, devMode };

    service.create().article(entity).then(success => {
      console.log(success)
      enqueueSnackbar(message, {variant: 'success'});
      onClose();
      actions.handleLoadSite();
    });
  }

  const message = <FormattedMessage id="snack.article.createdMessage" values={{ name }} />

  return (
    <Burger.Dialog open={true} onClose={onClose}
      backgroundColor="uiElements.main" title="article.composer.title"
      submit={{ title: "article.create", onClick: handleCreate, disabled: !name }}>
      <>

        <Burger.Select label="article.composer.parent"
          helperText={"article.parent.helper"}
          selected={parentId}
          onChange={setParentId}
          empty={{ id: DUMMY_ID, label: 'article.composer.parent.unselected' }}
          items={session.articles
            .map(view => view.article)
            .map(({ id, body }) => ({
              id,
              value: (<Box sx={ body.parentId ? { ml: 2, color: 'article.dark' } : undefined }>{`${body.order} - ${body.name}`}</Box>)
            }))}
        />
        <Box display='flex'>
          <Box>
            <Burger.NumberField label="article.order" helperText='article.composer.orderhelper'
              onChange={setOrder}
              value={order}
              placeholder={400}
            />
          </Box>
          <Box sx={{width: '10%'}}>
            <OrderNumberTooltip />
          </Box>
        </Box>
        <Burger.TextField label="article.name" required
          value={name}
          onChange={setName}
        />
        <Box maxWidth="50%" sx={{ ml: 1 }}>
          <Burger.Switch
            checked={devMode}
            helperText="article.devmode.helper"
            label="article.devmode"
            onChange={setDevMode}
          />
        </Box>
      </>
    </Burger.Dialog>
  );
}

export { ArticleComposer }