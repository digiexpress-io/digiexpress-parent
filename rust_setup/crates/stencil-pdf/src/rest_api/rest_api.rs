use chrono::{DateTime, FixedOffset};
use serde::{Deserialize, Serialize};

#[derive(Serialize)]
pub struct AnyResponse<T> {
    pub data: Option<T>,
    pub success: bool,
    pub error: Option<String>,
}

#[derive(Deserialize)]
pub struct PdfRequest {
    pub main: String, // main template id
    pub now: DateTime<FixedOffset>,
    pub templates: Vec<PdfTemplate>, // all templates
    pub props: Vec<PdfProps>, // json data for the templates
}

#[derive(Deserialize)]
pub struct PdfProps {
    pub module_name: String,
    pub body_name: String,
    pub body_value: serde_json::Value
}

#[derive(Deserialize)]
pub struct PdfTemplate {
    pub id: String, // unique template id
    pub value: String // template content
}


#[derive(serde::Serialize)]
pub struct PdfResponse {
    pub main: String, // main template id
    pub base64: String,
    pub cost_in_millis: u64,
}

#[async_trait::async_trait]
pub trait StencilPdfClient {
    async fn health_check(&self) -> AnyResponse<String>;
    async fn compile_pdf(&self, payload: PdfRequest) -> AnyResponse<PdfResponse>; // data is base64 content
}