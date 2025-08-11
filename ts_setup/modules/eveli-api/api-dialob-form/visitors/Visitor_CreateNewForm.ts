import { DialobRestApi } from "../types-rest-api";



export namespace Visitor_CreateNewForm {

  export interface Input {
    name: string;
    label: string;
    template: DialobRestApi.CreateFormRequest; // The default form template
  }

  export interface Result {
    success: boolean;
    form?: DialobRestApi.Form;
    formId?: string;
    message: string;
    error?: string;
    validationErrors?: ValidationErrors;
  }

  export interface ValidationErrors {
    name?: string;
    label?: string;
  }
}

export class Visitor_CreateNewForm {
  async accept(backend: DialobRestApi.Backend, context: Visitor_CreateNewForm.Input): Promise<Visitor_CreateNewForm.Result> {
    try {
      // Validate input
      const validationErrors = Visitor_CreateNewForm.validateInput(context);
      if (this.hasValidationErrors(validationErrors)) {
        return this.buildValidationFailureResult(validationErrors);
      }

      // Prepare new form from template
      const newForm = this.prepareNewForm(context);
      
      // Create the form
      const createdForm = await backend.createForm(newForm);
      
      return this.buildSuccessResult(createdForm);

    } catch (error) {
      return this.buildErrorResult(error);
    }
  }

  public static validateInput(context: {name?: string, label?: string}): Visitor_CreateNewForm.ValidationErrors {
    const errors: Visitor_CreateNewForm.ValidationErrors = {};


    if (!context.name) {
      //errors.name = 'Form name is required';
      errors.name = 'error.valueRequired';
    } else if (!/^[_\-a-zA-Z\d]*$/.test(context.name)) {
      //errors.name = 'Form name contains invalid characters (only letters, numbers, underscore, and dash allowed)';
      errors.name = 'error.invalidFormName';
    }

    if (!context.label) {
      //errors.label = 'Form label is required';
      errors.label = 'error.valueRequired';
    }
    return errors;
  }

  private hasValidationErrors(errors: Visitor_CreateNewForm.ValidationErrors): boolean {
    return Object.keys(errors).length > 0;
  }

  private prepareNewForm(context: Visitor_CreateNewForm.Input): DialobRestApi.CreateFormRequest {
    const newForm = { ...context.template };
    
    // Set name and label
    newForm.name = context.name;
    newForm.metadata = {
      ...newForm.metadata,
      label: context.label,
    };

    return newForm;
  }

  private buildSuccessResult(form: DialobRestApi.Form): Visitor_CreateNewForm.Result {
    return {
      success: true,
      form,
      formId: form._id || form.name,
      message: `Successfully created form: ${form.metadata?.label || form.name}`
    };
  }

  private buildValidationFailureResult(validationErrors: Visitor_CreateNewForm.ValidationErrors): Visitor_CreateNewForm.Result {
    return {
      success: false,
      message: 'Form validation failed',
      validationErrors
    };
  }

  private buildErrorResult(error: unknown): Visitor_CreateNewForm.Result {
    const errorMessage = error instanceof Error ? error.message : 'Unknown error occurred';
    
    return {
      success: false,
      message: `Error while creating form: ${errorMessage}`,
      error: errorMessage
    };
  }
}
