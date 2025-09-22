use thiserror::Error;
use std::result::Result;

use typst::{diag::{FileError, HintedString, SourceDiagnostic }, foundations::Datetime};
use typst::{foundations::{ Dict }};
use typst::{syntax::FileId};
use ecow::EcoVec;


#[derive(Debug, Clone, Error)]
pub enum PdfCompilerError {
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
pub struct PdfDataModule {
    pub module_name: String,
    pub value_key: String,
    pub value_data: serde_json::Value,
}

pub struct Pdf {
    pub base64: String,
    pub cost_in_millis: u64,
}

pub struct PdfTemplate {
    pub id: String, 
    pub value: String
}

pub trait PdfCompiler {
    fn today(self, today: Datetime) -> Self;
    fn main(self, template_id: String) -> Self;
    fn add_template<T: Into<PdfTemplate>>(self, template: T) -> Self;
    fn add_templates<T: Into<PdfTemplate>>(self, template: Vec<T>) -> Self;
    fn add_modules<T: Into<PdfDataModule>>(self, data: Vec<T>) -> Self;
    fn compile(self) -> Result<Pdf, PdfCompilerError>;
}

impl From<(String, String)> for PdfTemplate {
    fn from((id, value): (String, String)) -> Self {
        PdfTemplate {
            id,
            value,
        }
    }
}