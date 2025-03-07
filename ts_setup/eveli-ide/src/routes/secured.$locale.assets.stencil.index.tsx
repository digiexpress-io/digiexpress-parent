import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale } from '@/burger'
import { StencilClient, Main, Toolbar, Secondary } from '../stencil';
import { Composer, StencilApi } from '../stencil/context';
import { useFetch } from '@dxs-ts/eveli-fetch';
import { EveliApp, useTabs, OneTab } from '@/burger';
import { useNavigate, useSearch } from '@tanstack/react-router';

export interface ExplorerItemArticlePages { 
  type: 'ARTICLE_PAGES', article: string, locale1: string, locale2?: string | undefined
}
export type ExplorerItem = (
  { type: 'ACTIVITIES' } | 
  { type: 'ARTICLES', article: string | undefined } | 
  //{ type: 'PAGES' } | 
  { type: 'SERVICES' } | 
  { type: 'LINKS' } | 
  { type: 'LOCALES' } | 
  { type: 'MIGRATIONS' } | 
  { type: 'TEMPLATES' } | 
  { type: 'RELEASES' } |
  ExplorerItemArticlePages  |
  { type: 'ARTICLE_LINKS', article: string } |
  { type: 'ARTICLE_WORKFLOWS', article: string }
);

export type NavInput = (
  'ACTIVITIES'|
  'ARTICLES'|
  'SERVICES'|
  'LINKS'|
  'LOCALES'|
  'MIGRATIONS'|
  'TEMPLATES' |
  'RELEASES' |

  { type: 'ARTICLE_PAGES', article: string, locale1: string, locale2?: string | undefined } |
  { type: 'ARTICLE_LINKS', article: string } |
  { type: 'ARTICLE_WORKFLOWS', article: string }
)

export interface StencilRouteParams {
  explorer: ExplorerItem[]
}

export const Route = createFileRoute('/secured/$locale/assets/stencil/')({
  component: Component,
  validateSearch: (search: Record<string, unknown>): StencilRouteParams => {
    // validate and parse the search params into a typed state

    return {
      explorer: (search.explorer as ExplorerItem[]) || [{ type: 'ARTICLES' }],
    }
  },
}) 



function Component() {
  const { locale } = Route.useParams();
  const navigate = useNavigate();

  const { setLocale } = useLocale();
  React.useLayoutEffect(() => setLocale(locale), [locale])

  const { getSite } = useFetch('worker/rest/api/assets/stencil.GET', {});
  const { delete: del } = useFetch('worker/rest/api/assets/stencil/$assetType.DELETE', {});
  const { create } = useFetch('worker/rest/api/assets/stencil/$assetType.POST', {});
  const { update } = useFetch('worker/rest/api/assets/stencil/$assetType.PUT', {});
  const { getReleaseContent } = useFetch('worker/rest/api/assets/stencil/releases/$releaseId.GET', {});
  const { version } = useFetch('worker/rest/api/assets/stencil/version.GET', {});

  const service = React.useMemo(() => {
    const store: StencilApi.StencilRestApi = {getSite, delete: del, create, update, getReleaseContent, version};
    return StencilClient.service({ store });
  }, [getSite, del, create, update, getReleaseContent, version]);
  

  function handleOnTablClose(tab: OneTab<any>, nextActive: OneTab<any> | undefined) {
    navigate({ 
      from: '/secured/$locale/assets/stencil', 
      search: (prev) => {
        
        const explorer = [...prev.explorer].filter(t => toTab(t).id !== tab.id);
        const newItem: ExplorerItem | undefined = nextActive?.data;

        if(newItem) {
          const itemIndex = explorer.indexOf(newItem);
          if(itemIndex !== explorer.length - 1) {
            delete explorer[itemIndex];
            explorer.push(newItem);
          }
        }

        return { ...prev, explorer: explorer.filter(e => !!e) };
      }
    });
  }

  return (
    <Composer.Provider service={service} >
      <EveliApp main={Main} secondary={Secondary} toolbar={Toolbar} onTabClose={handleOnTablClose}>
        <LoadTabsFromSearchParams />
      </EveliApp>
    </Composer.Provider>)
}

function LoadTabsFromSearchParams() {
  const tabs = useTabs();
  const { explorer } = useSearch({ from: '/secured/$locale/assets/stencil/' });

  // load only once...
  React.useEffect(() => {
    tabs.handleTabAddAll(explorer.map(toTab));
  }, []);
  return (<></>)
}


export function useTabNav(): { 
  activeItem: ExplorerItem | undefined;
  onNav: (newItem: NavInput) => void;
  findTab: (newItem: ExplorerItem['type'], articleId?: string) => ExplorerItem | undefined;
} {
  const navigate = useNavigate();
  const tabs = useTabs();
  
  const { explorer } = useSearch({ from: '/secured/$locale/assets/stencil/' });
  const activeItem = explorer.find(explorer => toTab(explorer).id === tabs.session.activeTab?.id);

  function onNav(input: NavInput) {
    const newItem = toExplorerItem(input);
    const newTab = toTab(newItem);
    tabs.handleTabAdd(newTab);
    navigate({ 
      from: '/secured/$locale/assets/stencil', 
      search: (prev) => ({ ...prev, explorer: calculateNextSearch(newItem, prev.explorer) })
    });
  }

  function findTab(newItem: ExplorerItem['type'], articleId?: string): ExplorerItem | undefined {
    return tabs.session.tabs
      .filter(tab => tab.data?.type === newItem)
      .find(tab => articleId ? tab.data?.article === articleId : true)?.data;
  }

  return { activeItem, onNav, findTab }
}

function calculateNextSearch(newExplorerItem: ExplorerItem, prev: ExplorerItem[]): ExplorerItem[]  {
  const newItemId = toTab(newExplorerItem).id;
  const explorer = [...prev.filter(explorer => toTab(explorer).id !== newItemId), newExplorerItem];
  return explorer;
}


function toExplorerItem(input: NavInput): ExplorerItem {
  const data: ExplorerItem = ((input as any)['type'] ? input : { type: input }) as any;
  return data;
}

function toTab(data: ExplorerItem) {
  const id = JSON.stringify(Object.entries(data)
    .filter(([key]) => key === 'type' || key === 'article')
    .reduce((result, [key, value]) => result + "/" + value, ''));
  const label = data.type.toLowerCase();
  return { id, label, data };
}


/*
const ArticleTabIndicator: React.FC<{ article: StencilApi.Article, type: StencilComposerApi.NavType }> = ({ article }) => {
  const theme = useTheme();
  const { isArticleSaved } = StencilComposerApi.useComposer();
  const saved = isArticleSaved(article);
  return <span style={{
    paddingLeft: "5px",
    fontSize: '30px',
    color: theme.palette.secondary.light,
    display: saved ? "none" : undefined
  }}>*</span>
}
*/