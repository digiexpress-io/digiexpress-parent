
function checkResponse(response) {
  if (response.ok) {
    return response;
  } else {
    console.error('Service error', response);
    throw new Error(`FATAL_${response.status}`);
  }
}

export default class FormService {
  constructor(baseUrl, csrf, tenantId, credentials) {
    this.baseUrl = baseUrl;
    this.csrf = csrf;
    this.tenantId = tenantId;
    this.credentials = credentials;
  }

  doFetch(url, method, body = undefined) {
    let headers = {
      'Accept': 'application/json',
      'Content-Type': 'application/json; charset=UTF-8'
    };
    if (this.csrf) {
      headers[this.csrf.headerName] = this.csrf.token;
    }
    let options = {
      method,
      credentials: this.credentials,
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
    return fetch(url, options)
      .then(response => checkResponse(response))
      .then(response => response.json());
  }

  loadForm(formId, tagName = null) {
    let url = this.baseUrl + '/forms/' + formId;
    if (tagName && tagName !== 'LATEST') {
      url += `?rev=${tagName}`;
    }
    return this.doFetch(url, 'get');
  }

  saveForm(formData, dryRun = false) {
    return this.doFetch(`${this.baseUrl}/forms/${formData._id}${dryRun ? '?dryRun=true' : ''}`, 'put', formData);
  }

  duplicateItem(formData, itemId) {
    return this.doFetch(this.baseUrl + '/forms/actions/itemCopy?itemId=' + itemId, 'post', formData);
  }

  changeItemId(formData, oldId, newId) {
    return this.doFetch(`${this.baseUrl}/forms/${formData._id}?oldId=${oldId}&newId=${newId}`, 'put', formData);
  }

  createSession(formId, language, context) {
    let session = {
      metadata: {
        formId,
        formRev: 'LATEST',
        language
      }
    };
    if (context) {
      session.context = context;
    }
    return this.doFetch(`${this.baseUrl}/questionnaires`, 'post', session);
  }

  loadVersions(formName) {
    return this.doFetch(`${this.baseUrl}/forms/${formName}/tags`);
  }

  createTag(formName, tagName, tagDescription, formId = null) {
    let tagData = {
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