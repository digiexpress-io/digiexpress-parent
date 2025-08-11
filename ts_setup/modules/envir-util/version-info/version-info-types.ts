import { VersionInfoBuilder } from "./VersionInfoBuilder";

export declare namespace VersionInfoApi {
  
  export interface ProjectInfo {
    projectName: string;
    version: string;
    releaseDate: string | Date;
  }

  // Theme definitions
  export interface ThemeColors {
    logo: string;
    separator: string;
    projectName: string;
    info: string;
    componentTitle: string;
    componentItem: string;
  }

}



export namespace VersionInfoApi {
  
  export const builder = () => new VersionInfoBuilder();

}
