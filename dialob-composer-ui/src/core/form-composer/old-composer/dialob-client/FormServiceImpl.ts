import * as Client from './FormService';


function checkResponse(response: Response) {
    console.error('Service response', response);
  
  if (response.ok) {
    return response;
  } else {
    console.error('Service error', response);
    throw new Error(`FATAL_${response.status}`);
  }
}

export default class FormServiceImpl implements Client.FormService {
  private baseUrl: string;
  private csrf: Client.Csrf;
  private tenantId: string;

  constructor(baseUrl: string, csrf: Client.Csrf, tenantId: string) {
    this.baseUrl = baseUrl;
    this.csrf = csrf;
    this.tenantId = tenantId;
  }

  doFetch(url: string, method: string, body: Client.Form | Client.Tag | Client.Questionnaire | undefined = undefined) {
    
    const headers: HeadersInit = {
      'Accepts': 'application/json',
      'Content-Type': 'application/json; charset=UTF-8'
    };

    if (this.csrf && this.csrf.headerName) {
      headers[this.csrf.headerName] = this.csrf.token;
    }
    const options: RequestInit = {
      method,
      credentials: 'same-origin',
      headers
    };
    if (body) {
      options.body = JSON.stringify(body);
    }
    if (this.tenantId) {
      if (url.indexOf('?') >= 0) {
        url = url + `&tenantId=${this.tenantId}`;
      } else {
        url = url + `?tenantId=${this.tenantId}`;
      }
    }
    console.error("FormServiceImpl.doFetch", url, options);
    return fetch(url, options)
      .catch(e => {
        
        console.error(e)
        return {} as any;
      })
      .then(response => {
        console.error(response);
        return  checkResponse(response);
      })
      .then(response => {
        console.error(response);
        return response.json();
      })
      ;
  }

  loadForm(formId: Client.FormId, tagName?: string): Promise<Client.Form> {
    let url = this.baseUrl + '/forms/' + formId;
    if (tagName && tagName !== 'LATEST') {
      url += `?rev=${tagName}`;
    }
    return this.doFetch(url, 'GET');
  }

  saveForm(formData: Client.Form, dryRun = false) {
    return this.doFetch(`${this.baseUrl}/forms/${formData._id}${dryRun ? '?dryRun=true' : ''}`, 'put', formData);
  }

  duplicateItem(formData: Client.Form, itemId: string) {
    return this.doFetch(this.baseUrl + '/forms/actions/itemCopy?itemId=' + itemId, 'post', formData);
  }

  changeItemId(formData: Client.Form, oldId: string, newId: string) {
    return this.doFetch(`${this.baseUrl}/forms/${formData._id}?oldId=${oldId}&newId=${newId}`, 'put', formData);
  }

  createSession(formId: Client.FormId, language: string, context?: {}) {
    let session: any = {
      metadata: {
        formId,
        formRev: 'LATEST',
        language
      },
    };
    if (context) {
      session.context = context;
    }
    return this.doFetch(`${this.baseUrl}/questionnaires`, 'post', session);
  }

  loadVersions(formName: string) {
    return this.doFetch(`${this.baseUrl}/forms/${formName}/tags`, "GET");
  }

  createTag(formName: string, tagName: string, tagDescription: string, formId: Client.FormId | null = null) {
    let tagData: any = {
      name: tagName,
      description: tagDescription,
      formName
    };
    if (formId) {
      tagData.formId = formId;
    }
    let url = `${this.baseUrl}/forms/${formName}/tags`;
    if (!formId) {
      url += '?snapshot=true';
    }
    return this.doFetch(url, 'post', tagData);
  }

}