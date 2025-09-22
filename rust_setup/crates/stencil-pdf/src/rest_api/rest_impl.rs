use chrono::{Datelike, Timelike};
use crate::rest_api::{ AnyResponse, PdfRequest, PdfResponse, StencilPdfClient };
use crate::pdf_compiler::{ PdfCompiler, PdfCompilerImpl };

pub struct StencilPdfClientImpl;


impl StencilPdfClientImpl {
    pub fn new() -> Self {
        Self {
        }
    }
}

// hard mapping for TEMPLATES
impl From<crate::rest_api::PdfTemplate> for crate::pdf_compiler::PdfTemplate {
    fn from(api_template: crate::rest_api::PdfTemplate) -> Self {
        crate::pdf_compiler::PdfTemplate {
            id: api_template.id,
            value: api_template.value,
        }
    }
}

impl From<crate::rest_api::PdfProps> for crate::pdf_compiler::PdfDataModule {
    fn from(api_template: crate::rest_api::PdfProps) -> Self {
        crate::pdf_compiler::PdfDataModule {
            module_name: api_template.module_name,
            value_key: api_template.body_name,
            value_data: api_template.body_value,
        }
    }
}

#[async_trait::async_trait]
impl StencilPdfClient for StencilPdfClientImpl {
    
    async fn health_check(&self) -> AnyResponse<String> {
        AnyResponse {
            success: true,
            data: Some("Stencil PDF - UP".to_string()),
            error: None,
        }
    }

    async fn compile_pdf(&self, payload: PdfRequest) -> AnyResponse<PdfResponse> {
        let cloned_main = payload.main.clone();

        // Get the local time (with offset applied)
        let local_time = payload.now.naive_local();
        let result: Result<crate::pdf_compiler::Pdf, crate::pdf_compiler::PdfCompilerError> = PdfCompilerImpl::new()
            .today(typst::foundations::Datetime::from_ymd_hms(
                local_time.year(),
                local_time.month() as u8,
                local_time.day() as u8,
                local_time.hour() as u8,
                local_time.minute() as u8,
                local_time.second() as u8).unwrap())
            .main(payload.main)
            .add_templates(payload.templates)
            .add_modules(payload.props)
            .compile();

        match result {
            Ok(pdf) => {
               AnyResponse { 
                    data: Some(PdfResponse { 
                        main: cloned_main, 
                        base64: pdf.base64, 
                        cost_in_millis: pdf.cost_in_millis 
                    }), 
                    success: true, 
                    error: None 
                }
            },
            Err(error) => {
                AnyResponse { 
                    data: None, 
                    success: false, 
                    error: Some(error.to_string())
                }
            }
        }
    }
}



