import React from 'react';

import { TagomiApi } from './TagomiApi'
import { ReducerDispatch, Reducer } from './Reducer';
import { SessionData } from './SessionData';

export declare namespace TagomiComposerApi {

  interface Session {
    site: TagomiApi.TagomiContainer,
    templates: Record<TagomiApi.TemplateId, TemplateUpdate>;
    services: ServiceView[];
    resources: ResourceView[];

    getServiceView(workflowId: TagomiApi.ServiceId): ServiceView;

    getServicesForLocale(locale: TagomiApi.LocaleId): TagomiApi.Service[];
    getServicesForLocales(locales: TagomiApi.LocaleId[]): TagomiApi.Service[];

    withTemplate(page: TagomiApi.TemplateId): Session;
    withTemplateValue(page: TagomiApi.TemplateId, value: TagomiApi.LocalisedContent): Session;
    withoutTemplates(pages: TagomiApi.TemplateId[]): Session;

    withSite(site: TagomiApi.TagomiContainer): Session;
  }

  interface Actions {
    handleLoad(): Promise<void>;
    handleLoadSite(): Promise<void>;
    handleTemplateUpdate(page: TagomiApi.TemplateId, value: TagomiApi.LocalisedContent): void;
    handleTemplateUpdateRemove(pages: TagomiApi.TemplateId[]): void;
  }

  interface ContextType {
    session: Session;
    actions: Actions;
    backend: TagomiApi.Backend;
  }

  interface TemplateUpdate {
    saved: boolean;
    origin: TagomiApi.Template;
    value: TagomiApi.LocalisedContent;
    withValue(value: TagomiApi.LocalisedContent): TemplateUpdate;
  }

  interface ServiceView {
    service: TagomiApi.Service;
    templates: TemplateView[];
    canCreate: TagomiApi.Locale[];
    resources: ResourceView[];
    displayOrder: number;
    labels: LabelView[]

    getTemplateById(pageId: TagomiApi.TemplateId): TemplateView;
    getTemplateByLocaleId(localeId: TagomiApi.LocaleId): TemplateView;
    findTemplateByLocaleId(localeId: TagomiApi.LocaleId): TemplateView | undefined;
  }

  interface TemplateView {
    title: string;
    template: TagomiApi.Template;
    locale: TagomiApi.Locale;
    resources: TagomiApi.Resource[];
  }

  interface LabelView {
    label: TagomiApi.LocaleAndLabel;
    locale: TagomiApi.Locale;
  }

  interface ResourceView {
    resource: TagomiApi.Resource;
    templates: TemplateView[];
  }
}



export namespace TagomiComposerApi {
  const sessionData = new SessionData({});

  export const ComposerContext = React.createContext<ContextType>({
    session: sessionData,
    actions: {} as Actions,
    backend: {} as TagomiApi.Backend
  });

  export const useUnsaved = (article: TagomiApi.Service) => {
    const ide: ContextType = React.useContext(ComposerContext);
    return !isSaved(article, ide);
  }

  const isSaved = (article: TagomiApi.Service, ide: ContextType): boolean => {
    const unsaved = Object.values(ide.session.templates).filter(p => !p.saved).filter(p => p.origin.serviceId === article.id);
    return unsaved.length === 0
  }

  export const useComposer = () => {
    const result: ContextType = React.useContext(ComposerContext);
    const isServiceSaved = (article: TagomiApi.Service): boolean => isSaved(article, result);

    return {
      session: result.session, 
      backend: result.backend, 
      actions: result.actions, 
      site: result.session.site,
      isServiceSaved
    };
  }

  export const useSite = () => {
    const result: ContextType = React.useContext(ComposerContext);
    return result.session.site;
  }

  export const useSession = () => {
    const result: ContextType = React.useContext(ComposerContext);
    return result.session;
  }


  export const Provider: React.FC<{ children: React.ReactNode, backend: TagomiApi.Backend }> = ({ children, backend }) => {
    const [session, dispatch] = React.useReducer(Reducer, sessionData);
    const actions = React.useMemo(() => {
      return new ReducerDispatch(dispatch, backend)
    }, [dispatch, backend]);

    React.useLayoutEffect(() => {
      actions.handleLoad();
    }, [backend, actions]);

    return (<ComposerContext.Provider value={{ session, actions, backend }}>{children}</ComposerContext.Provider>);
  };
}

