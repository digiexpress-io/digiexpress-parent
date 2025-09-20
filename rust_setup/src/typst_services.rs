use anyhow::{anyhow, Result};

use thiserror::Error;
use typst::{diag::{FileError, HintedString, SourceDiagnostic }};
use typst::{foundations::{Dict, Module, Scope, Value}};
use typst::{syntax::FileId};
use typst::{utils::LazyHash, Library};
use ecow::EcoVec;



use typst_pdf::PdfOptions;
use typst::foundations::{Bytes, Datetime};
use typst::syntax::{Source, VirtualPath};
use typst::{compile, World};
use typst::text::{Font, FontBook};

use typst_kit::fonts::{FontSlot, Fonts};




#[derive(Debug, Clone, Error)]
pub enum TypstAsLibError {
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
struct InjectLocation {
    module_name: &'static str,
    value_name: &'static str,
}


pub struct TypstService;

// Simple world implementation for Typst compilation
struct SimpleWorld {
    library: LazyHash<Library>,
    book: LazyHash<FontBook>,
    fonts: Vec<FontSlot>,
    source: Source,
}

impl SimpleWorld {
    fn new(content: &str, input: Dict, inject_location: Option<InjectLocation>) -> Result<Self> {
        
        // Create a virtual path for our main file
        let path = VirtualPath::new("main.typ");
        let source = Source::new(FileId::new(None, path), content.to_string());

        let fonts = Fonts::searcher().include_system_fonts(true).search();

        // If you want to see the font names too:
        for (i, font) in fonts.fonts.iter().enumerate().take(10) {
            match font.path() {
                // or whatever the actual method is
                Some(p) => println!("Font {}: {}", i, p.display()),
                None => println!("Font {}: No path", i),
            }
        }

        Ok(Self {
            library: Self::create_lib(input, inject_location)?,
            book: LazyHash::new(fonts.book),
            fonts: fonts.fonts,
            source,
        })
    }


    fn create_lib(input: Dict, inject_location: Option<InjectLocation>) -> Result<LazyHash<Library>, TypstAsLibError>
    {

        let mut lib = Library::default();

        // variable name definitions
        let (module_name, value_name) = 
        
        if let Some(InjectLocation { module_name, value_name, }) = inject_location {
            (module_name, value_name)
        } else {
            ("sys", "inputs")
        };

        {
            let global = lib.global.scope_mut();
            let mut scope = Scope::new();
            
            scope.define(value_name, input);
            if let Some(value) = global.get_mut(module_name) {
                let value = value.write().map_err(TypstAsLibError::Unspecified)?;
                if let Value::Module(module) = value {
                    *module.scope_mut() = scope;
                } else {
                    let module = Module::new(module_name, scope);
                    *value = Value::Module(module);
                }
            } else {
                let module = Module::new(module_name, scope);
                global.define(module_name, module);
            }
        }

        Ok(LazyHash::new(lib))
    }

}

impl World for SimpleWorld {
    fn library(&self) -> &LazyHash<Library> {
        &self.library
    }

    fn book(&self) -> &LazyHash<FontBook> {
        &self.book
    }

    fn main(&self) -> FileId {
        self.source.id()
    }

    fn source(&self, id: FileId) -> Result<Source, FileError> {
        if id == self.source.id() {
            Ok(self.source.clone())
        } else {
            Err(FileError::NotFound(
                id.vpath().as_rootless_path().to_path_buf(),
            ))
        }
    }

    fn file(&self, _id: FileId) -> Result<Bytes, FileError> {
        Err(FileError::NotFound(std::path::PathBuf::new()))
    }

    fn font(&self, index: usize) -> Option<Font> {
        let font = self.fonts.get(index)?.get();
        println!(
            "sdkjhfkdshfkjsdhgjkkjdghdkjfhg     Font {}: {:?}",
            index, font
        );
        font
    }

    fn today(&self, _offset: Option<i64>) -> Option<Datetime> {
        Datetime::from_ymd(2024, 1, 1)
    }
}



impl TypstService {
    pub async fn compile(content: &str, input: Dict, format: &str) -> Result<Vec<u8>> {
        let world = SimpleWorld::new(content, input, None)?;

        // Compile the document
        let warned = compile(&world);
        let document = warned.output.map_err(|errors| {
            let error_msg = errors
                .iter()
                .map(|e| e.message.to_string())
                .collect::<Vec<_>>()
                .join("; ");
            anyhow!("Compilation failed: {}", error_msg)
        })?;

        let pdf = typst_pdf::pdf(&document, &PdfOptions::default());
        return pdf.map_err(|errors| anyhow!("PDF generation failed: {:?}", errors));
    }
}