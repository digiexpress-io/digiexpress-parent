import ModelAPI from './ModelAPI';


declare namespace ResourceAPI {
  
  interface Asset {
    id: string;
    name: string;
    type: ModelAPI.ServiceType;
    content: string;
    errors: ModelAPI.ModelError[];
  }

  interface ServiceCallback<T> {
    onSuccess: (handle: (resource: T) => void) => void; 
  }
  
  interface Service {
    export(): Promise<string>;
    get(id: string): Promise<Asset>;
    delete(id: string): Promise<void>;
    update(asset: Asset, body: string): Promise<Asset>;
    create(name: string, type: ModelAPI.ServiceType): Promise<Asset>;
    copy(id: string, as: string): Promise<Asset>;
  }

}

export default ResourceAPI;


/*  
  assets(successHandler, errorHandler) {
    const method = { method: 'GET', credentials: 'same-origin', headers: this.readHeaders }
    const url = this.config.getIn(['url', 'dataModels'])
    return fetch(url, method)
      .then(getResponse)
      .then(successHandler)
      .catch(errors => getErrors(errors, errorHandler, url))
  }
  
  asset(id, successHandler, errorHandler) {
    const method = { method: 'GET', credentials: 'same-origin', headers: this.readHeaders }
    const url = this.config.getIn(['url', 'resources']) + id
    return fetch(url, method)
      .then(getResponse)
      .then(successHandler)
      .catch(errors => getErrors(errors, errorHandler, url))
  }

  remove(id, successHandler, errorHandler) {
    const method = { method: 'DELETE', credentials: 'same-origin', headers: this.readHeaders }
    const url = this.config.getIn(['url', 'resources']) + id
    return fetch(url, method)
      .then(getResponse)
      .then(successHandler)
      .catch(errors => getErrors(errors, errorHandler, url))
  }
    
  deployment(successHandler, errorHandler) {
    const method = { method: 'GET', credentials: 'same-origin', headers: this.readHeaders }
    const url = this.config.getIn(['url', 'summaries'])
    return fetch(url, method)
      .then(getResponse)
      .then(successHandler)
      .catch(errors => getErrors(errors, errorHandler, url))
  }

  model(body, successHandler, errorHandler) {
    const method = { method: 'POST', body: JSON.stringify(body), credentials: 'same-origin', headers: this.writeHeaders }
    const url = this.config.getIn(['url', 'commands'])
    return fetch(url, method)
      .then(getResponse)
      .then(successHandler)
      .catch(errors => getErrors(errors, errorHandler, url))
  }

  save(id, body, successHandler, errorHandler) {
    const method = { method: 'PUT', body: JSON.stringify(body), credentials: 'same-origin', headers: this.writeHeaders }
    const url = this.config.getIn(['url', 'resources']) + id
    return fetch(url, method)
      .then(getResponse)
      .then(successHandler)
      .catch(errors => getErrors(errors, errorHandler, url))
  }

  create(body, successHandler, errorHandler) {
    const method = { method: 'POST', body: JSON.stringify(body), credentials: 'same-origin', headers: this.writeHeaders }
    const url = this.config.getIn(['url', 'resources'])
    return fetch(url, method)
      .then(getResponse)
      .then(data => successHandler(data, url))
      .catch(errors => getErrors(errors, errorHandler, url))
  }
  
  copyas(body, successHandler, errorHandler) {
    const method = { method: 'POST', body: JSON.stringify(body), credentials: 'same-origin', headers: this.writeHeaders }
    const url = this.config.getIn(['url', 'copyas'])
    return fetch(url, method)
      .then(getResponse)
      .then(data => successHandler(data, url))
      .catch(errors => getErrors(errors, errorHandler, url))
  }

  debug(body, successHandler, errorHandler) {
    const method = { method: 'POST', body: JSON.stringify(body), credentials: 'same-origin', headers: this.writeHeaders }
    const url = this.config.getIn(['url', 'debugs'])
    return fetch(url, method)
      .then(getResponse)
      .then(data => successHandler(data, url))
      .catch(errors => getErrors(errors, errorHandler, url))
  }
}
*/