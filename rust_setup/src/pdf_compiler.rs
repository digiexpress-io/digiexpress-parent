use thiserror::Error;
use typst::{diag::{FileError, HintedString, SourceDiagnostic }};
use typst::{foundations::{Dict, Module, Scope, Value}};
use typst::{syntax::FileId};
use typst::{utils::LazyHash, Library};
use ecow::EcoVec;
use std::ops::Deref;


#[derive(Debug, Clone, Error)]
pub enum TypstAsLibError {
    #[error("Typst source error: {0:?}")]
    TypstSource(EcoVec<SourceDiagnostic>),
    #[error("Typst file error: {0}")]
    TypstFile(#[from] FileError),
    #[error("Source file does not exist in collection: {0:?}")]
    MainSourceFileDoesNotExist(FileId),
    #[error("Typst hinted String: {0:?}")]
    HintedString(HintedString),
    #[error("Unspecified: {0}!")]
    Unspecified(ecow::EcoString),
}


#[derive(Debug, Clone)]
struct InjectLocation {
    module_name: &'static str,
    value_name: &'static str,
}

pub struct PdfCompiler {
    library: LazyHash<Library>,
    inject_location: Option<InjectLocation>,
}


impl PdfCompiler {


    fn createLib<D>(&self, input: D) -> Result<LazyHash<Library>, TypstAsLibError>
    where D: Into<Dict> {

        let Self {
            inject_location,
            library,
            ..
        } = self;

        let mut lib = library.deref().clone();

        // variable name definitions
        let (module_name, value_name) = 
        
        if let Some(InjectLocation { module_name, value_name, }) = inject_location {
            (*module_name, *value_name)
        } else {
            ("sys", "inputs")
        };

        {
            let global = lib.global.scope_mut();
            let mut scope = Scope::new();
            
            scope.define(value_name, input.into());
            if let Some(value) = global.get_mut(module_name) {
                let value = value.write().map_err(TypstAsLibError::Unspecified)?;
                if let Value::Module(module) = value {
                    *module.scope_mut() = scope;
                } else {
                    let module = Module::new(module_name, scope);
                    *value = Value::Module(module);
                }
            } else {
                let module = Module::new(module_name, scope);
                global.define(module_name, module);
            }
        }

        Ok(LazyHash::new(lib))
    }
}