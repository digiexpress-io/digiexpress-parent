use std::collections::HashMap;

use axum::{
    http::{StatusCode, header},
    response::{Json, Response},
    routing::{get, post},
    Router,
};
use serde::{Deserialize, Serialize};
use tower_http::cors::{CorsLayer};
use tracing::{info, error};
use anyhow::Result;
use base64::{ Engine, engine::general_purpose };

mod typst_services;
use typst_services::TypstService;
mod pdf_compiler;


use typst::foundations::{Dict, Value, Str};
use ecow::EcoVec;



#[derive(Serialize)]
struct ApiResponse<T> {
    success: bool,
    data: Option<T>,
    error: Option<String>,
}

#[derive(Deserialize)]
struct CompileRequest {
    content: String,
    props: HashMap<String, serde_json::Value>
}

#[derive(Serialize)]
struct CompileResponse {
    output: String, // base64 encoded
    format: String,
}

#[tokio::main]
async fn main() {
    // Initialize tracing
    tracing_subscriber::fmt()
        .with_env_filter(tracing_subscriber::EnvFilter::from_default_env())
        .init();
    
    let app = Router::new()
        .route("/", get(health_check))
        .route("/health", get(health_check))
        .route("/compile", post(compile_typst))
        .layer(CorsLayer::permissive());

    let listener = tokio::net::TcpListener::bind("0.0.0.0:3000").await.unwrap();
    info!("Server running on http://0.0.0.0:3000");
    
    axum::serve(listener, app).await.unwrap();
}

async fn health_check() -> Json<ApiResponse<String>> {
    Json(ApiResponse {
        success: true,
        data: Some("Typst REST API is running".to_string()),
        error: None,
    })
}

async fn compile_typst(Json(payload): Json<CompileRequest>) -> Result<Response, StatusCode> {
    match TypstService::compile(&payload.content, hashmap_to_dict(payload.props), "pdf").await {
        Ok(output) => {
            let response = CompileResponse {
                output: general_purpose::STANDARD.encode(&output),
                format: "pdf".to_string(),
            };
            
            let json_response = ApiResponse {
                success: true,
                data: Some(response),
                error: None,
            };
            
            Ok(Response::builder()
                .status(StatusCode::OK)
                .header(header::CONTENT_TYPE, "application/json")
                .body(serde_json::to_string(&json_response).unwrap().into())
                .unwrap())
        }
        Err(e) => {
            error!("Compilation failed: {}", e);
            let error_response = ApiResponse::<()> {
                success: false,
                data: None,
                error: Some(e.to_string()),
            };
            
            Ok(Response::builder()
                .status(StatusCode::BAD_REQUEST)
                .header(header::CONTENT_TYPE, "application/json")
                .body(serde_json::to_string(&error_response).unwrap().into())
                .unwrap())
        }
    }
}


fn json_to_typst_value(value: serde_json::Value) -> Option<Value> {
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
                .filter_map(json_to_typst_value)
                .collect();
            Some(Value::Array(converted.into()))
        },
        serde_json::Value::Object(obj) => {
            let mut dict = Dict::new();
            for (key, val) in obj {
                if let Some(typst_val) = json_to_typst_value(val) {
                    dict.insert(Str::from(key), typst_val);
                }
            }
            Some(Value::Dict(dict))
        },
        serde_json::Value::Null => None,
    }
}


fn hashmap_to_dict(map: HashMap<String, serde_json::Value>) -> Dict {
    let mut dict = Dict::new();
    for (key, value) in map {
        if let Some(typst_value) = json_to_typst_value(value) {
            dict.insert(Str::from(key), typst_value);
        }
    }
    dict
}