import React from 'react';
import { Paper, Box, useTheme } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { DialobEditor } from 'components-dialob';
import * as colors from 'components-colors';
import Burger from 'components-burger';
import { StencilClient } from 'components-stencil';
import { SysConfigService } from 'descriptor-sys-config';
import { useSysConfig } from '../SysConfigContext';


const StencilRelease: React.FC<{ index: number }> = ({ index }) => {
  const { sysConfig, stencilSite } = useSysConfig();
  if (!sysConfig) {
    return null;
  }
  if (!stencilSite) {
    return null;
  }
  const service = sysConfig.services[index];

  return <Box>
    <StencilArticles release={stencilSite} service={service} />
  </Box>

}

const StencilArticles: React.FC<{ release: StencilClient.Release, service: SysConfigService }> = ({ release, service }) => {

  const workflow = release.body.workflows.find(wk => wk.value === service.serviceName);

  if (!workflow) {
    return (<FormattedMessage id='releaseMgmt.stencil.articles.noneAvailable' />);
  }
  function handleArticleToggle() {

  }

  return <Box>
    <FormattedMessage id='releaseMgmt.stencil.articles' />
    {workflow.articles
      .flatMap(articleId => release.body.articles.filter(article => article.id === articleId))
      .map(article => (<Burger.SecondaryButton
        key={article.id}
        label={<>{article.name}</>} // prevent translation errors
        onClick={handleArticleToggle} />))}
  </Box>
}



export const SysConfigForm: React.FC<{ index: number }> = ({ index }) => {
  const theme = useTheme();
  const { sysConfig } = useSysConfig();
  const [active, setActive] = React.useState(false);
  function handleFormToggle() {
    setActive((current) => !current);
  }

  if (!sysConfig) {
    return null;
  }
  const service = sysConfig.services[index];

  return (
    <>
      {active ? <DialobEditor onClose={handleFormToggle} form={{ _id: service.formId }} entry={{ tenantId: "whatever-thx-mikki" }} /> : null}
      <Paper sx={{ minHeight: '100px' }} >
        <Box display="flex">
          <Box sx={{
            minHeight: "100px",
            width: theme.spacing(1),
            backgroundColor: active ? colors.cyan : undefined,
            borderTopLeftRadius: theme.spacing(0.5),
            borderBottomLeftRadius: theme.spacing(0.5)
          }} />

          <Box flexGrow={1}>
            <Box><FormattedMessage id='releaseMgmt.stencil.serviceName' values={{ serviceName: service.serviceName }} /></Box>
            <StencilRelease index={index} />
            <Box>
              <FormattedMessage id='releaseMgmt.dialob.form' />
              <Burger.SecondaryButton
                label={'releaseMgmt.dialobComposer.open'}
                onClick={handleFormToggle} />
            </Box>
          </Box>
        </Box>
      </Paper>
    </>
  );
}