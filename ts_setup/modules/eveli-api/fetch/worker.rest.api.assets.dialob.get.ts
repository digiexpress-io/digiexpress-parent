import { createFileFetch } from '@dxs-ts/envir-fetch';
import { useQuery } from '@tanstack/react-query'

export const Hook = createFileFetch('worker/rest/api/assets/dialob.GET')({
  hook
}) 

function hook(props: {}): { dialobForms: DialobFormEntry[] | undefined, refresh: typeof refetch} {
  const params = Hook.useParams();
  const { url } = params;
  const query = url({});
  
  const { data, error, refetch, isPending } = useQuery({
    queryKey: [query],
    queryFn: () => params
      .fetch(query).then(resp => resp.json())
      .then((data: DialobFormEntry[]) => (data ?? []).sort()),
  });

  return { dialobForms: data, refresh: refetch }
}


export interface DialobFormMetadata {
  label?: string;
    creator?: string;
    tenantId?: string;
    savedBy?: string;
    languages?: string[];
    valid?: boolean;
    created?: string;
    lastSaved?: string;
    purpose?: string;
    [prop: string]: any;
};

export interface DialobForm {
  _id: string;
  name: string;
  metadata: DialobFormMetadata;
  data?: any;
};

export interface DialobFormEntry {
  id: string;
  metadata: DialobFormMetadata;
};

export const DEFAULT_FORM: Partial<DialobForm> = {
  name: '',
  data: {
    questionnaire : {
      id: 'questionnaire',
      type: 'questionnaire',
      items: []
    }
  },
  metadata: {
    label: '',
    languages: [
      'fi',
      'en'
    ]
  }
};

export interface DialobCreateFormCommand {
  purpose: string;
  title: string;
};



export interface DialobQuestionnaireMetadata {
  status: 'NEW' | 'OPEN' | 'COMPLETED';
  formId: string;
  formName?: string;
  opened?: string;
  completed?: string;
}

export interface DialobQuestionnaire {
  _id: string;
  metadata: DialobQuestionnaireMetadata;
};
