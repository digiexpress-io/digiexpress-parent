import React from 'react';

import { Tabs, Tab, Box } from '@mui/material';

import { FormattedMessage } from 'react-intl';
import { useSnackbar } from 'notistack';

import ArticleOutlinedIcon from '@mui/icons-material/ArticleOutlined';
import DashboardIcon from '@mui/icons-material/Dashboard';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';
import SearchOutlinedIcon from '@mui/icons-material/SearchOutlined';
import SaveIcon from '@mui/icons-material/Save';
import HomeOutlinedIcon from '@mui/icons-material/HomeOutlined';

import * as Burger from '@/burger';
import { Composer } from './context';




const Toolbar: React.FC<{}> = () => {

  const composer = Composer.useComposer();
  const tabs = Burger.useTabs();
  const [secondary, setSecondary] = React.useState<string>();
  const { enqueueSnackbar } = useSnackbar();

  //const articlePagesView = active?.data?.nav?.type === "ARTICLE_PAGES";
  const unsavedPages = Object.values(composer.session.pages).filter(p => !p.saved);
  const saveSx = unsavedPages.length ? { color: "secondary.light" } : undefined;


  const handleChange = (_event: React.SyntheticEvent, newValue: string) => {
    if (newValue === 'toolbar.save' && unsavedPages) {
      if (unsavedPages.length === 0) {
        return;
      }
      const active = tabs.session.tabs.length ? tabs.session.tabs[tabs.session.history.open] : undefined;
      
      const article = active ? composer.session.getEntity(active.id) : undefined;
      if(!article) {
        return;
      }
      const toBeSaved = unsavedPages.filter(p => !p.saved).filter(p => p.origin.id === article.id);
      if(toBeSaved.length !== 1) {
        return;
      }
      
      const unsavedArticlePages: Composer.PageUpdate = toBeSaved[0];
      composer.service.update(article.id, unsavedArticlePages.value).then(success => {
        composer.actions.handlePageUpdateRemove([article.id]);
        enqueueSnackbar(<FormattedMessage id="activities.assets.saveSuccess" values={{ name: article.ast?.name }} />);
        composer.actions.handleLoadSite(success);
      }).catch((error) => {
        
      });

    } else if (newValue === 'toolbar.activities') {
      tabs.handleTabAdd({ id: 'activities', label: "Activities" });

    } else if (newValue === 'toolbar.assets') {
      setSecondary("toolbar.assets")

    } else if (newValue === 'toolbar.search') {
      setSecondary("toolbar.search")

    } else if (newValue === 'toolbar.import') {
      tabs.handleTabAdd({ id: 'import', label: 'Import' })

    }
  };

    // open dashboard
    React.useLayoutEffect(() => {
      tabs.handleTabAdd({ id: 'activities', label: "Activities" });
    }, []);


  return (
    <Tabs orientation='vertical' onChange={handleChange} value={secondary}>
      <Tab value='toolbar.activities' icon={<DashboardIcon />}/>
      <Tab value='toolbar.save'
        icon={<SaveIcon sx={saveSx} />}
        disabled={unsavedPages.length === 0}
        label={unsavedPages.length ? (<Box sx={saveSx}>{unsavedPages.length}</Box>) : undefined} />
      <Tab value='toolbar.search' icon={<SearchOutlinedIcon />} />
      <Tab value='toolbar.assets' icon={<ArticleOutlinedIcon />} />
      <Tab value='toolbar.help' icon={<HelpOutlineOutlinedIcon />} />
    </Tabs>
  );
}


export default Toolbar;