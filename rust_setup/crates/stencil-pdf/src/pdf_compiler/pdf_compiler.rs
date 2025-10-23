use chrono::{Datelike, Utc};

use tracing::debug;
use typst::foundations::Datetime;
use std::collections::HashMap;
use std::time::Instant;

use base64::{ Engine, engine::general_purpose };


use typst::syntax::{FileId, Source, VirtualPath};
use typst::{utils::LazyHash};
use typst::{compile};
use typst_kit::fonts::{Fonts};
use typst_pdf::PdfOptions;

use crate::pdf_compiler::{map_to_lib, Pdf, PdfCompiler, TypstWorld};

pub struct PdfCompilerImpl {
    main_template_id: Option<String>,
    templates: HashMap<FileId, Source>,
    modules: Vec<super::PdfDataModule>,
    leakes: Vec<Box<str>>,
    now: Datetime,
    start_time: Instant
}

impl PdfCompilerImpl {
    pub fn new() -> Self {
        let now = Utc::now();
        Self {
            main_template_id: None,
            templates: HashMap::new(),
            modules: Vec::new(),
            leakes: Vec::new(),
            now: Datetime::from_ymd(now.year(), now.month() as u8, now.day() as u8).unwrap(),
            start_time: Instant::now()
        }
    }
}

// Clean up resources
impl Drop for PdfCompilerImpl {
    fn drop(&mut self) {
        // No need to do anything here!
        // The Vec<Box<str>> and Vec<Box<[u8]>> will be dropped automatically,
        // freeing the memory.
    }
}



impl PdfCompiler for PdfCompilerImpl {
    fn main(mut self, template_id: String) -> Self {
        self.main_template_id = Some(template_id);
        self
    }

    fn add_template<T: Into<super::PdfTemplate>>(mut self, raw: T) -> Self {
        let template = raw.into();
        let source_path = VirtualPath::new(template.id.to_string() + ".typ");
        let source = Source::new(FileId::new(None, source_path), template.value.to_string());
        self.templates.insert(source.id(), source);
        self
    }
    fn add_templates<T: Into<super::PdfTemplate>>(self, all_templates: Vec<T>) -> Self {
        all_templates.into_iter()
            .fold(self, |compiler, template| compiler.add_template(template))
    }
    fn add_modules<T: Into<super::PdfDataModule>>(mut self, data: Vec<T>) -> Self {
        self.modules.extend(data.into_iter().map(|item| item.into()));
        self
    }
    fn today(mut self, today: Datetime) -> Self {
        self.now = today;
        self
    }
    fn compile(mut self) -> Result<super::Pdf, super::PdfCompilerError> {
        // to the basic data validation
        let main_template = self.main_template_id.as_ref().ok_or_else(|| {
            super::PdfCompilerError::Unspecified(
                "No main template provided. Call main_template() before compile().".into(),
            )
        })?;

        let source_path = FileId::new(None, VirtualPath::new(main_template.to_string() + ".typ"));
        let main = self.templates.get(&source_path).ok_or_else(|| {
            super::PdfCompilerError::Unspecified(
                format!("No main template found. Make sure there is template with id: {}.", main_template).into(),
            )
        })?;




        let library = map_to_lib(&self.modules, &mut self.leakes);
        //let fonts = Fonts::searcher()
        //    .include_system_fonts(false)
        //    .search_with(["./assets/fonts/"]);
        let fonts = Fonts::searcher().include_system_fonts(true).search();

        // Debug loaded fonts
        for (i, font) in fonts.fonts.iter().enumerate() {
            match font.path() {
                Some(p) => debug!("Font {}: {}", i, p.display()),
                None => debug!("Font {}: No path", i),
            }
        }

        // Create PDF world
        let world_state = TypstWorld {
            library: LazyHash::new(library),
            book: LazyHash::new(fonts.book),
            fonts: fonts.fonts,
            source: self.templates.clone(),
            now: self.now,
            main: main.id(),
        };

        let warned = compile(&world_state);
        let document = warned.output.map_err(|errors| {
            let error_msg = errors
                .iter()
                .map(|e| e.message.to_string())
                .collect::<Vec<_>>()
                .join("; ");
            super::PdfCompilerError::Unspecified(
                format!("Compilation failed: {}", error_msg).into(),
            )
        })?;
        let pdf = typst_pdf::pdf(&document, &PdfOptions::default());

        return pdf.map_err(|errors| 
            super::PdfCompilerError::Unspecified(
                format!("PDF generation failed: {:?}", errors).into(),
            )
        ).map(|bytes| {
            let base64 = general_purpose::STANDARD.encode(&bytes);
            let cost_in_millis = self.start_time.elapsed().as_millis() as u64;
            Pdf { base64, cost_in_millis }
        });
    }
    

}