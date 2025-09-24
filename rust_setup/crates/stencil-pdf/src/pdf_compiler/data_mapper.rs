use ecow::EcoVec;
use itertools::Itertools;
use typst::foundations::{Dict, Module, Scope, Str, Value};
use typst::{ Library };


use crate::pdf_compiler::{PdfDataModule};

fn map_from_json_to_typst_value(value: serde_json::Value) -> Option<Value> {
    match value {
        serde_json::Value::String(s) => {
            Some(Value::Str(Str::from(s)))
        },

        serde_json::Value::Number(n) => {
            if let Some(i) = n.as_i64() {
                Some(Value::Int(i))
            } else if let Some(f) = n.as_f64() {
                Some(Value::Float(f))
            } else {
                None
            }
        },
        serde_json::Value::Bool(b) => Some(Value::Bool(b)),
        
        serde_json::Value::Array(arr) => {
            let converted: EcoVec<Value> = arr.into_iter()
                .filter_map(map_from_json_to_typst_value)
                .collect();
            Some(Value::Array(converted.into()))
        },

        serde_json::Value::Object(obj) => {
            let mut dict = Dict::new();
            for (key, val) in obj {
                if let Some(typst_val) = map_from_json_to_typst_value(val) {
                    dict.insert(Str::from(key), typst_val);
                }
            }
            Some(Value::Dict(dict))
        },
        serde_json::Value::Null => None,
    }
}


fn group_modules(modules: Vec<PdfDataModule>) -> Vec<(String, Vec<PdfDataModule>)> {
        let grouped_modules: Vec<(String, Vec<PdfDataModule>)> = modules
            .iter()
            .sorted_by_key(|module| &module.module_name)
            .chunk_by(|module| &module.module_name)
            .into_iter()
            .map(|(name, group)| (name.to_string(), group.cloned().collect()))
            .collect();
        grouped_modules
    }

pub fn map_to_lib(modules: &Vec<PdfDataModule>, leakes:  &mut Vec<Box<str>>) -> Library {
    
    // build the input data into library
    let grouped_modules: Vec<(String, Vec<PdfDataModule>)> = group_modules(modules.clone());
    
    let mut library = Library::default();
    let global = library.global.scope_mut();  
    for (module_name, module_values) in grouped_modules {
        
        // create scope with data in it
        let mut scope = Scope::new();
        for data_module in module_values {

            // memory leak
            let key_box = data_module.value_key.into_boxed_str();
            let key: &mut str = Box::leak(key_box.clone());
            leakes.push(key_box);

            // scope data
            scope.define(key, map_from_json_to_typst_value(data_module.value_data).clone());
        }

        // memory leak
        let key_box = module_name.into_boxed_str();
        let key: &mut str = Box::leak(key_box.clone());
        leakes.push(key_box);

        // define module
        let module = Module::new(&*key, scope);
        global.define(key, module);
    }

    library
}
