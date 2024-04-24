import React from 'react';
import { Paper, Box, useTheme } from '@mui/material';


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
    return <Box>articles: not available</Box>;
  }
  function handleArticleToggle() {

  }

  return <Box>
    articles: {workflow.articles
      .flatMap(articleId => release.body.articles.filter(article => article.id === articleId))
      .map(article => (<Burger.SecondaryButton
        label={article.name}
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
            <Box>service name: {service.serviceName}</Box>
            <StencilRelease index={index} />
            <Box>form:
              <Burger.SecondaryButton
                label={"assetMgmt.dialobComposer.open"}
                onClick={handleFormToggle} />
            </Box>

          </Box>
        </Box>
      </Paper>
    </>
  );
}