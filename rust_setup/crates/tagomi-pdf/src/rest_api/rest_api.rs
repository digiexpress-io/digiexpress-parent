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
    pub main_template_id: String,
    pub timestamp: DateTime<FixedOffset>,
    pub templates: Vec<PdfTemplate>,
    pub data_modules: Vec<PdfDataModule>, // json data for the templates
}

#[derive(Deserialize)]
pub struct PdfDataModule {
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
pub struct PdfDocument {
    pub main_template_id: String,
    pub base64: String,
    pub cost_in_millis: u64,
}

#[async_trait::async_trait]
pub trait TagomiPdfClient {
    async fn health_check(&self) -> AnyResponse<String>;
    async fn compile_pdf(&self, payload: PdfRequest) -> AnyResponse<PdfDocument>; // data is base64 content
}