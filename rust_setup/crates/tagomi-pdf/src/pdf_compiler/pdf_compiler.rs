use chrono::{Datelike, Utc};

use tracing::debug;
use typst::foundations::Datetime;
use std::collections::HashMap;
use std::path::PathBuf;
use std::time::Instant;

use base64::{ Engine, engine::general_purpose };


use typst::syntax::{FileId, Source, VirtualPath};
use typst::{utils::LazyHash};
use typst::{compile};
use typst_kit::fonts::{Fonts};
use typst_pdf::PdfOptions;

use crate::pdf_compiler::{map_to_lib, Pdf, PdfCompiler, TypstWorld};
use crate::pdf_compiler::package_resolver::PackageResolver;

pub struct PdfCompilerImpl {
    main_template_id: Option<String>,
    templates: HashMap<FileId, Source>,
    modules: Vec<super::PdfDataModule>,
    leaks: Vec<Box<str>>,
    now: Datetime,
    start_time: Instant,
    fonts_path: PathBuf,
    use_system_fonts: bool,
    packages_path: PathBuf,
}

impl PdfCompilerImpl {
    pub fn new() -> Self {
        let now = Utc::now();
        Self {
            main_template_id: None,
            templates: HashMap::new(),
            modules: Vec::new(),
            leaks: Vec::new(),
            now: Datetime::from_ymd(now.year(), now.month() as u8, now.day() as u8).unwrap(),
            start_time: Instant::now(),
            fonts_path: PathBuf::from("./assets/fonts"),
            use_system_fonts: true,
            packages_path: PathBuf::from("./assets/packages"),
        }
    }
}

impl Drop for PdfCompilerImpl {
    fn drop(&mut self) {
    }
}



impl PdfCompiler for PdfCompilerImpl {
    fn main_template_id(mut self, template_id: String) -> Self {
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
    fn fonts_config(mut self, fonts_path: PathBuf, use_system_fonts: bool) -> Self {
        self.fonts_path = fonts_path;
        self.use_system_fonts = use_system_fonts;
        self
    }
    fn packages_config(mut self, packages_path: PathBuf) -> Self {
        self.packages_path = packages_path;
        self
    }
    fn compile(mut self) -> Result<super::Pdf, super::PdfCompilerError> {
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

        let library = map_to_lib(&self.modules, &mut self.leaks);
        
        let fonts = if self.use_system_fonts {
            debug!("Loading fonts from {} and system fonts", self.fonts_path.display());
            Fonts::searcher()
                .include_system_fonts(true)
                .search_with([self.fonts_path.as_path()])
        } else {
            debug!("Loading fonts from {} only", self.fonts_path.display());
            Fonts::searcher()
                .include_system_fonts(false)
                .search_with([self.fonts_path.as_path()])
        };

        debug!("Total fonts loaded: {}", fonts.fonts.len());
        for (i, font) in fonts.fonts.iter().enumerate() {
            match font.path() {
                Some(p) => debug!("Font {}: {}", i, p.display()),
                None => debug!("Font {}: No path", i),
            }
        }

        let package_resolver = PackageResolver::new(self.packages_path.clone());
        debug!("Package resolver: path={}", self.packages_path.display());

        let world_state = TypstWorld {
            library: LazyHash::new(library),
            book: LazyHash::new(fonts.book),
            fonts: fonts.fonts,
            source: self.templates.clone(),
            now: self.now,
            main: main.id(),
            package_resolver,
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