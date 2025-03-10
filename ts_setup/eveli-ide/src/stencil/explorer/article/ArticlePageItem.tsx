import React from "react";
import { Box, Typography, Theme, useTheme } from "@mui/material";

import LeftEditIcon from "@mui/icons-material/BorderLeft";
import RightEditIcon from "@mui/icons-material/BorderRight";
import ConstructionIcon from '@mui/icons-material/Construction';

import * as Burger from '@/burger';
import { Composer } from '../../context';
import { ExplorerItemArticlePages, useStencilNav } from '../../nav';



const ArticlePageItem: React.FC<{ article: Composer.ArticleView, page: Composer.PageView, saved?: boolean }> = (props) => {

  const theme = useTheme<Theme>();
  const localeIconColor = theme.palette.secondary.contrastText;


  const { onNav, findTab } = useStencilNav();

  const page = props.page.page;
  const article = props.article.article;
  const itemId = props.page.page.id
  const nav: ExplorerItemArticlePages | undefined = findTab('ARTICLE_PAGES', article.id) as any;

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

  const icon = () => {
    if (page.body.devMode && page.body.devMode === true) {
      return <ConstructionIcon />
    } else {
      return <LeftEditIcon />
    }
  }

  return (
    <Burger.TreeItemRoot
      itemId={itemId}
      onClick={onLeftEdit}
      label={
        <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
          <Box component={icon} color={props.saved === false ? 
            "secondary.light": 
            (nav?.locale1 === page.body.locale ? localeIconColor : "inherit")} />
          
          <Typography
            variant="body2"
            sx={{ fontWeight: "inherit", flexGrow: 1, pl: 1 }}
          >
            {props.page.locale?.body.value}
          </Typography>

          <Box component={RightEditIcon}
            color={nav?.locale2 === page.body.locale ? localeIconColor : "inherit"}
            onClick={(event) => {
              event.stopPropagation()
              onRightEdit();
            }}
          />
        </Box>
      }
    />
  );
}

export default ArticlePageItem;
