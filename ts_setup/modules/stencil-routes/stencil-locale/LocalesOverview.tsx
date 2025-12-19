import React from 'react';
import { Table, Tooltip, Paper, TableHead, TableBody } from '@mui/material';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableRow from '@mui/material/TableRow';
import { CheckCircleOutline as CheckCircleOutlineIcon } from '@mui/icons-material';
import { ErrorOutline as ErrorOutlineIcon } from '@mui/icons-material';
import { FormattedMessage } from 'react-intl';

import { StencilApi } from '@dxs-ts/stencil-api';



const LocalesOverview: React.FC<{ site: StencilApi.Site }> = ({ site }) => {

  const locales: StencilApi.SiteLocale[] = Object.values(site.locales);
  const articles: StencilApi.Article[] = Object.values(site.articles);
  const pages: StencilApi.Page[] = Object.values(site.pages);


  //check if page has content
  const isContent = (locale: StencilApi.SiteLocale, article: StencilApi.Article) => {
    const contents = pages
      .filter(p => p.body.article === article.id)
      .filter(p => p.body.locale === locale.id)
      .filter(p => p.body.content);
    return contents.length > 0;
  }

  // check if locale exists on article
  const isLocale = (locale: StencilApi.SiteLocale, article: StencilApi.Article): boolean => {
    const articlePages = pages
      .filter(p => p.body.article === article.id)
      .filter(p => p.body.locale === locale.id);
    return articlePages.length > 0;
  }

  return (
    <TableContainer component={Paper}>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell colSpan={1 + locales.length}>
              <FormattedMessage id="locale.overview" />
            </TableCell>
          </TableRow>

          <TableRow>
            <TableCell sx={{ fontWeight: 'bold' }} align="left"><FormattedMessage id="article.name" /></TableCell>
            {locales.map((locale, index) => <TableCell key={index} sx={{ fontWeight: 'bold' }} align="left" >{locale.body.value}</TableCell>
            )}
          </TableRow>
        </TableHead>

        <TableBody>
          {articles.map((article, index) => (
            <TableRow key={index} sx={{ p: 1 }}>
              <TableCell align="left" >{article.body.name}</TableCell>
              {locales.map((locale, index) => (
                <TableCell key={index} sx={{ fontWeight: 'bold' }} align="left">
                  {isLocale(locale, article) && isContent(locale, article) ?
                    (<span><Tooltip title={<FormattedMessage id="locales.content" />}><CheckCircleOutlineIcon sx={{ color: 'primary.main' }} /></Tooltip></span>) :
                    isLocale(locale, article) === true ?
                      (<span><Tooltip title={<FormattedMessage id="locales.nocontent" />}><CheckCircleOutlineIcon sx={{ color: 'warning.main' }} /></Tooltip></span>) :
                      (<span><Tooltip title={<FormattedMessage id="locales.nopage" />}><ErrorOutlineIcon sx={{ color: 'error.main' }} /></Tooltip></span>)
                  }
                </TableCell>)
              )}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>


  );
}

export { LocalesOverview }

