use lazy_static::lazy_static;
use axum::{
    http::{StatusCode, header},
    response::{Json, Response},
    routing::{get, post},
    Router,
};
use axum_server::tls_openssl::OpenSSLConfig;
use tower_http::cors::CorsLayer;
use tracing::info;
use anyhow::Result;
use std::net::SocketAddr;

mod config;
mod rest_api;
mod pdf_compiler;

use config::Config;

use crate::rest_api::{TagomiPdfClient, TagomiPdfClientImpl, AnyResponse, PdfRequest};

lazy_static! {
    static ref CONFIG: Config = Config::from_env();
    static ref TAGOMI_CLIENT: TagomiPdfClientImpl = TagomiPdfClientImpl::new(
        CONFIG.clone()
    );
}

#[tokio::main]
async fn main() {
    // Initialize tracing
    tracing_subscriber::fmt()
        .with_env_filter(tracing_subscriber::EnvFilter::from_default_env())
        .init();

    let config = &*CONFIG;
    info!("Configuration: {:?}", config);

    let app = Router::new()
        .route("/", get(health_check))
        .route("/health", get(health_check))
        .route("/compile", post(compile_pdf))
        .layer(CorsLayer::permissive());

    let addr = SocketAddr::from(([0, 0, 0, 0], config.port));

    if config.https {
        let tls_config = OpenSSLConfig::from_pem_file(&config.cert, &config.key).unwrap();
        
        info!("Server running on https://{}", addr);
        axum_server::bind_openssl(addr, tls_config)
            .serve(app.into_make_service())
            .await
            .unwrap();
    } else {
        let listener = tokio::net::TcpListener::bind(addr).await.unwrap();
        info!("Server running on http://{}", addr);
        axum::serve(listener, app).await.unwrap();
    }
}

async fn health_check() -> Json<AnyResponse<String>> {
    let resp = TAGOMI_CLIENT.health_check().await;
    Json(resp)
}
 
async fn compile_pdf(Json(payload): Json<PdfRequest>) -> Result<Response, Response> {
    let pdf_result = TAGOMI_CLIENT.compile_pdf(payload).await; 

    match pdf_result.success {
        true => {
           Ok(Response::builder()
                .status(StatusCode::OK)
                .header(header::CONTENT_TYPE, "application/json")
                .body(serde_json::to_string(&pdf_result).unwrap().into())
                .unwrap())
        },
        false => {
            Err(Response::builder()
                .status(StatusCode::BAD_REQUEST)
                .header(header::CONTENT_TYPE, "application/json")
                .body(serde_json::to_string(&pdf_result).unwrap().into())
                .unwrap())
        }
    }    
}