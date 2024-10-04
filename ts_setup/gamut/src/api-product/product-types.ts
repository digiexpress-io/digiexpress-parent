
export namespace ProductApi {

}

export declare namespace ProductApi {
  export type ProductPageId = string;

  export interface ProductPage {
    id: ProductPageId;
    name: string;
    parentId: ProductPageId | undefined;
    children: ProductPageId[];
    headings: ProductHeading[];


    products: Product[];
    contacts: Phone[];
    additionalInfo: AdditionalInfo[];
  }

  export interface ProductHeading {
    id: string;
    name: string;
    order: number;
    level: number;
  }

  export interface AdditionalInfo {
    id: string;
    name: string;
    value: string;
    variant: 'md' | 'hyperlink';
  }

  export interface Phone {
    id: string;
    name: string;
    value: string;
  }


  export interface Product {
    id: string;
    productPage: string;
    anon: boolean;
  }

  export type GetProductFetchGET = () => Promise<Response>;
}
