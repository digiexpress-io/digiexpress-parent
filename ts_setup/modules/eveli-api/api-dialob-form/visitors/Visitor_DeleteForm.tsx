import { DialobRestApi } from "../types-rest-api";

export namespace Visitor_DeleteForm {
  export interface Input {
    form: DialobRestApi.FormListItem; // ID of the form to delete
  }

  export interface Result {
    success: boolean;
    formId: string;
    message: string;
    error?: string;
    response?: DialobRestApi.ApiResponse;
  }
}


export class Visitor_DeleteForm {
  async accept(backend: DialobRestApi.Backend, context: Visitor_DeleteForm.Input): Promise<Visitor_DeleteForm.Result> {
    try {
      // Validate input
      this.validateInput(context);
      
      // Delete the form
      const response = await backend.deleteForm(context.form.id);
      
      return this.buildSuccessResult(context.form.id, response);
      
    } catch (error) {
      return this.buildErrorResult(context.form.id, error);
    }
  }

  private validateInput(context: Visitor_DeleteForm.Input): void {
    if (!context.form.id) {
      throw new Error('Form ID is required');
    }
    
    if (typeof context.form.id !== 'string' || context.form.id.trim() === '') {
      throw new Error('Form ID must be a non-empty string');
    }
  }

  private buildSuccessResult(formId: string, response: DialobRestApi.ApiResponse): Visitor_DeleteForm.Result {
    return {
      success: true,
      formId,
      message: `Successfully deleted form: ${formId}`,
      response,
    };
  }

  private buildErrorResult(formId: string, error: unknown): Visitor_DeleteForm.Result {
    const errorMessage = error instanceof Error ? error.message : 'Unknown error occurred';
    
    return {
      success: false,
      formId,
      message: `Error while deleting form: ${errorMessage}`,
      error: errorMessage
    };
  }
}

