import React from "react";
import { Box, Typography, Theme, useTheme, Grid2 } from "@mui/material";
import { SwitchRight as SwitchRightIcon } from '@mui/icons-material';
import { SwitchLeft as SwitchLeftIcon } from '@mui/icons-material';
import { Construction as ConstructionIcon } from '@mui/icons-material';

import { useIntl } from "react-intl";

import * as Burger from '@dxs-ts/eveli-primitives';
import { TagomiApi, TagomiComposerApi as Composer } from "@dxs-ts/tagomi-api";
import { ExplorerItemServiceTemplates, useTagomiNav } from "../tagomi-nav";

/*
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
*/

export const TemplateEditMenuItem: React.FC<{ service: Composer.ServiceView, template: Composer.TemplateView }> = (props) => {

  const theme = useTheme<Theme>();
  const { onNav, findTab } = useTagomiNav();
  const { session, site } = Composer.useComposer();
  const intl = useIntl();

  const template = props.template.template;
  const service = props.service.service;
  const locale = site.locales[template.localeId];
  const nav: ExplorerItemServiceTemplates | undefined = findTab('SERVICE_TEMPLATES', service.id) as any;
  const onLeftEdit = () => onNav({ article: service.id, type: "SERVICE_TEMPLATES", locale1: template.localeId })
  const localeLabelValue = service.labels.find(l => l.locale === template.localeId);

  const isPrimary = template.localeId === nav?.locale1;
  const activeColor = theme.palette.primary.main;

  /* 
      
    const isPageSaved = () => {
      const update = session.pages[itemId];
      if (!update) {
        return true;
      }
      return update.saved;
    }
  
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
  
      */



  return (
    <Burger.TreeItemRoot itemId={template.id} onClick={onLeftEdit}
      label={
        <Grid2 container display='flex' sx={{ alignItems: 'center' }}>
          {/* Show 2-letter locale code and dev mode if applicable */}
          <Grid2 size={{ md: 5, lg: 5, xl: 5 }} display='flex'>
            <Typography variant="body2" sx={{ fontWeight: "bold", mr: 1 }}>
              {locale.localeCode}
              {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
            </Typography>
            <Typography variant="body2" sx={{ fontWeight: "inherit", ml: 1 }}>{localeLabelValue?.labelValue}</Typography>
          </Grid2>

          {/* Edit primary */}
          <Grid2 size={{ md: 3, lg: 3, xl: 3 }} display='flex' sx={{ alignItems: 'center' }} color={isPrimary ? activeColor : "inherit"}>

            {isPrimary ?
              <>
                <SwitchRightIcon />
                <Typography variant='subtitle2' fontWeight='bold'>{intl.formatMessage({ id: 'tagomi.template.edit.currentlyEditing.language1' })}</Typography>
              </>
              :
              <>
                <SwitchRightIcon sx={{ visibility: 'hidden' }} />
                <Typography variant='subtitle2'>{intl.formatMessage({ id: 'tagomi.template.edit.language1' })}</Typography>
              </>
            }
          </Grid2>

        </Grid2>
      }
    />
  );
}

