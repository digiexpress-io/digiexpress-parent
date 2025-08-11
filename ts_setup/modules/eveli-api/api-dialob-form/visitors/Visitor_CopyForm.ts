import { DialobRestApi } from "../types-rest-api";
import { Visitor_CreateNewForm } from "./Visitor_CreateNewForm";



export namespace Visitor_CopyForm {
  export interface Input {
    
    sourceFormId: string; // Form to copy from
    newName: string;      // Name for the copied form
    newLabel: string;     // Label for the copied form
  }

  export interface Result {
    success: boolean;
    originalForm?: DialobRestApi.Form;
    copiedForm?: DialobRestApi.Form;
    copiedFormId?: string;
    message: string;
    error?: string;
    validationErrors?: ValidationErrors;
  }

  export interface ValidationErrors {
    name?: string;
    label?: string;
  }
}

export class Visitor_CopyForm {
  async accept(backend: DialobRestApi.Backend, context: Visitor_CopyForm.Input): Promise<Visitor_CopyForm.Result> {
    try {
      // Validate input
      const validationErrors = Visitor_CreateNewForm.validateInput({ label: context.newLabel, name: context.newName });
      if (this.hasValidationErrors(validationErrors)) {
        return this.buildValidationFailureResult(validationErrors);
      }
      // Fetch the source form
      const sourceForm = await backend.getForm(context.sourceFormId);
      
      // Prepare the copied form
      const copiedForm = this.prepareCopiedForm(sourceForm, context);
      
      // Create the copied form
      const createdForm = await backend.createForm(copiedForm);
      
      return this.buildSuccessResult(sourceForm, createdForm);

    } catch (error) {
      return this.buildErrorResult(error);
    }
  }



  private hasValidationErrors(errors: Visitor_CopyForm.ValidationErrors): boolean {
    return Object.keys(errors).length > 0;
  }

  private prepareCopiedForm(
    sourceForm: DialobRestApi.Form, 
    context: Visitor_CopyForm.Input
  ): DialobRestApi.Form {
    const copiedForm = { ...sourceForm };
    
    // Remove database artifacts
    delete copiedForm._id;
    delete copiedForm._rev;
    
    // Update with new name and label
    copiedForm.name = context.newName;
    copiedForm.metadata = {
      ...copiedForm.metadata,
      label: context.newLabel,
    };

    return copiedForm;
  }

  private buildSuccessResult(
    originalForm: DialobRestApi.Form,
    copiedForm: DialobRestApi.Form
  ): Visitor_CopyForm.Result {
    return {
      success: true,
      originalForm,
      copiedForm,
      copiedFormId: copiedForm._id || copiedForm.name,
      message: `Successfully copied form: ${copiedForm.metadata?.label || copiedForm.name}`,
    };
  }

  private buildValidationFailureResult(validationErrors: Visitor_CopyForm.ValidationErrors): Visitor_CopyForm.Result {
    return {
      success: false,
      message: 'Form validation failed',
      validationErrors
    };
  }

  private buildErrorResult(error: unknown): Visitor_CopyForm.Result {
    const errorMessage = error instanceof Error ? error.message : 'Unknown error occurred';
    
    return {
      success: false,
      message: `Error while copying form: ${errorMessage}`,
      error: errorMessage
    };
  }
}