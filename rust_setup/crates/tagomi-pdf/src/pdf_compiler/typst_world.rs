use std::collections::HashMap;
use tracing::debug;

use typst::diag::FileError;
use typst::utils::LazyHash;
use typst::Library;
use typst::foundations::{Bytes, Datetime};
use typst::syntax::{Source, FileId};
use typst::World;
use typst::text::{Font, FontBook};
use typst_kit::fonts::FontSlot;

use super::package_resolver::PackageResolver;


pub struct TypstWorld {
    pub library: LazyHash<Library>,
    pub book: LazyHash<FontBook>,
    pub fonts: Vec<FontSlot>,
    pub main: FileId,
    pub source: HashMap<FileId, Source>,
    pub now: Datetime,
    pub package_resolver: PackageResolver,
}

impl World for TypstWorld {
    fn library(&self) -> &LazyHash<Library> {
        &self.library
    }

    fn book(&self) -> &LazyHash<FontBook> {
        &self.book
    }

    fn main(&self) -> FileId {
        self.main
    }

    fn source(&self, id: FileId) -> Result<Source, FileError> {
        if let Some(value) = self.source.get(&id) {
            debug!("Found virtual source: {:?}", id);
            return Ok(value.clone());
        }

        if let Some(spec) = id.package() {
            let vpath = id.vpath().as_rootless_path();
            match self.package_resolver.resolve_file(spec, vpath) {
                Ok(path) => {
                    debug!("Found package source: {:?} -> {}", id, path.display());
                    let content = std::fs::read_to_string(&path)
                        .map_err(|_| FileError::NotFound(path))?;
                    return Ok(Source::new(id, content));
                }
                Err(e) => {
                    debug!("Package source resolution failed: {}", e);
                }
            }
        }

        debug!("Source not found: {:?}", id);
        Err(FileError::NotFound(
            id.vpath().as_rootless_path().to_path_buf(),
        ))
    }

    fn file(&self, id: FileId) -> Result<Bytes, FileError> {
        if let Some(spec) = id.package() {
            let vpath = id.vpath().as_rootless_path();
            match self.package_resolver.resolve_file(spec, vpath) {
                Ok(path) => {
                    debug!("Found package file: {:?} -> {}", id, path.display());
                    let content = std::fs::read(&path)
                        .map_err(|_| FileError::NotFound(path))?;
                    return Ok(Bytes::new(content));
                }
                Err(e) => {
                    debug!("Package file resolution failed: {}", e);
                }
            }
        }

        Err(FileError::NotFound(
            id.vpath().as_rootless_path().to_path_buf(),
        ))
    }

    fn font(&self, index: usize) -> Option<Font> {
        let font = self.fonts.get(index)?.get();
        font
    }

    fn today(&self, _offset: Option<i64>) -> Option<Datetime> {
        Some(self.now)
    }
}
