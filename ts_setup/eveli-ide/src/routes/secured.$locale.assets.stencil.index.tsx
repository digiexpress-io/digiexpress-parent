import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale } from '@/burger'
import { StencilClient, Main, Toolbar, Secondary } from '../stencil';
import { Composer, StencilApi } from '../stencil/context';
import { useFetch } from '@dxs-ts/eveli-fetch';
import { EveliApp, useTabs, OneTab } from '@/burger';
import { useNavigate, useSearch } from '@tanstack/react-router';


export type ExplorerItem = 'ARTICLES' | 'PAGES' | 'SERVICES' | 'LINKS' | 'LOCALES' | 'MIGRATIONS' | 'TEMPLATES' | 'RELEASES';
export interface StencilRouteParams {
  explorer: ExplorerItem[]
}

export const Route = createFileRoute('/secured/$locale/assets/stencil/')({
  component: Component,
  validateSearch: (search: Record<string, unknown>): StencilRouteParams => {
    // validate and parse the search params into a typed state
    return {
      explorer: (search.explorer as ExplorerItem[]) || ['ARTICLES'],
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
        
        const explorer = [...prev.explorer].filter(t => t !== tab.id.toUpperCase());
        const newItem: ExplorerItem | undefined = nextActive?.id.toUpperCase() as any;

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
    tabs.handleTabAddAll(explorer.map(tabId => ({ id: tabId.toLowerCase(), label: tabId.toLowerCase() })));
  }, []);
  return (<></>)
}

export function useTabNav(): { 
  activeItem: ExplorerItem;
  onNav: (newItem: ExplorerItem) => void;
} {
  const navigate = useNavigate();
  const tabs = useTabs();
  
  const { explorer } = useSearch({ from: '/secured/$locale/assets/stencil/' });
  const activeItem = explorer[explorer.length - 1];

  function onNav(newItem: ExplorerItem) {
    tabs.handleTabAdd({ id: newItem.toLowerCase(), label: newItem.toLowerCase() });

    navigate({ 
      from: '/secured/$locale/assets/stencil', 
      search: (prev) => {
        
        const explorer = [...prev.explorer];
        if(!prev.explorer.includes(newItem)) {
          explorer.push(newItem);
        }
        // set item as last
        const itemIndex = explorer.indexOf(newItem);
        if(itemIndex !== explorer.length - 1) {
          delete explorer[itemIndex];
          explorer.push(newItem);
        }
  
        return { ...prev, explorer: explorer.filter(e => !!e) };
      }
    });
  }
  return { activeItem, onNav }
}
