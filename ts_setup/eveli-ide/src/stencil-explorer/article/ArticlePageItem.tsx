import React from "react";
import { Box, Typography, Theme, useTheme, Grid2 } from "@mui/material";
import SwitchRightIcon from '@mui/icons-material/SwitchRight';
import SwitchLeftIcon from '@mui/icons-material/SwitchLeft';
import ConstructionIcon from '@mui/icons-material/Construction';

import * as Burger from '@/eveli-styles';
import { StencilComposerApi as Composer } from '../../stencil-setup';
import { ExplorerItemArticlePages, useStencilNav } from '../../stencil-nav';
import { StencilApi } from "../../api-stencil";
import { useIntl } from "react-intl";



const SecondaryLabel: React.FC<{ article: StencilApi.Article, page: StencilApi.Page }> = (props) => {
  const theme = useTheme();
  const page = props.page;
  const article = props.article;
  const intl = useIntl();

  const { findTab } = useStencilNav();
  const nav: ExplorerItemArticlePages | undefined = findTab('ARTICLE_PAGES', article.id) as any;
  const isPrimarySelected = nav?.locale1 === page.body.locale;
  const noPrimarySelected = !nav?.locale1;

  if (noPrimarySelected) {
    return <></>;
  } else if (isPrimarySelected) {
    return (
      <Box sx={{ display: 'flex', alignItems: 'center', cursor: 'not-allowed' }}>
        <SwitchLeftIcon sx={{ visibility: 'hidden' }} />
        <Typography variant='subtitle2' color={theme.palette.action.disabled}>{intl.formatMessage({ id: 'pages.edit.language2' })}</Typography>
      </Box>);
  } else if (nav?.locale2 === page.body.locale) {
    return (
      <Box sx={{ display: 'flex', alignItems: 'center' }}>
        <SwitchLeftIcon />
        <Typography variant='subtitle2' fontWeight='bold'>{intl.formatMessage({ id: 'pages.edit.currentlyEditing.language2' })}</Typography>
      </Box>);
  } else {
    return (
      <Box sx={{ display: 'flex', alignItems: 'center' }}>
        <SwitchLeftIcon style={{ visibility: 'hidden' }} />
        <Typography variant='subtitle2'>{intl.formatMessage({ id: 'pages.edit.language2' })}</Typography>
      </Box>);
  }
}


const ArticlePageItem: React.FC<{ article: Composer.ArticleView, page: Composer.PageView }> = (props) => {

  const theme = useTheme<Theme>();
  const { onNav, findTab } = useStencilNav();
  const { session } = Composer.useComposer();
  const intl = useIntl();

  const page = props.page.page;
  const article = props.article.article;
  const itemId = props.page.page.id
  const activeColor = theme.palette.primary.main;
  const nav: ExplorerItemArticlePages | undefined = findTab('ARTICLE_PAGES', article.id) as any;



  const isPageSaved = () => {
    const update = session.pages[itemId];
    if (!update) {
      return true;
    }
    return update.saved;
  }

  const onLeftEdit = () => onNav({ article: article.id, type: "ARTICLE_PAGES", locale1: page.body.locale })
  const onRightEdit = () => {

    // Same locale on the right side
    if (nav?.locale1 && nav?.locale1 === page.body.locale) {
      return;
    }

    // Close the locale     
    if (nav?.locale2 === page.body.locale) {
      onNav({ article: article.id, type: "ARTICLE_PAGES", locale1: nav!.locale1, locale2: undefined })
    } else {
      onNav({ article: article.id, type: "ARTICLE_PAGES", locale1: nav!.locale1, locale2: page.body.locale })
    }
  }

  const isPrimary = nav?.locale1 === page.body.locale;

  return (
    <Burger.TreeItemRoot itemId={itemId} onClick={onLeftEdit}
      label={
        <Grid2 container display='flex' sx={{ alignItems: 'center' }}>
          {/* Show 2-letter locale code and dev mode if applicable */}
          <Grid2 size={{ md: 2, lg: 2, xl: 2 }} display='flex'>
            <Typography variant="body2" sx={{ fontWeight: "inherit", mr: 1 }}>
              {props.page.locale?.body.value}
            </Typography>
            {page.body.devMode && <ConstructionIcon color='error' />}
          </Grid2>

          {/* Edit primary */}
          <Grid2 size={{ md: 5, lg: 5, xl: 5 }} display='flex' sx={{ alignItems: 'center' }} color={isPrimary ? activeColor : "inherit"}>

            {isPrimary ?
              <>
                <SwitchRightIcon />
                <Typography variant='subtitle2' fontWeight='bold'>{intl.formatMessage({ id: 'pages.edit.currentlyEditing.language1' })}</Typography>
              </>
              :
              <>
                <SwitchRightIcon sx={{ visibility: 'hidden' }} />
                <Typography variant='subtitle2'>{intl.formatMessage({ id: 'pages.edit.language1' })}</Typography>
              </>
            }
          </Grid2>

          {/* Edit secondary - can edit only if isPrimary is true */}
          <Grid2 size={{ md: 5, lg: 5, xl: 5 }} sx={{ display: "flex", alignItems: "center" }}
            color={nav?.locale2 === page.body.locale ? activeColor : "inherit"}
            onClick={(event) => {
              event.stopPropagation();
              onRightEdit();
            }}
          >
            <Grid2><SecondaryLabel article={article} page={page} /></Grid2>
          </Grid2>
        </Grid2 >
      }
    />
  );
}

export default ArticlePageItem;
