use std::path::{Path, PathBuf};

use tracing::debug;
use typst::syntax::package::PackageSpec;

pub struct PackageResolver {
    packages_path: PathBuf,
}

impl PackageResolver {
    pub fn new(packages_path: PathBuf) -> Self {
        Self { packages_path }
    }

    fn package_dir(&self, spec: &PackageSpec) -> PathBuf {
        self.packages_path
            .join(spec.namespace.as_str())
            .join(spec.name.as_str())
            .join(spec.version.to_string())
    }
    pub fn ensure_package(&self, spec: &PackageSpec) -> Result<PathBuf, String> {
        let dir = self.package_dir(spec);

        if dir.exists() && dir.join("typst.toml").exists() {
            debug!("Package @{}/{}:{} found at {}", spec.namespace, spec.name, spec.version, dir.display());
            return Ok(dir);
        }

        Err(format!(
            "Package @{}/{}:{} not found at {}",
            spec.namespace, spec.name, spec.version, dir.display()
        ))
    }

    pub fn resolve_file(&self, spec: &PackageSpec, vpath: &Path) -> Result<PathBuf, String> {
        let dir = self.ensure_package(spec)?;
        let relative = vpath.strip_prefix("/").unwrap_or(vpath);
        let file_path = dir.join(relative);

        if file_path.exists() {
            Ok(file_path)
        } else {
            Err(format!(
                "File '{}' not found in package @{}/{}:{} (looked at {})",
                vpath.display(), spec.namespace, spec.name, spec.version, file_path.display()
            ))
        }
    }
}
