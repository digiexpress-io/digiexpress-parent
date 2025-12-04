use std::collections::HashMap;
use tracing::debug;
use anyhow::{Result};

use typst::{diag::{FileError}};
use typst::{utils::LazyHash, Library};
use typst::foundations::{Bytes, Datetime};
use typst::syntax::{Source,FileId};
use typst::{World};
use typst::text::{Font, FontBook};
use typst_kit::fonts::{FontSlot};


pub struct TypstWorld {
    pub library: LazyHash<Library>,
    pub book: LazyHash<FontBook>,
    pub fonts: Vec<FontSlot>,
    pub main: FileId,
    pub source: HashMap<FileId, Source>,
    pub now: Datetime,
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
        let target = self.source.get(&id);
        match target {
            Some(value) => {
                debug!("Found source: {:?}", id);
                Ok(value.clone())
            },
            None => {
                // target doesn't exist
                debug!("Can't find source: {:?}", id);
                Err(FileError::NotFound(
                    id.vpath().as_rootless_path().to_path_buf(),
                ))
            }
        }

    }

    fn file(&self, _id: FileId) -> Result<Bytes, FileError> {
        Err(FileError::NotFound(std::path::PathBuf::new()))
    }

    fn font(&self, index: usize) -> Option<Font> {
        let font = self.fonts.get(index)?.get();
        font
    }

    fn today(&self, _offset: Option<i64>) -> Option<Datetime> {
        Some(self.now)
    }
}
