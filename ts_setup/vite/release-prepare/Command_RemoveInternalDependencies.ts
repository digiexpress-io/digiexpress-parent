import { ModuleInfo, ModuleRegistry } from "../module-registry";


export declare namespace Command_RemoveInternalDependencies {
 export interface Input {
   registry: ModuleRegistry;
   moduleInfo: ModuleInfo;
   packageJson: any;
 }
 
 export interface Result {
   removedDependencies: string[];
   removedPeerDependencies: string[];
 }
}

export class Command_RemoveInternalDependencies {
 execute(input: Command_RemoveInternalDependencies.Input): Command_RemoveInternalDependencies.Result {
   const { registry, moduleInfo, packageJson } = input;
   

   if (!moduleInfo) {
     throw new Error(`Module not found in registry`);
   }
   
   console.log(`🗑️  Removing internal dependencies for ${moduleInfo.name}`);
   
   const removedDependencies: string[] = [];
   const removedPeerDependencies: string[] = [];
   
   // Remove @dxs-ts packages from dependencies
   if (packageJson.dependencies) {
     const originalDeps = { ...packageJson.dependencies };
     for (const [depName, version] of Object.entries(originalDeps)) {
       if (depName.startsWith('@dxs-ts/')) {
         delete packageJson.dependencies[depName];
         removedDependencies.push(depName);
         console.log(`   🗑️  Removed dependency: ${depName}`);
       }
     }
     
     // Clean up empty dependencies object
     if (Object.keys(packageJson.dependencies).length === 0) {
       delete packageJson.dependencies;
     }
   }
   
   // Remove @dxs-ts packages from peerDependencies
   if (packageJson.peerDependencies) {
     const originalPeerDeps = { ...packageJson.peerDependencies };
     for (const [depName, version] of Object.entries(originalPeerDeps)) {
       if (depName.startsWith('@dxs-ts/')) {
         delete packageJson.peerDependencies[depName];
         removedPeerDependencies.push(depName);
         console.log(`   🗑️  Removed peerDependency: ${depName}`);
       }
     }
     
     // Clean up empty peerDependencies object
     if (Object.keys(packageJson.peerDependencies).length === 0) {
       delete packageJson.peerDependencies;
     }
   }
   
   const totalRemoved = removedDependencies.length + removedPeerDependencies.length;
   console.log(`✅ Removed ${totalRemoved} internal dependencies`);
   
   return {
     removedDependencies,
     removedPeerDependencies,
   };
 }
}