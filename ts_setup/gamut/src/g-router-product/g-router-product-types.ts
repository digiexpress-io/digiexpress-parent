import { IamApi, useIam } from "../api-iam";
import { SiteApi, useSite } from "../api-site";


export interface GRouterProductProps {
  productId: string,
  pageId: string,
  locale: string
}


export interface GRouterProductOwnerState {
  ownerState: {
    topic: SiteApi.TopicView;
    topicLink: SiteApi.TopicLink | undefined;
    locale: string;
    isAnon: boolean;
    status: IamApi.FormLinkAuthType;
  }
}


export function useGRouterProducState(props: GRouterProductProps): GRouterProductOwnerState {
    const iam = useIam();
    const site = useSite();
  
    const topic = site.views[props.pageId];
    const topicLink = topic.links.find(l => l.id === props.productId)
    const anonymousUser = iam.authType === 'ANON';
  
    const ownerState: GRouterProductOwnerState['ownerState'] = {
      topic,
      topicLink,
      locale: props.locale,
      isAnon: anonymousUser,
      status: iam.getFormLinkAuthType(topicLink)
    }
  
    return { ownerState };
}