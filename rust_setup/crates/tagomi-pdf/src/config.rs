use std::env;
use std::path::PathBuf;
use std::str::FromStr;

/// Server configuration loaded from environment variables
#[derive(Debug, Clone)]
pub struct Config {
    /// Enable HTTPS mode (env: HTTPS_ENABLED, default: false)
    pub https: bool,
    /// Port to listen on (env: PORT, default: 8085)
    pub port: u16,
    /// Path to TLS certificate file (env: TLS_CERT_PATH, default: /etc/tls/cert.pem)
    pub cert: PathBuf,
    /// Path to TLS private key file (env: TLS_KEY_PATH, default: /etc/tls/key.pem)
    pub key: PathBuf,
    /// Path to custom fonts directory (env: FONTS_PATH, default: ./assets/fonts)
    pub fonts_path: PathBuf,
    /// Include system fonts in addition to custom fonts (env: USE_SYSTEM_FONTS, default: true)
    pub use_system_fonts: bool,
    /// Path to Typst packages directory (env: PACKAGES_PATH, default: ./assets/packages)
    pub packages_path: PathBuf,
}

fn env_or_default<T: FromStr>(key: &str, default: T) -> T {
    env::var(key)
        .ok()
        .and_then(|v| v.parse().ok())
        .unwrap_or(default)
}

fn env_path(key: &str, default: &str) -> PathBuf {
    env::var(key)
        .unwrap_or_else(|_| default.to_string())
        .into()
}

impl Config {
    pub fn from_env() -> Self {
        let https = env_or_default("HTTPS_ENABLED", false);
        let port = env_or_default("PORT", 8085);
        let cert = env_path("TLS_CERT_PATH", "/etc/tls/cert.pem");
        let key = env_path("TLS_KEY_PATH", "/etc/tls/key.pem");
        let fonts_path = env_path("FONTS_PATH", "./assets/fonts");
        let use_system_fonts = env_or_default("USE_SYSTEM_FONTS", true);

        let packages_path = env_path("PACKAGES_PATH", "./assets/packages");

        Self { https, port, cert, key, fonts_path, use_system_fonts, packages_path }
    }
}

