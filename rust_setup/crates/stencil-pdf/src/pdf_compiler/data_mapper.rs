use ecow::EcoVec;
use itertools::Itertools;
use typst::foundations::{Dict, Str, Value};
use typst::{ Features, Library };


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

pub fn map_to_lib(modules: &Vec<PdfDataModule>, leakes: &mut Vec<Box<str>>) -> Library {
    // Build the input data into a Dict first
    let grouped_modules: Vec<(String, Vec<PdfDataModule>)> = group_modules(modules.clone());

    let mut inputs = Dict::new();

    for (module_name, module_values) in grouped_modules {
        let mut module_dict = Dict::new();

        for data_module in module_values {
            // Memory leak handling
            let key_box = data_module.value_key.into_boxed_str();
            let key: &str = Box::leak(key_box.clone());
            leakes.push(key_box);

            // Add to module dict
            module_dict.insert(key.into(), map_from_json_to_typst_value(data_module.value_data).unwrap_or(Value::None));
        }

        // Memory leak handling
        let key_box = module_name.into_boxed_str();
        let key: &str = Box::leak(key_box.clone());
        leakes.push(key_box);

        // Add module to inputs
        inputs.insert(key.into(), Value::Dict(module_dict));
    }

    // Build library with inputs AND features - this includes standard functions like underline
    Library::builder()
        .with_inputs(inputs)
        .with_features(Features::default())
        .build()
}