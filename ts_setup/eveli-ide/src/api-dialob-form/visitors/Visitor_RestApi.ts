import { DialobRestApi } from "../types-rest-api";




export namespace Visitor_RestApi {

  // Context for visitor configuration
  export interface Input {
    config: DialobRestApi.DialobAdminConfig;
    fetch: typeof window.fetch;
  }
  
}


// The main visitor - generates Backend implementation from configuration
export class Visitor_RestApi {
  
  accept(context: Visitor_RestApi.Input): DialobRestApi.Backend {
    return new BackendImpl(context);;
  }
}



class BackendImpl implements DialobRestApi.Backend {

  private _context: Visitor_RestApi.Input;

  constructor(input: Visitor_RestApi.Input) {
    this._context = input;
  }
  private buildUrlWithTenant( baseUrl: string): string {
    if (!this._context.config.tenantId) return baseUrl;
    const separator = baseUrl.includes('?') ? '&' : '?';
    return `${baseUrl}${separator}tenantId=${this._context.config.tenantId}`;
  }

  private async makeApiCall<T>(
    url: string, 
    options: RequestInit = {}
  ): Promise<T> {
    try {
      const response = await this._context.fetch(url, {
        headers: {
          'Content-Type': 'application/json',
          ...options.headers,
        },
        ...options,
      });

      if (!response.ok) {
        // Try to parse error response
        let errorData: DialobRestApi.RestApiErrorResponse;
        try {
          errorData = await response.json();
        } catch {
          errorData = {
            status: response.status,
            error: response.statusText,
            message: `HTTP ${response.status}: ${response.statusText}`,
          };
        }
        
        const errorMessage = errorData.message || errorData.error || `HTTP ${response.status}`;
        throw new Error(errorMessage);
      }

      // Parse response based on content type
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        return await response.json();
      } else {
        // For non-JSON responses, return response text as data
        return await response.text() as unknown as T;
      }
    } catch (error) {
      if (error instanceof Error) {
        throw error;
      }
      throw new Error(`API call failed: ${error}`);
    }
  }

  async findAllForms(): Promise<DialobRestApi.FormListItem[]> {
    const url = this.buildUrlWithTenant(`${this._context.config.dialobApiUrl}/api/forms`);
    return this.makeApiCall<DialobRestApi.FormListItem[]>(url, { method: 'GET' });
  }

  async findAllTags( filters?: DialobRestApi.TagFilters): Promise<DialobRestApi.FormTag[]> {
    let url = this.buildUrlWithTenant(`${this._context.config.dialobApiUrl}/api/tags`);
    
    if (filters) {
      const params = new URLSearchParams();
      if (filters.formName) params.set('formName', filters.formName);
      if (filters.formId) params.set('formId', filters.formId);
      if (filters.name) params.set('name', filters.name);
      
      if (params.toString()) {
        const separator = url.includes('?') ? '&' : '?';
        url += separator + params.toString();
      }
    }
    
    return this.makeApiCall<DialobRestApi.FormTag[]>(url, { method: 'GET' });
  }

  async getForm( formId: string, rev?: string): Promise<DialobRestApi.Form> {
    let url = this.buildUrlWithTenant(`${this._context.config.dialobApiUrl}/api/forms/${formId}`);
    if (rev) {
      const separator = url.includes('?') ? '&' : '?';
      url += `${separator}rev=${rev}`;
    }
    return this.makeApiCall<DialobRestApi.Form>(url, { method: 'GET' });
  }

  async getFormTags( formId: string): Promise<DialobRestApi.FormTag[]> {
    const url = this.buildUrlWithTenant(`${this._context.config.dialobApiUrl}/api/forms/${formId}/tags`);
    return this.makeApiCall<DialobRestApi.FormTag[]>(url, { method: 'GET' });
  }

  async createForm( form: DialobRestApi.CreateFormRequest | DialobRestApi.Form): Promise<DialobRestApi.Form> {
    // Clean the form data
    const cleanForm = this.cleanFormData(form);
    
    const url = this.buildUrlWithTenant(`${this._context.config.dialobApiUrl}/api/forms`);
    return this.makeApiCall<DialobRestApi.Form>(url, {
      method: 'POST',
      body: JSON.stringify(cleanForm),
    });
  }

  async updateForm(
  
    formId: string, 
    form: DialobRestApi.Form, 
    options?: DialobRestApi.UpdateFormOptions
  ): Promise<DialobRestApi.ApiResponse> {
    // Clean the form data
    const cleanForm = this.cleanFormData(form);
    
    let url = `${this._context.config.dialobApiUrl}/api/forms/${formId}`;
    const params = new URLSearchParams();
    
    if (options?.oldId) params.set('oldId', options.oldId);
    if (options?.newId) params.set('newId', options.newId);
    if (options?.force) params.set('force', 'true');
    if (options?.dryRun) params.set('dryRun', 'true');
    if (this._context.config.tenantId) params.set('tenantId', this._context.config.tenantId);
    
    if (params.toString()) {
      url += '?' + params.toString();
    }
    
    return this.makeApiCall<DialobRestApi.ApiResponse>(url, {
      method: 'PUT',
      body: JSON.stringify(cleanForm),
    });
  }

  async deleteForm( formId: string): Promise<DialobRestApi.ApiResponse> {
    const url = this.buildUrlWithTenant(`${this._context.config.dialobApiUrl}/api/forms/${formId}`);
    return this.makeApiCall<DialobRestApi.ApiResponse>(url, { method: 'DELETE' });
  }

  async createFormFromCsv( csvData: string): Promise<DialobRestApi.ApiResponse> {
    const url = this.buildUrlWithTenant(`${this._context.config.dialobApiUrl}/api/forms`);
    return this.makeApiCall<DialobRestApi.ApiResponse>(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'text/csv',
      },
      body: csvData,
    });
  }

  private cleanFormData(form: DialobRestApi.CreateFormRequest | DialobRestApi.Form): DialobRestApi.Form {
    const cleanForm: any = { ...form };
    // Remove database artifacts
    delete cleanForm._id;
    delete cleanForm._rev;
    return cleanForm;
  }
}