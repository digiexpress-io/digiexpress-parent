import { DashboardItem } from "../types-dashboard";
import { DialobRestApi } from "../types-rest-api";

export namespace Visitor_OpenForm {
  export interface Input {
    form: DashboardItem // ID of the form to delete
    tenantId?: string | undefined;
    dialobApiUrl?: string | undefined; 
    onOpen?: (props: DashboardItem) => void;
  }

  export interface Result {

  }
}


export class Visitor_OpenForm {
  async accept(context: Visitor_OpenForm.Input): Promise<Visitor_OpenForm.Result> {
    if (context.onOpen) {
      context.onOpen(context.form);
    } else {
      const tenantParam = context.tenantId ? `?tenantId=${context.tenantId}` : "";
      window.location.replace(`${context.dialobApiUrl}/composer/${context.form.id}${tenantParam}`);
    }
    return {};
  }

}

