import { DialobRestApi } from "../types-rest-api";

export namespace Visitor_LabelAdd {
  export interface Input {
    form: DialobRestApi.FormListItem; // ID of the form to delete
    newLabel: string
  }

  export interface Result {
    success: boolean;
    formId: string;
    message: string;
    error?: string;
    response?: DialobRestApi.ApiResponse;
  }
}

export class Visitor_LabelAdd {
  async accept(backend: DialobRestApi.Backend, context: Visitor_LabelAdd.Input): Promise<Visitor_LabelAdd.Result> {
    try {
      
      const form = await backend.getForm(context.form.id);
      const labels = Array.from(new Set([...(form.metadata.labels || []), context.newLabel]));
      
      const json: DialobRestApi.Form = { ...form, metadata: {...form.metadata, labels } };
      delete json._id;
      delete json._rev;
      const response = await backend.updateForm(context.form.id, json, { force: true });
      
      return this.buildSuccessResult(context.form.id, response);
      
    } catch (error) {
      return this.buildErrorResult(context.form.id, error);
    }
  }


  private buildSuccessResult(formId: string, response: DialobRestApi.ApiResponse): Visitor_LabelAdd.Result {
    return {
      success: true,
      formId,
      message: `Successfully add form label: ${formId}`,
      response,
    };
  }

  private buildErrorResult(formId: string, error: unknown): Visitor_LabelAdd.Result {
    const errorMessage = error instanceof Error ? error.message : 'Unknown error occurred';
    
    return {
      success: false,
      formId,
      message: `Error while adding form label: ${errorMessage}`,
      error: errorMessage
    };
  }
}